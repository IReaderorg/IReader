package ireader.domain.services

import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * LRU cache implementation for chapters with memory management
 * 
 * Features:
 * - LRU eviction policy
 * - Memory-based limits
 * - Thread-safe operations
 * - Cache statistics
 */
class ChapterCacheServiceImpl(
    private val maxCapacity: Int = 5,
    private val maxMemoryMB: Int = 50
) : ChapterCacheService {
    
    private val cache = LinkedHashMap<Long, CacheEntry>(maxCapacity, 0.75f, true)
    private val mutex = Mutex()
    
    // Statistics
    private var hitCount = 0L
    private var missCount = 0L
    private var evictionCount = 0L
    
    private data class CacheEntry(
        val chapter: Chapter,
        val sizeBytes: Long,
        val cachedAt: Long = System.currentTimeMillis()
    )
    
    override suspend fun getChapter(chapterId: Long): Chapter? = mutex.withLock {
        val entry = cache[chapterId]
        
        if (entry != null) {
            hitCount++
            Log.debug("Cache hit for chapter $chapterId")
            return entry.chapter
        } else {
            missCount++
            Log.debug("Cache miss for chapter $chapterId")
            return null
        }
    }
    
    override suspend fun cacheChapter(chapter: Chapter) = mutex.withLock {
        val sizeBytes = estimateChapterSize(chapter)
        
        // Check if we need to evict entries
        while (shouldEvict(sizeBytes)) {
            evictOldest()
        }
        
        // Add to cache
        cache[chapter.id] = CacheEntry(chapter, sizeBytes)
        
        Log.debug("Cached chapter ${chapter.id} (${sizeBytes / 1024}KB)")
    }
    
    override suspend fun preloadChapter(chapterId: Long) {
        // This would be implemented by the caller
        // The service just manages the cache
    }
    
    override suspend fun removeChapter(chapterId: Long) = mutex.withLock {
        cache.remove(chapterId)
        Log.debug("Removed chapter $chapterId from cache")
    }
    
    override fun clearCache() {
        cache.clear()
        hitCount = 0
        missCount = 0
        evictionCount = 0
        Log.debug("Cache cleared")
    }
    
    override fun getCacheStats(): CacheStats {
        val memoryUsed = cache.values.sumOf { it.sizeBytes }
        val maxMemory = maxMemoryMB * 1024L * 1024L
        
        return CacheStats(
            cachedChapters = cache.size,
            maxCapacity = maxCapacity,
            memoryUsedBytes = memoryUsed,
            maxMemoryBytes = maxMemory,
            hitCount = hitCount,
            missCount = missCount,
            evictionCount = evictionCount
        )
    }
    
    override fun isCacheFull(): Boolean {
        return cache.size >= maxCapacity || getMemoryUsage() >= maxMemoryMB * 1024L * 1024L
    }
    
    override fun getMemoryUsage(): Long {
        return cache.values.sumOf { it.sizeBytes }
    }
    
    /**
     * Check if we should evict entries before adding new one
     */
    private fun shouldEvict(newEntrySizeBytes: Long): Boolean {
        val currentMemory = getMemoryUsage()
        val maxMemory = maxMemoryMB * 1024L * 1024L
        
        // Evict if:
        // 1. Cache is at capacity
        // 2. Adding new entry would exceed memory limit
        return cache.size >= maxCapacity || 
               (currentMemory + newEntrySizeBytes) > maxMemory
    }
    
    /**
     * Evict the oldest (least recently used) entry
     */
    private fun evictOldest() {
        val oldest = cache.entries.firstOrNull()
        if (oldest != null) {
            cache.remove(oldest.key)
            evictionCount++
            Log.debug("Evicted chapter ${oldest.key} from cache")
        }
    }
    
    /**
     * Estimate chapter size in bytes
     * 
     * This is a rough estimate based on content length
     */
    private fun estimateChapterSize(chapter: Chapter): Long {
        var size = 0L
        
        // Base chapter object size
        size += 1024 // ~1KB for chapter metadata
        
        // Content size
        chapter.content?.forEach { page ->
            when (page) {
                is ireader.core.source.model.Text -> {
                    // Estimate 2 bytes per character (UTF-16)
                    size += page.text.length * 2L
                }
                is ireader.core.source.model.Command<*> -> {
                    size += 100 // Small overhead for commands
                }
                is ireader.core.source.model.ImageBase64 -> {
                    size += page.data.length.toLong()
                }
                is ireader.core.source.model.ImageUrl -> {
                    size += 200 // URL overhead
                }
                is ireader.core.source.model.MovieUrl -> {
                    size += 200 // URL overhead
                }
                is ireader.core.source.model.Subtitle -> {
                    size += page.url.length * 2L
                }
                is ireader.core.source.model.PageUrl -> {
                    size += 200 // URL overhead
                }
            }
        }
        
        return size
    }
}
