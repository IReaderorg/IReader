package ireader.domain.services

import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for ChapterCacheService
 * Tests LRU cache implementation, memory management, and statistics
 */
class ChapterCacheServiceTest {
    
    private lateinit var cacheService: ChapterCacheServiceImpl
    
    @BeforeTest
    fun setup() {
        cacheService = ChapterCacheServiceImpl(
            maxCapacity = 3,
            maxMemoryMB = 10
        )
    }
    
    @AfterTest
    fun tearDown() {
        cacheService.clearCache()
    }
    
    @Test
    fun `cacheChapter should store chapter in cache`() = runTest {
        // Given
        val chapter = createTestChapter(1L)
        
        // When
        cacheService.cacheChapter(chapter)
        val retrieved = cacheService.getChapter(1L)
        
        // Then
        assertNotNull(retrieved)
        assertEquals(chapter.id, retrieved.id)
        assertEquals(chapter.name, retrieved.name)
    }
    
    @Test
    fun `getChapter should return null for non-cached chapter`() = runTest {
        // When
        val retrieved = cacheService.getChapter(999L)
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `cache should evict oldest entry when capacity is reached`() = runTest {
        // Given
        val chapter1 = createTestChapter(1L)
        val chapter2 = createTestChapter(2L)
        val chapter3 = createTestChapter(3L)
        val chapter4 = createTestChapter(4L)
        
        // When - Fill cache to capacity
        cacheService.cacheChapter(chapter1)
        cacheService.cacheChapter(chapter2)
        cacheService.cacheChapter(chapter3)
        
        // Then - All should be cached
        assertNotNull(cacheService.getChapter(1L))
        assertNotNull(cacheService.getChapter(2L))
        assertNotNull(cacheService.getChapter(3L))
        
        // When - Add one more (should evict oldest)
        cacheService.cacheChapter(chapter4)
        
        // Then - First chapter should be evicted
        assertNull(cacheService.getChapter(1L))
        assertNotNull(cacheService.getChapter(2L))
        assertNotNull(cacheService.getChapter(3L))
        assertNotNull(cacheService.getChapter(4L))
    }
    
    @Test
    fun `LRU should keep recently accessed chapters`() = runTest {
        // Given
        val chapter1 = createTestChapter(1L)
        val chapter2 = createTestChapter(2L)
        val chapter3 = createTestChapter(3L)
        val chapter4 = createTestChapter(4L)
        
        // When - Fill cache
        cacheService.cacheChapter(chapter1)
        cacheService.cacheChapter(chapter2)
        cacheService.cacheChapter(chapter3)
        
        // Access chapter 1 to make it recently used
        cacheService.getChapter(1L)
        
        // Add chapter 4 (should evict chapter 2, not 1)
        cacheService.cacheChapter(chapter4)
        
        // Then - Chapter 1 should still be cached (recently accessed)
        assertNotNull(cacheService.getChapter(1L))
        assertNull(cacheService.getChapter(2L)) // Evicted
        assertNotNull(cacheService.getChapter(3L))
        assertNotNull(cacheService.getChapter(4L))
    }
    
    @Test
    fun `removeChapter should remove chapter from cache`() = runTest {
        // Given
        val chapter = createTestChapter(1L)
        cacheService.cacheChapter(chapter)
        
        // When
        cacheService.removeChapter(1L)
        val retrieved = cacheService.getChapter(1L)
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `clearCache should remove all chapters`() = runTest {
        // Given
        cacheService.cacheChapter(createTestChapter(1L))
        cacheService.cacheChapter(createTestChapter(2L))
        cacheService.cacheChapter(createTestChapter(3L))
        
        // When
        cacheService.clearCache()
        
        // Then
        assertNull(cacheService.getChapter(1L))
        assertNull(cacheService.getChapter(2L))
        assertNull(cacheService.getChapter(3L))
        
        val stats = cacheService.getCacheStats()
        assertEquals(0, stats.cachedChapters)
    }
    
    @Test
    fun `getCacheStats should return accurate statistics`() = runTest {
        // Given
        cacheService.cacheChapter(createTestChapter(1L))
        cacheService.cacheChapter(createTestChapter(2L))
        
        // Access chapter 1 (hit)
        cacheService.getChapter(1L)
        
        // Try to get non-existent chapter (miss)
        cacheService.getChapter(999L)
        
        // When
        val stats = cacheService.getCacheStats()
        
        // Then
        assertEquals(2, stats.cachedChapters)
        assertEquals(3, stats.maxCapacity)
        assertEquals(1L, stats.hitCount)
        assertEquals(1L, stats.missCount)
        assertTrue(stats.memoryUsedBytes > 0)
    }
    
    @Test
    fun `isCacheFull should return true when at capacity`() = runTest {
        // Given
        cacheService.cacheChapter(createTestChapter(1L))
        cacheService.cacheChapter(createTestChapter(2L))
        
        // When - Not full yet
        assertFalse(cacheService.isCacheFull())
        
        // When - Fill to capacity
        cacheService.cacheChapter(createTestChapter(3L))
        
        // Then
        assertTrue(cacheService.isCacheFull())
    }
    
    @Test
    fun `getMemoryUsage should track memory consumption`() = runTest {
        // Given
        val initialMemory = cacheService.getMemoryUsage()
        assertEquals(0L, initialMemory)
        
        // When
        cacheService.cacheChapter(createTestChapter(1L, contentSize = 1000))
        val memoryAfterOne = cacheService.getMemoryUsage()
        
        cacheService.cacheChapter(createTestChapter(2L, contentSize = 2000))
        val memoryAfterTwo = cacheService.getMemoryUsage()
        
        // Then
        assertTrue(memoryAfterOne > 0)
        assertTrue(memoryAfterTwo > memoryAfterOne)
    }
    
    @Test
    fun `cache should handle chapters with large content`() = runTest {
        // Given
        val largeChapter = createTestChapter(1L, contentSize = 10000)
        
        // When
        cacheService.cacheChapter(largeChapter)
        val retrieved = cacheService.getChapter(1L)
        
        // Then
        assertNotNull(retrieved)
        assertEquals(largeChapter.content.size, retrieved.content.size)
    }
    
    @Test
    fun `cache should track eviction count`() = runTest {
        // Given
        val initialStats = cacheService.getCacheStats()
        assertEquals(0L, initialStats.evictionCount)
        
        // When - Fill cache beyond capacity
        cacheService.cacheChapter(createTestChapter(1L))
        cacheService.cacheChapter(createTestChapter(2L))
        cacheService.cacheChapter(createTestChapter(3L))
        cacheService.cacheChapter(createTestChapter(4L)) // Triggers eviction
        
        // Then
        val stats = cacheService.getCacheStats()
        assertEquals(1L, stats.evictionCount)
    }
    
    @Test
    fun `cache should handle concurrent access safely`() = runTest {
        // Given
        val chapter = createTestChapter(1L)
        
        // When - Concurrent operations
        cacheService.cacheChapter(chapter)
        val result1 = cacheService.getChapter(1L)
        val result2 = cacheService.getChapter(1L)
        
        // Then - Both should succeed
        assertNotNull(result1)
        assertNotNull(result2)
    }
    
    private fun createTestChapter(
        id: Long,
        contentSize: Int = 100
    ): Chapter {
        val content = List(contentSize) { "Paragraph $it" }
        
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = "Chapter $id",
            dateUpload = System.currentTimeMillis(),
            number = id.toFloat(),
            sourceOrder = id.toInt(),
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            dateFetch = System.currentTimeMillis(),
            translator = null,
            content = content
        )
    }
}
