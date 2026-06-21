package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookmarkDataTest {

    @Test
    fun `ChapterSyncData should be created with valid data`() {
        // Arrange
        val globalId = "source-1|chapter-key-123"
        val bookGlobalId = "source-1|book-key-456"
        val key = "chapter-key-123"
        val name = "Chapter 1"
        val read = true
        val bookmark = false
        val lastPageRead = 5L
        val sourceOrder = 1L
        val number = 1.0f
        val dateUpload = System.currentTimeMillis()
        val dateFetch = System.currentTimeMillis()
        val translator = "Translator Name"
        val content = """[{"type":"text","value":"Page 1 content"}]"""

        // Act
        val chapterData = ChapterSyncData(
            globalId = globalId,
            bookGlobalId = bookGlobalId,
            key = key,
            name = name,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            sourceOrder = sourceOrder,
            number = number,
            dateUpload = dateUpload,
            dateFetch = dateFetch,
            translator = translator,
            content = content
        )

        // Assert
        assertEquals(globalId, chapterData.globalId)
        assertEquals(bookGlobalId, chapterData.bookGlobalId)
        assertEquals(key, chapterData.key)
        assertEquals(name, chapterData.name)
        assertEquals(read, chapterData.read)
        assertEquals(bookmark, chapterData.bookmark)
        assertEquals(lastPageRead, chapterData.lastPageRead)
        assertEquals(sourceOrder, chapterData.sourceOrder)
        assertEquals(number, chapterData.number)
        assertEquals(dateUpload, chapterData.dateUpload)
        assertEquals(dateFetch, chapterData.dateFetch)
        assertEquals(translator, chapterData.translator)
        assertEquals(content, chapterData.content)
    }

    @Test
    fun `ChapterSyncData should reject blank globalId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject blank bookGlobalId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject blank key`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject blank name`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject negative lastPageRead`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = -1L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject negative sourceOrder`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = -1L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject negative dateUpload`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = -1L,
                dateFetch = System.currentTimeMillis(),
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should reject negative dateFetch`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ChapterSyncData(
                globalId = "source-1|chapter-key",
                bookGlobalId = "source-1|book-key",
                key = "chapter-key",
                name = "Chapter 1",
                read = false,
                bookmark = false,
                lastPageRead = 0L,
                sourceOrder = 0L,
                number = 1.0f,
                dateUpload = System.currentTimeMillis(),
                dateFetch = -1L,
                translator = "",
                content = ""
            )
        }
    }

    @Test
    fun `ChapterSyncData should allow empty translator`() {
        // Arrange & Act
        val chapterData = ChapterSyncData(
            globalId = "source-1|chapter-key",
            bookGlobalId = "source-1|book-key",
            key = "chapter-key",
            name = "Chapter 1",
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            sourceOrder = 0L,
            number = 1.0f,
            dateUpload = System.currentTimeMillis(),
            dateFetch = System.currentTimeMillis(),
            translator = "",
            content = ""
        )

        // Assert
        assertEquals("", chapterData.translator)
    }

    @Test
    fun `ChapterSyncData should allow empty content`() {
        // Arrange & Act
        val chapterData = ChapterSyncData(
            globalId = "source-1|chapter-key",
            bookGlobalId = "source-1|book-key",
            key = "chapter-key",
            name = "Chapter 1",
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            sourceOrder = 0L,
            number = 1.0f,
            dateUpload = System.currentTimeMillis(),
            dateFetch = System.currentTimeMillis(),
            translator = "",
            content = ""
        )

        // Assert
        assertEquals("", chapterData.content)
    }
}
