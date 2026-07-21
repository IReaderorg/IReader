package ireader.data.downloads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadMapperTest {

    @Test
    fun downloadMapperShouldMapAllFields() {
        // downloadMapper: (Long, Long, Int, Long, String, String, String, String?, Long, Boolean) -> SavedDownloadWithInfo
        val download = downloadMapper(
            1L,         // chapterId
            100L,       // bookId
            5,          // priority
            1000L,      // _id
            "Test Book", // title
            "https://example.com/chapter", // url (mapped to chapterKey)
            "Chapter 1", // name (mapped to chapterName)
            "Scanlator", // scanlator (mapped to translator)
            200L,       // source
            true        // is_downloaded
        )

        assertEquals(1L, download.chapterId)
        assertEquals(100L, download.bookId)
        assertEquals(5, download.priority)
        assertEquals(1000L, download.id)
        assertEquals("Test Book", download.bookName)
        assertEquals("https://example.com/chapter", download.chapterKey)
        assertEquals("Chapter 1", download.chapterName)
        assertEquals("Scanlator", download.translator)
        assertEquals(200L, download.sourceId)
        assertTrue(download.isDownloaded)
    }

    @Test
    fun downloadMapperShouldHandleNullScanlator() {
        val download = downloadMapper(
            1L, 100L, 1, 1000L, "Book", "url", "Ch 1", null, 200L, false
        )

        assertEquals("", download.translator)
    }

    @Test
    fun downloadMapperShouldHandleNotDownloaded() {
        val download = downloadMapper(
            1L, 100L, 1, 1000L, "Book", "url", "Ch 1", null, 200L, false
        )

        assertFalse(download.isDownloaded)
    }

    @Test
    fun downloadMapperShouldMapPriority() {
        val download = downloadMapper(
            1L, 100L, 10, 1000L, "Book", "url", "Ch 1", null, 200L, true
        )

        assertEquals(10, download.priority)
    }
}
