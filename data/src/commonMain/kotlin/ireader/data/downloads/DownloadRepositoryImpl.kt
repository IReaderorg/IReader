package ireader.data.downloads

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.data.util.BaseDao
import ireader.data.core.DatabaseHandler


class DownloadRepositoryImpl(private val handler: DatabaseHandler,) :
    DownloadRepository, BaseDao<Download>() {
    override fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>> {
       return handler.subscribeToList {
            downloadQueries.findAll(downloadMapper)
        }
    }

    override suspend fun findAllDownloads(): List<SavedDownloadWithInfo> {
        return handler.awaitList {
            downloadQueries.findAll(downloadMapper)
        }
    }

    override suspend fun insertDownload(savedDownload: Download) {
        handler.await {
                downloadQueries.upsert(savedDownload.chapterId,savedDownload.bookId,savedDownload.priority)
        }
    }

    override suspend fun insertDownloads(savedDownloads: List<Download>) {
        handler.await {
            dbOperation(savedDownloads) {
                downloadQueries.upsert(it.chapterId,it.bookId,it.priority)
            }
        }
    }

    override suspend fun deleteSavedDownload(savedDownload: Download) {
        handler.await {
            downloadQueries.deleteByChapterId(savedDownload.chapterId)
        }
    }

    override suspend fun deleteSavedDownload(savedDownloads: List<Download>) {
        handler.await(true) {
            savedDownloads.forEach {  savedDownload ->
                downloadQueries.deleteByChapterId(savedDownload.chapterId)
            }
        }
    }

    override suspend fun deleteSavedDownloadByBookId(bookId: Long) {
        handler.await {
            downloadQueries.deleteByBookId(bookId)
        }
    }

    override suspend fun deleteAllSavedDownload() {
        handler.await {
           downloadQueries.deleteAll()
        }
    }
    
    override suspend fun updateDownloadPriority(chapterId: Long, priority: Int) {
        handler.await {
            downloadQueries.updatePriority(priority, chapterId)
        }
    }
    
    override suspend fun markDownloadAsFailed(chapterId: Long, errorMessage: String) {
        // Failed downloads are tracked in DownloadServiceState
        // This method is here for future database schema updates
    }
    
    override suspend fun retryFailedDownload(chapterId: Long) {
        // Retry is handled by re-adding to download queue
        // This method is here for future enhancements
    }

    // Enhanced methods following Mihon's pattern - stub implementations
    override fun getDownloadQueueAsFlow(): Flow<List<ireader.domain.models.download.DownloadItem>> {
        TODO("Enhanced download queue not yet implemented")
    }
    
    override suspend fun getDownloadQueue(): List<ireader.domain.models.download.DownloadItem> {
        TODO("Enhanced download queue not yet implemented")
    }
    
    override suspend fun addToQueue(item: ireader.domain.models.download.DownloadItem) {
        TODO("Enhanced download queue not yet implemented")
    }
    
    override suspend fun addToQueue(items: List<ireader.domain.models.download.DownloadItem>) {
        TODO("Enhanced download queue not yet implemented")
    }
    
    override suspend fun removeFromQueue(chapterId: Long) {
        deleteSavedDownload(Download(chapterId = chapterId, bookId = 0, priority = 0))
    }
    
    override suspend fun removeFromQueue(chapterIds: List<Long>) {
        deleteSavedDownload(chapterIds.map { Download(chapterId = it, bookId = 0, priority = 0) })
    }
    
    override suspend fun updateDownloadStatus(chapterId: Long, status: ireader.domain.models.download.DownloadStatus) {
        TODO("Enhanced download status not yet implemented")
    }
    
    override suspend fun updateDownloadProgress(
        chapterId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speed: Float
    ) {
        TODO("Enhanced download progress not yet implemented")
    }
    
    override suspend fun markDownloadFailed(chapterId: Long, errorMessage: String, retryCount: Int) {
        markDownloadAsFailed(chapterId, errorMessage)
    }
    
    override suspend fun markDownloadCompleted(chapterId: Long, filePath: String) {
        TODO("Enhanced download completion not yet implemented")
    }
    
    override suspend fun getDownloadsByStatus(status: ireader.domain.models.download.DownloadStatus): List<ireader.domain.models.download.DownloadItem> {
        TODO("Enhanced download filtering not yet implemented")
    }
    
    override suspend fun getDownloadsByBook(bookId: Long): List<ireader.domain.models.download.DownloadItem> {
        TODO("Enhanced download filtering not yet implemented")
    }
    
    override suspend fun getDownloadStats(): ireader.domain.models.download.DownloadStats {
        TODO("Enhanced download stats not yet implemented")
    }
    
    override suspend fun clearCompletedDownloads() {
        TODO("Enhanced download clearing not yet implemented")
    }
    
    override suspend fun clearFailedDownloads() {
        TODO("Enhanced download clearing not yet implemented")
    }
    
    override suspend fun reorderQueue(chapterIds: List<Long>) {
        TODO("Enhanced download reordering not yet implemented")
    }
    
    override suspend fun pauseAllDownloads() {
        TODO("Enhanced download pausing not yet implemented")
    }
    
    override suspend fun resumeAllDownloads() {
        TODO("Enhanced download resuming not yet implemented")
    }
    
    override suspend fun cancelAllDownloads() {
        deleteAllSavedDownload()
    }
    
    override suspend fun getDownloadCacheEntries(): List<ireader.domain.models.download.DownloadCacheEntry> {
        TODO("Enhanced download cache not yet implemented")
    }
    
    override suspend fun addDownloadCacheEntry(entry: ireader.domain.models.download.DownloadCacheEntry) {
        TODO("Enhanced download cache not yet implemented")
    }
    
    override suspend fun removeDownloadCacheEntry(chapterId: Long) {
        TODO("Enhanced download cache not yet implemented")
    }
    
    override suspend fun cleanupInvalidCacheEntries() {
        TODO("Enhanced download cache cleanup not yet implemented")
    }
    
    override suspend fun getDownloadQueueConfig(): ireader.domain.models.download.DownloadQueueConfig {
        TODO("Enhanced download config not yet implemented")
    }
    
    override suspend fun saveDownloadQueueConfig(config: ireader.domain.models.download.DownloadQueueConfig) {
        TODO("Enhanced download config not yet implemented")
    }
}
