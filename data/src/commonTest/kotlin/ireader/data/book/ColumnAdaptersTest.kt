package ireader.data.book

import kotlin.test.*

/**
 * Comprehensive tests for column adapters
 */
class ColumnAdaptersTest {
    
    // Float to Double adapter tests
    
    @Test
    fun `floatDoubleColumnAdapter encode should convert float to double`() {
        // Given
        val floatValue = 3.14f
        
        // When
        val result = floatDoubleColumnAdapter.encode(floatValue)
        
        // Then
        assertEquals(3.14, result, 0.001)
    }
    
    @Test
    fun `floatDoubleColumnAdapter decode should convert double to float`() {
        // Given
        val doubleValue = 3.14
        
        // When
        val result = floatDoubleColumnAdapter.decode(doubleValue)
        
        // Then
        assertEquals(3.14f, result, 0.001f)
    }
    
    @Test
    fun `floatDoubleColumnAdapter should handle zero`() {
        // Given
        val floatValue = 0f
        
        // When
        val encoded = floatDoubleColumnAdapter.encode(floatValue)
        val decoded = floatDoubleColumnAdapter.decode(encoded)
        
        // Then
        assertEquals(0f, decoded)
    }
    
    @Test
    fun `floatDoubleColumnAdapter should handle negative values`() {
        // Given
        val floatValue = -123.456f
        
        // When
        val encoded = floatDoubleColumnAdapter.encode(floatValue)
        val decoded = floatDoubleColumnAdapter.decode(encoded)
        
        // Then
        assertEquals(floatValue, decoded, 0.001f)
    }
    
    // Int to Long adapter tests
    
    @Test
    fun `intLongColumnAdapter encode should convert int to long`() {
        // Given
        val intValue = 42
        
        // When
        val result = intLongColumnAdapter.encode(intValue)
        
        // Then
        assertEquals(42L, result)
    }
    
    @Test
    fun `intLongColumnAdapter decode should convert long to int`() {
        // Given
        val longValue = 42L
        
        // When
        val result = intLongColumnAdapter.decode(longValue)
        
        // Then
        assertEquals(42, result)
    }
    
    @Test
    fun `intLongColumnAdapter should handle zero`() {
        // Given
        val intValue = 0
        
        // When
        val encoded = intLongColumnAdapter.encode(intValue)
        val decoded = intLongColumnAdapter.decode(encoded)
        
        // Then
        assertEquals(0, decoded)
    }
    
    @Test
    fun `intLongColumnAdapter should handle negative values`() {
        // Given
        val intValue = -100
        
        // When
        val encoded = intLongColumnAdapter.encode(intValue)
        val decoded = intLongColumnAdapter.decode(encoded)
        
        // Then
        assertEquals(-100, decoded)
    }
    
    @Test
    fun `intLongColumnAdapter should handle max int value`() {
        // Given
        val intValue = Int.MAX_VALUE
        
        // When
        val encoded = intLongColumnAdapter.encode(intValue)
        val decoded = intLongColumnAdapter.decode(encoded)
        
        // Then
        assertEquals(Int.MAX_VALUE, decoded)
    }
    
    // Boolean to Long adapter tests
    
    @Test
    fun `booleanIntAdapter encode should convert true to 1`() {
        // Given
        val boolValue = true
        
        // When
        val result = booleanIntAdapter.encode(boolValue)
        
        // Then
        assertEquals(1L, result)
    }
    
    @Test
    fun `booleanIntAdapter encode should convert false to 0`() {
        // Given
        val boolValue = false
        
        // When
        val result = booleanIntAdapter.encode(boolValue)
        
        // Then
        assertEquals(0L, result)
    }
    
    @Test
    fun `booleanIntAdapter decode should convert 1 to true`() {
        // Given
        val longValue = 1L
        
        // When
        val result = booleanIntAdapter.decode(longValue)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `booleanIntAdapter decode should convert 0 to false`() {
        // Given
        val longValue = 0L
        
        // When
        val result = booleanIntAdapter.decode(longValue)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `booleanIntAdapter decode should convert any non-zero to true`() {
        // Given
        val longValue = 5L
        
        // When
        val result = booleanIntAdapter.decode(longValue)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `booleanIntAdapter decode should convert negative to true`() {
        // Given
        val longValue = -1L
        
        // When
        val result = booleanIntAdapter.decode(longValue)
        
        // Then
        assertTrue(result)
    }
    
    // Long converter tests
    
    @Test
    fun `longConverter encode should return same value`() {
        // Given
        val longValue = 123456789L
        
        // When
        val result = longConverter.encode(longValue)
        
        // Then
        assertEquals(123456789L, result)
    }
    
    @Test
    fun `longConverter decode should return same value`() {
        // Given
        val longValue = 123456789L
        
        // When
        val result = longConverter.decode(longValue)
        
        // Then
        assertEquals(123456789L, result)
    }
    
    @Test
    fun `longConverter should handle zero`() {
        // Given
        val longValue = 0L
        
        // When
        val encoded = longConverter.encode(longValue)
        val decoded = longConverter.decode(encoded)
        
        // Then
        assertEquals(0L, decoded)
    }
    
    @Test
    fun `longConverter should handle negative values`() {
        // Given
        val longValue = -999999L
        
        // When
        val encoded = longConverter.encode(longValue)
        val decoded = longConverter.decode(encoded)
        
        // Then
        assertEquals(-999999L, decoded)
    }
}
