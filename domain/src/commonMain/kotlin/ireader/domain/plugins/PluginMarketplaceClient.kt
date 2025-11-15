package ireader.domain.plugins

import java.io.File

/**
 * Client interface for interacting with the plugin marketplace
 * This is an interface to allow for different implementations (REST API, local, etc.)
 * Requirements: 12.1, 12.2
 */
interface PluginMarketplaceClient {
    
    /**
     * Get the latest version information for a plugin
     * Requirements: 12.1
     */
    suspend fun getLatestVersion(pluginId: String): PluginVersionInfo
    
    /**
     * Download a plugin package from the marketplace
     * Requirements: 12.2
     * 
     * @param url The download URL for the plugin
     * @param onProgress Callback for download progress (0-100)
     * @return The downloaded plugin package file
     */
    suspend fun downloadPlugin(
        url: String,
        onProgress: (Int) -> Unit = {}
    ): File
    
    /**
     * Get the download URL for a specific version of a plugin
     * Used for rollback functionality
     * Requirements: 12.4
     */
    suspend fun getVersionDownloadUrl(pluginId: String, versionCode: Int): String
    
    /**
     * Get all available versions for a plugin
     * Requirements: 12.4, 12.5
     */
    suspend fun getAvailableVersions(pluginId: String): List<PluginVersionInfo>
}

/**
 * Information about a specific plugin version
 * Requirements: 12.1, 12.2
 */
data class PluginVersionInfo(
    val pluginId: String,
    val version: String,
    val versionCode: Int,
    val changelog: String,
    val downloadUrl: String,
    val releaseDate: Long,
    val minIReaderVersion: String,
    val fileSize: Long
)
