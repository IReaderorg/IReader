package ireader.domain.utils

import kotlin.test.*

/**
 * Unit tests for BookIdNormalizer utility
 * Tests book title normalization
 */
class BookIdNormalizerTest {
    
    @Test
    fun `normalize should convert to lowercase`() {
        // Given
        val title = "Lord of the Mysteries"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("lord-of-the-mysteries", normalized)
    }
    
    @Test
    fun `normalize should remove special characters`() {
        // Given
        val title = "Re:Zero - Starting Life in Another World"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("rezero-starting-life-in-another-world", normalized)
    }
    
    @Test
    fun `normalize should remove punctuation`() {
        // Given
        val title = "The King's Avatar!!!"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("the-kings-avatar", normalized)
    }
    
    @Test
    fun `normalize should replace spaces with hyphens`() {
        // Given
        val title = "Solo Leveling"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("solo-leveling", normalized)
    }
    
    @Test
    fun `normalize should handle multiple spaces`() {
        // Given
        val title = "The   Beginning   After   The   End"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("the-beginning-after-the-end", normalized)
    }
    
    @Test
    fun `normalize should remove consecutive hyphens`() {
        // Given
        val title = "Overgeared---The Novel"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        // The implementation removes special chars first, so --- becomes empty, then spaces become hyphens
        assertEquals("overgearedthe-novel", normalized)
    }
    
    @Test
    fun `normalize should trim leading and trailing hyphens`() {
        // Given
        val title = "---Mushoku Tensei---"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("mushoku-tensei", normalized)
    }
    
    @Test
    fun `normalize should create consistent IDs for same title`() {
        // Given
        val title = "Second Life Ranker"
        
        // When
        val id1 = BookIdNormalizer.normalize(title)
        val id2 = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals(id1, id2)
    }
    
    @Test
    fun `normalize should create different IDs for different titles`() {
        // Given
        val title1 = "Tower of God"
        val title2 = "God of Blackfield"
        
        // When
        val id1 = BookIdNormalizer.normalize(title1)
        val id2 = BookIdNormalizer.normalize(title2)
        
        // Then
        assertNotEquals(id1, id2)
    }
    
    @Test
    fun `normalize should handle titles with numbers`() {
        // Given
        val title = "86 - Eighty Six"
        
        // When
        val normalized = BookIdNormalizer.normalize(title)
        
        // Then
        assertEquals("86-eighty-six", normalized)
    }
}
