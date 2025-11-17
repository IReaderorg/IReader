package ireader.domain.usecases.download

import ireader.domain.data.repository.DownloadRepository
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.download.*
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Enhanced download manager following Mihon's DownloadManager pattern
 */
class DownloadManagerUseCase(
    private val downloadRepository: DownloadRepository,
    private val notificationRepository: NotificationRepository
) {
    
    private val downloadSemaphore = Semaphore(3) // Max concurrent downloads
    
    /**
     * Add chapter to download queue
     */
    suspend fun addToQueue(book: Book, chapter: Chapter, priority: Int = 0) {
        val downloadItem = DownloadItem(
            chapterId = chapter.id,
            bookId = book.id,
            sourceId = book.sourceId,
            bookTitle = book.title,
            chapterTitle = chapter.name,
            chapterUrl = chapter.key,
            priority = priority
        )
        
        downloadRepository.addToQueue(downloadItem)
    }
    
    /**
     * Add multiple chapters to download queue
     */
    suspend fun addToQueue(book: Book, chapters: List<Chapter>, priority: Int = 0) {
        val downloadItems = chapters.map { chapter ->
            DownloadItem(
                chapterId = chapter.id,
                bookId = book.id,
                sourceId = book.sourceId,
                bookTitle = book.title,
                chapterTitle = chapter.name,
                chapterUrl = chapter.key,
                priority = priority
            )
        }
        
        downloadRepository.addToQueue(downloadItems)
    }
    
    /**
     * Process download queue
     */
    suspend fun processQueue(): Flow<DownloadStats> = flow {
        val config = downloadRepository.getDownloadQueueConfig()
        
        while (true) {
            val queuedDownloads = downloadRepository.getDownloadsByStatus(DownloadStatus.QUEUED)
                .sortedByDescending { it.priority }
                .take(config.maxConcurrentDownloads)
            
            if (queuedDownloads.isEmpty()) {
                delay(5000) // Wait before checking again
                continue
            }
            
            // Process downloads concurrently
            queuedDownloads.forEach { download ->
                downloadSemaphore.withPermit {
                    processDownload(download, config)
                }
            }
            
            // Emit current stats
            val stats = downloadRepository.getDownloadStats()
            emit(stats)
            
            delay(1000) // Update interval
        }
    }
    
    /**
     * Process individual download
     */
    private suspend fun processDownload(download: DownloadItem, config: DownloadQueueConfig) {
        try {
            // Mark as downloading
            downloadRepository.updateDownloadStatus(download.chapterId, DownloadStatus.DOWNLOADING)
            
            // Show progress notification
            notificationRepository.showDownloadProgressNotification(
                downloadCount = 1,
                progress = 0f,
                speed = 0f,
                eta = 0L
            )
            
            // Simulate download process
            var progress = 0f
            val totalBytes = 1024L * 1024L // 1MB placeholder
            var downloadedBytes = 0L
            
            while (progress < 1f) {
                delay(100) // Simulate download time
                progress += 0.1f
                downloadedBytes = (totalBytes * progress).toLong()
                val speed = 1024f * 10f // 10KB/s placeholder
                
                downloadRepository.updateDownloadProgress(
                    download.chapterId,
                    progress,
                    downloadedBytes,
                    totalBytes,
                    speed
                )
                
                // Update notification
                notificationRepository.showDownloadProgressNotification(
                    downloadCount = 1,
                    progress = progress,
                    speed = speed,
                    eta = ((totalBytes - downloadedBytes) / speed).toLong()
                )
            }
            
            // Mark as completed
            downloadRepository.markDownloadCompleted(download.chapterId, "/path/to/file")
            
            // Show completion notification
            notificationRepository.showDownloadCompletedNotification(
                bookTitle = download.bookTitle,
                chapterTitle = download.chapterTitle,
                totalDownloads = 1
            )
            
        } catch (e: Exception) {
            // Mark as failed
            downloadRepository.markDownloadFailed(
                download.chapterId,
                e.message ?: "Unknown error",
                download.retryCount + 1
            )
            
            // Show error notification
            notificationRepository.showDownloadFailedNotification(
                bookTitle = download.bookTitle,
                chapterTitle = download.chapterTitle,
                errorMessage = e.message ?: "Unknown error"
            )
            
            // Retry if configured
            if (config.autoRetryOnFailure && download.retryCount < config.maxRetryAttempts) {
                delay(config.retryDelayMs)
                downloadRepository.updateDownloadStatus(download.chapterId, DownloadStatus.QUEUED)
            }
        }
    }
    
    /**
     * Pause download
     */
    suspend fun pauseDownload(chapterId: Long) {
        downloadRepository.updateDownloadStatus(chapterId, DownloadStatus.PAUSED)
    }
    
    /**
     * Resume download
     */
    suspend fun resumeDownload(chapterId: Long) {
        downloadRepository.updateDownloadStatus(chapterId, DownloadStatus.QUEUED)
    }
    
    /**
     * Cancel download
     */
    suspend fun cancelDownload(chapterId: Long) {
        downloadRepository.updateDownloadStatus(chapterId, DownloadStatus.CANCELLED)
        downloadRepository.removeFromQueue(chapterId)
    }
    
    /**
     * Retry failed download
     */
    suspend fun retryDownload(chapterId: Long) {
        downloadRepository.updateDownloadStatus(chapterId, DownloadStatus.QUEUED)
    }
    
    /**
     * Get download queue
     */
    fun getDownloadQueue(): Flow<List<DownloadItem>> {
        return downloadRepository.getDownloadQueueAsFlow()
    }
    
    /**
     * Get download statistics
     */
    suspend fun getDownloadStats(): DownloadStats {
        return downloadRepository.getDownloadStats()
    }
    
    /**
     * Clear completed downloads
     */
    suspend fun clearCompleted() {
        downloadRepository.clearCompletedDownloads()
    }
    
    /**
     * Clear failed downloads
     */
    suspend fun clearFailed() {
        downloadRepository.clearFailedDownloads()
    }
    
    /**
     * Reorder download queue
     */
    suspend fun reorderQueue(chapterIds: List<Long>) {
        downloadRepository.reorderQueue(chapterIds)
    }
}

/**
 * Use case for batch download operations
 */
class BatchDownloadUseCase(
    private val downloadManagerUseCase: DownloadManagerUseCase,
    private val downloadRepository: DownloadRepository
) {
    
    /**
     * Download all chapters of a book
     */
    suspend fun downloadBook(book: Book, chapters: List<Chapter>, skipDownloaded: Boolean = true) {
        val chaptersToDownload = if (skipDownloaded) {
            val existingDownloads = downloadRepository.getDownloadsByBook(book.id)
            val existingChapterIds = existingDownloads.map { it.chapterId }.toSet()
            chapters.filter { it.id !in existingChapterIds }
        } else {
            chapters
        }
        
        downloadManagerUseCase.addToQueue(book, chaptersToDownload)
    }
    
    /**
     * Download unread chapters of a book
     */
    suspend fun downloadUnreadChapters(book: Book, chapters: List<Chapter>) {
        val unreadChapters = chapters.filter { !it.read }
        downloadManagerUseCase.addToQueue(book, unreadChapters)
    }
    
    /**
     * Download next N chapters
     */
    suspend fun downloadNextChapters(book: Book, chapters: List<Chapter>, count: Int) {
        val nextChapters = chapters.take(count)
        downloadManagerUseCase.addToQueue(book, nextChapters)
    }
}

/**
 * Use case for download cache management
 */
class DownloadCacheUseCase(
    private val downloadRepository: DownloadRepository
) {
    
    /**
     * Get cache size
     */
    suspend fun getCacheSize(): Long {
        val cacheEntries = downloadRepository.getDownloadCacheEntries()
        return cacheEntries.sumOf { it.fileSize }
    }
    
    /**
     * Clean up old cache entries
     */
    suspend fun cleanupOldEntries(maxAgeMs: Long = 30L * 24L * 60L * 60L * 1000L) { // 30 days
        val currentTime = System.currentTimeMillis()
        val cacheEntries = downloadRepository.getDownloadCacheEntries()
        
        cacheEntries.forEach { entry ->
            if (currentTime - entry.lastAccessedAt > maxAgeMs) {
                downloadRepository.removeDownloadCacheEntry(entry.chapterId)
            }
        }
    }
    
    /**
     * Clean up invalid cache entries
     */
    suspend fun cleanupInvalidEntries() {
        downloadRepository.cleanupInvalidCacheEntries()
    }
    
    /**
     * Clear all cache
     */
    suspend fun clearAllCache() {
        val cacheEntries = downloadRepository.getDownloadCacheEntries()
        cacheEntries.forEach { entry ->
            downloadRepository.removeDownloadCacheEntry(entry.chapterId)
        }
    }
}