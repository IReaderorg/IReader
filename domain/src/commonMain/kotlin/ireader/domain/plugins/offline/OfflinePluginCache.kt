package ireader.domain.plugins.offline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Offline Plugin Cache System
 * 
 * Features:
 * - Pre-download plugin updates for offline use
 * - Cache plugin packages
 * - Manage cache storage
 * - Background download scheduling
 */

/**
 * Cached plugin information.
 */
@Serializable
data class CachedPlugin(
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val versionCode: Int,
    val cachedAt: Long,
    val expiresAt: Long?,
    val filePath: String,
    val fileSize: Long,
    val checksum: String,
    val isUpdate: Boolean,
    val currentInstalledVersion: String?,
    val downloadUrl: String,
    val status: CacheStatus
)

@Serializable
enum class CacheStatus {
    PENDING,
    DOWNLOADING,
    CACHED,
    EXPIRED,
    CORRUPTED,
    FAILED
}

/**
 * Cache configuration.
 */
@Serializable
data class CacheConfig(
    val enabled: Boolean = true,
    val maxCacheSizeBytes: Long = 500 * 1024 * 1024, // 500 MB
    val autoDownloadUpdates: Boolean = true,
    val downloadOnWifiOnly: Boolean = true,
    val cacheExpirationDays: Int = 30,
    val preloadPopularPlugins: Boolean = false,
    val maxConcurrentDownloads: Int = 3
)

/**
 * Cache statistics.
 */
@Serializable
data class CacheStatistics(
    val totalCachedPlugins: Int,
    val totalCacheSize: Long,
    val maxCacheSize: Long,
    val usagePercent: Float,
    val pendingDownloads: Int,
    val failedDownloads: Int,
    val expiredItems: Int,
    val lastCleanup: Long?
)

/**
 * Download task for a plugin.
 */
@Serializable
data class DownloadTask(
    val id: String,
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val downloadUrl: String,
    val fileSize: Long?,
    val priority: DownloadPriority,
    val status: DownloadStatus,
    val progress: Float,
    val downloadedBytes: Long,
    val createdAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val errorMessage: String?,
    val retryCount: Int = 0
)

@Serializable
enum class DownloadPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

@Serializable
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Download event for progress tracking.
 */
sealed class DownloadEvent {
    data class Queued(val task: DownloadTask) : DownloadEvent()
    data class Started(val taskId: String) : DownloadEvent()
    data class Progress(val taskId: String, val progress: Float, val downloadedBytes: Long) : DownloadEvent()
    data class Completed(val taskId: String, val cachedPlugin: CachedPlugin) : DownloadEvent()
    data class Failed(val taskId: String, val error: String) : DownloadEvent()
    data class Cancelled(val taskId: String) : DownloadEvent()
    data class Paused(val taskId: String) : DownloadEvent()
    data class Resumed(val taskId: String) : DownloadEvent()
}

/**
 * Update availability information.
 */
@Serializable
data class PluginUpdateInfo(
    val pluginId: String,
    val pluginName: String,
    val currentVersion: String,
    val currentVersionCode: Int,
    val newVersion: String,
    val newVersionCode: Int,
    val downloadUrl: String,
    val fileSize: Long?,
    val changelog: String?,
    val isBreakingChange: Boolean,
    val isCached: Boolean,
    val cachedAt: Long?
)

/**
 * Cleanup result.
 */
@Serializable
data class CleanupResult(
    val removedItems: Int,
    val freedBytes: Long,
    val remainingItems: Int,
    val remainingBytes: Long
)

/**
 * Cache entry metadata.
 */
@Serializable
data class CacheEntryMetadata(
    val pluginId: String,
    val version: String,
    val cachedAt: Long,
    val lastAccessed: Long,
    val accessCount: Int,
    val fileSize: Long,
    val checksum: String
)
