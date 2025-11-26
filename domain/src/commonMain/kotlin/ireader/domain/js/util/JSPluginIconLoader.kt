package ireader.domain.js.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import ireader.domain.js.models.JSPluginError
import ireader.domain.storage.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Loader for JavaScript plugin icons.
 * Handles downloading, caching, and validation of plugin icons.
 */
class JSPluginIconLoader(
    private val httpClient: HttpClient,
    private val cacheManager: CacheManager
) {
    
    companion object {
        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        private val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        )
        
        // JPEG signature: FF D8 FF
        private val JPEG_SIGNATURE = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()
        )
        
        private const val EXPECTED_ICON_SIZE = 96
    }
    
    private val iconCacheDir: File
        get() = cacheManager.getCacheSubDirectory("js-plugin-icons")
    
    /**
     * Loads an icon for a plugin.
     * First checks cache, then downloads if necessary.
     * @param iconUrl The icon URL (http/https or resource path)
     * @param pluginId The plugin ID for caching
     * @return ByteArray of the icon image, or null if loading fails
     */
    suspend fun loadIcon(iconUrl: String, pluginId: String): ByteArray? {
        // Check cache first
        val cachedIcon = loadFromCache(pluginId)
        if (cachedIcon != null) {
            return cachedIcon
        }
        
        // Load icon based on URL type
        val iconData = if (iconUrl.startsWith("http://") || iconUrl.startsWith("https://")) {
            downloadIcon(iconUrl)
        } else {
            // Load from plugin package resources (not implemented in this phase)
            null
        }
        
        if (iconData != null) {
            // Validate image format
            if (validateImageFormat(iconData)) {
                // Cache the icon
                cacheIcon(pluginId, iconData)
                return iconData
            } else {
                JSPluginLogger.logDebug(pluginId, "Icon validation failed: invalid format")
            }
        }
        
        return null
    }
    
    /**
     * Caches an icon to disk.
     * @param pluginId The plugin ID
     * @param bitmap The icon image data
     */
    fun cacheIcon(pluginId: String, bitmap: ByteArray) {
        try {
            val iconFile = getIconCacheFile(pluginId)
            iconFile.writeBytes(bitmap)
        } catch (e: Exception) {
            JSPluginLogger.logError(
                pluginId,
                JSPluginError.LoadError(pluginId, e)
            )
        }
    }
    
    /**
     * Loads an icon from cache.
     * @param pluginId The plugin ID
     * @return ByteArray of cached icon, or null if not cached
     */
    private fun loadFromCache(pluginId: String): ByteArray? {
        return try {
            val iconFile = getIconCacheFile(pluginId)
            if (iconFile.exists()) {
                iconFile.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Downloads an icon from a URL.
     * @param url The icon URL
     * @return ByteArray of downloaded icon, or null if download fails
     */
    private suspend fun downloadIcon(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.get(url)
                response.readBytes()
            } catch (e: Exception) {
                JSPluginLogger.logError(
                    "icon-loader",
                    JSPluginError.NetworkError("icon-loader", url, e)
                )
                null
            }
        }
    }
    
    /**
     * Validates that the image data is a valid PNG or JPEG.
     * @param data The image data
     * @return true if valid, false otherwise
     */
    private fun validateImageFormat(data: ByteArray): Boolean {
        if (data.size < 8) {
            return false
        }
        
        // Check PNG signature
        val isPng = data.take(PNG_SIGNATURE.size).toByteArray().contentEquals(PNG_SIGNATURE)
        
        // Check JPEG signature
        val isJpeg = data.take(JPEG_SIGNATURE.size).toByteArray().contentEquals(JPEG_SIGNATURE)
        
        if (!isPng && !isJpeg) {
            return false
        }
        
        // Note: Dimension validation would require image decoding
        // which is platform-specific. For now, we just validate the format.
        // A warning could be logged if dimensions don't match expected size.
        
        return true
    }
    
    /**
     * Gets the cache file for a plugin icon.
     * @param pluginId The plugin ID
     * @return File object for the cached icon
     */
    private fun getIconCacheFile(pluginId: String): File {
        return File(iconCacheDir, "$pluginId.png")
    }
    
    /**
     * Clears the icon cache for a specific plugin.
     * @param pluginId The plugin ID
     */
    fun clearCache(pluginId: String) {
        try {
            val iconFile = getIconCacheFile(pluginId)
            if (iconFile.exists()) {
                iconFile.delete()
            }
        } catch (e: Exception) {
            JSPluginLogger.logError(
                pluginId,
                JSPluginError.LoadError(pluginId, e)
            )
        }
    }
    
    /**
     * Clears all cached icons.
     */
    fun clearAllCache() {
        try {
            cacheManager.clearCacheDirectory(iconCacheDir)
        } catch (e: Exception) {
            JSPluginLogger.logError(
                "icon-loader",
                JSPluginError.LoadError("icon-loader", e)
            )
        }
    }
}
