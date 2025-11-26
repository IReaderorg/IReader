package ireader.domain.storage

import java.io.File

/**
 * Platform-agnostic cache manager interface.
 * Abstracts cache operations and directory management.
 */
interface CacheManager {
    /**
     * Main cache directory
     */
    val cacheDirectory: File
    
    /**
     * Cache directory for extensions
     */
    val extensionCacheDirectory: File
    
    /**
     * Get a cache subdirectory
     */
    fun getCacheSubDirectory(name: String): File
    
    /**
     * Clear all image caches (memory and disk)
     */
    fun clearImageCache()
    
    /**
     * Clear all application caches
     */
    fun clearAllCache()
    
    /**
     * Get total cache size in human-readable format
     */
    fun getCacheSize(): String
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSizeBytes(): Long
    
    /**
     * Clear specific cache directory
     */
    fun clearCacheDirectory(directory: File)
}
