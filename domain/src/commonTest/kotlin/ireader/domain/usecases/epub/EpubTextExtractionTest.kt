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
        assertEquals("This is paragraph 1", result[0].value)
        assertEquals("This is paragraph 2", result[1].value)
    }
    
    @Test
    fun `extractTextContent should extract text nodes between paragraph tags`() {
        // Arrange - This is the actual structure from the EPUB file
        val html = """
            <html>
            <body>
                <p class="calibre2"> </p>
                花式要抱抱？
                <p class="calibre3"> </p>
                傅清泽不好意思当着黎雨彤的面无视白芊芊，可是白芊芊的话又让他不知作何反应……
                <p class="calibre3"> </p>
            </body>
            </html>
        """.trimIndent()
        val doc = Ksoup.parse(html)
        
        // Act
        val result = extractTextContent(doc)
        
        // Assert
        assertTrue(result.size >= 2, "Should extract at least 2 text nodes")
        assertTrue(result.any { it.value.contains("花式要抱抱") }, "Should contain first text")
        assertTrue(result.any { it.value.contains("傅清泽不好意思") }, "Should contain second text")
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
        assertTrue(result.any { it.value.contains("Chapter Title") })
        assertTrue(result.any { it.value.contains("First paragraph") })
        assertTrue(result.any { it.value.contains("Text between paragraphs") })
        assertTrue(result.any { it.value.contains("Second paragraph") })
        assertTrue(result.any { it.value.contains("More text") })
        assertTrue(result.any { it.value.contains("Final text") })
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
        assertTrue(result.all { it.value.isNotBlank() })
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
        assertTrue(result[0].value.contains("First"))
        assertTrue(result[1].value.contains("Second"))
        assertTrue(result[2].value.contains("Third"))
        assertTrue(result[3].value.contains("Fourth"))
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
        assertTrue(result.any { it.value.contains("Paragraph in div") })
        assertTrue(result.any { it.value.contains("Text in div") })
        assertTrue(result.any { it.value.contains("Text in span") })
    }
    
    // Helper function that mirrors the actual implementation
    private fun extractTextContent(doc: com.fleeksoft.ksoup.nodes.Document): List<Text> {
        val textList = mutableListOf<Text>()
        
        // Get the body element
        val body = doc.body() ?: return emptyList()
        
        // Traverse all child nodes (including text nodes)
        fun traverseNodes(node: com.fleeksoft.ksoup.nodes.Node) {
            when (node) {
                is com.fleeksoft.ksoup.nodes.TextNode -> {
                    // Extract text from text nodes
                    val text = node.text().trim()
                    if (text.isNotBlank()) {
                        textList.add(Text(text))
                    }
                }
                is com.fleeksoft.ksoup.nodes.Element -> {
                    // For block-level elements, process their children
                    if (node.tagName() in listOf("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "blockquote", "pre", "li", "body")) {
                        node.childNodes().forEach { traverseNodes(it) }
                    } else {
                        // For inline elements, get their text content
                        val text = node.text().trim()
                        if (text.isNotBlank()) {
                            textList.add(Text(text))
                        }
                    }
                }
            }
        }
        
        // Start traversal from body
        body.childNodes().forEach { traverseNodes(it) }
        
        return textList.ifEmpty {
            // Fallback: get all text from body
            val bodyText = body.text().trim()
            if (bodyText.isNotBlank()) {
                listOf(Text(bodyText))
            } else {
                emptyList()
            }
        }
    }
}
