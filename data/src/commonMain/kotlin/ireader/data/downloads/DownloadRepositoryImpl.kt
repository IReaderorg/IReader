package ireader.data.downloads

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.domain.models.entities.Download
import ireader.domain.models.entities.SavedDownloadWithInfo
import ireader.domain.models.download.DownloadItem
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.download.DownloadStats
import ireader.domain.models.download.DownloadCacheEntry
import ireader.domain.models.download.DownloadQueueConfig
import ireader.data.util.BaseDao
import ireader.data.core.DatabaseHandler


class DownloadRepositoryImpl(private val handler: DatabaseHandler) :
    DownloadRepository, BaseDao<Download>() {
    
    // In-memory state for enhanced features (can be persisted to preferences later)
    private var downloadQueueConfig = DownloadQueueConfig()
    private val downloadStatusMap = mutableMapOf<Long, DownloadStatus>()
    private val downloadProgressMap = mutableMapOf<Long, Float>()
    private val downloadCacheEntries = mutableListOf<DownloadCacheEntry>()
    
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
            downloadQueries.upsert(savedDownload.chapterId, savedDownload.bookId, savedDownload.priority)
        }
        downloadStatusMap[savedDownload.chapterId] = DownloadStatus.QUEUED
    }

    override suspend fun insertDownloads(savedDownloads: List<Download>) {
        handler.await {
            dbOperation(savedDownloads) {
                downloadQueries.upsert(it.chapterId, it.bookId, it.priority)
            }
        }
        savedDownloads.forEach { download ->
            downloadStatusMap[download.chapterId] = DownloadStatus.QUEUED
        }
    }

    override suspend fun deleteSavedDownload(savedDownload: Download) {
        handler.await {
            downloadQueries.deleteByChapterId(savedDownload.chapterId)
        }
        downloadStatusMap.remove(savedDownload.chapterId)
        downloadProgressMap.remove(savedDownload.chapterId)
    }

    override suspend fun deleteSavedDownload(savedDownloads: List<Download>) {
        handler.await(true) {
            savedDownloads.forEach { savedDownload ->
                downloadQueries.deleteByChapterId(savedDownload.chapterId)
            }
        }
        savedDownloads.forEach { download ->
            downloadStatusMap.remove(download.chapterId)
            downloadProgressMap.remove(download.chapterId)
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
        downloadStatusMap.clear()
        downloadProgressMap.clear()
    }

    override suspend fun updateDownloadPriority(chapterId: Long, priority: Int) {
        handler.await {
            downloadQueries.updatePriority(priority, chapterId)
        }
    }

    override suspend fun markDownloadAsFailed(chapterId: Long, errorMessage: String) {
        downloadStatusMap[chapterId] = DownloadStatus.FAILED
    }

    override suspend fun retryFailedDownload(chapterId: Long) {
        downloadStatusMap[chapterId] = DownloadStatus.QUEUED
        downloadProgressMap[chapterId] = 0f
    }

    // Enhanced methods following Mihon's pattern
    override fun getDownloadQueueAsFlow(): Flow<List<DownloadItem>> {
        return subscribeAllDownloads().map { downloads ->
            downloads.map { it.toDownloadItem() }
        }
    }

    override suspend fun getDownloadQueue(): List<DownloadItem> {
        return findAllDownloads().map { it.toDownloadItem() }
    }

    override suspend fun addToQueue(item: DownloadItem) {
        insertDownload(Download(
            chapterId = item.chapterId,
            bookId = item.bookId,
            priority = item.priority
        ))
        downloadStatusMap[item.chapterId] = item.status
    }

    override suspend fun addToQueue(items: List<DownloadItem>) {
        insertDownloads(items.map { item ->
            Download(
                chapterId = item.chapterId,
                bookId = item.bookId,
                priority = item.priority
            )
        })
        items.forEach { item ->
            downloadStatusMap[item.chapterId] = item.status
        }
    }

    override suspend fun removeFromQueue(chapterId: Long) {
        deleteSavedDownload(Download(chapterId = chapterId, bookId = 0, priority = 0))
    }

    override suspend fun removeFromQueue(chapterIds: List<Long>) {
        deleteSavedDownload(chapterIds.map { Download(chapterId = it, bookId = 0, priority = 0) })
    }

    override suspend fun updateDownloadStatus(chapterId: Long, status: DownloadStatus) {
        downloadStatusMap[chapterId] = status
        if (status == DownloadStatus.COMPLETED) {
            // Remove from queue when completed
            removeFromQueue(chapterId)
        }
    }

    override suspend fun updateDownloadProgress(
        chapterId: Long,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
        speed: Float
    ) {
        downloadProgressMap[chapterId] = progress
        if (downloadStatusMap[chapterId] != DownloadStatus.DOWNLOADING) {
            downloadStatusMap[chapterId] = DownloadStatus.DOWNLOADING
        }
    }

    override suspend fun markDownloadFailed(chapterId: Long, errorMessage: String, retryCount: Int) {
        downloadStatusMap[chapterId] = DownloadStatus.FAILED
    }

    override suspend fun markDownloadCompleted(chapterId: Long, filePath: String) {
        downloadStatusMap[chapterId] = DownloadStatus.COMPLETED
        downloadProgressMap[chapterId] = 1f
        // Remove from download queue
        removeFromQueue(chapterId)
    }

    override suspend fun getDownloadsByStatus(status: DownloadStatus): List<DownloadItem> {
        return getDownloadQueue().filter { 
            downloadStatusMap[it.chapterId] == status || 
            (status == DownloadStatus.QUEUED && !downloadStatusMap.containsKey(it.chapterId))
        }
    }

    override suspend fun getDownloadsByBook(bookId: Long): List<DownloadItem> {
        return getDownloadQueue().filter { it.bookId == bookId }
    }

    override suspend fun getDownloadStats(): DownloadStats {
        val allDownloads = getDownloadQueue()
        val statusCounts = allDownloads.groupBy { 
            downloadStatusMap[it.chapterId] ?: DownloadStatus.QUEUED 
        }
        
        return DownloadStats(
            totalDownloads = allDownloads.size,
            completedDownloads = statusCounts[DownloadStatus.COMPLETED]?.size ?: 0,
            failedDownloads = statusCounts[DownloadStatus.FAILED]?.size ?: 0,
            queuedDownloads = statusCounts[DownloadStatus.QUEUED]?.size ?: 0,
            downloadingCount = statusCounts[DownloadStatus.DOWNLOADING]?.size ?: 0,
            totalBytesDownloaded = 0L,
            averageSpeed = 0f,
            totalDownloadTime = 0L
        )
    }

    override suspend fun clearCompletedDownloads() {
        val completedChapterIds = downloadStatusMap.entries
            .filter { it.value == DownloadStatus.COMPLETED }
            .map { it.key }
        
        completedChapterIds.forEach { chapterId ->
            downloadStatusMap.remove(chapterId)
            downloadProgressMap.remove(chapterId)
        }
    }

    override suspend fun clearFailedDownloads() {
        val failedChapterIds = downloadStatusMap.entries
            .filter { it.value == DownloadStatus.FAILED }
            .map { it.key }
        
        removeFromQueue(failedChapterIds)
    }

    override suspend fun reorderQueue(chapterIds: List<Long>) {
        chapterIds.forEachIndexed { index, chapterId ->
            updateDownloadPriority(chapterId, index)
        }
    }

    override suspend fun pauseAllDownloads() {
        downloadStatusMap.entries
            .filter { it.value == DownloadStatus.DOWNLOADING }
            .forEach { entry ->
                downloadStatusMap[entry.key] = DownloadStatus.PAUSED
            }
    }

    override suspend fun resumeAllDownloads() {
        downloadStatusMap.entries
            .filter { it.value == DownloadStatus.PAUSED }
            .forEach { entry ->
                downloadStatusMap[entry.key] = DownloadStatus.QUEUED
            }
    }

    override suspend fun cancelAllDownloads() {
        deleteAllSavedDownload()
    }

    override suspend fun getDownloadCacheEntries(): List<DownloadCacheEntry> {
        return downloadCacheEntries.toList()
    }

    override suspend fun addDownloadCacheEntry(entry: DownloadCacheEntry) {
        downloadCacheEntries.removeAll { it.chapterId == entry.chapterId }
        downloadCacheEntries.add(entry)
    }

    override suspend fun removeDownloadCacheEntry(chapterId: Long) {
        downloadCacheEntries.removeAll { it.chapterId == chapterId }
    }

    override suspend fun cleanupInvalidCacheEntries() {
        downloadCacheEntries.removeAll { !it.isValid }
    }

    override suspend fun getDownloadQueueConfig(): DownloadQueueConfig {
        return downloadQueueConfig
    }

    override suspend fun saveDownloadQueueConfig(config: DownloadQueueConfig) {
        downloadQueueConfig = config
    }
    
    /**
     * Convert SavedDownloadWithInfo to DownloadItem
     */
    private fun SavedDownloadWithInfo.toDownloadItem(): DownloadItem {
        return DownloadItem(
            chapterId = this.chapterId,
            bookId = this.bookId,
            sourceId = 0L, // Not available in SavedDownloadWithInfo
            bookTitle = this.bookName,
            chapterTitle = this.chapterName,
            chapterUrl = this.chapterKey,
            priority = this.priority,
            status = downloadStatusMap[this.chapterId] ?: DownloadStatus.QUEUED,
            progress = downloadProgressMap[this.chapterId] ?: 0f
        )
    }
}
