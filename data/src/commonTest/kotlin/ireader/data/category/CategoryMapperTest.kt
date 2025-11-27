package ireader.data.category

import ireader.domain.models.entities.Category
import kotlin.test.*

/**
 * Comprehensive tests for category mappers
 */
class CategoryMapperTest {
    
    @Test
    fun `categoryMapper should create Category with all fields`() {
        // Given
        val id = 1L
        val name = "Fantasy"
        val order = 5L
        val flags = 10L
        
        // When
        val result = categoryMapper(id, name, order, flags)
        
        // Then
        assertEquals(id, result.id)
        assertEquals(name, result.name)
        assertEquals(order, result.order)
        assertEquals(flags, result.flags)
    }
    
    @Test
    fun `categoryMapper should handle empty name`() {
        // Given
        val id = 1L
        val name = ""
        val order = 0L
        val flags = 0L
        
        // When
        val result = categoryMapper(id, name, order, flags)
        
        // Then
        assertEquals("", result.name)
    }
    
    @Test
    fun `categoryMapper should handle zero values`() {
        // Given
        val id = 0L
        val name = "Default"
        val order = 0L
        val flags = 0L
        
        // When
        val result = categoryMapper(id, name, order, flags)
        
        // Then
        assertEquals(0L, result.id)
        assertEquals(0L, result.order)
        assertEquals(0L, result.flags)
    }
    
    @Test
    fun `categoryMapper should handle negative id for uncategorized`() {
        // Given
        val id = Category.UNCATEGORIZED_ID
        val name = "Uncategorized"
        val order = 0L
        val flags = 0L
        
        // When
        val result = categoryMapper(id, name, order, flags)
        
        // Then
        assertEquals(Category.UNCATEGORIZED_ID, result.id)
    }
    
    @Test
    fun `categoryWithCountMapper should create CategoryWithCount`() {
        // Given
        val id = 1L
        val name = "Action"
        val order = 1L
        val flags = 0L
        val count = 42L
        
        // When
        val result = categoryWithCountMapper(id, name, order, flags, count)
        
        // Then
        assertEquals(id, result.category.id)
        assertEquals(name, result.category.name)
        assertEquals(42, result.bookCount)
    }
    
    @Test
    fun `categoryWithCountMapper should handle zero count`() {
        // Given
        val id = 1L
        val name = "Empty Category"
        val order = 1L
        val flags = 0L
        val count = 0L
        
        // When
        val result = categoryWithCountMapper(id, name, order, flags, count)
        
        // Then
        assertEquals(0, result.bookCount)
    }
    
    @Test
    fun `categoryWithCountMapper should convert long count to int`() {
        // Given
        val id = 1L
        val name = "Large Category"
        val order = 1L
        val flags = 0L
        val count = 1000L
        
        // When
        val result = categoryWithCountMapper(id, name, order, flags, count)
        
        // Then
        assertEquals(1000, result.bookCount)
    }
}
