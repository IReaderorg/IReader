package ireader.domain.storage

import okio.Path

/**
 * Platform-agnostic cache manager interface.
 * Abstracts cache operations and directory management.
 * Uses Okio Path for KMP compatibility.
 */
interface CacheManager {
    /**
     * Main cache directory
     */
    val cacheDirectory: Path
    
    /**
     * Cache directory for extensions
     */
    val extensionCacheDirectory: Path
    
    /**
     * Get a cache subdirectory
     */
    fun getCacheSubDirectory(name: String): Path
    
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
    fun clearCacheDirectory(directory: Path)
}
