package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookSyncDataTest {

    @Test
    fun `BookSyncData should be created with valid data`() {
        // Arrange
        val bookId = 123L
        val title = "Test Book"
        val author = "Test Author"
        val coverUrl = "https://example.com/cover.jpg"
        val sourceId = "source-1"
        val sourceUrl = "https://example.com/book/123"
        val addedAt = System.currentTimeMillis()
        val updatedAt = System.currentTimeMillis()
        val fileHash = "abc123def456"

        // Act
        val bookData = BookSyncData(
            bookId = bookId,
            title = title,
            author = author,
            coverUrl = coverUrl,
            sourceId = sourceId,
            sourceUrl = sourceUrl,
            addedAt = addedAt,
            updatedAt = updatedAt,
            fileHash = fileHash
        )

        // Assert
        assertEquals(bookId, bookData.bookId)
        assertEquals(title, bookData.title)
        assertEquals(author, bookData.author)
        assertEquals(coverUrl, bookData.coverUrl)
        assertEquals(sourceId, bookData.sourceId)
        assertEquals(sourceUrl, bookData.sourceUrl)
        assertEquals(addedAt, bookData.addedAt)
        assertEquals(updatedAt, bookData.updatedAt)
        assertEquals(fileHash, bookData.fileHash)
    }

    @Test
    fun `BookSyncData should reject negative bookId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                bookId = -1L,
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                sourceUrl = "https://example.com/book/123",
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                fileHash = "abc123"
            )
        }
    }

    @Test
    fun `BookSyncData should reject empty title`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                bookId = 123L,
                title = "",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                sourceUrl = "https://example.com/book/123",
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                fileHash = "abc123"
            )
        }
    }

    @Test
    fun `BookSyncData should reject negative addedAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                bookId = 123L,
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                sourceUrl = "https://example.com/book/123",
                addedAt = -1L,
                updatedAt = System.currentTimeMillis(),
                fileHash = "abc123"
            )
        }
    }

    @Test
    fun `BookSyncData should reject negative updatedAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                bookId = 123L,
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                sourceUrl = "https://example.com/book/123",
                addedAt = System.currentTimeMillis(),
                updatedAt = -1L,
                fileHash = "abc123"
            )
        }
    }

    @Test
    fun `BookSyncData should allow null coverUrl`() {
        // Arrange & Act
        val bookData = BookSyncData(
            bookId = 123L,
            title = "Test Book",
            author = "Test Author",
            coverUrl = null,
            sourceId = "source-1",
            sourceUrl = "https://example.com/book/123",
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileHash = "abc123"
        )

        // Assert
        assertEquals(null, bookData.coverUrl)
    }

    @Test
    fun `BookSyncData should allow null fileHash for books without files`() {
        // Arrange & Act
        val bookData = BookSyncData(
            bookId = 123L,
            title = "Test Book",
            author = "Test Author",
            coverUrl = "https://example.com/cover.jpg",
            sourceId = "source-1",
            sourceUrl = "https://example.com/book/123",
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileHash = null
        )

        // Assert
        assertEquals(null, bookData.fileHash)
    }
}
