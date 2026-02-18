package ireader.domain.services.tts_service.v2

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.usecases.tts.TTSTextSanitizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for TTSContentLoader with TTSTextSanitizer.
 * 
 * Verifies that brackets and special characters are removed from
 * chapter content before being sent to TTS engines.
 */
class TTSContentLoaderSanitizerTest {
    
    private val sanitizer = TTSTextSanitizer()
    
    @Test
    fun `parseContent should remove brackets from text pages`() {
        // Arrange
        val pages = listOf(
            Text("Hello (note) world"),
            Text("Test [TL: translation] content"),
            Text("More {annotation} text")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(3, result.size)
        assertEquals("Hello world", result[0])
        assertEquals("Test content", result[1])
        assertEquals("More text", result[2])
    }
    
    @Test
    fun `parseContent should remove asterisks from text`() {
        // Arrange
        val pages = listOf(
            Text("Hello * world * test")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello world test", result[0])
    }
    
    @Test
    fun `parseContent should handle mixed brackets and asterisks`() {
        // Arrange
        val pages = listOf(
            Text("Hello * (note) * [TL] world")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello world", result[0])
    }
    
    @Test
    fun `parseContent should remove empty paragraphs after sanitization`() {
        // Arrange
        val pages = listOf(
            Text("(only brackets)"),
            Text("Hello world"),
            Text("[only translation note]")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello world", result[0])
    }
    
    @Test
    fun `parseContent should handle HTML with brackets`() {
        // Arrange
        val pages = listOf(
            Text("<p>Hello (note) world</p>"),
            Text("<br/>Test [TL] content<br/>")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals("Hello world", result[0])
        assertEquals("Test content", result[1])
    }
    
    @Test
    fun `parseContent should preserve regular punctuation`() {
        // Arrange
        val pages = listOf(
            Text("Hello, world! How are you? I'm fine.")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello, world! How are you? I'm fine.", result[0])
    }
    
    @Test
    fun `parseContent should handle nested brackets`() {
        // Arrange
        val pages = listOf(
            Text("Hello (outer (inner) note) world")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello world", result[0])
    }
    
    @Test
    fun `parseContent should normalize whitespace after removal`() {
        // Arrange
        val pages = listOf(
            Text("Hello   (note)   world")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals("Hello world", result[0])
    }
    
    @Test
    fun `parseContent should handle multiple paragraphs with brackets`() {
        // Arrange
        val pages = listOf(
            Text("First (note) paragraph\nSecond [TL] paragraph\nThird {annotation} paragraph")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        assertEquals(3, result.size)
        assertEquals("First paragraph", result[0])
        assertEquals("Second paragraph", result[1])
        assertEquals("Third paragraph", result[2])
    }
    
    @Test
    fun `parseContent should not contain any brackets in output`() {
        // Arrange
        val pages = listOf(
            Text("Text with (round) [square] {curly} <angle> brackets"),
            Text("More * asterisks * here")
        )
        
        // Act
        val result = parseContentWithSanitizer(pages)
        
        // Assert
        result.forEach { paragraph ->
            assertFalse(paragraph.contains("("), "Should not contain (")
            assertFalse(paragraph.contains(")"), "Should not contain )")
            assertFalse(paragraph.contains("["), "Should not contain [")
            assertFalse(paragraph.contains("]"), "Should not contain ]")
            assertFalse(paragraph.contains("{"), "Should not contain {")
            assertFalse(paragraph.contains("}"), "Should not contain }")
            assertFalse(paragraph.contains("<"), "Should not contain <")
            assertFalse(paragraph.contains(">"), "Should not contain >")
            assertFalse(paragraph.contains("*"), "Should not contain *")
        }
    }
    
    /**
     * Helper method that simulates the parseContent logic with sanitizer
     */
    private fun parseContentWithSanitizer(pages: List<Page>): List<String> {
        val textContent = pages
            .filterIsInstance<Text>()
            .map { it.text }
            .filter { it.isNotBlank() }
        
        if (textContent.isEmpty()) {
            return emptyList()
        }
        
        val paragraphs = textContent.flatMap { text ->
            cleanAndSplitText(text)
        }
        
        // Sanitize for TTS
        return sanitizer.sanitizeList(paragraphs)
    }
    
    /**
     * Helper method that simulates HTML cleaning and splitting
     */
    private fun cleanAndSplitText(text: String): List<String> {
        val cleanContent = text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
        
        return cleanContent
            .split(Regex("\n+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
