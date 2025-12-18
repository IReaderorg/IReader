package ireader.domain.services.common

import ireader.domain.plugins.PluginInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Service for downloading and installing plugins with progress tracking and notifications
 */
interface PluginDownloadService : PlatformService {
    /**
     * Current service state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Active downloads map (pluginId -> progress)
     */
    val downloads: StateFlow<Map<String, PluginDownloadProgress>>
    
    /**
     * Queue a plugin for download and installation
     */
    suspend fun downloadPlugin(pluginInfo: PluginInfo): ServiceResult<Unit>
    
    /**
     * Cancel a plugin download
     */
    suspend fun cancelDownload(pluginId: String): ServiceResult<Unit>
    
    /**
     * Cancel all downloads
     */
    suspend fun cancelAll(): ServiceResult<Unit>
    
    /**
     * Retry a failed download
     */
    suspend fun retryDownload(pluginId: String): ServiceResult<Unit>
    
    /**
     * Get download status for a plugin
     */
    fun getDownloadStatus(pluginId: String): PluginDownloadProgress?
    
    /**
     * Check if a plugin is currently downloading
     */
    fun isDownloading(pluginId: String): Boolean
}

/**
 * Plugin download progress data
 */
data class PluginDownloadProgress(
    val pluginId: String,
    val pluginName: String,
    val version: String,
    val status: PluginDownloadStatus = PluginDownloadStatus.QUEUED,
    val progress: Float = 0f, // 0.0 to 1.0
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val errorMessage: String? = null,
    val retryCount: Int = 0
) {
    val progressPercent: Int get() = (progress * 100).toInt()
    
    val formattedSize: String get() {
        return when {
            totalBytes >= 1024 * 1024 -> {
                val downloadedMB = bytesDownloaded / (1024.0 * 1024.0)
                val totalMB = totalBytes / (1024.0 * 1024.0)
                "${formatDecimal(downloadedMB)} / ${formatDecimal(totalMB)} MB"
            }
            totalBytes >= 1024 -> {
                val downloadedKB = bytesDownloaded / 1024.0
                val totalKB = totalBytes / 1024.0
                "${formatDecimal(downloadedKB)} / ${formatDecimal(totalKB)} KB"
            }
            totalBytes > 0 -> "$bytesDownloaded / $totalBytes B"
            else -> {
                val downloadedMB = bytesDownloaded / (1024.0 * 1024.0)
                "${formatDecimal(downloadedMB)} MB"
            }
        }
    }
    
    private fun formatDecimal(value: Double): String {
        val rounded = (value * 10).toLong() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            "${rounded.toLong()}.0"
        } else {
            rounded.toString()
        }
    }
}

/**
 * Plugin download status enum
 */
enum class PluginDownloadStatus {
    QUEUED,
    DOWNLOADING,
    VALIDATING,
    INSTALLING,
    COMPLETED,
    FAILED,
    CANCELLED
}
