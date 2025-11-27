package ireader.data.book

import kotlin.test.*

/**
 * Comprehensive tests for book genres converter
 */
class BookGenresConverterTest {
    
    @Test
    fun `encode should join genres with semicolon`() {
        // Given
        val genres = listOf("Fantasy", "Action", "Adventure")
        
        // When
        val result = bookGenresConverter.encode(genres)
        
        // Then
        assertEquals("Fantasy;Action;Adventure", result)
    }
    
    @Test
    fun `encode should return empty string for empty list`() {
        // Given
        val genres = emptyList<String>()
        
        // When
        val result = bookGenresConverter.encode(genres)
        
        // Then
        assertEquals("", result)
    }
    
    @Test
    fun `encode should handle single genre`() {
        // Given
        val genres = listOf("Romance")
        
        // When
        val result = bookGenresConverter.encode(genres)
        
        // Then
        assertEquals("Romance", result)
    }
    
    @Test
    fun `decode should split genres by semicolon`() {
        // Given
        val databaseValue = "Fantasy;Action;Adventure"
        
        // When
        val result = bookGenresConverter.decode(databaseValue)
        
        // Then
        assertEquals(listOf("Fantasy", "Action", "Adventure"), result)
    }
    
    @Test
    fun `decode should return empty list for empty string`() {
        // Given
        val databaseValue = ""
        
        // When
        val result = bookGenresConverter.decode(databaseValue)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `decode should handle single genre`() {
        // Given
        val databaseValue = "Romance"
        
        // When
        val result = bookGenresConverter.decode(databaseValue)
        
        // Then
        assertEquals(listOf("Romance"), result)
    }
    
    @Test
    fun `encode and decode should be reversible`() {
        // Given
        val originalGenres = listOf("Fantasy", "Action", "Adventure", "Comedy")
        
        // When
        val encoded = bookGenresConverter.encode(originalGenres)
        val decoded = bookGenresConverter.decode(encoded)
        
        // Then
        assertEquals(originalGenres, decoded)
    }
    
    @Test
    fun `encode should handle genres with spaces`() {
        // Given
        val genres = listOf("Slice of Life", "Martial Arts", "School Life")
        
        // When
        val result = bookGenresConverter.encode(genres)
        
        // Then
        assertEquals("Slice of Life;Martial Arts;School Life", result)
    }
    
    @Test
    fun `decode should preserve spaces in genres`() {
        // Given
        val databaseValue = "Slice of Life;Martial Arts;School Life"
        
        // When
        val result = bookGenresConverter.decode(databaseValue)
        
        // Then
        assertEquals(listOf("Slice of Life", "Martial Arts", "School Life"), result)
    }
}
