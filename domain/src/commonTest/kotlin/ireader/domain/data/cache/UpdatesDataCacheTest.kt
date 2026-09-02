package ireader.domain.data.cache

import ireader.domain.models.BookCover
import ireader.domain.models.entities.UpdatesWithRelations
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdatesDataCacheTest {

    private val testDate = LocalDateTime(2026, 9, 2, 12, 0, 0)

    @BeforeTest
    fun setUp() {
        UpdatesDataCache.invalidate()
    }

    @Test
    fun testEmptyCache() {
        assertFalse(UpdatesDataCache.hasCache())
        assertTrue(UpdatesDataCache.getCachedUpdates().isEmpty())
    }

    @Test
    fun testUpdateCache() {
        val sampleUpdate = UpdatesWithRelations(
            bookId = 10L,
            bookTitle = "Test Novel",
            chapterId = 100L,
            chapterName = "Chapter 1",
            scanlator = "Translator",
            read = false,
            bookmark = false,
            sourceId = 1L,
            dateFetch = 1000L,
            coverData = BookCover(
                bookId = 10L,
                sourceId = 1L,
                favorite = true,
                cover = ""
            ),
            downloaded = false
        )

        val grouped = mapOf(testDate to listOf(sampleUpdate))
        UpdatesDataCache.updateCache(grouped)

        assertTrue(UpdatesDataCache.hasCache())
        val cached = UpdatesDataCache.getCachedUpdates()
        assertEquals(1, cached.size)
        assertEquals(1, cached[testDate]?.size)
        assertEquals("Test Novel", cached[testDate]?.first()?.bookTitle)
    }

    @Test
    fun testInvalidateCache() {
        val sampleUpdate = UpdatesWithRelations(
            bookId = 10L,
            bookTitle = "Test Novel",
            chapterId = 100L,
            chapterName = "Chapter 1",
            scanlator = "Translator",
            read = false,
            bookmark = false,
            sourceId = 1L,
            dateFetch = 1000L,
            coverData = BookCover(
                bookId = 10L,
                sourceId = 1L,
                favorite = true,
                cover = ""
            ),
            downloaded = false
        )

        val grouped = mapOf(testDate to listOf(sampleUpdate))
        UpdatesDataCache.updateCache(grouped)
        assertTrue(UpdatesDataCache.hasCache())

        UpdatesDataCache.invalidate()
        assertFalse(UpdatesDataCache.hasCache())
        assertTrue(UpdatesDataCache.getCachedUpdates().isEmpty())
    }
}
