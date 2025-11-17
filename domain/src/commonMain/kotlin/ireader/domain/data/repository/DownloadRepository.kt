package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.models.download.*

/**
 * Enhanced download repository following Mihon's DownloadManager pattern
 */
interface DownloadRepository {

    // Legacy methods for compatibility
    fun subscribeAllDownloads(): Flow<List<SavedDownloadWithInfo>>
    suspend fun findAllDownloads(): List<SavedDownloadWithInfo>
    suspend fun insertDownload(savedDownload: Download)
    suspend fun insertDownloads(savedDownloads: List<Download>)
    suspend fun deleteSavedDownload(savedDownload: Download)
    suspend fun deleteSavedDownload(savedDownloads: List<Download>)
    suspend fun deleteSavedDownloadByBookId(bookId: Long)
    suspend fun deleteAllSavedDownload()
    suspend fun updateDownloadPriority(chapterId: Long, priority: Int)
    suspend fun markDownloadAsFailed(chapterId: Long, errorMessage: String)
    suspend fun retryFailedDownload(chapterId: Long)

    // Enhanced methods following Mihon's pattern
    /**
     * Get download queue as flow
     */
    fun getDownloadQueueAsFlow(): Flow<List<DownloadItem>>
    
    /**
     * Get download queue
     */
    suspend fun getDownloadQueue(): List<DownloadItem>
    
    /**
     * Add download to queue
     */
    suspend fun addToQueue(item: DownloadItem)
    
    /**
     * Add multiple downloads to queue
     */
    suspend fun addToQueue(items: List<DownloadItem>)
    
    /**
     * Remove download from queue
     */
    suspend fun removeFromQueue(chapterId: Long)
    
    /**
     * Remove multiple downloads from queue
     */
    suspend fun removeFromQueue(chapterIds: List<Long>)
    
    /**
     * Update download status
     */
    suspend fun updateDownloadStatus(chapterId: Long, status: DownloadStatus)
    
    /**
     * Update download progress
     */
    suspend fun updateDownloadProgress(
        chapterId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speed: Float
    )
    
    /**
     * Mark download as failed with error
     */
    suspend fun markDownloadFailed(chapterId: Long, errorMessage: String, retryCount: Int)
    
    /**
     * Mark download as completed
     */
    suspend fun markDownloadCompleted(chapterId: Long, filePath: String)
    
    /**
     * Get downloads by status
     */
    suspend fun getDownloadsByStatus(status: DownloadStatus): List<DownloadItem>
    
    /**
     * Get downloads by book
     */
    suspend fun getDownloadsByBook(bookId: Long): List<DownloadItem>
    
    /**
     * Get download statistics
     */
    suspend fun getDownloadStats(): DownloadStats
    
    /**
     * Clear completed downloads
     */
    suspend fun clearCompletedDownloads()
    
    /**
     * Clear failed downloads
     */
    suspend fun clearFailedDownloads()
    
    /**
     * Reorder downloads in queue
     */
    suspend fun reorderQueue(chapterIds: List<Long>)
    
    /**
     * Pause all downloads
     */
    suspend fun pauseAllDownloads()
    
    /**
     * Resume all downloads
     */
    suspend fun resumeAllDownloads()
    
    /**
     * Cancel all downloads
     */
    suspend fun cancelAllDownloads()
    
    /**
     * Get download cache entries
     */
    suspend fun getDownloadCacheEntries(): List<DownloadCacheEntry>
    
    /**
     * Add download cache entry
     */
    suspend fun addDownloadCacheEntry(entry: DownloadCacheEntry)
    
    /**
     * Remove download cache entry
     */
    suspend fun removeDownloadCacheEntry(chapterId: Long)
    
    /**
     * Clean up invalid cache entries
     */
    suspend fun cleanupInvalidCacheEntries()
    
    /**
     * Get download queue configuration
     */
    suspend fun getDownloadQueueConfig(): DownloadQueueConfig
    
    /**
     * Save download queue configuration
     */
    suspend fun saveDownloadQueueConfig(config: DownloadQueueConfig)
}
