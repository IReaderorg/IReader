package ireader.presentation.ui.settings.downloader

import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.NotificationService
import ireader.domain.services.common.NotificationPriority
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import ireader.domain.usecases.download.DownloadUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel for managing downloads
 * 
 * ✅ CLEAN ARCHITECTURE: This ViewModel correctly uses DownloadService interface
 * instead of the deprecated DownloadServiceStateImpl. It observes service state
 * through StateFlow and uses ServiceResult for error handling.
 */
class DownloaderViewModel(
        private val downloadUseCases: DownloadUseCases,
        private val downloadState: DownloadStateImpl,
        private val downloadService: DownloadService,  // ✅ Uses service interface
        private val notificationService: NotificationService
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DownloadState by downloadState {

    // Expose service states
    val downloadServiceState: StateFlow<ServiceState> = downloadService.state.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceState.IDLE
    )
    
    val downloadServiceProgress = downloadService.downloadProgress.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // Computed properties for UI
    val isRunning: Boolean
        get() = downloadServiceState.value == ServiceState.RUNNING
    
    val isPaused: Boolean
        get() = downloadServiceState.value == ServiceState.PAUSED

    init {
        subscribeDownloads()
        scope.launch {
            downloadService.initialize()
            notificationService.initialize()
        }
    }

    private var getBooksJob: Job? = null
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = scope.launch {
            downloadUseCases.subscribeDownloadsUseCase().collect { list ->
                downloads = list.filter { it.chapterId != 0L }
            }
        }
    }

    /**
     * Start or resume downloads
     */
    fun startDownloadService(chapterIds: List<Long>) {
        if (downloads.isEmpty()) return
        
        scope.launch {
            if (downloadServiceState.value == ServiceState.PAUSED) {
                resumeDownloads()
            } else {
                when (val result = downloadService.queueChapters(chapterIds)) {
                    is ServiceResult.Success -> {
                        notificationService.showNotification(
                            id = 1001,
                            title = "Downloads Started",
                            message = "${chapterIds.size} chapter(s) queued",
                            priority = NotificationPriority.DEFAULT
                        )
                    }
                    is ServiceResult.Error -> {
                        notificationService.showNotification(
                            id = 1002,
                            title = "Download Failed",
                            message = result.message,
                            priority = NotificationPriority.HIGH
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Pause downloads
     */
    fun pauseDownloads() {
        scope.launch {
            downloadService.pause()
        }
    }

    /**
     * Resume paused downloads
     */
    fun resumeDownloads() {
        scope.launch {
            downloadService.resume()
        }
    }

    /**
     * Stop downloads completely
     */
    fun stopDownloads() {
        scope.launch {
            when (downloadService.cancelAll()) {
                is ServiceResult.Success -> {
                    notificationService.showNotification(
                        id = 1003,
                        title = "Downloads Stopped",
                        message = "All downloads cancelled",
                        priority = NotificationPriority.DEFAULT
                    )
                }
                is ServiceResult.Error -> {
                    // Handle error silently
                }
                else -> {}
            }
        }
    }

    fun toggleExpandMenu(enable: Boolean = true) {
        isMenuExpanded = enable
    }

    fun deleteAllDownloads() {
        scope.launch(Dispatchers.IO) {
            downloadUseCases.deleteAllSavedDownload()
        }
    }
    fun deleteSelectedDownloads(list: List<SavedDownload>) {
        scope.launch(Dispatchers.IO) {
            downloadUseCases.deleteSavedDownloads(list.map { it.toDownload() })
        }
    }
    
    /**
     * Move a download up in priority (decrease priority number)
     */
    fun moveDownloadUp(download: SavedDownloadWithInfo) {
        scope.launch(Dispatchers.IO) {
            val currentIndex = downloads.indexOfFirst { it.chapterId == download.chapterId }
            if (currentIndex > 0) {
                // Swap priorities with the item above
                val itemAbove = downloads[currentIndex - 1]
                val currentPriority = download.priority
                val abovePriority = itemAbove.priority
                
                // Update priorities in database
                downloadUseCases.updateDownloadPriority(download.chapterId, abovePriority)
                downloadUseCases.updateDownloadPriority(itemAbove.chapterId, currentPriority)
            }
        }
    }
    
    /**
     * Move a download down in priority (increase priority number)
     */
    fun moveDownloadDown(download: SavedDownloadWithInfo) {
        scope.launch(Dispatchers.IO) {
            val currentIndex = downloads.indexOfFirst { it.chapterId == download.chapterId }
            if (currentIndex < downloads.size - 1) {
                // Swap priorities with the item below
                val itemBelow = downloads[currentIndex + 1]
                val currentPriority = download.priority
                val belowPriority = itemBelow.priority
                
                // Update priorities in database
                downloadUseCases.updateDownloadPriority(download.chapterId, belowPriority)
                downloadUseCases.updateDownloadPriority(itemBelow.chapterId, currentPriority)
            }
        }
    }
    
    /**
     * Reorder downloads by dragging (for future implementation with drag-drop library)
     */
    fun reorderDownloads(fromIndex: Int, toIndex: Int) {
        scope.launch(Dispatchers.IO) {
            if (fromIndex == toIndex) return@launch
            
            val newList = downloads.toMutableList()
            val item = newList.removeAt(fromIndex)
            newList.add(toIndex, item)
            
            // Update all priorities based on new order
            newList.forEachIndexed { index, download ->
                downloadUseCases.updateDownloadPriority(download.chapterId, index)
            }
        }
    }
    
    /**
     * Retry a failed download
     */
    fun retryFailedDownload(chapterId: Long) {
        scope.launch {
            when (val result = downloadService.retryDownload(chapterId)) {
                is ServiceResult.Success -> {
                    notificationService.showNotification(
                        id = 1004,
                        title = "Retrying Download",
                        message = "Download queued for retry",
                        priority = NotificationPriority.LOW
                    )
                }
                is ServiceResult.Error -> {
                    notificationService.showNotification(
                        id = 1005,
                        title = "Retry Failed",
                        message = result.message,
                        priority = NotificationPriority.HIGH
                    )
                }
                else -> {}
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            downloadService.cleanup()
            notificationService.cleanup()
        }
    }
}
