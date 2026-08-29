package ireader.domain.data.cache

import ireader.domain.models.entities.HistoryWithRelations
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryDataCacheTest {

    @BeforeTest
    fun setUp() {
        HistoryDataCache.invalidate()
    }

    @Test
    fun testEmptyCache() {
        assertFalse(HistoryDataCache.hasCache())
        assertTrue(HistoryDataCache.getCachedHistories().isEmpty())
    }

    @Test
    fun testUpdateCache() {
        val sampleHistory = HistoryWithRelations(
            id = 1L,
            bookId = 10L,
            chapterId = 100L,
            title = "Test Novel",
            readAt = 1000L,
            readDuration = 60L,
            progress = 0.5f,
            chapterName = "Chapter 1",
            chapterNumber = 1f,
            coverData = ireader.domain.models.BookCover(
                bookId = 10L,
                sourceId = 1L,
                cover = "",
                favorite = true
            )
        )

        val grouped = mapOf(1000L to listOf(sampleHistory))
        HistoryDataCache.updateCache(grouped)

        assertTrue(HistoryDataCache.hasCache())
        val cached = HistoryDataCache.getCachedHistories()
        assertEquals(1, cached.size)
        assertEquals(1, cached[1000L]?.size)
        assertEquals("Test Novel", cached[1000L]?.first()?.title)
    }

    @Test
    fun testInvalidateCache() {
        val sampleHistory = HistoryWithRelations(
            id = 1L,
            bookId = 10L,
            chapterId = 100L,
            title = "Test Novel",
            readAt = 1000L,
            readDuration = 60L,
            progress = 0.5f,
            chapterName = "Chapter 1",
            chapterNumber = 1f,
            coverData = ireader.domain.models.BookCover(
                bookId = 10L,
                sourceId = 1L,
                cover = "",
                favorite = true
            )
        )

        val grouped = mapOf(1000L to listOf(sampleHistory))
        HistoryDataCache.updateCache(grouped)
        assertTrue(HistoryDataCache.hasCache())

        HistoryDataCache.invalidate()
        assertFalse(HistoryDataCache.hasCache())
        assertTrue(HistoryDataCache.getCachedHistories().isEmpty())
    }
}
