package ireader.presentation.ui.settings.downloader

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.preferences.prefs.DownloadPreferences
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceState
import ireader.domain.services.download.NetworkStateProvider
import ireader.domain.services.download.NetworkType
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Download Queue screen.
 * 
 * Uses DownloadService as the backend for download operations.
 * Provides a clean, modern API for the UI.
 */
class DownloaderViewModel(
    private val downloadService: DownloadService,
    private val downloadUseCases: DownloadUseCases,
    private val networkStateProvider: NetworkStateProvider,
    private val downloadPreferences: DownloadPreferences
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // Selection State (for multi-select in UI)
    // ═══════════════════════════════════════════════════════════════
    
    val selection: SnapshotStateList<Long> = mutableStateListOf()
    
    val hasSelection: Boolean
        get() = selection.isNotEmpty()
    
    // ═══════════════════════════════════════════════════════════════
    // Download Queue from Service
    // ═══════════════════════════════════════════════════════════════
    
    private var subscribeJob: Job? = null
    private val _downloads = MutableStateFlow<List<SavedDownloadWithInfo>>(emptyList())
    val downloads: StateFlow<List<SavedDownloadWithInfo>> = _downloads.asStateFlow()
    
    /**
     * Download queue converted to Download model for UI.
     */
    val downloadQueue: StateFlow<List<Download>> = _downloads
        .map { list ->
            list.map { saved ->
                Download(
                    chapterId = saved.chapterId,
                    bookId = saved.bookId,
                    sourceId = 0L, // Not available in SavedDownloadWithInfo
                    chapterName = saved.chapterName,
                    bookTitle = saved.bookName,
                    coverUrl = "",
                    status = getDownloadStatus(saved.chapterId),
                    progress = getDownloadProgress(saved.chapterId),
                    errorMessage = getDownloadError(saved.chapterId)
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    
    /**
     * Progress map from service.
     */
    val progressMap = downloadService.downloadProgress.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )
    
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
    
    // Network-related pauses (simplified - check if paused and on mobile)
    private val _isPausedDueToNetwork = MutableStateFlow(false)
    val isPausedDueToNetwork: StateFlow<Boolean> = _isPausedDueToNetwork.asStateFlow()
    
    private val _isPausedDueToDiskSpace = MutableStateFlow(false)
    val isPausedDueToDiskSpace: StateFlow<Boolean> = _isPausedDueToDiskSpace.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════
    // Statistics (Optimized: single pass through progressMap)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Combined stats for header display.
     * Computed in a single pass through progressMap for better performance.
     */
    data class DownloadStats(
        val downloading: Int = 0,
        val queued: Int = 0,
        val completed: Int = 0,
        val failed: Int = 0
    ) {
        val total: Int get() = downloading + queued + completed + failed
        val hasActiveDownloads: Boolean get() = downloading > 0 || queued > 0
    }
    
    /**
     * Optimized: Compute all stats in a single pass through the progress map.
     * This replaces 4 separate StateFlows that each iterated the entire map.
     */
    val stats: StateFlow<DownloadStats> = progressMap
        .map { map ->
            var downloading = 0
            var queued = 0
            var completed = 0
            var failed = 0
            map.forEach { (_, progress) ->
                when (progress.status) {
                    ireader.domain.services.common.DownloadStatus.DOWNLOADING -> downloading++
                    ireader.domain.services.common.DownloadStatus.QUEUED -> queued++
                    ireader.domain.services.common.DownloadStatus.COMPLETED -> completed++
                    ireader.domain.services.common.DownloadStatus.FAILED -> failed++
                    else -> {}
                }
            }
            DownloadStats(downloading, queued, completed, failed)
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), DownloadStats())
    
    // Derived from stats for backward compatibility
    val downloadingCount: StateFlow<Int> = stats
        .map { it.downloading }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val queuedCount: StateFlow<Int> = stats
        .map { it.queued }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val completedCount: StateFlow<Int> = stats
        .map { it.completed }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val failedCount: StateFlow<Int> = stats
        .map { it.failed }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    // ═══════════════════════════════════════════════════════════════
    // Network State
    // ═══════════════════════════════════════════════════════════════
    
    val networkType: StateFlow<NetworkType> = networkStateProvider.networkState.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkType.NONE
    )
    
    val isOnWifi: StateFlow<Boolean> = networkStateProvider.networkState
        .map { it == NetworkType.WIFI }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    // ═══════════════════════════════════════════════════════════════
    // Preferences
    // ═══════════════════════════════════════════════════════════════
    
    private val _isWifiOnlyMode = MutableStateFlow(false)
    val isWifiOnlyMode: StateFlow<Boolean> = _isWifiOnlyMode.asStateFlow()
    
    /**
     * Current download being processed.
     */
    val currentDownload: StateFlow<Download?> = progressMap
        .map { map ->
            map.entries.find { it.value.status == ireader.domain.services.common.DownloadStatus.DOWNLOADING }
                ?.let { entry ->
                    val saved = _downloads.value.find { it.chapterId == entry.key }
                    if (saved != null) {
                        Download(
                            chapterId = entry.key,
                            bookId = saved.bookId,
                            sourceId = 0L,
                            chapterName = saved.chapterName,
                            bookTitle = saved.bookName,
                            coverUrl = "",
                            status = DownloadStatus.DOWNLOADING,
                            progress = (entry.value.progress * 100).toInt()
                        )
                    } else null
                }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    
    init {
        scope.launch {
            downloadService.initialize()
            _isWifiOnlyMode.value = downloadPreferences.downloadOnlyOnWifi().get()
        }
        subscribeDownloads()
        observeNetworkState()
    }
    
    private fun subscribeDownloads() {
        subscribeJob?.cancel()
        subscribeJob = scope.launch {
            downloadUseCases.subscribeDownloadsUseCase().collect { list ->
                _downloads.value = list.filter { it.chapterId != 0L }
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
    
    // ═══════════════════════════════════════════════════════════════
    // Helper Functions
    // ═══════════════════════════════════════════════════════════════
    
    private fun getDownloadStatus(chapterId: Long): DownloadStatus {
        val serviceStatus = progressMap.value[chapterId]?.status
        return when (serviceStatus) {
            ireader.domain.services.common.DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            ireader.domain.services.common.DownloadStatus.COMPLETED -> DownloadStatus.DOWNLOADED
            ireader.domain.services.common.DownloadStatus.FAILED -> DownloadStatus.ERROR
            ireader.domain.services.common.DownloadStatus.PAUSED -> DownloadStatus.QUEUE
            ireader.domain.services.common.DownloadStatus.QUEUED -> DownloadStatus.QUEUE
            else -> DownloadStatus.QUEUE
        }
    }
    
    private fun getDownloadProgress(chapterId: Long): Int {
        return ((progressMap.value[chapterId]?.progress ?: 0f) * 100).toInt()
    }
    
    private fun getDownloadError(chapterId: Long): String? {
        return progressMap.value[chapterId]?.errorMessage
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Actions
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Start downloading the queue.
     */
    fun startDownloads() {
        scope.launch {
            val chapterIds = _downloads.value.map { it.chapterId }
            if (chapterIds.isNotEmpty()) {
                downloadService.queueChapters(chapterIds)
            }
        }
    }
    
    /**
     * Pause all downloads.
     */
    fun pauseDownloads() {
        scope.launch {
            downloadService.pause()
        }
    }
    
    /**
     * Resume paused downloads.
     */
    fun resumeDownloads() {
        scope.launch {
            downloadService.resume()
        }
    }
    
    /**
     * Cancel all downloads and clear queue.
     */
    fun cancelAllDownloads() {
        scope.launch {
            downloadService.cancelAll()
        }
    }
    
    /**
     * Remove a single download from queue.
     */
    fun removeDownload(chapterId: Long) {
        scope.launch(ioDispatcher) {
            downloadService.cancelDownload(chapterId)
            val download = _downloads.value.find { it.chapterId == chapterId }
            if (download != null) {
                downloadUseCases.deleteSavedDownload(
                    ireader.domain.models.entities.Download(
                        chapterId = download.chapterId,
                        bookId = download.bookId,
                        priority = 0
                    )
                )
            }
        }
    }
    
    /**
     * Remove selected downloads.
     */
    fun removeSelectedDownloads() {
        scope.launch(ioDispatcher) {
            selection.forEach { chapterId ->
                downloadService.cancelDownload(chapterId)
                val download = _downloads.value.find { it.chapterId == chapterId }
                if (download != null) {
                    downloadUseCases.deleteSavedDownload(
                        ireader.domain.models.entities.Download(
                            chapterId = download.chapterId,
                            bookId = download.bookId,
                            priority = 0
                        )
                    )
                }
            }
            selection.clear()
        }
    }
    
    /**
     * Retry a failed download.
     */
    fun retryDownload(chapterId: Long) {
        scope.launch {
            downloadService.retryDownload(chapterId)
        }
    }
    
    /**
     * Retry all failed downloads.
     */
    fun retryAllFailed() {
        scope.launch {
            val failedIds = progressMap.value
                .filter { it.value.status == ireader.domain.services.common.DownloadStatus.FAILED }
                .keys
            failedIds.forEach { chapterId ->
                downloadService.retryDownload(chapterId)
            }
        }
    }
    
    /**
     * Clear completed downloads from queue.
     */
    fun clearCompleted() {
        scope.launch(ioDispatcher) {
            val completedIds = progressMap.value
                .filter { it.value.status == ireader.domain.services.common.DownloadStatus.COMPLETED }
                .keys
            completedIds.forEach { chapterId ->
                val download = _downloads.value.find { it.chapterId == chapterId }
                if (download != null) {
                    downloadUseCases.deleteSavedDownload(
                        ireader.domain.models.entities.Download(
                            chapterId = download.chapterId,
                            bookId = download.bookId,
                            priority = 0
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Clear failed downloads from queue.
     */
    fun clearFailed() {
        scope.launch(ioDispatcher) {
            val failedIds = progressMap.value
                .filter { it.value.status == ireader.domain.services.common.DownloadStatus.FAILED }
                .keys
            failedIds.forEach { chapterId ->
                val download = _downloads.value.find { it.chapterId == chapterId }
                if (download != null) {
                    downloadUseCases.deleteSavedDownload(
                        ireader.domain.models.entities.Download(
                            chapterId = download.chapterId,
                            bookId = download.bookId,
                            priority = 0
                        )
                    )
                }
            }
        }
    }
    
    /**
     * Toggle WiFi-only mode.
     */
    fun setWifiOnlyMode(enabled: Boolean) {
        scope.launch {
            downloadPreferences.downloadOnlyOnWifi().set(enabled)
            _isWifiOnlyMode.value = enabled
        }
    }
    
    /**
     * Allow mobile data temporarily and resume downloads.
     */
    fun allowMobileDataTemporarily() {
        scope.launch {
            downloadService.resume()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Selection Management
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
        selection.addAll(_downloads.value.map { it.chapterId })
    }
    
    fun clearSelection() {
        selection.clear()
    }
}
