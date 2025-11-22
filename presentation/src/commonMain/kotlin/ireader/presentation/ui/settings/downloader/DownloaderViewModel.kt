package ireader.presentation.ui.settings.downloader

import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.services.ServiceUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class DownloaderViewModel(
        private val downloadUseCases: DownloadUseCases,
        private val serviceUseCases: ServiceUseCases,
        private val downloadState: DownloadStateImpl,
        val downloadServiceStateImpl: DownloadServiceStateImpl
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DownloadState by downloadState {

    init {
        subscribeDownloads()
    }

    private var getBooksJob: Job? = null
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = scope.launch {
            downloadUseCases.subscribeDownloadsUseCase().distinctUntilChanged().collect { list ->
                downloads = list.filter { it.chapterId != 0L }
            }
        }
    }

    /**
     * Start or resume downloads
     */
    fun startDownloadService(chapterIds: List<Long>) {
        if (downloads.isEmpty()) return
        
        if (downloadServiceStateImpl.isPaused) {
            // Resume paused downloads
            resumeDownloads()
        } else {
            // Start new download service
            serviceUseCases.startDownloadServicesUseCase.start(
                downloadModes = true
            )
        }
    }

    /**
     * Pause downloads
     */
    fun pauseDownloads() {
        scope.launch(Dispatchers.IO) {
            downloadServiceStateImpl.isPaused = true
        }
    }

    /**
     * Resume paused downloads
     */
    fun resumeDownloads() {
        scope.launch(Dispatchers.IO) {
            downloadServiceStateImpl.isPaused = false
        }
    }

    /**
     * Stop downloads completely
     */
    fun stopDownloads() {
        scope.launch(Dispatchers.IO) {
            downloadServiceStateImpl.isRunning = false
            downloadServiceStateImpl.isPaused = false
        }
        serviceUseCases.startDownloadServicesUseCase.stop()
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
        scope.launch(Dispatchers.IO) {
            // Reset the download progress for this chapter
            val currentProgress = downloadServiceStateImpl.downloadProgress[chapterId]
            if (currentProgress != null) {
                downloadServiceStateImpl.downloadProgress = downloadServiceStateImpl.downloadProgress + 
                    (chapterId to currentProgress.copy(
                        status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED,
                        progress = 0f,
                        errorMessage = null,
                        retryCount = 0
                    ))
            }
            
            // Restart the download service if not running
            if (!downloadServiceStateImpl.isRunning) {
                serviceUseCases.startDownloadServicesUseCase.start(downloadModes = true)
            }
        }
    }
}
