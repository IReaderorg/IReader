package ireader.presentation.ui.settings.downloader

import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.NotificationService
import ireader.domain.services.common.NotificationPriority
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import ireader.domain.usecases.download.DownloadUseCases
import ireader.i18n.LocalizeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res


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
        private val notificationService: NotificationService,
        private val localizeHelper: LocalizeHelper
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DownloadState by downloadState {

    // Expose service states
    val downloadServiceState: StateFlow<ServiceState> = downloadService.state.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceState.IDLE
    )
    
    val downloadServiceProgress = downloadService.downloadProgress.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
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
            val idsToDownload = if (chapterIds.isNotEmpty()) {
                chapterIds
            } else {
                // Use downloads from database if no specific chapters provided
                downloads.map { it.chapterId }
            }
            
            if (idsToDownload.isEmpty()) return@launch
            
            // Always queue chapters - the service will handle deduplication
            when (val result = downloadService.queueChapters(idsToDownload)) {
                is ServiceResult.Success -> {
                    // Don't show notification - the download service will show progress
                }
                is ServiceResult.Error -> {
                    notificationService.showNotification(
                        id = 1002,
                        title = localizeHelper.localize(Res.string.download_failed_title),
                        message = result.message,
                        priority = NotificationPriority.HIGH
                    )
                }
                else -> {}
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
                        title = localizeHelper.localize(Res.string.downloads_stopped),
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
                        title = localizeHelper.localize(Res.string.retrying_download),
                        message = "Download queued for retry",
                        priority = NotificationPriority.LOW
                    )
                }
                is ServiceResult.Error -> {
                    notificationService.showNotification(
                        id = 1005,
                        title = localizeHelper.localize(Res.string.retry_failed),
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
