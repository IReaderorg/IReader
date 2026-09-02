package ireader.presentation.ui.settings.downloader

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.BookCover
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.preferences.prefs.DownloadPreferences
import ireader.domain.services.common.DownloadProgress
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceState
import ireader.domain.services.download.NetworkStateProvider
import ireader.domain.services.download.NetworkType
import ireader.domain.usecases.download.DownloadUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Modern, robust ViewModel for the Downloader screen and Spotify-style bottom player bar.
 * Supports manual download triggering, queue priority adjustments, and dynamic book cover fetching.
 */
class DownloaderViewModel(
    private val downloadService: DownloadService,
    private val downloadUseCases: DownloadUseCases,
    private val networkStateProvider: NetworkStateProvider,
    private val downloadPreferences: DownloadPreferences,
    private val bookRepository: BookRepository
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // Multi-Selection State
    // ═══════════════════════════════════════════════════════════════
    val selection: SnapshotStateList<Long> = mutableStateListOf()

    val hasSelection: Boolean
        get() = selection.isNotEmpty()

    // ═══════════════════════════════════════════════════════════════
    // Service State
    // ═══════════════════════════════════════════════════════════════
    val serviceState: StateFlow<ServiceState> = downloadService.state.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ServiceState.IDLE
    )

    val isRunning: StateFlow<Boolean> = serviceState
        .map { it == ServiceState.RUNNING }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val isPaused: StateFlow<Boolean> = serviceState
        .map { it == ServiceState.PAUSED }
        .stateIn(scope, SharingStarted.Eagerly, false)

    // ═══════════════════════════════════════════════════════════════
    // Downloads Data Sources
    // ═══════════════════════════════════════════════════════════════
    private var subscribeJob: Job? = null
    private val _dbDownloads = MutableStateFlow<List<SavedDownloadWithInfo>>(emptyList())

    // Book covers cache for real book cover display in player & list
    private val _bookCovers = MutableStateFlow<Map<Long, BookCover>>(emptyMap())
    val bookCovers: StateFlow<Map<Long, BookCover>> = _bookCovers.asStateFlow()

    // Tracks recently completed downloads in this session so they don't abruptly disappear
    private val _sessionCompletedDownloads = MutableStateFlow<Map<Long, Download>>(emptyMap())

    val progressMap: StateFlow<Map<Long, DownloadProgress>> = downloadService.downloadProgress.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    // ═══════════════════════════════════════════════════════════════
    // Unified Download Queue with Priority Ordering & Covers
    // ═══════════════════════════════════════════════════════════════
    val downloadQueue: StateFlow<List<Download>> = combine(
        _dbDownloads,
        progressMap,
        _sessionCompletedDownloads,
        _bookCovers
    ) { dbList, progress, completedMap, covers ->
        val activeAndQueued = dbList.map { saved ->
            val p = progress[saved.chapterId]
            val status = when (p?.status) {
                ireader.domain.services.common.DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
                ireader.domain.services.common.DownloadStatus.COMPLETED -> DownloadStatus.DOWNLOADED
                ireader.domain.services.common.DownloadStatus.FAILED -> DownloadStatus.ERROR
                ireader.domain.services.common.DownloadStatus.PAUSED -> DownloadStatus.QUEUE
                ireader.domain.services.common.DownloadStatus.QUEUED -> DownloadStatus.QUEUE
                else -> DownloadStatus.QUEUE
            }
            Download(
                chapterId = saved.chapterId,
                bookId = saved.bookId,
                sourceId = saved.sourceId,
                chapterName = saved.chapterName,
                bookTitle = saved.bookName,
                coverUrl = covers[saved.bookId]?.cover ?: "",
                status = status,
                progress = ((p?.progress ?: 0f) * 100).toInt(),
                errorMessage = p?.errorMessage,
                priority = saved.priority
            )
        }

        // Merge active + pending with any session-completed items not already in active list
        val activeIds = activeAndQueued.map { it.chapterId }.toSet()
        val extraCompleted = completedMap.values.filter { it.chapterId !in activeIds }

        (activeAndQueued + extraCompleted).sortedWith(
            compareBy<Download> {
                when (it.status) {
                    DownloadStatus.DOWNLOADING -> 0
                    DownloadStatus.QUEUE -> 1
                    DownloadStatus.ERROR -> 2
                    DownloadStatus.DOWNLOADED -> 3
                    else -> 4
                }
            }.thenBy { it.priority }
        )
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // ═══════════════════════════════════════════════════════════════
    // Active Download for Spotify-like Player Bar
    // ═══════════════════════════════════════════════════════════════
    val activeDownload: StateFlow<Download?> = combine(
        downloadQueue,
        isRunning,
        isPaused
    ) { queue, running, paused ->
        val currentDownloading = queue.find { it.status == DownloadStatus.DOWNLOADING }
        if (currentDownloading != null) return@combine currentDownloading

        if (running || paused) {
            queue.firstOrNull { it.status == DownloadStatus.QUEUE } ?: queue.firstOrNull()
        } else {
            queue.firstOrNull { it.status == DownloadStatus.QUEUE } ?: queue.firstOrNull()
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    // ═══════════════════════════════════════════════════════════════
    // Statistics
    // ═══════════════════════════════════════════════════════════════
    data class DownloadStats(
        val downloading: Int = 0,
        val queued: Int = 0,
        val completed: Int = 0,
        val failed: Int = 0
    ) {
        val total: Int get() = downloading + queued + completed + failed
        val hasActiveDownloads: Boolean get() = downloading > 0 || queued > 0
    }

    val stats: StateFlow<DownloadStats> = downloadQueue.map { queue ->
        var downloading = 0
        var queued = 0
        var completed = 0
        var failed = 0
        queue.forEach { item ->
            when (item.status) {
                DownloadStatus.DOWNLOADING -> downloading++
                DownloadStatus.QUEUE -> queued++
                DownloadStatus.DOWNLOADED -> completed++
                DownloadStatus.ERROR -> failed++
                else -> queued++
            }
        }
        DownloadStats(downloading, queued, completed, failed)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), DownloadStats())

    // ═══════════════════════════════════════════════════════════════
    // Network & Disk Guards
    // ═══════════════════════════════════════════════════════════════
    val networkType: StateFlow<NetworkType> = networkStateProvider.networkState.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkType.NONE
    )

    private val _isWifiOnlyMode = MutableStateFlow(false)
    val isWifiOnlyMode: StateFlow<Boolean> = _isWifiOnlyMode.asStateFlow()

    private val _isPausedDueToNetwork = MutableStateFlow(false)
    val isPausedDueToNetwork: StateFlow<Boolean> = _isPausedDueToNetwork.asStateFlow()

    private val _isPausedDueToDiskSpace = MutableStateFlow(false)
    val isPausedDueToDiskSpace: StateFlow<Boolean> = _isPausedDueToDiskSpace.asStateFlow()

    // ═══════════════════════════════════════════════════════════════
    // Initialization
    // ═══════════════════════════════════════════════════════════════
    init {
        scope.launch {
            downloadService.initialize()
            _isWifiOnlyMode.value = downloadPreferences.downloadOnlyOnWifi().get()
        }
        subscribeDatabaseDownloads()
        observeNetworkState()
        observeProgressCompletions()
    }

    private fun subscribeDatabaseDownloads() {
        subscribeJob?.cancel()
        subscribeJob = scope.launch {
            downloadUseCases.subscribeDownloadsUseCase().collect { list ->
                val filtered = list.filter { it.chapterId != 0L }
                _dbDownloads.value = filtered
                fetchBookCovers(filtered.map { it.bookId }.distinct())
            }
        }
    }

    private fun fetchBookCovers(bookIds: List<Long>) {
        val missing = bookIds.filter { it !in _bookCovers.value && it > 0 }
        if (missing.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            val loaded = mutableMapOf<Long, BookCover>()
            missing.forEach { bookId ->
                try {
                    val book = bookRepository.findBookById(bookId)
                    if (book != null) {
                        loaded[bookId] = BookCover.from(book)
                    }
                } catch (e: Exception) {
                    // Ignore lookup error
                }
            }
            if (loaded.isNotEmpty()) {
                _bookCovers.update { it + loaded }
            }
        }
    }

    private fun observeNetworkState() {
        scope.launch {
            combine(
                isPaused,
                isWifiOnlyMode,
                networkType
            ) { paused, wifiOnly, network ->
                paused && wifiOnly && network != NetworkType.WIFI
            }.collect { pausedDueToNetwork ->
                _isPausedDueToNetwork.value = pausedDueToNetwork
            }
        }
    }

    private fun observeProgressCompletions() {
        scope.launch {
            progressMap.collect { progress ->
                progress.forEach { (chapterId, p) ->
                    if (p.status == ireader.domain.services.common.DownloadStatus.COMPLETED) {
                        _sessionCompletedDownloads.update { current ->
                            if (chapterId !in current) {
                                current + (chapterId to Download(
                                    chapterId = chapterId,
                                    bookId = 0,
                                    sourceId = 0,
                                    chapterName = p.chapterName.ifEmpty { "Chapter" },
                                    bookTitle = p.bookName.ifEmpty { "Book" },
                                    coverUrl = "",
                                    status = DownloadStatus.DOWNLOADED,
                                    progress = 100
                                ))
                            } else current
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Priority & Manual Download Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Download this specific chapter manually/immediately.
     * Moves it to top priority and starts/resumes the download service.
     */
    fun downloadImmediately(chapterId: Long) {
        scope.launch {
            moveToTop(chapterId)
            if (isPaused.value) {
                resumeDownloads()
            } else if (!isRunning.value) {
                startDownloads()
            }
        }
    }

    /**
     * Move a chapter to the very top of the queue.
     */
    fun moveToTop(chapterId: Long) {
        scope.launch {
            val minPriority = _dbDownloads.value.minOfOrNull { it.priority } ?: 0
            val newPriority = minPriority - 1
            downloadUseCases.updateDownloadPriority(chapterId, newPriority)
        }
    }

    /**
     * Move a chapter up one slot in priority.
     */
    fun moveUp(chapterId: Long) {
        scope.launch {
            val currentList = _dbDownloads.value.sortedBy { it.priority }
            val index = currentList.indexOfFirst { it.chapterId == chapterId }
            if (index > 0) {
                val prev = currentList[index - 1]
                val current = currentList[index]
                downloadUseCases.updateDownloadPriority(current.chapterId, prev.priority)
                downloadUseCases.updateDownloadPriority(prev.chapterId, current.priority)
            }
        }
    }

    /**
     * Move a chapter down one slot in priority.
     */
    fun moveDown(chapterId: Long) {
        scope.launch {
            val currentList = _dbDownloads.value.sortedBy { it.priority }
            val index = currentList.indexOfFirst { it.chapterId == chapterId }
            if (index >= 0 && index < currentList.size - 1) {
                val next = currentList[index + 1]
                val current = currentList[index]
                downloadUseCases.updateDownloadPriority(current.chapterId, next.priority)
                downloadUseCases.updateDownloadPriority(next.chapterId, current.priority)
            }
        }
    }

    /**
     * Reorders the queue via drag and drop, updating the priorities in SQLDelight.
     */
    fun reorder(fromIndex: Int, toIndex: Int) {
        scope.launch {
            val current = downloadQueue.value.toMutableList()
            if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
                val moved = current.removeAt(fromIndex)
                current.add(toIndex, moved)
                // Re-assign priorities sequentially based on new visual queue position
                current.forEachIndexed { index, item ->
                    downloadUseCases.updateDownloadPriority(item.chapterId, index)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // User Actions
    // ═══════════════════════════════════════════════════════════════

    /**
     * Toggle Play/Pause directly from the Spotify player bar.
     */
    fun togglePlayPause() {
        scope.launch {
            if (isPaused.value) {
                resumeDownloads()
            } else if (isRunning.value) {
                pauseDownloads()
            } else {
                startDownloads()
            }
        }
    }

    /**
     * Skip the currently active download and proceed to the next chapter.
     */
    fun skipCurrent() {
        val current = activeDownload.value ?: return
        scope.launch {
            downloadService.cancelDownload(current.chapterId)
        }
    }

    fun startDownloads() {
        scope.launch {
            val chapterIds = _dbDownloads.value.sortedBy { it.priority }.map { it.chapterId }
            if (chapterIds.isNotEmpty()) {
                downloadService.queueChapters(chapterIds)
            }
            downloadService.start()
        }
    }

    fun pauseDownloads() {
        scope.launch {
            downloadService.pause()
        }
    }

    fun resumeDownloads() {
        scope.launch {
            downloadService.resume()
        }
    }

    fun cancelAllDownloads() {
        scope.launch {
            _sessionCompletedDownloads.value = emptyMap()
            downloadService.cancelAll()
        }
    }

    fun removeDownload(chapterId: Long) {
        scope.launch {
            _sessionCompletedDownloads.update { it - chapterId }
            downloadService.cancelDownload(chapterId)
        }
    }

    fun removeSelectedDownloads() {
        scope.launch {
            val toRemove = selection.toList()
            selection.clear()
            _sessionCompletedDownloads.update { current -> current - toRemove.toSet() }
            toRemove.forEach { chapterId ->
                downloadService.cancelDownload(chapterId)
            }
        }
    }

    fun retryDownload(chapterId: Long) {
        scope.launch {
            downloadService.retryDownload(chapterId)
        }
    }

    fun retryAllFailed() {
        scope.launch {
            downloadService.retryAllFailed()
        }
    }

    fun clearCompleted() {
        scope.launch {
            _sessionCompletedDownloads.value = emptyMap()
            downloadService.clearCompleted()
        }
    }

    fun clearFailed() {
        scope.launch {
            downloadService.clearFailed()
        }
    }

    fun setWifiOnlyMode(enabled: Boolean) {
        scope.launch {
            downloadPreferences.downloadOnlyOnWifi().set(enabled)
            _isWifiOnlyMode.value = enabled
        }
    }

    fun allowMobileDataTemporarily() {
        scope.launch {
            downloadService.resume()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Multi-Selection Controls
    // ═══════════════════════════════════════════════════════════════
    fun toggleSelection(chapterId: Long) {
        if (chapterId in selection) {
            selection.remove(chapterId)
        } else {
            selection.add(chapterId)
        }
    }

    fun selectAll() {
        selection.clear()
        selection.addAll(downloadQueue.value.map { it.chapterId })
    }

    fun clearSelection() {
        selection.clear()
    }
}
