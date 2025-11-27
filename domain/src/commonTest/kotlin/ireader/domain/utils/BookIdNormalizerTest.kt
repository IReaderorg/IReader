package ireader.domain.utils

import kotlin.test.*

/**
 * Unit tests for BookIdNormalizer utility
 * Tests URL normalization and book ID generation
 */
class BookIdNormalizerTest {
    
    @Test
    fun `normalize should remove trailing slashes`() {
        // Given
        val url = "https://example.com/book/123/"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/book/123", normalized)
    }
    
    @Test
    fun `normalize should convert to lowercase`() {
        // Given
        val url = "HTTPS://EXAMPLE.COM/BOOK/123"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/book/123", normalized)
    }
    
    @Test
    fun `normalize should remove query parameters`() {
        // Given
        val url = "https://example.com/book/123?ref=home&page=1"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/book/123", normalized)
    }
    
    @Test
    fun `normalize should remove fragments`() {
        // Given
        val url = "https://example.com/book/123#chapter-1"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/book/123", normalized)
    }
    
    @Test
    fun `normalize should handle multiple trailing slashes`() {
        // Given
        val url = "https://example.com/book/123///"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/book/123", normalized)
    }
    
    @Test
    fun `normalize should preserve path structure`() {
        // Given
        val url = "https://example.com/novels/fantasy/book-123"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com/novels/fantasy/book-123", normalized)
    }
    
    @Test
    fun `normalize should handle URLs with ports`() {
        // Given
        val url = "https://example.com:8080/book/123"
        
        // When
        val normalized = BookIdNormalizer.normalize(url)
        
        // Then
        assertEquals("https://example.com:8080/book/123", normalized)
    }
    
    @Test
    fun `normalize should be idempotent`() {
        // Given
        val url = "https://example.com/book/123"
        
        // When
        val normalized1 = BookIdNormalizer.normalize(url)
        val normalized2 = BookIdNormalizer.normalize(normalized1)
        
        // Then
        assertEquals(normalized1, normalized2)
    }
    
    @Test
    fun `generateId should create consistent IDs for same URL`() {
        // Given
        val url = "https://example.com/book/123"
        
        // When
        val id1 = BookIdNormalizer.generateId(url)
        val id2 = BookIdNormalizer.generateId(url)
        
        // Then
        assertEquals(id1, id2)
    }
    
    @Test
    fun `generateId should create different IDs for different URLs`() {
        // Given
        val url1 = "https://example.com/book/123"
        val url2 = "https://example.com/book/456"
        
        // When
        val id1 = BookIdNormalizer.generateId(url1)
        val id2 = BookIdNormalizer.generateId(url2)
        
        // Then
        assertNotEquals(id1, id2)
    }
    
    @Test
    fun `generateId should normalize before hashing`() {
        // Given
        val url1 = "https://example.com/book/123/"
        val url2 = "https://example.com/book/123"
        
        // When
        val id1 = BookIdNormalizer.generateId(url1)
        val id2 = BookIdNormalizer.generateId(url2)
        
        // Then
        assertEquals(id1, id2)
    }
}
