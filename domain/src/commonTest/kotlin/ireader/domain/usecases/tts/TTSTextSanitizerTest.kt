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
    fun `sanitize should preserve text inside round brackets`() {
        // Arrange
        val text = "Hello (this is a thought) world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello this is a thought world", result)
    }
    
    @Test
    fun `sanitize should preserve text inside square brackets`() {
        // Arrange
        val text = "Hello [translator note] world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello translator note world", result)
    }
    
    @Test
    fun `sanitize should preserve RPG skill and status window brackets`() {
        // Arrange
        val text = "[Skill: Fireball Level 3] [Status Window]"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Skill: Fireball Level 3 Status Window", result)
    }
    
    @Test
    fun `sanitize should preserve text inside curly braces`() {
        // Arrange
        val text = "Hello {some annotation} world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello some annotation world", result)
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
    fun `sanitize should preserve multiple types of brackets in same text`() {
        // Arrange
        val text = "Hello (note) world [TL: translation] test {annotation} end"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello note world TL: translation test annotation end", result)
    }
    
    @Test
    fun `sanitize should handle nested brackets while preserving text`() {
        // Arrange
        val text = "Hello (outer (inner) note) world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello outer inner note world", result)
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
    fun `sanitize should preserve text in bracket-only strings`() {
        // Arrange
        val text = "(note) [TL] {annotation}"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("note TL annotation", result)
    }
    
    @Test
    fun `sanitize should normalize whitespace after removal`() {
        // Arrange
        val text = "Hello   (note)   world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello note world", result)
    }
    
    @Test
    fun `sanitize should handle brackets at start and end`() {
        // Arrange
        val text = "(start note) Hello world [end note]"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("start note Hello world end note", result)
    }
    
    @Test
    fun `sanitize should preserve text inside angle brackets`() {
        // Arrange
        val text = "Hello <some tag> world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello some tag world", result)
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
        assertEquals("Hello note TL world", result)
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
    fun `sanitize should preserve text following unmatched opening bracket`() {
        // Arrange
        val text = "Hello (world"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
    
    @Test
    fun `sanitize should handle unmatched closing bracket`() {
        // Arrange
        val text = "Hello world)"
        
        // Act
        val result = sanitizer.sanitize(text)
        
        // Assert
        assertEquals("Hello world", result)
    }
}
