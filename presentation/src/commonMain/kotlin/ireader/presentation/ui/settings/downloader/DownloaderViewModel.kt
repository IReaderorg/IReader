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

    fun startDownloadService(chapterIds: List<Long>) {
        if (downloads.isEmpty()) return
        serviceUseCases.startDownloadServicesUseCase.start(
            downloadModes = true
        )
    }

    fun stopDownloads() {
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
            // Remove from failed downloads
            val failedDownloads = downloadServiceStateImpl.failedDownloads.toMutableMap()
            val failedDownload = failedDownloads.remove(chapterId)
            downloadServiceStateImpl.failedDownloads = failedDownloads
            
            // Re-add to download queue by restarting the download service
            if (failedDownload != null) {
                // The download is already in the queue, just restart the service
                serviceUseCases.startDownloadServicesUseCase.start(downloadModes = true)
            }
        }
    }
    
    /**
     * Mark a download as failed
     */
    fun markDownloadAsFailed(chapterId: Long, errorMessage: String) {
        val failedDownloads = downloadServiceStateImpl.failedDownloads.toMutableMap()
        val existingFailure = failedDownloads[chapterId]
        val retryCount = (existingFailure?.retryCount ?: 0) + 1
        
        failedDownloads[chapterId] = ireader.domain.services.downloaderService.FailedDownload(
            chapterId = chapterId,
            errorMessage = errorMessage,
            retryCount = retryCount
        )
        downloadServiceStateImpl.failedDownloads = failedDownloads
    }
    
    /**
     * Add a download to completed list
     */
    fun addCompletedDownload(download: SavedDownloadWithInfo) {
        val completedDownloads = downloadServiceStateImpl.completedDownloads.toMutableList()
        completedDownloads.add(
            ireader.domain.services.downloaderService.CompletedDownload(
                chapterId = download.chapterId,
                bookId = download.bookId,
                bookName = download.bookName,
                chapterName = download.chapterName,
                completedAt = System.currentTimeMillis()
            )
        )
        downloadServiceStateImpl.completedDownloads = completedDownloads
    }
    
    /**
     * Clear all completed downloads
     */
    fun clearCompletedDownloads() {
        downloadServiceStateImpl.completedDownloads = emptyList()
    }
    
    /**
     * Remove a specific completed download
     */
    fun removeCompletedDownload(chapterId: Long) {
        val completedDownloads = downloadServiceStateImpl.completedDownloads.toMutableList()
        completedDownloads.removeAll { it.chapterId == chapterId }
        downloadServiceStateImpl.completedDownloads = completedDownloads
    }
}
