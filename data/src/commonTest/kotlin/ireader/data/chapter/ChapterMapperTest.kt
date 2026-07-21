package ireader.data.chapter

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChapterMapperTest {

    @Test
    fun chapterMapperShouldMapAllFields() {
        // chapterMapper: (Long, Long, String, String, String?, Boolean, Boolean, Long, Float, Long, Long, Long, List<Page>, Long) -> Chapter
        val content = listOf(Text("Page 1 content"), Text("Page 2 content"))
        val chapter = chapterMapper(
            1L,           // _id
            100L,         // book_id
            "chapter-key", // url
            "Chapter 1",  // name
            "Test Scanlator", // scanlator
            false,        // read
            true,         // bookmark
            5L,           // last_page_read
            1.5f,         // chapter_number
            3L,           // source_order
            1000L,        // date_fetch
            2000L,        // date_upload
            content,      // content
            0L            // type
        )

        assertEquals(1L, chapter.id)
        assertEquals(100L, chapter.bookId)
        assertEquals("chapter-key", chapter.key)
        assertEquals("Chapter 1", chapter.name)
        assertEquals("Test Scanlator", chapter.translator)
        assertFalse(chapter.read)
        assertTrue(chapter.bookmark)
        assertEquals(5L, chapter.lastPageRead)
        assertEquals(1.5f, chapter.number)
        assertEquals(3L, chapter.sourceOrder)
        assertEquals(1000L, chapter.dateFetch)
        assertEquals(2000L, chapter.dateUpload)
        assertEquals(content, chapter.content)
        assertEquals(0L, chapter.type)
    }

    @Test
    fun chapterMapperShouldHandleNullScanlator() {
        val chapter = chapterMapper(
            1L, 100L, "key", "Ch 1", null, false, false, 0L, 0f, 1L, 0L, 0L, emptyList(), 0L
        )

        assertEquals("", chapter.translator)
    }

    @Test
    fun chapterMapperShouldHandleEmptyContent() {
        val chapter = chapterMapper(
            1L, 100L, "key", "Ch 1", "scan", true, false, 10L, 2f, 1L, 100L, 200L, emptyList(), 1L
        )

        assertEquals(emptyList(), chapter.content)
        assertTrue(chapter.read)
        assertEquals(10L, chapter.lastPageRead)
        assertEquals(2f, chapter.number)
        assertEquals(1L, chapter.type)
    }

    @Test
    fun chapterMapperLightShouldMapWithoutContent() {
        // chapterMapperLight: (Long, Long, String, String, String?, Boolean, Boolean, Long, Float, Long, Long, Long, Long, Long) -> Chapter
        val chapter = chapterMapperLight(
            1L,           // _id
            100L,         // book_id
            "chapter-key", // url
            "Chapter 1",  // name
            "Scanlator",  // scanlator
            false,        // read
            true,         // bookmark
            5L,           // last_page_read
            1.0f,         // chapter_number
            2L,           // source_order
            1000L,        // date_fetch
            2000L,        // date_upload
            0L,           // type
            0L            // is_downloaded
        )

        assertEquals(1L, chapter.id)
        assertEquals(100L, chapter.bookId)
        assertEquals("chapter-key", chapter.key)
        assertEquals("Chapter 1", chapter.name)
        assertEquals("Scanlator", chapter.translator)
        assertFalse(chapter.read)
        assertTrue(chapter.bookmark)
        assertEquals(5L, chapter.lastPageRead)
        assertEquals(1.0f, chapter.number)
        assertEquals(2L, chapter.sourceOrder)
        assertEquals(emptyList(), chapter.content) // Not downloaded, so empty
    }

    @Test
    fun chapterMapperLightShouldAddPlaceholderWhenDownloaded() {
        val chapter = chapterMapperLight(
            1L, 100L, "key", "Ch 1", null, false, false, 0L, 0f, 1L, 0L, 0L, 0L,
            1L // is_downloaded = 1
        )

        assertEquals(1, chapter.content.size)
        val page = chapter.content[0] as Text
        assertEquals(DOWNLOADED_CHAPTER_PLACEHOLDER, page.text)
        assertTrue(page.text.length >= 50) // Must be at least 50 chars
    }

    @Test
    fun chapterMapperLightShouldNotAddPlaceholderWhenNotDownloaded() {
        val chapter = chapterMapperLight(
            1L, 100L, "key", "Ch 1", null, false, false, 0L, 0f, 1L, 0L, 0L, 0L,
            0L // is_downloaded = 0
        )

        assertEquals(emptyList(), chapter.content)
    }

    @Test
    fun downloadedChapterPlaceholderShouldBeLongEnough() {
        assertTrue(
            DOWNLOADED_CHAPTER_PLACEHOLDER.length >= 50,
            "Placeholder must be at least 50 characters"
        )
    }
}
