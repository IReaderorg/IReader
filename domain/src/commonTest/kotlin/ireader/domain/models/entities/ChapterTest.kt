package ireader.domain.models.entities

import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Text
import kotlin.test.*

/**
 * Unit tests for Chapter entity and related functions
 */
class ChapterTest {

    // ==================== Chapter Creation Tests ====================

    @Test
    fun `chapter default values are correct`() {
        val chapter = Chapter(
            bookId = 1L,
            key = "chapter-key",
            name = "Chapter 1"
        )
        
        assertEquals(0L, chapter.id)
        assertEquals(1L, chapter.bookId)
        assertEquals("chapter-key", chapter.key)
        assertEquals("Chapter 1", chapter.name)
        assertFalse(chapter.read)
        assertFalse(chapter.bookmark)
        assertEquals(0L, chapter.dateUpload)
        assertEquals(0L, chapter.dateFetch)
        assertEquals(0L, chapter.sourceOrder)
        assertTrue(chapter.content.isEmpty())
        assertEquals(-1f, chapter.number)
        assertEquals("", chapter.translator)
        assertEquals(0L, chapter.lastPageRead)
        assertEquals(ChapterInfo.NOVEL, chapter.type)
    }

    @Test
    fun `chapter with all fields set`() {
        val pages = listOf(Text("Page 1 content"), Text("Page 2 content"))
        val chapter = Chapter(
            id = 10L,
            bookId = 5L,
            key = "ch-10",
            name = "Chapter 10: The Beginning",
            read = true,
            bookmark = true,
            dateUpload = 1234567890L,
            dateFetch = 1234567900L,
            sourceOrder = 10L,
            content = pages,
            number = 10.5f,
            translator = "Translator Name",
            lastPageRead = 2L,
            type = ChapterInfo.NOVEL
        )
        
        assertEquals(10L, chapter.id)
        assertEquals(5L, chapter.bookId)
        assertEquals("ch-10", chapter.key)
        assertEquals("Chapter 10: The Beginning", chapter.name)
        assertTrue(chapter.read)
        assertTrue(chapter.bookmark)
        assertEquals(1234567890L, chapter.dateUpload)
        assertEquals(1234567900L, chapter.dateFetch)
        assertEquals(10L, chapter.sourceOrder)
        assertEquals(2, chapter.content.size)
        assertEquals(10.5f, chapter.number)
        assertEquals("Translator Name", chapter.translator)
        assertEquals(2L, chapter.lastPageRead)
    }

    // ==================== isRecognizedNumber Tests ====================

    @Test
    fun `isRecognizedNumber returns true for positive number`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Ch 1", number = 1f)
        
        assertTrue(chapter.isRecognizedNumber)
    }

    @Test
    fun `isRecognizedNumber returns true for zero`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Prologue", number = 0f)
        
        assertTrue(chapter.isRecognizedNumber)
    }

    @Test
    fun `isRecognizedNumber returns false for negative number`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Unknown", number = -1f)
        
        assertFalse(chapter.isRecognizedNumber)
    }

    @Test
    fun `isRecognizedNumber returns true for decimal number`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Ch 1.5", number = 1.5f)
        
        assertTrue(chapter.isRecognizedNumber)
    }

    // ==================== isEmpty Tests ====================

    @Test
    fun `isEmpty returns true for empty content`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Empty", content = emptyList())
        
        assertTrue(chapter.isEmpty())
    }

    @Test
    fun `isEmpty returns false for content with whitespace text objects`() {
        // Note: isEmpty() uses joinToString() which includes the Text wrapper
        // So Text("   ") becomes "Text(text=   )" which is not blank
        val chapter = Chapter(
            bookId = 1L, 
            key = "key", 
            name = "Whitespace",
            content = listOf(Text("   "), Text("\n\t"))
        )
        
        // The content list is not empty, so isEmpty returns false
        // because joinToString includes the data class representation
        assertFalse(chapter.isEmpty())
    }

    @Test
    fun `isEmpty returns false for content with text`() {
        val chapter = Chapter(
            bookId = 1L,
            key = "key",
            name = "Has Content",
            content = listOf(Text("Some actual content"))
        )
        
        assertFalse(chapter.isEmpty())
    }

    @Test
    fun `isEmpty returns false for mixed content`() {
        val chapter = Chapter(
            bookId = 1L,
            key = "key",
            name = "Mixed",
            content = listOf(Text(""), Text("Content"), Text(""))
        )
        
        assertFalse(chapter.isEmpty())
    }

    // ==================== Conversion Tests ====================

    @Test
    fun `toChapterInfo converts chapter correctly`() {
        val chapter = Chapter(
            id = 5L,
            bookId = 10L,
            key = "chapter-key",
            name = "Chapter Name",
            dateUpload = 1234567890L,
            number = 5.0f,
            translator = "Scanlator Group"
        )
        
        val chapterInfo = chapter.toChapterInfo()
        
        assertEquals("chapter-key", chapterInfo.key)
        assertEquals("Chapter Name", chapterInfo.name)
        assertEquals(1234567890L, chapterInfo.dateUpload)
        assertEquals(5.0f, chapterInfo.number)
        assertEquals("Scanlator Group", chapterInfo.scanlator)
    }

    @Test
    fun `ChapterInfo toChapter conversion`() {
        val chapterInfo = ChapterInfo(
            key = "info-key",
            name = "Info Chapter",
            dateUpload = 9876543210L,
            number = 3.5f,
            scanlator = "Info Scanlator",
            type = ChapterInfo.NOVEL
        )
        
        val chapter = chapterInfo.toChapter(bookId = 20L)
        
        assertEquals(0L, chapter.id)
        assertEquals(20L, chapter.bookId)
        assertEquals("info-key", chapter.key)
        assertEquals("Info Chapter", chapter.name)
        assertEquals(9876543210L, chapter.dateUpload)
        assertEquals(3.5f, chapter.number)
        assertEquals("Info Scanlator", chapter.translator)
        assertEquals(ChapterInfo.NOVEL, chapter.type)
    }

    @Test
    fun `ChapterInfo toChapter with default values`() {
        val chapterInfo = ChapterInfo(
            key = "minimal-key",
            name = "Minimal Chapter"
        )
        
        val chapter = chapterInfo.toChapter(bookId = 1L)
        
        assertEquals(0L, chapter.id)
        assertEquals(1L, chapter.bookId)
        assertEquals("minimal-key", chapter.key)
        assertEquals("Minimal Chapter", chapter.name)
        assertEquals(-1f, chapter.number)
        assertEquals("", chapter.translator)
    }

    // ==================== Chapter State Tests ====================

    @Test
    fun `chapter read state can be set`() {
        val unreadChapter = Chapter(bookId = 1L, key = "key", name = "Ch", read = false)
        val readChapter = unreadChapter.copy(read = true)
        
        assertFalse(unreadChapter.read)
        assertTrue(readChapter.read)
    }

    @Test
    fun `chapter bookmark state can be set`() {
        val unbookmarked = Chapter(bookId = 1L, key = "key", name = "Ch", bookmark = false)
        val bookmarked = unbookmarked.copy(bookmark = true)
        
        assertFalse(unbookmarked.bookmark)
        assertTrue(bookmarked.bookmark)
    }

    @Test
    fun `chapter lastPageRead can be updated`() {
        val chapter = Chapter(bookId = 1L, key = "key", name = "Ch", lastPageRead = 0L)
        val updatedChapter = chapter.copy(lastPageRead = 5L)
        
        assertEquals(0L, chapter.lastPageRead)
        assertEquals(5L, updatedChapter.lastPageRead)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `chapter with very long name`() {
        val longName = "A".repeat(1000)
        val chapter = Chapter(bookId = 1L, key = "key", name = longName)
        
        assertEquals(1000, chapter.name.length)
    }

    @Test
    fun `chapter with special characters in name`() {
        val specialName = "Chapter 1: The Beginning! @#\$%^&*() 日本語 中文"
        val chapter = Chapter(bookId = 1L, key = "key", name = specialName)
        
        assertEquals(specialName, chapter.name)
    }

    @Test
    fun `chapter with large content list`() {
        val pages = (1..100).map { Text("Page $it content") }
        val chapter = Chapter(bookId = 1L, key = "key", name = "Large", content = pages)
        
        assertEquals(100, chapter.content.size)
        assertFalse(chapter.isEmpty())
    }

    @Test
    fun `chapter number edge cases`() {
        val zeroChapter = Chapter(bookId = 1L, key = "key", name = "Ch 0", number = 0f)
        val negativeChapter = Chapter(bookId = 1L, key = "key", name = "Ch -1", number = -1f)
        val largeChapter = Chapter(bookId = 1L, key = "key", name = "Ch 9999", number = 9999f)
        val decimalChapter = Chapter(bookId = 1L, key = "key", name = "Ch 1.5", number = 1.5f)
        
        assertTrue(zeroChapter.isRecognizedNumber)
        assertFalse(negativeChapter.isRecognizedNumber)
        assertTrue(largeChapter.isRecognizedNumber)
        assertTrue(decimalChapter.isRecognizedNumber)
    }
}
