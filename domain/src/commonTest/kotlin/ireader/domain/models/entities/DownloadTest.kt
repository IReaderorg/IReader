package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for Download-related entities
 */
class DownloadTest {

    // ==================== Download Tests ====================

    @Test
    fun `Download creation with all fields`() {
        val download = Download(
            chapterId = 100L,
            bookId = 10L,
            priority = 5
        )
        
        assertEquals(100L, download.chapterId)
        assertEquals(10L, download.bookId)
        assertEquals(5, download.priority)
    }

    @Test
    fun `Download equality`() {
        val download1 = Download(chapterId = 1L, bookId = 1L, priority = 0)
        val download2 = Download(chapterId = 1L, bookId = 1L, priority = 0)
        
        assertEquals(download1, download2)
    }

    @Test
    fun `Download inequality with different chapterId`() {
        val download1 = Download(chapterId = 1L, bookId = 1L, priority = 0)
        val download2 = Download(chapterId = 2L, bookId = 1L, priority = 0)
        
        assertNotEquals(download1, download2)
    }

    // ==================== SavedDownload Tests ====================

    @Test
    fun `SavedDownload creation with all fields`() {
        val savedDownload = SavedDownload(
            chapterId = 100L,
            bookId = 10L,
            priority = 1,
            bookName = "Test Book",
            chapterKey = "chapter-key",
            chapterName = "Chapter 1",
            translator = "Translator Name"
        )
        
        assertEquals(100L, savedDownload.chapterId)
        assertEquals(10L, savedDownload.bookId)
        assertEquals(1, savedDownload.priority)
        assertEquals("Test Book", savedDownload.bookName)
        assertEquals("chapter-key", savedDownload.chapterKey)
        assertEquals("Chapter 1", savedDownload.chapterName)
        assertEquals("Translator Name", savedDownload.translator)
    }

    @Test
    fun `SavedDownload toDownload conversion`() {
        val savedDownload = SavedDownload(
            chapterId = 100L,
            bookId = 10L,
            priority = 5,
            bookName = "Test Book",
            chapterKey = "key",
            chapterName = "Chapter",
            translator = "Trans"
        )
        
        val download = savedDownload.toDownload()
        
        assertEquals(100L, download.chapterId)
        assertEquals(10L, download.bookId)
        assertEquals(5, download.priority)
    }

    @Test
    fun `SavedDownload equality`() {
        val saved1 = SavedDownload(
            chapterId = 1L, bookId = 1L, priority = 0,
            bookName = "Book", chapterKey = "key", chapterName = "Ch", translator = ""
        )
        val saved2 = SavedDownload(
            chapterId = 1L, bookId = 1L, priority = 0,
            bookName = "Book", chapterKey = "key", chapterName = "Ch", translator = ""
        )
        
        assertEquals(saved1, saved2)
    }

    // ==================== SavedDownloadWithInfo Tests ====================

    @Test
    fun `SavedDownloadWithInfo creation with all fields`() {
        val info = SavedDownloadWithInfo(
            chapterId = 100L,
            bookId = 10L,
            priority = 1,
            id = 1L,
            sourceId = 50L,
            bookName = "Test Book",
            chapterKey = "chapter-key",
            chapterName = "Chapter 1",
            translator = "Translator",
            isDownloaded = true,
            isFailed = false,
            errorMessage = null,
            retryCount = 0
        )
        
        assertEquals(100L, info.chapterId)
        assertEquals(10L, info.bookId)
        assertEquals(1, info.priority)
        assertEquals(1L, info.id)
        assertEquals(50L, info.sourceId)
        assertEquals("Test Book", info.bookName)
        assertTrue(info.isDownloaded)
        assertFalse(info.isFailed)
        assertNull(info.errorMessage)
        assertEquals(0, info.retryCount)
    }

    @Test
    fun `SavedDownloadWithInfo with default values`() {
        val info = SavedDownloadWithInfo(
            chapterId = 100L,
            bookId = 10L,
            id = 1L,
            sourceId = 50L,
            bookName = "Test Book",
            chapterKey = "key",
            chapterName = "Chapter",
            translator = "",
            isDownloaded = false
        )
        
        assertEquals(0, info.priority)
        assertFalse(info.isFailed)
        assertNull(info.errorMessage)
        assertEquals(0, info.retryCount)
    }

    @Test
    fun `SavedDownloadWithInfo with error state`() {
        val info = SavedDownloadWithInfo(
            chapterId = 100L,
            bookId = 10L,
            id = 1L,
            sourceId = 50L,
            bookName = "Test Book",
            chapterKey = "key",
            chapterName = "Chapter",
            translator = "",
            isDownloaded = false,
            isFailed = true,
            errorMessage = "Network error",
            retryCount = 3
        )
        
        assertTrue(info.isFailed)
        assertEquals("Network error", info.errorMessage)
        assertEquals(3, info.retryCount)
    }

    @Test
    fun `SavedDownloadWithInfo toDownload conversion`() {
        val info = SavedDownloadWithInfo(
            chapterId = 100L,
            bookId = 10L,
            priority = 5,
            id = 1L,
            sourceId = 50L,
            bookName = "Test Book",
            chapterKey = "key",
            chapterName = "Chapter",
            translator = "",
            isDownloaded = true
        )
        
        val download = info.toDownload()
        
        assertEquals(100L, download.chapterId)
        assertEquals(10L, download.bookId)
        assertEquals(5, download.priority)
    }

    @Test
    fun `SavedDownloadWithInfo toSavedDownload conversion`() {
        val info = SavedDownloadWithInfo(
            chapterId = 100L,
            bookId = 10L,
            priority = 5,
            id = 1L,
            sourceId = 50L,
            bookName = "Test Book",
            chapterKey = "chapter-key",
            chapterName = "Chapter 1",
            translator = "Trans",
            isDownloaded = true
        )
        
        val savedDownload = info.toSavedDownload()
        
        assertEquals(100L, savedDownload.chapterId)
        assertEquals(10L, savedDownload.bookId)
        assertEquals(1, savedDownload.priority) // Note: toSavedDownload sets priority to 1
        assertEquals("Test Book", savedDownload.bookName)
        assertEquals("chapter-key", savedDownload.chapterKey)
        assertEquals("Chapter 1", savedDownload.chapterName)
        assertEquals("Trans", savedDownload.translator)
    }

    // ==================== buildSavedDownload Tests ====================

    @Test
    fun `buildSavedDownload creates correct SavedDownload`() {
        val book = Book(
            id = 10L,
            sourceId = 1L,
            title = "Test Book",
            key = "book-key"
        )
        val chapter = Chapter(
            id = 100L,
            bookId = 10L,
            key = "chapter-key",
            name = "Chapter 1",
            translator = "Translator Name"
        )
        
        val savedDownload = buildSavedDownload(book, chapter)
        
        assertEquals(100L, savedDownload.chapterId)
        assertEquals(10L, savedDownload.bookId)
        assertEquals(0, savedDownload.priority) // Default priority is 0 (pending)
        assertEquals("Test Book", savedDownload.bookName)
        assertEquals("chapter-key", savedDownload.chapterKey)
        assertEquals("Chapter 1", savedDownload.chapterName)
        assertEquals("Translator Name", savedDownload.translator)
    }

    @Test
    fun `buildSavedDownload with empty translator`() {
        val book = Book(id = 1L, sourceId = 1L, title = "Book", key = "key")
        val chapter = Chapter(id = 1L, bookId = 1L, key = "ch-key", name = "Ch 1", translator = "")
        
        val savedDownload = buildSavedDownload(book, chapter)
        
        assertEquals("", savedDownload.translator)
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `filter downloads by book`() {
        val downloads = listOf(
            Download(chapterId = 1L, bookId = 10L, priority = 0),
            Download(chapterId = 2L, bookId = 10L, priority = 0),
            Download(chapterId = 3L, bookId = 20L, priority = 0),
            Download(chapterId = 4L, bookId = 10L, priority = 0)
        )
        
        val book10Downloads = downloads.filter { it.bookId == 10L }
        
        assertEquals(3, book10Downloads.size)
    }

    @Test
    fun `sort downloads by priority`() {
        val downloads = listOf(
            Download(chapterId = 1L, bookId = 1L, priority = 3),
            Download(chapterId = 2L, bookId = 1L, priority = 1),
            Download(chapterId = 3L, bookId = 1L, priority = 2)
        )
        
        val sorted = downloads.sortedBy { it.priority }
        
        assertEquals(2L, sorted[0].chapterId)
        assertEquals(3L, sorted[1].chapterId)
        assertEquals(1L, sorted[2].chapterId)
    }

    @Test
    fun `filter failed downloads`() {
        val downloads = listOf(
            SavedDownloadWithInfo(
                chapterId = 1L, bookId = 1L, id = 1L, sourceId = 1L,
                bookName = "B1", chapterKey = "k1", chapterName = "C1",
                translator = "", isDownloaded = true, isFailed = false
            ),
            SavedDownloadWithInfo(
                chapterId = 2L, bookId = 1L, id = 2L, sourceId = 1L,
                bookName = "B1", chapterKey = "k2", chapterName = "C2",
                translator = "", isDownloaded = false, isFailed = true,
                errorMessage = "Error"
            ),
            SavedDownloadWithInfo(
                chapterId = 3L, bookId = 1L, id = 3L, sourceId = 1L,
                bookName = "B1", chapterKey = "k3", chapterName = "C3",
                translator = "", isDownloaded = false, isFailed = false
            )
        )
        
        val failed = downloads.filter { it.isFailed }
        
        assertEquals(1, failed.size)
        assertEquals(2L, failed[0].chapterId)
    }

    @Test
    fun `count downloaded chapters`() {
        val downloads = listOf(
            SavedDownloadWithInfo(
                chapterId = 1L, bookId = 1L, id = 1L, sourceId = 1L,
                bookName = "B", chapterKey = "k1", chapterName = "C1",
                translator = "", isDownloaded = true
            ),
            SavedDownloadWithInfo(
                chapterId = 2L, bookId = 1L, id = 2L, sourceId = 1L,
                bookName = "B", chapterKey = "k2", chapterName = "C2",
                translator = "", isDownloaded = false
            ),
            SavedDownloadWithInfo(
                chapterId = 3L, bookId = 1L, id = 3L, sourceId = 1L,
                bookName = "B", chapterKey = "k3", chapterName = "C3",
                translator = "", isDownloaded = true
            )
        )
        
        val downloadedCount = downloads.count { it.isDownloaded }
        
        assertEquals(2, downloadedCount)
    }
}
