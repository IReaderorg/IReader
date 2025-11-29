package ireader.domain.services

import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ChapterCacheServiceTest {

    @Test
    fun `test infinite loop fix in cacheChapter`() = runTest {
        // Create a cache with very small memory limit (0 MB)
        val service = ChapterCacheServiceImpl(maxCapacity = 5, maxMemoryMB = 0) 
        
        // Create a chapter that is definitely larger than 0 bytes
        val chapter = Chapter(
            id = 1,
            bookId = 1,
            key = "test",
            name = "Test Chapter",
            read = false,
            bookmark = false,
            dateUpload = 0,
            dateFetch = 0,
            sourceOrder = 0,
            content = emptyList(), // Empty content but metadata has size
            number = 1f,
            translator = "",
            lastPageRead = 0,
            type = 0L
        )
        
        // This should NOT hang in infinite loop
        service.cacheChapter(chapter)
        
        val stats1 = service.getCacheStats()
        assertEquals(1, stats1.cachedChapters)
        
        // Now add another one
        val chapter2 = Chapter(
            id = 2,
            bookId = 1,
            key = "test2",
            name = "Test Chapter 2",
            read = false,
            bookmark = false,
            dateUpload = 0,
            dateFetch = 0,
            sourceOrder = 0,
            content = emptyList(),
            number = 2f,
            translator = "",
            lastPageRead = 0,
            type = 0L
        )
        
        service.cacheChapter(chapter2)
        
        val stats2 = service.getCacheStats()
        // After adding chapter 2, chapter 1 should be evicted (memory limit is 0)
        assertEquals(1, stats2.cachedChapters)
        assertEquals(1L, stats2.evictionCount)
    }
}
