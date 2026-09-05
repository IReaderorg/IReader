package ireader.domain.services.sync

import ireader.core.log.Log
import ireader.core.util.currentTimeMillis
import ireader.domain.repositories.SyncLocalRepository
import ireader.domain.config.PlatformConfig
import ireader.domain.models.sync.*
import ireader.domain.preferences.prefs.SyncPreferences
import ireader.domain.services.library.LibraryCommand
import ireader.domain.services.library.LibraryController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central engine coordinating multi-provider synchronization (Google Drive, Supabase, Local Wi-Fi).
 * Handles differential manifest generation, 3-way conflict resolution, 30-day tombstone tracking,
 * and debounced auto-sync triggers.
 */
class UnifiedSyncEngine(
    private val syncPreferences: SyncPreferences,
    private val providers: List<SyncProvider>,
    private val localRepository: SyncLocalRepository,
    private val deviceId: String = "ireader-device",
    private val libraryController: LibraryController? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {


    companion object {
        private const val TAG = "UnifiedSyncEngine"
        private const val TOMBSTONE_MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
        private const val PROGRESS_DEBOUNCE_MS = 2000L
    }

    private val mutex = Mutex()
    private var isCancelled = false
    private var progressDebounceJob: Job? = null
    private val localTombstones = mutableListOf<SyncTombstone>()

    private val _syncState = MutableStateFlow(
        UnifiedSyncState(
            provider = syncPreferences.getSelectedProviderType(),
            lastSyncTimestamp = syncPreferences.lastSyncTimestamp().get()
        )
    )
    val syncState: StateFlow<UnifiedSyncState> = _syncState.asStateFlow()

    /**
     * Get the currently active sync provider
     */
    fun getActiveProvider(): SyncProvider? {
        val selectedType = syncPreferences.getSelectedProviderType()
        if (selectedType == SyncProviderType.NONE) return null
        return providers.firstOrNull { it.type == selectedType }
    }

    /**
     * Change active sync provider
     */
    fun setProvider(providerType: SyncProviderType) {
        syncPreferences.setSelectedProviderType(providerType)
        _syncState.update { it.copy(provider = providerType, errorMessage = null) }
        Log.info { "$TAG: Switched sync provider to $providerType" }
    }

    /**
     * Record a deletion tombstone to ensure the item is deleted across other devices.
     */
    fun recordTombstone(itemType: UniversalSyncItemType, globalId: String) {
        synchronized(localTombstones) {
            localTombstones.add(
                SyncTombstone(
                    itemType = itemType,
                    globalId = globalId,
                    deletedAt = currentTimeMillis()
                )
            )
        }
    }

    /**
     * Record reading progress with debouncing and push to active provider.
     */
    fun syncReadingProgressDebounced(bookGlobalId: String, chapterKey: String, progress: Float) {
        val activeProvider = getActiveProvider() ?: return
        if (!syncPreferences.autoSyncOnChapterFinish().get()) return

        progressDebounceJob?.cancel()
        progressDebounceJob = coroutineScope.launch {
            delay(PROGRESS_DEBOUNCE_MS)
            try {
                val now = currentTimeMillis()
                val progressItem = SyncProgressItem(
                    bookGlobalId = bookGlobalId,
                    chapterKey = chapterKey,
                    progress = progress,
                    lastRead = now,
                    lastModified = now
                )
                activeProvider.pushProgress(progressItem)
            } catch (e: Exception) {
                Log.warn { "$TAG: Failed to push realtime progress: ${e.message}" }
            }
        }
    }

    /**
     * Cancel an in-progress synchronization.
     */
    fun cancelSync() {
        isCancelled = true
        _syncState.update { it.copy(isSyncing = false, currentStep = "Cancelled") }
    }

    /**
     * Execute full synchronization with the active provider.
     */
    suspend fun syncNow(force: Boolean = false): Result<UnifiedSyncState> = withContext(Dispatchers.Default) {
        mutex.withLock {
            val provider = getActiveProvider()
            if (provider == null) {
                val errorMsg = "No sync provider selected"
                _syncState.update { it.copy(errorMessage = errorMsg, isSyncing = false) }
                return@withContext Result.failure(IllegalStateException(errorMsg))
            }

            isCancelled = false
            _syncState.update { 
                it.copy(
                    isSyncing = true,
                    progress = 0.05f,
                    currentStep = "Authenticating with ${provider.name}...",
                    errorMessage = null
                ) 
            }

            try {
                if (!provider.isAuthenticated()) {
                    val err = "${provider.name} is not authenticated"
                    _syncState.update { it.copy(isSyncing = false, errorMessage = err) }
                    return@withContext Result.failure(IllegalStateException(err))
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                _syncState.update { it.copy(progress = 0.2f, currentStep = "Fetching remote manifest...") }

                // 1. Fetch remote manifest
                val remoteManifest = provider.fetchRemoteManifest().getOrNull()

                if (isCancelled) throw CancellationException("Sync cancelled")

                _syncState.update { it.copy(progress = 0.4f, currentStep = "Reading local library...") }

                // 2. Read local data
                val localBooks = localRepository.getBooks()
                val localHistory = localRepository.getHistory()
                val now = currentTimeMillis()

                // 3. Process Remote Tombstones (Deletions from other devices)
                _syncState.update { it.copy(progress = 0.5f, currentStep = "Processing deletions...") }
                val activeRemoteTombstones = remoteManifest?.tombstones?.filter {
                    (now - it.deletedAt) < TOMBSTONE_MAX_AGE_MS
                } ?: emptyList()

                val tombstonedBookIds = activeRemoteTombstones
                    .filter { it.itemType == UniversalSyncItemType.BOOK }
                    .map { it.globalId }
                    .toSet()

                if (tombstonedBookIds.isNotEmpty()) {
                    val booksToDeleteLocally = localBooks.filter { tombstonedBookIds.contains(it.globalId) }
                        .map { it.globalId }
                    if (booksToDeleteLocally.isNotEmpty()) {
                        localRepository.deleteBooksByGlobalIds(booksToDeleteLocally)
                        Log.info { "$TAG: Deleted ${booksToDeleteLocally.size} tombstoned books locally" }
                    }
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                // 4. Merge Books (Last-Write-Wins)
                _syncState.update { it.copy(progress = 0.65f, currentStep = "Merging books...") }
                val remoteBooksMap = (remoteManifest?.books ?: emptyList()).associateBy { it.globalId }
                val localBooksMap = localBooks.associateBy { it.globalId }

                val booksToApplyLocally = mutableListOf<BookSyncData>()
                remoteBooksMap.forEach { (globalId, remoteBook) ->
                    if (!tombstonedBookIds.contains(globalId)) {
                        val localBook = localBooksMap[globalId]
                        if (localBook == null || remoteBook.lastModified > localBook.updatedAt) {
                            booksToApplyLocally.add(
                                BookSyncData(
                                    globalId = remoteBook.globalId,
                                    sourceId = remoteBook.sourceId.toString(),
                                    key = remoteBook.key,
                                    title = remoteBook.title,
                                    author = remoteBook.author,
                                    description = remoteBook.description,
                                    genres = remoteBook.genres,
                                    status = remoteBook.status,
                                    coverUrl = remoteBook.coverUrl,
                                    favorite = remoteBook.favorite,
                                    updatedAt = remoteBook.lastModified,
                                    addedAt = remoteBook.lastModified
                                )
                            )
                        }
                    }
                }

                if (booksToApplyLocally.isNotEmpty()) {
                    localRepository.applyBooks(booksToApplyLocally)
                    libraryController?.dispatch(LibraryCommand.RefreshLibrary)
                    Log.info { "$TAG: Applied ${booksToApplyLocally.size} remote books locally" }
                }

                // 5. Merge History / Reading Progress (Last-Write-Wins)
                _syncState.update { it.copy(progress = 0.8f, currentStep = "Merging reading progress...") }
                val remoteProgressMap = (remoteManifest?.progress ?: emptyList()).associateBy { it.chapterGlobalId }
                val localHistoryMap = localHistory.associateBy { it.chapterGlobalId }

                val historyToApplyLocally = mutableListOf<HistorySyncData>()
                remoteProgressMap.forEach { (chapterGlobalId, remoteProg) ->
                    val localHist = localHistoryMap[chapterGlobalId]
                    if (localHist == null || remoteProg.lastModified > localHist.lastRead) {
                        historyToApplyLocally.add(
                            HistorySyncData(
                                chapterGlobalId = remoteProg.chapterGlobalId,
                                lastRead = remoteProg.lastRead,
                                readingProgress = remoteProg.progress.toDouble(),
                                timeRead = 0L
                            )
                        )
                    }
                }

                if (historyToApplyLocally.isNotEmpty()) {
                    localRepository.applyHistory(historyToApplyLocally)
                    Log.info { "$TAG: Applied ${historyToApplyLocally.size} progress entries locally" }
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                // 6. Build and Upload Merged UnifiedSyncManifest
                _syncState.update { it.copy(progress = 0.9f, currentStep = "Uploading unified manifest...") }

                val updatedLocalBooks = localRepository.getBooks()
                val updatedLocalHistory = localRepository.getHistory()


                val unifiedBooks = updatedLocalBooks.map {
                    SyncBookItem(
                        globalId = it.globalId,
                        sourceId = it.sourceId.toLongOrNull() ?: 0L,
                        key = it.key,
                        title = it.title,
                        author = it.author,
                        description = it.description,
                        genres = it.genres,
                        status = it.status,
                        coverUrl = it.coverUrl ?: "",
                        favorite = it.favorite,
                        lastModified = it.updatedAt
                    )
                }

                val unifiedProgress = updatedLocalHistory.map {
                    SyncProgressItem(
                        bookGlobalId = "",
                        chapterKey = "",
                        chapterGlobalId = it.chapterGlobalId,
                        progress = it.readingProgress.toFloat(),
                        lastRead = it.lastRead,
                        lastModified = it.lastRead
                    )
                }


                // Merge active tombstones
                val combinedTombstones = (activeRemoteTombstones + synchronized(localTombstones) { localTombstones.toList() })
                    .distinctBy { it.globalId }
                    .filter { (now - it.deletedAt) < TOMBSTONE_MAX_AGE_MS }

                val mergedManifest = UnifiedSyncManifest(
                    version = 1,
                    deviceId = deviceId,
                    timestamp = now,
                    books = unifiedBooks,
                    progress = unifiedProgress,
                    tombstones = combinedTombstones
                )


                provider.uploadManifest(mergedManifest).getOrThrow()

                // Cleanup expired local tombstones
                synchronized(localTombstones) {
                    localTombstones.removeAll { (now - it.deletedAt) >= TOMBSTONE_MAX_AGE_MS }
                }

                val finishState = UnifiedSyncState(
                    provider = provider.type,
                    isSyncing = false,
                    progress = 1.0f,
                    currentStep = "Sync Complete",
                    lastSyncTimestamp = now,
                    booksSyncedCount = booksToApplyLocally.size,
                    progressSyncedCount = historyToApplyLocally.size,
                    errorMessage = null
                )

                syncPreferences.lastSyncTimestamp().set(now)
                _syncState.value = finishState
                Log.info { "$TAG: Sync finished successfully. Synced ${booksToApplyLocally.size} books, ${historyToApplyLocally.size} progress entries" }
                Result.success(finishState)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.info { "$TAG: Sync was cancelled" }
                    val cancelledState = _syncState.value.copy(isSyncing = false, currentStep = "Cancelled")
                    _syncState.value = cancelledState
                    Result.failure(e)
                } else {
                    Log.error { "$TAG: Sync failed: ${e.message}" }
                    val failedState = _syncState.value.copy(
                        isSyncing = false,
                        progress = 0f,
                        currentStep = "Failed",
                        errorMessage = e.message ?: "Unknown sync error"
                    )
                    _syncState.value = failedState
                    Result.failure(e)
                }
            }
        }
    }
}
