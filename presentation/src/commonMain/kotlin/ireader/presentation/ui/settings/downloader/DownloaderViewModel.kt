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
 * Uses DownloadService interface to observe service state through StateFlow
 * and uses ServiceResult for error handling.
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
    
    // Computed properties for UI - these are reactive through StateFlow collection
    // Note: Use downloadServiceState.collectAsState() in Composables for reactivity

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
        scope.launch {
            when (downloadServiceState.value) {
                ServiceState.PAUSED -> {
                    // Resume paused downloads
                    resumeDownloads()
                }
                ServiceState.RUNNING -> {
                    // Already running, just add more chapters if provided
                    if (chapterIds.isNotEmpty()) {
                        when (val result = downloadService.queueChapters(chapterIds)) {
                            is ServiceResult.Success -> {
                                notificationService.showNotification(
                                    id = 1001,
                                    title = "Downloads Queued",
                                    message = "${chapterIds.size} chapter(s) added to queue",
                                    priority = NotificationPriority.DEFAULT
                                )
                            }
                            is ServiceResult.Error -> {
                                notificationService.showNotification(
                                    id = 1002,
                                    title = "Queue Failed",
                                    message = result.message,
                                    priority = NotificationPriority.HIGH
                                )
                            }
                            else -> {}
                        }
                    }
                }
                else -> {
                    // Not running - start downloads
                    val idsToDownload = if (chapterIds.isNotEmpty()) {
                        chapterIds
                    } else {
                        // Use downloads from database if no specific chapters provided
                        downloads.map { it.chapterId }
                    }
                    
                    if (idsToDownload.isEmpty()) return@launch
                    
                    when (val result = downloadService.queueChapters(idsToDownload)) {
                        is ServiceResult.Success -> {
                            notificationService.showNotification(
                                id = 1001,
                                title = "Downloads Started",
                                message = "${idsToDownload.size} chapter(s) queued",
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
     * Reorder downloads by index (for drag-and-drop reordering)
     * Note: Swipe-to-reorder requires a drag-and-drop library like 
     * org.burnoutcrew.reorderable or similar. This function is ready
     * to be connected when such a library is added.
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
