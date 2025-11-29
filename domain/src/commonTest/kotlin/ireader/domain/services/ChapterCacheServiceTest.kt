package ireader.domain.services

import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ChapterCacheServiceImpl fixes.
 * 
 * These tests verify:
 * 1. Infinite loop fix when cache is full and can't evict
 * 2. LRU eviction works correctly
 * 3. Cache hit/miss tracking works
 * 4. Memory limits are respected
 */
class ChapterCacheServiceTest {

    @Test
    fun `test infinite loop fix in cacheChapter`() = runTest {
        // Create a cache with very small memory limit (0 MB)
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 0) 
        
        // Create a chapter that is definitely larger than 0 bytes
        val chapter = createTestChapter(id = 1)
        
        // This should NOT hang in infinite loop
        service.cacheChapter(chapter)
        
        val stats1 = service.getCacheStats()
        assertEquals(1, stats1.cachedChapters)
        
        // Now add another one
        val chapter2 = createTestChapter(id = 2)
        
        service.cacheChapter(chapter2)
        
        val stats2 = service.getCacheStats()
        // After adding chapter 2, chapter 1 should be evicted (memory limit is 0)
        assertEquals(1, stats2.cachedChapters)
        assertEquals(1L, stats2.evictionCount)
    }

    @Test
    fun `cache returns null for non-existent chapter`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 50)
        
        val result = service.getChapter(999L)
        
        assertNull(result, "Should return null for non-cached chapter")
    }

    @Test
    fun `cache returns cached chapter`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 50)
        val chapter = createTestChapter(id = 1)
        
        service.cacheChapter(chapter)
        val result = service.getChapter(1L)
        
        assertNotNull(result, "Should return cached chapter")
        assertEquals(chapter.id, result.id)
        assertEquals(chapter.name, result.name)
    }

    @Test
    fun `cache tracks hit and miss counts`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 50)
        val chapter = createTestChapter(id = 1)
        
        // Miss
        service.getChapter(1L)
        
        // Cache
        service.cacheChapter(chapter)
        
        // Hit
        service.getChapter(1L)
        
        val stats = service.getCacheStats()
        assertEquals(1L, stats.hitCount, "Should have 1 hit")
        assertEquals(1L, stats.missCount, "Should have 1 miss")
    }

    @Test
    fun `cache evicts oldest entry when at capacity`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 2, maxMemoryMB = 100)
        
        // Add 2 chapters (at capacity)
        service.cacheChapter(createTestChapter(id = 1))
        service.cacheChapter(createTestChapter(id = 2))
        
        // Add 3rd chapter (should evict chapter 1)
        service.cacheChapter(createTestChapter(id = 3))
        
        val stats = service.getCacheStats()
        assertEquals(2, stats.cachedChapters, "Should have 2 chapters")
        assertTrue(stats.evictionCount > 0, "Should have evicted at least one chapter")
        
        // Chapter 1 should be evicted
        assertNull(service.getChapter(1L), "Chapter 1 should be evicted")
        assertNotNull(service.getChapter(2L), "Chapter 2 should still be cached")
        assertNotNull(service.getChapter(3L), "Chapter 3 should be cached")
    }

    @Test
    fun `clearCache removes all entries and resets stats`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 50)
        
        service.cacheChapter(createTestChapter(id = 1))
        service.cacheChapter(createTestChapter(id = 2))
        service.getChapter(1L) // Hit
        service.getChapter(999L) // Miss
        
        service.clearCache()
        
        val stats = service.getCacheStats()
        assertEquals(0, stats.cachedChapters, "Should have no cached chapters")
        assertEquals(0L, stats.hitCount, "Hit count should be reset")
        assertEquals(0L, stats.missCount, "Miss count should be reset")
    }

    @Test
    fun `removeChapter removes specific chapter`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 50)
        
        service.cacheChapter(createTestChapter(id = 1))
        service.cacheChapter(createTestChapter(id = 2))
        
        service.removeChapter(1L)
        
        assertNull(service.getChapter(1L), "Chapter 1 should be removed")
        assertNotNull(service.getChapter(2L), "Chapter 2 should still be cached")
    }

    @Test
    fun `isCacheFull returns correct value`() = runTest {
        val service = ChapterCacheServiceImpl(maxCapacity = 2, maxMemoryMB = 100)
        
        // Initially not full
        assertTrue(!service.isCacheFull(), "Cache should not be full initially")
        
        // Add chapters
        service.cacheChapter(createTestChapter(id = 1))
        service.cacheChapter(createTestChapter(id = 2))
        
        // Now full
        assertTrue(service.isCacheFull(), "Cache should be full at capacity")
    }

    private fun createTestChapter(id: Long): Chapter {
        return Chapter(
            id = id,
            bookId = 1,
            key = "test-$id",
            name = "Test Chapter $id",
            read = false,
            bookmark = false,
            dateUpload = 0,
            dateFetch = 0,
            sourceOrder = id,
            content = emptyList(),
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0,
            type = 0L
        )
    }
}
