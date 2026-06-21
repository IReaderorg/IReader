package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookSyncDataTest {

    @Test
    fun `BookSyncData should be created with valid data`() {
        // Arrange
        val globalId = "source-1|book-key-123"
        val title = "Test Book"
        val author = "Test Author"
        val coverUrl = "https://example.com/cover.jpg"
        val sourceId = "source-1"
        val key = "book-key-123"
        val favorite = true
        val addedAt = System.currentTimeMillis()
        val updatedAt = System.currentTimeMillis()
        val description = "A test book description"
        val genres = listOf("Fiction", "Adventure")
        val status = 1L

        // Act
        val bookData = BookSyncData(
            globalId = globalId,
            title = title,
            author = author,
            coverUrl = coverUrl,
            sourceId = sourceId,
            key = key,
            favorite = favorite,
            addedAt = addedAt,
            updatedAt = updatedAt,
            description = description,
            genres = genres,
            status = status
        )

        // Assert
        assertEquals(globalId, bookData.globalId)
        assertEquals(title, bookData.title)
        assertEquals(author, bookData.author)
        assertEquals(coverUrl, bookData.coverUrl)
        assertEquals(sourceId, bookData.sourceId)
        assertEquals(key, bookData.key)
        assertEquals(favorite, bookData.favorite)
        assertEquals(addedAt, bookData.addedAt)
        assertEquals(updatedAt, bookData.updatedAt)
        assertEquals(description, bookData.description)
        assertEquals(genres, bookData.genres)
        assertEquals(status, bookData.status)
    }

    @Test
    fun `BookSyncData should reject blank globalId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "",
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                key = "book-key",
                favorite = false,
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should reject empty title`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "source-1|book-key",
                title = "",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                key = "book-key",
                favorite = false,
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should reject blank sourceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "source-1|book-key",
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "",
                key = "book-key",
                favorite = false,
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should reject blank key`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "source-1|book-key",
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                key = "",
                favorite = false,
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should reject negative addedAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "source-1|book-key",
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                key = "book-key",
                favorite = false,
                addedAt = -1L,
                updatedAt = System.currentTimeMillis(),
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should reject negative updatedAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            BookSyncData(
                globalId = "source-1|book-key",
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "source-1",
                key = "book-key",
                favorite = false,
                addedAt = System.currentTimeMillis(),
                updatedAt = -1L,
                description = "Description",
                genres = emptyList(),
                status = 0L
            )
        }
    }

    @Test
    fun `BookSyncData should allow null coverUrl`() {
        // Arrange & Act
        val bookData = BookSyncData(
            globalId = "source-1|book-key",
            title = "Test Book",
            author = "Test Author",
            coverUrl = null,
            sourceId = "source-1",
            key = "book-key",
            favorite = false,
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            description = "Description",
            genres = emptyList(),
            status = 0L
        )

        // Assert
        assertEquals(null, bookData.coverUrl)
    }

    @Test
    fun `BookSyncData should allow empty genres list`() {
        // Arrange & Act
        val bookData = BookSyncData(
            globalId = "source-1|book-key",
            title = "Test Book",
            author = "Test Author",
            coverUrl = "https://example.com/cover.jpg",
            sourceId = "source-1",
            key = "book-key",
            favorite = false,
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            description = "Description",
            genres = emptyList(),
            status = 0L
        )

        // Assert
        assertEquals(emptyList(), bookData.genres)
    }
}
