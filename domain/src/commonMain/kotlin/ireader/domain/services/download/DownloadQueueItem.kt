package ireader.domain.services.download

import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import kotlinx.serialization.Serializable

/**
 * Serializable representation of a download queue item for persistence.
 * This is a lightweight version of Download that can be serialized to JSON.
 */
@Serializable
data class DownloadQueueItem(
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val chapterName: String = "",
    val bookTitle: String = "",
    val coverUrl: String = "",
    val status: String = DownloadStatus.QUEUE.name,
    val progress: Int = 0,
    val downloadedPages: Int = 0,
    val totalPages: Int = 0,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val priority: Int = 0
) {
    companion object {
        /**
         * Creates a DownloadQueueItem from a Download.
         */
        fun fromDownload(download: Download): DownloadQueueItem {
            return DownloadQueueItem(
                chapterId = download.chapterId,
                bookId = download.bookId,
                sourceId = download.sourceId,
                chapterName = download.chapterName,
                bookTitle = download.bookTitle,
                coverUrl = download.coverUrl,
                status = download.status.name,
                progress = download.progress,
                downloadedPages = download.downloadedPages,
                totalPages = download.totalPages,
                errorMessage = download.errorMessage,
                retryCount = download.retryCount,
                priority = download.priority
            )
        }
    }
    
    /**
     * Converts the status string back to DownloadStatus enum.
     */
    fun toDownloadStatus(): DownloadStatus {
        return try {
            DownloadStatus.valueOf(status)
        } catch (e: Exception) {
            DownloadStatus.QUEUE
        }
    }
    
    /**
     * Converts this queue item back to a Download.
     */
    fun toDownload(): Download {
        return Download(
            chapterId = chapterId,
            bookId = bookId,
            sourceId = sourceId,
            chapterName = chapterName,
            bookTitle = bookTitle,
            coverUrl = coverUrl,
            status = toDownloadStatus(),
            progress = progress,
            downloadedPages = downloadedPages,
            totalPages = totalPages,
            errorMessage = errorMessage,
            retryCount = retryCount,
            priority = priority
        )
    }
}

/**
 * Extension function to convert a list of Downloads to DownloadQueueItems.
 */
fun List<Download>.toQueueItems(): List<DownloadQueueItem> {
    return map { DownloadQueueItem.fromDownload(it) }
}
