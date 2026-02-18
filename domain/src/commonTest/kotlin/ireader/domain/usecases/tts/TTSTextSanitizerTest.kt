package ireader.domain.usecases.tts

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for TTSTextSanitizer - ensures brackets and special characters are removed
 * before text is sent to TTS engines.
 * 
 * TDD: RED phase - these tests will fail until we implement the sanitizer
 */
class TTSTextSanitizerTest {
    
    private val sanitizer = TTSTextSanitizer()
    
    @Test
    fun `sanitize should remove round brackets and their content`() {
        // Arrange
        val text = "Hello (this is a note) world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should remove square brackets and their content`() {
        // Arrange
        val text = "Hello [translator note] world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should remove curly braces and their content`() {
        // Arrange
        val text = "Hello {some annotation} world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should remove asterisks`() {
        // Arrange
        val text = "Hello * world * test"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world test", result)
    }
    
    @Test
    fun `sanitize should remove multiple types of brackets in same text`() {
        // Arrange
        val text = "Hello (note) world [TL: translation] test {annotation} end"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world test end", result)
    }
    
    @Test
    fun `sanitize should handle nested brackets`() {
        // Arrange
        val text = "Hello (outer (inner) note) world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should preserve text without brackets`() {
        // Arrange
        val text = "Hello world, this is a test."
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world, this is a test.", result)
    }
    
    @Test
    fun `sanitize should handle empty string`() {
        // Arrange
        val text = ""
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("", result)
    }
    
    @Test
    fun `sanitize should handle text with only brackets`() {
        // Arrange
        val text = "(note) [TL] {annotation}"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("", result)
    }
    
    @Test
    fun `sanitize should normalize whitespace after removal`() {
        // Arrange
        val text = "Hello   (note)   world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should handle brackets at start and end`() {
        // Arrange
        val text = "(start note) Hello world [end note]"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should remove angle brackets and their content`() {
        // Arrange
        val text = "Hello <some tag> world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should handle multiple asterisks in a row`() {
        // Arrange
        val text = "Hello *** world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should handle mixed brackets and asterisks`() {
        // Arrange
        val text = "Hello * (note) * [TL] world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should preserve punctuation`() {
        // Arrange
        val text = "Hello, world! How are you? I'm fine."
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello, world! How are you? I'm fine.", result)
    }
    
    @Test
    fun `sanitize should handle unmatched opening bracket`() {
        // Arrange
        val text = "Hello (world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        // Should remove from opening bracket to end
        assertEquals("Hello", result)
    }
    
    @Test
    fun `sanitize should handle unmatched closing bracket`() {
        // Arrange
        val text = "Hello world)"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        // Should just remove the closing bracket
        assertEquals("Hello world", result)
    }
}
