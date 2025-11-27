package ireader.data.remote

import kotlin.test.*

/**
 * Comprehensive tests for InputSanitizer
 */
class InputSanitizerTest {
    
    // Username sanitization tests
    
    @Test
    fun `sanitizeUsername should trim whitespace`() {
        // Given
        val username = "  testuser  "
        
        // When
        val result = InputSanitizer.sanitizeUsername(username)
        
        // Then
        assertEquals("testuser", result)
    }
    
    @Test
    fun `sanitizeUsername should truncate to 50 characters`() {
        // Given
        val longUsername = "a".repeat(100)
        
        // When
        val result = InputSanitizer.sanitizeUsername(longUsername)
        
        // Then
        assertEquals(50, result.length)
        assertEquals("a".repeat(50), result)
    }
    
    @Test
    fun `sanitizeUsername should preserve short usernames`() {
        // Given
        val username = "john_doe"
        
        // When
        val result = InputSanitizer.sanitizeUsername(username)
        
        // Then
        assertEquals("john_doe", result)
    }
    
    @Test
    fun `sanitizeUsername should handle empty string`() {
        // Given
        val username = ""
        
        // When
        val result = InputSanitizer.sanitizeUsername(username)
        
        // Then
        assertEquals("", result)
    }
    
    @Test
    fun `sanitizeUsername should handle whitespace only`() {
        // Given
        val username = "   "
        
        // When
        val result = InputSanitizer.sanitizeUsername(username)
        
        // Then
        assertEquals("", result)
    }
    
    // BookId sanitization tests
    
    @Test
    fun `sanitizeBookId should trim whitespace`() {
        // Given
        val bookId = "  book-123  "
        
        // When
        val result = InputSanitizer.sanitizeBookId(bookId)
        
        // Then
        assertEquals("book-123", result)
    }
    
    @Test
    fun `sanitizeBookId should truncate to 255 characters`() {
        // Given
        val longBookId = "b".repeat(300)
        
        // When
        val result = InputSanitizer.sanitizeBookId(longBookId)
        
        // Then
        assertEquals(255, result.length)
    }
    
    @Test
    fun `sanitizeBookId should preserve valid book IDs`() {
        // Given
        val bookId = "novel-123-chapter-1"
        
        // When
        val result = InputSanitizer.sanitizeBookId(bookId)
        
        // Then
        assertEquals("novel-123-chapter-1", result)
    }
    
    // ChapterSlug sanitization tests
    
    @Test
    fun `sanitizeChapterSlug should trim whitespace`() {
        // Given
        val slug = "  chapter-1  "
        
        // When
        val result = InputSanitizer.sanitizeChapterSlug(slug)
        
        // Then
        assertEquals("chapter-1", result)
    }
    
    @Test
    fun `sanitizeChapterSlug should truncate to 255 characters`() {
        // Given
        val longSlug = "c".repeat(300)
        
        // When
        val result = InputSanitizer.sanitizeChapterSlug(longSlug)
        
        // Then
        assertEquals(255, result.length)
    }
    
    @Test
    fun `sanitizeChapterSlug should preserve valid slugs`() {
        // Given
        val slug = "chapter-1-the-beginning"
        
        // When
        val result = InputSanitizer.sanitizeChapterSlug(slug)
        
        // Then
        assertEquals("chapter-1-the-beginning", result)
    }
    
    // Scroll position validation tests
    
    @Test
    fun `validateScrollPosition should return same value for valid position`() {
        // Given
        val position = 0.5f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(0.5f, result)
    }
    
    @Test
    fun `validateScrollPosition should clamp negative values to 0`() {
        // Given
        val position = -0.5f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(0f, result)
    }
    
    @Test
    fun `validateScrollPosition should clamp values above 1 to 1`() {
        // Given
        val position = 1.5f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(1f, result)
    }
    
    @Test
    fun `validateScrollPosition should accept 0`() {
        // Given
        val position = 0f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(0f, result)
    }
    
    @Test
    fun `validateScrollPosition should accept 1`() {
        // Given
        val position = 1f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(1f, result)
    }
    
    @Test
    fun `validateScrollPosition should handle very large negative values`() {
        // Given
        val position = -1000f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(0f, result)
    }
    
    @Test
    fun `validateScrollPosition should handle very large positive values`() {
        // Given
        val position = 1000f
        
        // When
        val result = InputSanitizer.validateScrollPosition(position)
        
        // Then
        assertEquals(1f, result)
    }
}
