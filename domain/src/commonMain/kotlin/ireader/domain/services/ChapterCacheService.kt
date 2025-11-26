package ireader.domain.services

import ireader.domain.models.entities.Chapter

/**
 * Service for caching chapters to improve reading performance
 * 
 * Implements LRU cache with memory management to prevent OOM issues
 */
interface ChapterCacheService {
    
    /**
     * Get a cached chapter
     * 
     * @param chapterId Chapter ID to retrieve
     * @return Cached chapter or null if not found
     */
    suspend fun getChapter(chapterId: Long): Chapter?
    
    /**
     * Cache a chapter
     * 
     * @param chapter Chapter to cache
     */
    suspend fun cacheChapter(chapter: Chapter)
    
    /**
     * Preload a chapter in the background
     * 
     * @param chapterId Chapter ID to preload
     */
    suspend fun preloadChapter(chapterId: Long)
    
    /**
     * Remove a chapter from cache
     * 
     * @param chapterId Chapter ID to remove
     */
    suspend fun removeChapter(chapterId: Long)
    
    /**
     * Clear all cached chapters
     */
    fun clearCache()
    
    /**
     * Get cache statistics
     * 
     * @return Cache statistics
     */
    fun getCacheStats(): CacheStats
    
    /**
     * Check if cache is full
     * 
     * @return true if cache is at capacity
     */
    fun isCacheFull(): Boolean
    
    /**
     * Get estimated memory usage
     * 
     * @return Memory usage in bytes
     */
    fun getMemoryUsage(): Long
}

/**
 * Cache statistics
 */
data class CacheStats(
    val cachedChapters: Int,
    val maxCapacity: Int,
    val memoryUsedBytes: Long,
    val maxMemoryBytes: Long,
    val hitCount: Long,
    val missCount: Long,
    val evictionCount: Long
) {
    val hitRate: Float
        get() = if (hitCount + missCount > 0) {
            hitCount.toFloat() / (hitCount + missCount).toFloat()
        } else {
            0f
        }
    
    val memoryUsedMB: Float
        get() = memoryUsedBytes / (1024f * 1024f)
    
    val maxMemoryMB: Float
        get() = maxMemoryBytes / (1024f * 1024f)
    
    val utilizationPercent: Float
        get() = if (maxCapacity > 0) {
            (cachedChapters.toFloat() / maxCapacity.toFloat()) * 100f
        } else {
            0f
        }
}
