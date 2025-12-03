package ireader.domain.models.download

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Download queue item with enhanced metadata
 */
data class DownloadItem(
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterUrl: String,
    val priority: Int = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val speed: Float = 0f, // bytes per second
    val estimatedTimeRemaining: Long = 0L, // milliseconds
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val createdAt: Long = currentTimeToLong(),
    val startedAt: Long? = null,
    val completedAt: Long? = null
)

/**
 * Download status
 */
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED,
    CANCELLED
}

/**
 * Download queue configuration
 */
data class DownloadQueueConfig(
    val maxConcurrentDownloads: Int = 3,
    val maxRetryAttempts: Int = 3,
    val retryDelayMs: Long = 5000L,
    val downloadTimeoutMs: Long = 30000L,
    val enableProgressNotifications: Boolean = true,
    val enableCompletionNotifications: Boolean = true,
    val enableErrorNotifications: Boolean = true,
    val autoRetryOnFailure: Boolean = true,
    val pauseOnLowBattery: Boolean = true,
    val pauseOnMeteredConnection: Boolean = false
)

/**
 * Download statistics
 */
data class DownloadStats(
    val totalDownloads: Int = 0,
    val completedDownloads: Int = 0,
    val failedDownloads: Int = 0,
    val queuedDownloads: Int = 0,
    val downloadingCount: Int = 0,
    val totalBytesDownloaded: Long = 0L,
    val averageSpeed: Float = 0f, // bytes per second
    val totalDownloadTime: Long = 0L // milliseconds
)

/**
 * Download cache entry
 */
data class DownloadCacheEntry(
    val chapterId: Long,
    val bookId: Long,
    val filePath: String,
    val fileSize: Long,
    val downloadedAt: Long,
    val lastAccessedAt: Long,
    val isValid: Boolean = true
)

/**
 * Download notification data
 */
data class DownloadNotificationData(
    val type: DownloadNotificationType,
    val title: String,
    val content: String,
    val progress: Float? = null,
    val isIndeterminate: Boolean = false,
    val actions: List<DownloadNotificationAction> = emptyList()
)

/**
 * Download notification types
 */
enum class DownloadNotificationType {
    PROGRESS,
    COMPLETED,
    FAILED,
    PAUSED,
    CANCELLED
}

/**
 * Download notification action
 */
data class DownloadNotificationAction(
    val id: String,
    val title: String,
    val icon: String? = null
)

/**
 * Batch download request
 */
data class BatchDownloadRequest(
    val bookId: Long,
    val chapterIds: List<Long>,
    val priority: Int = 0,
    val skipDownloaded: Boolean = true
)

/**
 * Download filter criteria
 */
data class DownloadFilter(
    val status: DownloadStatus? = null,
    val bookId: Long? = null,
    val sourceId: Long? = null,
    val priority: Int? = null,
    val hasError: Boolean? = null
)