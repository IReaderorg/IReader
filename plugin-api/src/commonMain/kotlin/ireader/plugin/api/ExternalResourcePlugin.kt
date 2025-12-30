package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Interface for plugins that require external resources to be downloaded.
 * 
 * This enables plugins to defer large binary downloads until the user actually
 * needs the functionality, keeping plugin sizes small while still providing
 * full functionality.
 * 
 * Example:
 * ```kotlin
 * class FlareSolverrPlugin : CloudflareBypassPlugin, ExternalResourcePlugin {
 *     override val resourceInfo = ExternalResourceInfo(
 *         id = "flaresolverr-binary",
 *         name = "FlareSolverr",
 *         description = "Browser automation for Cloudflare bypass",
 *         downloadUrl = "https://github.com/FlareSolverr/...",
 *         estimatedSize = 500_000_000L, // 500 MB
 *         requiredFor = "Cloudflare-protected sources"
 *     )
 *     
 *     override fun isResourceDownloaded(): Boolean = checkBinaryExists()
 *     
 *     override suspend fun downloadResource(onProgress: (ResourceDownloadProgress) -> Unit): ResourceDownloadResult {
 *         // Download and extract binary
 *     }
 * }
 * ```
 */
interface ExternalResourcePlugin : Plugin {
    
    /**
     * Information about the external resource this plugin requires.
     */
    val resourceInfo: ExternalResourceInfo
    
    /**
     * Check if the required resource is already downloaded.
     * 
     * @return true if the resource is available and ready to use
     */
    fun isResourceDownloaded(): Boolean
    
    /**
     * Check if a download is currently in progress.
     * 
     * @return true if downloading
     */
    fun isDownloading(): Boolean
    
    /**
     * Get the current download progress.
     * 
     * @return Current progress, or null if not downloading
     */
    fun getDownloadProgress(): ResourceDownloadProgress?
    
    /**
     * Start downloading the required resource.
     * 
     * @param onProgress Callback for progress updates
     * @return Result of the download operation
     */
    suspend fun downloadResource(onProgress: (ResourceDownloadProgress) -> Unit = {}): ResourceDownloadResult
    
    /**
     * Cancel an ongoing download.
     * 
     * @return true if cancellation was successful
     */
    fun cancelDownload(): Boolean
    
    /**
     * Delete the downloaded resource to free up space.
     * 
     * @return true if deletion was successful
     */
    suspend fun deleteResource(): Boolean
    
    /**
     * Get the size of the downloaded resource on disk.
     * 
     * @return Size in bytes, or null if not downloaded
     */
    fun getDownloadedSize(): Long?
}

/**
 * Information about an external resource required by a plugin.
 */
@Serializable
data class ExternalResourceInfo(
    /** Unique identifier for this resource */
    val id: String,
    /** Human-readable name */
    val name: String,
    /** Description of what this resource provides */
    val description: String,
    /** URL to download from (may be platform-specific) */
    val downloadUrl: String,
    /** Estimated download size in bytes */
    val estimatedSize: Long,
    /** What feature requires this resource */
    val requiredFor: String,
    /** Version of the resource */
    val version: String = "",
    /** Source attribution (e.g., "Official GitHub Release") */
    val source: String = "",
    /** Supported platforms (empty = all platforms) */
    val platforms: List<String> = emptyList()
) {
    /**
     * Get human-readable size string.
     */
    val estimatedSizeFormatted: String
        get() {
            val kb = estimatedSize / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> "${formatDouble(gb, 1)} GB"
                mb >= 1.0 -> "${formatDouble(mb, 1)} MB"
                kb >= 1.0 -> "${formatDouble(kb, 1)} KB"
                else -> "$estimatedSize B"
            }
        }
}

/**
 * Progress of a resource download (for ExternalResourcePlugin).
 */
@Serializable
data class ResourceDownloadProgress(
    /** Bytes downloaded so far */
    val downloadedBytes: Long = 0,
    /** Total bytes to download (0 if unknown) */
    val totalBytes: Long = 0,
    /** Current phase of the download */
    val phase: ResourceDownloadPhase = ResourceDownloadPhase.IDLE,
    /** Human-readable status message */
    val statusMessage: String = "",
    /** Download speed in bytes per second (0 if unknown) */
    val speedBytesPerSecond: Long = 0
) {
    /**
     * Progress as a fraction (0.0 to 1.0).
     */
    val progress: Float
        get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
    
    /**
     * Progress as a percentage (0 to 100).
     */
    val progressPercent: Int
        get() = (progress * 100).toInt()
    
    /**
     * Human-readable downloaded size.
     */
    val downloadedFormatted: String
        get() = formatBytes(downloadedBytes)
    
    /**
     * Human-readable total size.
     */
    val totalFormatted: String
        get() = formatBytes(totalBytes)
    
    /**
     * Human-readable speed.
     */
    val speedFormatted: String
        get() = if (speedBytesPerSecond > 0) "${formatBytes(speedBytesPerSecond)}/s" else ""
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "${formatDouble(gb, 2)} GB"
            mb >= 1.0 -> "${formatDouble(mb, 2)} MB"
            kb >= 1.0 -> "${formatDouble(kb, 2)} KB"
            else -> "$bytes B"
        }
    }
}

/**
 * Phases of a resource download (for ExternalResourcePlugin).
 */
@Serializable
enum class ResourceDownloadPhase {
    /** Not started */
    IDLE,
    /** Checking/preparing download */
    PREPARING,
    /** Actively downloading */
    DOWNLOADING,
    /** Extracting/installing */
    EXTRACTING,
    /** Verifying integrity */
    VERIFYING,
    /** Download complete */
    COMPLETE,
    /** Download failed */
    ERROR,
    /** Download cancelled */
    CANCELLED
}

/**
 * Result of a resource download operation (for ExternalResourcePlugin).
 */
@Serializable
sealed class ResourceDownloadResult {
    /**
     * Download completed successfully.
     */
    @Serializable
    data class Success(
        /** Path where resource was installed */
        val installedPath: String,
        /** Actual size of downloaded resource */
        val sizeBytes: Long
    ) : ResourceDownloadResult()
    
    /**
     * Download failed.
     */
    @Serializable
    data class Failed(
        /** Error message */
        val reason: String,
        /** Whether retry might succeed */
        val canRetry: Boolean = true,
        /** Underlying exception message */
        val exceptionMessage: String? = null
    ) : ResourceDownloadResult()
    
    /**
     * Download was cancelled.
     */
    @Serializable
    object Cancelled : ResourceDownloadResult()
    
    /**
     * Platform not supported.
     */
    @Serializable
    data class PlatformNotSupported(
        val currentPlatform: String,
        val supportedPlatforms: List<String>
    ) : ResourceDownloadResult()
}


/**
 * KMP-compatible helper function to format a double with specified decimal places.
 */
private fun formatDouble(value: Double, decimals: Int): String {
    var factor = 1.0
    repeat(decimals) { factor *= 10.0 }
    val rounded = kotlin.math.round(value * factor) / factor
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')
    return if (dotIndex == -1) {
        "$str.${"0".repeat(decimals)}"
    } else {
        val currentDecimals = str.length - dotIndex - 1
        if (currentDecimals >= decimals) {
            str.substring(0, dotIndex + decimals + 1)
        } else {
            str + "0".repeat(decimals - currentDecimals)
        }
    }
}
