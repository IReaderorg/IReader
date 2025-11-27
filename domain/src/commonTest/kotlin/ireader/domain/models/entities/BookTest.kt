package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for Book entity
 * Tests book data model and business logic
 */
class BookTest {
    
    @Test
    fun `book should be created with required fields`() {
        // Given & When
        val book = Book(
            id = 1L,
            sourceId = 100L,
            title = "Test Novel",
            author = "Test Author",
            description = "A test novel description",
            genres = listOf("Fantasy", "Adventure"),
            status = 1,
            cover = "https://example.com/cover.jpg",
            customCover = null,
            favorite = false,
            lastUpdate = System.currentTimeMillis(),
            lastInit = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0,
            key = "test-novel"
        )
        
        // Then
        assertEquals("Test Novel", book.title)
        assertEquals("Test Author", book.author)
        assertFalse(book.favorite)
        assertEquals(2, book.genres.size)
    }
    
    @Test
    fun `book should support favorite status`() {
        // Given
        val book = createTestBook()
        
        // When
        val favorited = book.copy(favorite = true)
        
        // Then
        assertTrue(favorited.favorite)
        assertFalse(book.favorite)
    }
    
    @Test
    fun `book should support custom cover`() {
        // Given
        val book = createTestBook()
        val customCover = "file:///custom/cover.jpg"
        
        // When
        val updated = book.copy(customCover = customCover)
        
        // Then
        assertEquals(customCover, updated.customCover)
        assertNotNull(updated.customCover)
    }
    
    @Test
    fun `book should track multiple genres`() {
        // Given
        val genres = listOf("Fantasy", "Adventure", "Romance", "Action")
        
        // When
        val book = createTestBook().copy(genres = genres)
        
        // Then
        assertEquals(4, book.genres.size)
        assertTrue(book.genres.contains("Fantasy"))
        assertTrue(book.genres.contains("Action"))
    }
    
    @Test
    fun `book should support empty genres`() {
        // Given & When
        val book = createTestBook().copy(genres = emptyList())
        
        // Then
        assertTrue(book.genres.isEmpty())
    }
    
    @Test
    fun `book status should indicate completion`() {
        // Given
        val ongoingBook = createTestBook().copy(status = 1)
        val completedBook = createTestBook().copy(status = 2)
        
        // Then
        assertEquals(1, ongoingBook.status)
        assertEquals(2, completedBook.status)
    }
    
    @Test
    fun `book should track last update time`() {
        // Given
        val initialTime = System.currentTimeMillis()
        val book = createTestBook().copy(lastUpdate = initialTime)
        
        // When
        val updatedTime = initialTime + 1000
        val updated = book.copy(lastUpdate = updatedTime)
        
        // Then
        assertTrue(updated.lastUpdate > book.lastUpdate)
    }
    
    @Test
    fun `book should have unique key`() {
        // Given
        val book1 = createTestBook().copy(key = "novel-1")
        val book2 = createTestBook().copy(key = "novel-2")
        
        // Then
        assertNotEquals(book1.key, book2.key)
    }
    
    private fun createTestBook(): Book {
        return Book(
            id = 1L,
            sourceId = 100L,
            title = "Test Novel",
            author = "Test Author",
            description = "Test description",
            genres = listOf("Fantasy"),
            status = 1,
            cover = "https://example.com/cover.jpg",
            customCover = null,
            favorite = false,
            lastUpdate = System.currentTimeMillis(),
            lastInit = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0,
            key = "test-novel"
        )
    }
}
