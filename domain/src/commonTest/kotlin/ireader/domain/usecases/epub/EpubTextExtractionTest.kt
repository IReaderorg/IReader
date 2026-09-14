package ireader.domain.usecases.epub

import com.fleeksoft.ksoup.Ksoup
import ireader.core.source.model.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for EPUB text extraction functionality
 * Following TDD methodology: Write tests first, then implement
 */
class EpubTextExtractionTest {
    
    @Test
    fun `extractTextContent should extract text from paragraph tags`() {
        // Arrange
        val html = """
            <html>
            <body>
                <p>This is paragraph 1</p>
                <p>This is paragraph 2</p>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals("This is paragraph 1", result[0].text)
        assertEquals("This is paragraph 2", result[1].text)
    }
    
    @Test
    fun `extractTextContent should extract text nodes between paragraph tags`() {
        // Arrange - This is the actual structure from the EPUB file
        val html = """
            <html>
            <body>
                <p class="calibre2"> </p>
                èŠ±å¼è¦æŠ±æŠ±ï¼Ÿ
                <p class="calibre3"> </p>
                å‚…æ¸…æ³½ä¸å¥½æ„æ€å½“ç€é»Žé›¨å½¤çš„é¢æ— è§†ç™½èŠŠèŠŠï¼Œå¯æ˜¯ç™½èŠŠèŠŠçš„è¯åˆè®©ä»–ä¸çŸ¥ä½œä½•ååº”â€¦â€¦
                <p class="calibre3"> </p>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertTrue(result.size >= 2, "Should extract at least 2 text nodes")
        assertTrue(result.any { it.text.contains("èŠ±å¼è¦æŠ±æŠ±") }, "Should contain first text")
        assertTrue(result.any { it.text.contains("å‚…æ¸…æ³½ä¸å¥½æ„æ€") }, "Should contain second text")
    }
    
    @Test
    fun `extractTextContent should handle mixed content with paragraphs and text nodes`() {
        // Arrange
        val html = """
            <html>
            <body>
                <h1>Chapter Title</h1>
                <p>First paragraph</p>
                Text between paragraphs
                <p>Second paragraph</p>
                More text
                <p> </p>
                Final text
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertTrue(result.size >= 5, "Should extract all text content")
        assertTrue(result.any { it.text.contains("Chapter Title") })
        assertTrue(result.any { it.text.contains("First paragraph") })
        assertTrue(result.any { it.text.contains("Text between paragraphs") })
        assertTrue(result.any { it.text.contains("Second paragraph") })
        assertTrue(result.any { it.text.contains("More text") })
        assertTrue(result.any { it.text.contains("Final text") })
    }
    
    @Test
    fun `extractTextContent should skip empty or whitespace-only text`() {
        // Arrange
        val html = """
            <html>
            <body>
                <p>   </p>
                Valid text
                <p></p>
                
                Another valid text
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.text.isNotBlank() })
    }
    
    @Test
    fun `extractTextContent should preserve text order`() {
        // Arrange
        val html = """
            <html>
            <body>
                <p>First</p>
                Second
                <p>Third</p>
                Fourth
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(4, result.size)
        assertTrue(result[0].text.contains("First"))
        assertTrue(result[1].text.contains("Second"))
        assertTrue(result[2].text.contains("Third"))
        assertTrue(result[3].text.contains("Fourth"))
    }
    
    @Test
    fun `extractTextContent should handle nested elements correctly`() {
        // Arrange
        val html = """
            <html>
            <body>
                <div>
                    <p>Paragraph in div</p>
                    Text in div
                    <span>Text in span</span>
                </div>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertTrue(result.isNotEmpty())
        assertTrue(result.any { it.text.contains("Paragraph in div") })
        assertTrue(result.any { it.text.contains("Text in div") })
        assertTrue(result.any { it.text.contains("Text in span") })
    }
    
    @Test
    fun `extractTextContent should preserve word spacing with inline formatting tags`() {
        // Arrange
        val html = """
            <html>
            <body>
                <p>This is <i>italic</i> and <b>bold</b> text with <span>span words</span>.</p>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(1, result.size, "Should be 1 paragraph, not split across inline tags")
        assertEquals("This is italic and bold text with span words.", result[0].text)
    }

    @Test
    fun `extractTextContent should split paragraphs on br tags`() {
        // Arrange
        val html = """
            <html>
            <body>
                <p>Line 1<br/>Line 2<br/><br/>Line 3</p>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(3, result.size)
        assertEquals("Line 1", result[0].text)
        assertEquals("Line 2", result[1].text)
        assertEquals("Line 3", result[2].text)
    }

    @Test
    fun `extractTextContent should handle div with br breaks and no p tags`() {
        // Arrange
        val html = """
            <html>
            <body>
                <div class="content">
                    Paragraph 1 text here.<br><br>
                    Paragraph 2 text here.
                </div>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals("Paragraph 1 text here.", result[0].text)
        assertEquals("Paragraph 2 text here.", result[1].text)
    }

    @Test
    fun `extractTextContent should handle ChatGPT styled epub with quotes and spans`() {
        // Arrange
        val html = """
            <html xmlns="http://www.w3.org/1999/xhtml">
            <body>
                <div class="chapter">
                    <p>He turned to her. <span style="font-style: italic;">“Are you ready?”</span> he asked.</p>
                    <p>“I am,” she replied firmly.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals("He turned to her. “Are you ready?” he asked.", result[0].text)
        assertEquals("“I am,” she replied firmly.", result[1].text)
    }

    @Test
    fun `extractTextContent should preserve preformatted text lines`() {
        // Arrange
        val html = """
            <html>
            <body>
                <pre>Line 1
Line 2
Line 3</pre>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertEquals(3, result.size)
        assertEquals("Line 1", result[0].text)
        assertEquals("Line 2", result[1].text)
        assertEquals("Line 3", result[2].text)
    }

    private fun extractTextContent(doc: com.fleeksoft.ksoup.nodes.Document): List<Text> {
        return EpubTextExtractor.extractTextContent(doc)
    }
}

