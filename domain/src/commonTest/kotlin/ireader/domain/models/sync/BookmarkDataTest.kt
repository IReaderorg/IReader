package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookmarkDataTest {

    @Test
    fun `BookmarkData should be created with valid data`() {
        // Arrange
        val bookmarkId = 789L
        val bookId = 123L
        val chapterId = 456L
        val position = 1024
        val note = "Important quote"
        val createdAt = System.currentTimeMillis()

        // Act
        val bookmarkData = BookmarkData(
            bookmarkId = bookmarkId,
            bookId = bookId,
            chapterId = chapterId,
            position = position,
            note = note,
            createdAt = createdAt
        )

        // Assert
        assertEquals(bookmarkId, bookmarkData.bookmarkId)
        assertEquals(bookId, bookmarkData.bookId)
        assertEquals(chapterId, bookmarkData.chapterId)
        assertEquals(position, bookmarkData.position)
        assertEquals(note, bookmarkData.note)
        assertEquals(createdAt, bookmarkData.createdAt)
    }

    @Test
    fun `BookmarkData should reject negative bookmarkId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookmarkData(
                bookmarkId = -1L,
                bookId = 123L,
                chapterId = 456L,
                position = 1024,
                note = "Test note",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `BookmarkData should reject negative bookId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookmarkData(
                bookmarkId = 789L,
                bookId = -1L,
                chapterId = 456L,
                position = 1024,
                note = "Test note",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `BookmarkData should reject negative chapterId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookmarkData(
                bookmarkId = 789L,
                bookId = 123L,
                chapterId = -1L,
                position = 1024,
                note = "Test note",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `BookmarkData should reject negative position`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookmarkData(
                bookmarkId = 789L,
                bookId = 123L,
                chapterId = 456L,
                position = -1,
                note = "Test note",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `BookmarkData should reject negative createdAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookmarkData(
                bookmarkId = 789L,
                bookId = 123L,
                chapterId = 456L,
                position = 1024,
                note = "Test note",
                createdAt = -1L
            )
        }
    }

    @Test
    fun `BookmarkData should allow null note`() {
        // Arrange & Act
        val bookmarkData = BookmarkData(
            bookmarkId = 789L,
            bookId = 123L,
            chapterId = 456L,
            position = 1024,
            note = null,
            createdAt = System.currentTimeMillis()
        )

        // Assert
        assertEquals(null, bookmarkData.note)
    }

    @Test
    fun `BookmarkData should allow empty note`() {
        // Arrange & Act
        val bookmarkData = BookmarkData(
            bookmarkId = 789L,
            bookId = 123L,
            chapterId = 456L,
            position = 1024,
            note = "",
            createdAt = System.currentTimeMillis()
        )

        // Assert
        assertEquals("", bookmarkData.note)
    }
}
