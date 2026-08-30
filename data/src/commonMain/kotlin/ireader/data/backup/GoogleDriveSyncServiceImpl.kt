package ireader.data.backup

import ireader.core.log.Log
import ireader.core.util.currentTimeMillis
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.domain.config.PlatformConfig
import ireader.domain.models.sync.CloudSyncBookItem
import ireader.domain.models.sync.CloudSyncManifest
import ireader.domain.models.sync.CloudSyncProgressItem
import ireader.domain.models.sync.CloudSyncResult
import ireader.domain.models.sync.CloudSyncTombstone
import ireader.domain.models.sync.SyncItemType
import ireader.domain.models.sync.SyncStatus
import ireader.domain.services.backup.GoogleDriveSyncService
import ireader.domain.services.library.LibraryCommand
import ireader.domain.services.library.LibraryController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Implementation of GoogleDriveSyncService for lightweight delta cloud synchronization.
 */
class GoogleDriveSyncServiceImpl(
    private val authenticator: GoogleDriveAuthenticator,
    private val localDataSource: SyncLocalDataSource,
    private val platformConfig: PlatformConfig,
    private val libraryController: LibraryController? = null
) : GoogleDriveSyncService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val mutex = Mutex()
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    @Volatile
    private var isCancelled = false

    private var driveClient: GoogleDriveClient? = null

    companion object {
        private const val SYNC_MANIFEST_FILE_NAME = "sync_manifest.json"
        private const val TOMBSTONE_MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    private fun getClient(): GoogleDriveClient {
        return driveClient ?: GoogleDriveClient(
            getAccessToken = { authenticator.getAccessToken() }
        ).also { driveClient = it }
    }

    override suspend fun isAuthenticated(): Boolean {
        return authenticator.isAuthenticated()
    }

    override suspend fun cancel() {
        isCancelled = true
        _syncStatus.value = SyncStatus.Idle
    }

    override suspend fun sync(): Result<CloudSyncResult> = withContext(Dispatchers.Default) {
        mutex.withLock {
            val startTime = currentTimeMillis()
            isCancelled = false

            try {
                if (!isAuthenticated()) {
                    return@withContext Result.failure(Exception("Google Drive is not authenticated"))
                }

                _syncStatus.value = SyncStatus.Syncing(
                    deviceName = "Google Drive",
                    progress = 0.1f,
                    currentItem = "Fetching remote manifest"
                )

                val client = getClient()

                // 1. Fetch remote sync manifest
                val filesResult = client.listFiles(SYNC_MANIFEST_FILE_NAME)
                val manifestFile = filesResult.getOrNull()?.firstOrNull()

                val remoteManifest: CloudSyncManifest? = if (manifestFile != null) {
                    val downloadResult = client.downloadFile(manifestFile.id)
                    downloadResult.getOrNull()?.let { bytes ->
                        try {
                            json.decodeFromString<CloudSyncManifest>(bytes.decodeToString())
                        } catch (e: Exception) {
                            Log.warn { "Failed to decode remote sync manifest: " }
                            null
                        }
                    }
                } else {
                    null
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                _syncStatus.value = SyncStatus.Syncing(
                    deviceName = "Google Drive",
                    progress = 0.3f,
                    currentItem = "Reading local library"
                )

                // 2. Fetch local books & progress
                val localBooks = localDataSource.getBooks()
                val localHistory = localDataSource.getHistory()
                val localDeviceId = platformConfig.getDeviceId()

                // 3. Process Remote Tombstones (deletions on other devices)
                var deletedCount = 0
                val now = currentTimeMillis()
                val activeRemoteTombstones = remoteManifest?.tombstones?.filter {
                    (now - it.deletedAt) < TOMBSTONE_MAX_AGE_MS
                } ?: emptyList()

                val tombstonedBookIds = activeRemoteTombstones
                    .filter { it.itemType == SyncItemType.BOOK }
                    .map { it.globalId }
                    .toSet()

                if (tombstonedBookIds.isNotEmpty()) {
                    val booksToDeleteLocally = localBooks.filter {
                        tombstonedBookIds.contains(it.globalId)
                    }.map { it.globalId }

                    if (booksToDeleteLocally.isNotEmpty()) {
                        localDataSource.deleteBooksByGlobalIds(booksToDeleteLocally)
                        deletedCount += booksToDeleteLocally.size
                    }
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                _syncStatus.value = SyncStatus.Syncing(
                    deviceName = "Google Drive",
                    progress = 0.5f,
                    currentItem = "Merging cloud changes"
                )

                // 4. Merge Books (Last-write-wins)
                val remoteBooksByGlobalId = (remoteManifest?.books ?: emptyList()).associateBy { it.globalId }
                val localBooksByGlobalId = localBooks.associateBy { it.globalId }

                val booksToApplyLocally = mutableListOf<ireader.domain.models.sync.BookSyncData>()
                var booksSynced = 0

                remoteBooksByGlobalId.forEach { (globalId, remoteBook) ->
                    if (!tombstonedBookIds.contains(globalId)) {
                        val localBook = localBooksByGlobalId[globalId]
                        if (localBook == null || remoteBook.lastModified > localBook.updatedAt) {
                            booksToApplyLocally.add(
                                ireader.domain.models.sync.BookSyncData(
                                    globalId = remoteBook.globalId,
                                    sourceId = remoteBook.sourceId.toString(),
                                    key = remoteBook.key,
                                    title = remoteBook.title,
                                    author = remoteBook.author,
                                    description = "",
                                    genres = emptyList(),
                                    status = 0L,
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
                    localDataSource.applyBooks(booksToApplyLocally)
                    booksSynced = booksToApplyLocally.size
                    libraryController?.dispatch(LibraryCommand.RefreshLibrary)
                }

                // 5. Merge History/Progress
                val remoteProgressByChapter = (remoteManifest?.progress ?: emptyList()).associateBy { it.chapterGlobalId }
                val localHistoryByChapter = localHistory.associateBy { it.chapterGlobalId }

                val historyToApplyLocally = mutableListOf<ireader.domain.models.sync.HistorySyncData>()
                var progressSynced = 0

                remoteProgressByChapter.forEach { (chapterGlobalId, remoteProg) ->
                    val localHist = localHistoryByChapter[chapterGlobalId]
                    if (localHist == null || remoteProg.lastModified > localHist.lastRead) {
                        historyToApplyLocally.add(
                            ireader.domain.models.sync.HistorySyncData(
                                chapterGlobalId = remoteProg.chapterGlobalId,
                                lastRead = remoteProg.lastRead,
                                readingProgress = remoteProg.progress,
                                timeRead = 0L
                            )
                        )
                    }
                }

                if (historyToApplyLocally.isNotEmpty()) {
                    localDataSource.applyHistory(historyToApplyLocally)
                    progressSynced = historyToApplyLocally.size
                }

                if (isCancelled) throw CancellationException("Sync cancelled")

                _syncStatus.value = SyncStatus.Syncing(
                    deviceName = "Google Drive",
                    progress = 0.8f,
                    currentItem = "Updating cloud manifest"
                )

                // 6. Build and upload unified CloudSyncManifest
                val updatedLocalBooks = localDataSource.getBooks()
                val updatedLocalHistory = localDataSource.getHistory()

                val unifiedBooks = updatedLocalBooks.map {
                    CloudSyncBookItem(
                        globalId = it.globalId,
                        title = it.title,
                        sourceId = it.sourceId.toLongOrNull() ?: 0L,
                        key = it.key,
                        author = it.author,
                        coverUrl = it.coverUrl,
                        favorite = it.favorite,
                        lastModified = it.updatedAt
                    )
                }

                val unifiedProgress = updatedLocalHistory.map {
                    CloudSyncProgressItem(
                        bookGlobalId = "",
                        chapterGlobalId = it.chapterGlobalId,
                        lastRead = it.lastRead,
                        progress = it.readingProgress,
                        lastModified = it.lastRead
                    )
                }

                val mergedManifest = CloudSyncManifest(
                    version = 1,
                    deviceId = localDeviceId,
                    lastUpdated = currentTimeMillis(),
                    books = unifiedBooks,
                    progress = unifiedProgress,
                    tombstones = activeRemoteTombstones
                )

                val manifestJson = json.encodeToString(CloudSyncManifest.serializer(), mergedManifest)
                val manifestBytes = manifestJson.encodeToByteArray()

                if (manifestFile != null) {
                    client.updateFile(manifestFile.id, manifestBytes, "application/json")
                } else {
                    client.uploadFile(SYNC_MANIFEST_FILE_NAME, manifestBytes, "application/json")
                }

                val duration = currentTimeMillis() - startTime
                val totalSynced = booksSynced + progressSynced + deletedCount
                val result = CloudSyncResult(
                    booksSynced = booksSynced,
                    progressSynced = progressSynced,
                    itemsDeleted = deletedCount,
                    durationMs = duration,
                    timestamp = currentTimeMillis()
                )

                _syncStatus.value = SyncStatus.Completed(
                    deviceName = "Google Drive",
                    syncedItems = totalSynced,
                    duration = duration
                )

                Log.info { "[GoogleDriveSync] Sync complete:  books,  progress in ms" }
                Result.success(result)
            } catch (e: CancellationException) {
                _syncStatus.value = SyncStatus.Idle
                Result.failure(e)
            } catch (e: Exception) {
                Log.error(e, "[GoogleDriveSync] Sync failed: ")
                _syncStatus.value = SyncStatus.Failed(
                    deviceName = "Google Drive",
                    error = ireader.domain.models.sync.SyncError.TransferFailed(e.message ?: "Sync failed")
                )
                Result.failure(e)
            }
        }
    }
}
