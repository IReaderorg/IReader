package ireader.domain.models.download

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Represents a download with rich state tracking.
 * Based on Mihon's download model for robust progress and error handling.
 * 
 * This is a lightweight version that stores IDs instead of full objects
 * for efficient serialization and queue persistence.
 */
data class Download(
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val chapterName: String = "",
    val bookTitle: String = "",
    val coverUrl: String = "",
    val status: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val progress: Int = 0,           // 0-100 percentage
    val downloadedPages: Int = 0,
    val totalPages: Int = 0,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val priority: Int = 0            // Lower = higher priority
) {
    /**
     * Progress as a float between 0.0 and 1.0.
     */
    val progressFloat: Float
        get() = progress / 100f
    
    /**
     * Returns true if the download is complete.
     */
    val isComplete: Boolean
        get() = status == DownloadStatus.DOWNLOADED
    
    /**
     * Returns true if the download has failed.
     */
    val isFailed: Boolean
        get() = status == DownloadStatus.ERROR
    
    /**
     * Returns true if the download is currently active.
     */
    val isActive: Boolean
        get() = status == DownloadStatus.DOWNLOADING
    
    /**
     * Returns true if the download is queued.
     */
    val isQueued: Boolean
        get() = status == DownloadStatus.QUEUE
    
    /**
     * Returns true if the download is pending (queued or downloading).
     */
    val isPending: Boolean
        get() = status.isPending
    
    /**
     * Returns true if the download can be retried.
     */
    val canRetry: Boolean
        get() = status.canRetry && retryCount < maxRetries
    
    /**
     * Returns true if the download can be cancelled.
     */
    val canCancel: Boolean
        get() = status.canCancel
    
    /**
     * Returns true if all retry attempts have been exhausted.
     */
    val retriesExhausted: Boolean
        get() = retryCount >= maxRetries
    
    /**
     * Returns a human-readable progress string.
     */
    val progressString: String
        get() = when {
            totalPages > 0 -> "$downloadedPages/$totalPages"
            progress > 0 -> "$progress%"
            else -> ""
        }
    
    /**
     * Creates a copy with updated status.
     */
    fun withStatus(newStatus: DownloadStatus): Download = copy(status = newStatus)
    
    /**
     * Creates a copy with updated progress.
     */
    fun withProgress(newProgress: Int, downloaded: Int = downloadedPages, total: Int = totalPages): Download =
        copy(progress = newProgress.coerceIn(0, 100), downloadedPages = downloaded, totalPages = total)
    
    /**
     * Creates a copy marked as failed with error message.
     */
    fun withError(message: String): Download =
        copy(status = DownloadStatus.ERROR, errorMessage = message)
    
    /**
     * Creates a copy with incremented retry count.
     */
    fun withRetry(): Download =
        copy(retryCount = retryCount + 1, errorMessage = null, status = DownloadStatus.QUEUE)
    
    /**
     * Creates a copy marked as completed.
     */
    fun asCompleted(): Download =
        copy(status = DownloadStatus.DOWNLOADED, progress = 100, errorMessage = null)
    
    /**
     * Creates a copy marked as downloading.
     */
    fun asDownloading(): Download =
        copy(status = DownloadStatus.DOWNLOADING)
    
    /**
     * Creates a copy marked as queued.
     */
    fun asQueued(): Download =
        copy(status = DownloadStatus.QUEUE, progress = 0, errorMessage = null)
    
    companion object {
        /**
         * Creates a new Download from chapter and book.
         */
        fun create(
            chapter: Chapter,
            book: Book,
            priority: Int = 0
        ): Download = Download(
            chapterId = chapter.id,
            bookId = book.id,
            sourceId = book.sourceId,
            chapterName = chapter.name,
            bookTitle = book.title,
            coverUrl = book.cover,
            status = DownloadStatus.QUEUE,
            priority = priority
        )
    }
}
