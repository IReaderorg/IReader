package ireader.data.book

import ireader.core.source.model.MangaInfo
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookMapperTest {

    @Test
    fun bookMapperShouldMapBasicFields() {
        // bookMapper: (Long, Long, String, String, String, String, List<String>, Long, String, String, Boolean, Long, Boolean, Long, Int, Int) -> Book
        val book = bookMapper(
            1L,        // _id
            100L,      // sourceId
            "Test Book", // title
            "book-key-1", // key
            "Test Author", // author
            "Test Description", // description
            listOf("Fiction", "Adventure"), // genres
            MangaInfo.COMPLETED, // status
            "https://example.com/cover.jpg", // cover
            "https://example.com/custom.jpg", // custom_cover
            true,      // favorite
            1234567890L, // last_update
            true,      // initialized
            1000000000L, // date_added
            1,         // viewer
            2          // flags
        )

        assertEquals(1L, book.id)
        assertEquals(100L, book.sourceId)
        assertEquals("Test Book", book.title)
        assertEquals("book-key-1", book.key)
        assertEquals("Test Author", book.author)
        assertEquals("Test Description", book.description)
        assertEquals(listOf("Fiction", "Adventure"), book.genres)
        assertEquals(MangaInfo.COMPLETED, book.status)
        assertEquals("https://example.com/cover.jpg", book.cover)
        assertEquals("https://example.com/custom.jpg", book.customCover)
        assertTrue(book.favorite)
        assertEquals(1234567890L, book.lastUpdate)
        assertTrue(book.initialized)
        assertEquals(1000000000L, book.dateAdded)
        assertEquals(1L, book.viewer)
        assertEquals(2L, book.flags)
    }

    @Test
    fun bookMapperShouldHandleEmptyFields() {
        val book = bookMapper(
            1L, 100L, "Test Book", "key", "", "", emptyList(),
            MangaInfo.UNKNOWN, "", "", false, 0L, false, 0L, 0, 0
        )

        assertEquals("", book.author)
        assertEquals("", book.description)
        assertEquals(emptyList(), book.genres)
        assertEquals(MangaInfo.UNKNOWN, book.status)
        assertFalse(book.favorite)
        assertFalse(book.initialized)
    }

    @Test
    fun booksMapperShouldMapToBook() {
        // booksMapper: (Long, Long, String, String?, String?, String?, List<String>?, String, Long, String?, String, Boolean, Long?, Long?, Boolean, Long, Long, Long, Long, Boolean, Long, Boolean, Long, Long, Long, Long, Long) -> Book
        val book = booksMapper(
            1L,        // _id
            100L,      // source
            "book-key", // url
            null,      // artist
            "Test Author", // author
            "Test Description", // description
            listOf("Fiction"), // genre
            "Test Book", // title
            MangaInfo.COMPLETED, // status
            "https://example.com/thumb.jpg", // thumbnail_url
            "",        // custom_cover
            true,      // favorite
            1234567890L, // last_update
            null,      // next_update
            true,      // initialized
            1L,        // viewer
            2L,        // chapter_flags
            1000L,     // cover_last_modified
            2000L,     // date_added
            false,     // is_pinned
            0L,        // pinned_order
            false,     // is_archived
            10L,       // cached_unread_count
            5L,        // cached_read_count
            15L,       // cached_total_chapters
            3000L,     // last_read_at
            2L         // chapter_page
        )

        assertEquals(1L, book.id)
        assertEquals(100L, book.sourceId)
        assertEquals("book-key", book.key)
        assertEquals("Test Book", book.title)
        assertEquals(MangaInfo.COMPLETED, book.status)
        assertEquals("https://example.com/thumb.jpg", book.cover)
        assertEquals("", book.customCover)
        assertTrue(book.favorite)
        assertTrue(book.initialized)
        assertFalse(book.isPinned)
        assertFalse(book.isArchived)
        assertEquals(2, book.chapterPage)
    }

    @Test
    fun booksMapperShouldHandleNullThumbnail() {
        val book = booksMapper(
            1L, 100L, "key", null, null, null, null, "Test",
            MangaInfo.UNKNOWN, null, "", false, null, null, false,
            0L, 0L, 0L, 0L, false, 0L, false, 0L, 0L, 0L, 0L, 0L
        )

        assertEquals("", book.cover)
        assertEquals("", book.author)
        assertEquals("", book.description)
        assertEquals(emptyList(), book.genres)
    }
}
