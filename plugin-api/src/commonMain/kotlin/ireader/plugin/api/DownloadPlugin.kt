package ireader.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Plugin interface for download management.
 * Enables plugins to provide custom download sources and accelerators.
 * 
 * Example:
 * ```kotlin
 * class Aria2DownloadPlugin : DownloadPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.aria2-download",
 *         name = "Aria2 Download Accelerator",
 *         type = PluginType.FEATURE,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.STORAGE),
 *         // ... other manifest fields
 *     )
 *     
 *     override val downloadCapabilities = listOf(
 *         DownloadCapability.MULTI_CONNECTION,
 *         DownloadCapability.RESUME
 *     )
 * }
 * ```
 */
interface DownloadPlugin : Plugin {
    /**
     * Download capabilities.
     */
    val downloadCapabilities: List<DownloadCapability>
    
    /**
     * Download configuration.
     */
    val downloadConfig: DownloadConfig
    
    /**
     * Start a download.
     */
    suspend fun startDownload(request: DownloadRequest): DownloadResult<DownloadTask>
    
    /**
     * Pause a download.
     */
    suspend fun pauseDownload(taskId: String): DownloadResult<Unit>
    
    /**
     * Resume a download.
     */
    suspend fun resumeDownload(taskId: String): DownloadResult<Unit>
    
    /**
     * Cancel a download.
     */
    suspend fun cancelDownload(taskId: String): DownloadResult<Unit>
    
    /**
     * Get download progress.
     */
    fun getProgress(taskId: String): Flow<DownloadProgress>
    
    /**
     * Get all active downloads.
     */
    suspend fun getActiveDownloads(): List<DownloadTask>
    
    /**
     * Get download history.
     */
    suspend fun getHistory(limit: Int = 50): List<DownloadHistoryEntry>
    
    /**
     * Clear download history.
     */
    suspend fun clearHistory()
    
    /**
     * Get download statistics.
     */
    suspend fun getStatistics(): DownloadStatistics
    
    /**
     * Set download priority.
     */
    suspend fun setPriority(taskId: String, priority: DownloadPriority): DownloadResult<Unit>
    
    /**
     * Batch download.
     */
    suspend fun startBatchDownload(requests: List<DownloadRequest>): DownloadResult<List<DownloadTask>>
}

/**
 * Download capabilities.
 */
@Serializable
enum class DownloadCapability {
    /** Multi-connection download */
    MULTI_CONNECTION,
    /** Resume interrupted downloads */
    RESUME,
    /** Download scheduling */
    SCHEDULING,
    /** Bandwidth limiting */
    BANDWIDTH_LIMIT,
    /** Download queue management */
    QUEUE_MANAGEMENT,
    /** Torrent/magnet support */
    TORRENT,
    /** Metalink support */
    METALINK,
    /** HTTP/2 support */
    HTTP2,
    /** Proxy support */
    PROXY
}

/**
 * Download configuration.
 */
@Serializable
data class DownloadConfig(
    /** Maximum concurrent downloads */
    val maxConcurrentDownloads: Int = 3,
    /** Maximum connections per download */
    val maxConnectionsPerDownload: Int = 4,
    /** Default download directory */
    val defaultDownloadDir: String = "downloads",
    /** Auto-retry on failure */
    val autoRetry: Boolean = true,
    /** Maximum retry attempts */
    val maxRetries: Int = 3,
    /** Retry delay in milliseconds */
    val retryDelayMs: Long = 5000,
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 30000,
    /** Read timeout in milliseconds */
    val readTimeoutMs: Long = 60000,
    /** Bandwidth limit in bytes per second (0 = unlimited) */
    val bandwidthLimitBps: Long = 0,
    /** Whether to verify file integrity */
    val verifyIntegrity: Boolean = true
)

/**
 * Download request.
 */
@Serializable
data class DownloadRequest(
    /** Download URL */
    val url: String,
    /** Output filename (optional, auto-detect if null) */
    val filename: String? = null,
    /** Output directory (optional, use default if null) */
    val directory: String? = null,
    /** HTTP headers */
    val headers: Map<String, String> = emptyMap(),
    /** Expected file size (for progress calculation) */
    val expectedSize: Long? = null,
    /** Expected checksum (for verification) */
    val checksum: FileChecksum? = null,
    /** Download priority */
    val priority: DownloadPriority = DownloadPriority.NORMAL,
    /** Number of connections to use */
    val connections: Int? = null,
    /** Metadata for tracking */
    val metadata: Map<String, String> = emptyMap(),
    /** Whether to overwrite existing file */
    val overwrite: Boolean = false
)

/**
 * File checksum for verification.
 */
@Serializable
data class FileChecksum(
    val algorithm: ChecksumAlgorithm,
    val value: String
)

@Serializable
enum class ChecksumAlgorithm {
    MD5,
    SHA1,
    SHA256,
    SHA512
}

/**
 * Download priority.
 */
@Serializable
enum class DownloadPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Download task.
 */
@Serializable
data class DownloadTask(
    /** Task ID */
    val id: String,
    /** Download URL */
    val url: String,
    /** Output file path */
    val filePath: String,
    /** Task status */
    val status: DownloadStatus,
    /** Total size in bytes */
    val totalSize: Long,
    /** Downloaded size in bytes */
    val downloadedSize: Long,
    /** Download speed in bytes per second */
    val speedBps: Long,
    /** Estimated time remaining in milliseconds */
    val etaMs: Long?,
    /** Number of connections */
    val connections: Int,
    /** Priority */
    val priority: DownloadPriority,
    /** Created timestamp */
    val createdAt: Long,
    /** Started timestamp */
    val startedAt: Long?,
    /** Completed timestamp */
    val completedAt: Long?,
    /** Error message (if failed) */
    val error: String? = null,
    /** Metadata */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Download status.
 */
@Serializable
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
    VERIFYING
}

/**
 * Download progress.
 */
@Serializable
data class DownloadProgress(
    val taskId: String,
    val status: DownloadStatus,
    val downloadedSize: Long,
    val totalSize: Long,
    val percentage: Float,
    val speedBps: Long,
    val etaMs: Long?,
    val connections: Int
)

/**
 * Download history entry.
 */
@Serializable
data class DownloadHistoryEntry(
    val id: String,
    val url: String,
    val filename: String,
    val filePath: String,
    val fileSize: Long,
    val status: DownloadStatus,
    val startedAt: Long,
    val completedAt: Long?,
    val durationMs: Long,
    val averageSpeedBps: Long
)

/**
 * Download statistics.
 */
@Serializable
data class DownloadStatistics(
    val totalDownloads: Int,
    val completedDownloads: Int,
    val failedDownloads: Int,
    val totalBytesDownloaded: Long,
    val averageSpeedBps: Long,
    val totalDownloadTimeMs: Long
)

/**
 * Result wrapper for download operations.
 */
sealed class DownloadResult<out T> {
    data class Success<T>(val data: T) : DownloadResult<T>()
    data class Error(val error: DownloadError) : DownloadResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}

/**
 * Download errors.
 */
@Serializable
sealed class DownloadError {
    data class NetworkError(val reason: String) : DownloadError()
    data class FileError(val reason: String) : DownloadError()
    data class TaskNotFound(val taskId: String) : DownloadError()
    data class InvalidUrl(val url: String) : DownloadError()
    data class StorageFull(val requiredBytes: Long, val availableBytes: Long) : DownloadError()
    data class ChecksumMismatch(val expected: String, val actual: String) : DownloadError()
    data class Timeout(val timeoutMs: Long) : DownloadError()
    data object Cancelled : DownloadError()
    data class Unknown(val message: String) : DownloadError()
}