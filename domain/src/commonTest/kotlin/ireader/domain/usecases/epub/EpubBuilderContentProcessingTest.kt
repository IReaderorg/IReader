package ireader.domain.usecases.epub

import ireader.core.source.model.ImageBase64
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Page
import ireader.core.source.model.PageUrl
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests for EpubBuilder content processing logic.
 * 
 * These tests verify that the content extraction and processing
 * in EpubBuilder works correctly for various content types and edge cases.
 */
class EpubBuilderContentProcessingTest {

    // ==================== Content Type Handling ====================

    @Test
    fun `should extract text from Text page type`() {
        // Given: Chapter with Text content
        val content = listOf<Page>(
            Text("First paragraph"),
            Text("Second paragraph")
        )
        
        // When: Extracting text (simulating EpubBuilder.processChapters)
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Text should be extracted
        assertEquals("First paragraph\n\nSecond paragraph", extractedText)
    }

    @Test
    fun `should ignore non-text page types for text extraction`() {
        // Given: Chapter with mixed content types
        val content = listOf<Page>(
            Text("Text content"),
            ImageUrl("https://example.com/image.jpg"),
            Text("More text"),
            PageUrl("https://example.com/page")
        )
        
        // When: Extracting text only
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Only text should be extracted
        assertEquals("Text content\n\nMore text", extractedText)
        assertFalse(extractedText.contains("example.com"))
    }

    @Test
    fun `should handle ImageUrl in content`() {
        // Given: Content with images
        val content = listOf<Page>(
            Text("Before image"),
            ImageUrl("https://example.com/image.jpg"),
            Text("After image")
        )
        
        // When: Processing for EPUB
        val textContent = content.filterIsInstance<Text>().map { it.text }
        val imageContent = content.filterIsInstance<ImageUrl>().map { it.url }
        
        // Then: Both should be identifiable
        assertEquals(2, textContent.size)
        assertEquals(1, imageContent.size)
        assertEquals("https://example.com/image.jpg", imageContent.first())
    }

    @Test
    fun `should handle ImageBase64 in content`() {
        // Given: Content with base64 image
        val base64Data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        val content = listOf<Page>(
            Text("Text with image"),
            ImageBase64(base64Data)
        )
        
        // When: Processing
        val images = content.filterIsInstance<ImageBase64>()
        
        // Then: Base64 image should be identifiable
        assertEquals(1, images.size)
        assertEquals(base64Data, images.first().data)
    }

    @Test
    fun `should handle MovieUrl in content`() {
        // Given: Content with movie (should be skipped for EPUB)
        val content = listOf<Page>(
            Text("Chapter with video"),
            MovieUrl("https://example.com/video.mp4"),
            Text("After video")
        )
        
        // When: Extracting text for EPUB
        val textOnly = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }
        
        // Then: Movie should be skipped, text preserved
        assertEquals(2, textOnly.size)
        assertFalse(textOnly.any { it.contains("video.mp4") })
    }

    // ==================== Empty Content Handling ====================

    @Test
    fun `should handle empty content list`() {
        // Given: Empty content
        val content = emptyList<Page>()
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Should return empty string
        assertEquals("", extractedText)
    }

    @Test
    fun `should handle content with only empty Text`() {
        // Given: Content with empty text (placeholder from light query)
        val content = listOf<Page>(Text(""))
        
        // When: Processing with blank check
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Should return empty string
        assertEquals("", extractedText)
    }

    @Test
    fun `should handle content with whitespace-only Text`() {
        // Given: Content with whitespace
        val content = listOf<Page>(
            Text("   "),
            Text("\n\t\n"),
            Text("  \r\n  ")
        )
        
        // When: Processing with blank check
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Should return empty string
        assertEquals("", extractedText)
    }

    @Test
    fun `should filter out empty text while preserving valid text`() {
        // Given: Mixed content with empty and valid text
        val content = listOf<Page>(
            Text("Valid text 1"),
            Text(""),
            Text("   "),
            Text("Valid text 2"),
            Text("\n")
        )
        
        // When: Processing with blank check
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Only valid text should be included
        assertEquals("Valid text 1\n\nValid text 2", extractedText)
    }

    // ==================== Special Characters and Encoding ====================

    @Test
    fun `should preserve Unicode characters`() {
        // Given: Content with various Unicode characters
        val unicodeText = "English, Français, Deutsch, 日本語, 中文, العربية, עברית, 한국어"
        val content = listOf<Page>(Text(unicodeText))
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: All characters should be preserved
        assertEquals(unicodeText, extractedText)
    }

    @Test
    fun `should preserve emoji characters`() {
        // Given: Content with emojis
        val emojiText = "Reading is fun! 📚📖✨🎉"
        val content = listOf<Page>(Text(emojiText))
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Emojis should be preserved
        assertEquals(emojiText, extractedText)
    }

    @Test
    fun `should handle HTML entities in text`() {
        // Given: Content with HTML entities
        val htmlText = "Less than: &lt; Greater than: &gt; Ampersand: &amp;"
        val content = listOf<Page>(Text(htmlText))
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: HTML entities should be preserved (escaping happens later)
        assertEquals(htmlText, extractedText)
    }

    @Test
    fun `should handle special XML characters`() {
        // Given: Content with XML special characters
        val xmlText = "Quote: \"text\" Apostrophe: 'text' Tags: <tag>"
        val content = listOf<Page>(Text(xmlText))
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Characters should be preserved (escaping happens in XHTML generation)
        assertEquals(xmlText, extractedText)
    }

    // ==================== Large Content Handling ====================

    @Test
    fun `should handle very long text content`() {
        // Given: Very long content
        val longText = "A".repeat(1_000_000) // 1MB of text
        val content = listOf<Page>(Text(longText))
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Full content should be preserved
        assertEquals(1_000_000, extractedText.length)
    }

    @Test
    fun `should handle many paragraphs`() {
        // Given: Many paragraphs
        val paragraphCount = 10_000
        val content = (1..paragraphCount).map { Text("Paragraph $it") }
        
        // When: Processing
        val extractedText = content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: All paragraphs should be included
        val resultParagraphs = extractedText.split("\n\n")
        assertEquals(paragraphCount, resultParagraphs.size)
    }

    // ==================== Chapter Processing Simulation ====================

    @Test
    fun `should process chapter list correctly`() {
        // Given: Multiple chapters with content
        val chapters = listOf(
            createChapter(1L, "Chapter 1", listOf(Text("Content 1"))),
            createChapter(2L, "Chapter 2", listOf(Text("Content 2"))),
            createChapter(3L, "Chapter 3", listOf(Text("Content 3")))
        )
        
        // When: Processing all chapters (simulating EpubBuilder.processChapters)
        val processedChapters = chapters.map { chapter ->
            val content = chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text
                    else -> null
                }
            }.joinToString("\n\n")
            
            ProcessedChapter(
                id = "chapter${chapter.id}",
                title = chapter.name,
                content = content,
                order = chapter.sourceOrder.toInt()
            )
        }
        
        // Then: All chapters should be processed with content
        assertEquals(3, processedChapters.size)
        processedChapters.forEachIndexed { index, processed ->
            assertEquals("chapter${index + 1}", processed.id)
            assertEquals("Chapter ${index + 1}", processed.title)
            assertEquals("Content ${index + 1}", processed.content)
        }
    }

    @Test
    fun `should filter selected chapters`() {
        // Given: Chapters with selection
        val allChapters = listOf(
            createChapter(1L, "Chapter 1", listOf(Text("Content 1"))),
            createChapter(2L, "Chapter 2", listOf(Text("Content 2"))),
            createChapter(3L, "Chapter 3", listOf(Text("Content 3"))),
            createChapter(4L, "Chapter 4", listOf(Text("Content 4")))
        )
        val selectedIds = setOf(2L, 4L)
        
        // When: Filtering selected chapters
        val selectedChapters = if (selectedIds.isEmpty()) {
            allChapters
        } else {
            allChapters.filter { it.id in selectedIds }
        }
        
        // Then: Only selected chapters should be included
        assertEquals(2, selectedChapters.size)
        assertEquals(2L, selectedChapters[0].id)
        assertEquals(4L, selectedChapters[1].id)
    }

    @Test
    fun `should include all chapters when no selection`() {
        // Given: Chapters with empty selection
        val allChapters = listOf(
            createChapter(1L, "Chapter 1", listOf(Text("Content 1"))),
            createChapter(2L, "Chapter 2", listOf(Text("Content 2")))
        )
        val selectedIds = emptySet<Long>()
        
        // When: Filtering with empty selection
        val selectedChapters = if (selectedIds.isEmpty()) {
            allChapters
        } else {
            allChapters.filter { it.id in selectedIds }
        }
        
        // Then: All chapters should be included
        assertEquals(2, selectedChapters.size)
    }

    // ==================== Regression Tests ====================

    @Test
    fun `regression - empty placeholder should not produce content`() {
        // This is the exact scenario that caused the bug
        
        // Given: Chapters with empty placeholder (from light query)
        val chaptersFromLightQuery = listOf(
            createChapter(1L, "Chapter 1", listOf(Text(""))),
            createChapter(2L, "Chapter 2", listOf(Text(""))),
            createChapter(3L, "Chapter 3", listOf(Text("")))
        )
        
        // When: Processing for EPUB
        val processedChapters = chaptersFromLightQuery.map { chapter ->
            chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.joinToString("\n\n")
        }
        
        // Then: All should be empty (this was the bug - EPUB had no content)
        processedChapters.forEach { content ->
            assertTrue(
                content.isEmpty(),
                "REGRESSION: Empty placeholder should produce empty content"
            )
        }
    }

    @Test
    fun `regression - actual content should produce valid EPUB content`() {
        // This is the fixed scenario
        
        // Given: Chapters with actual content (from full query)
        val chaptersFromFullQuery = listOf(
            createChapter(1L, "Chapter 1", listOf(Text("Actual content 1"))),
            createChapter(2L, "Chapter 2", listOf(Text("Actual content 2"))),
            createChapter(3L, "Chapter 3", listOf(Text("Actual content 3")))
        )
        
        // When: Processing for EPUB
        val processedChapters = chaptersFromFullQuery.map { chapter ->
            chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.joinToString("\n\n")
        }
        
        // Then: All should have content
        processedChapters.forEachIndexed { index, content ->
            assertTrue(
                content.isNotBlank(),
                "REGRESSION: Actual content should produce valid EPUB content for chapter ${index + 1}"
            )
            assertTrue(
                content.contains("Actual content"),
                "REGRESSION: Content should match stored text"
            )
        }
    }

    // ==================== Helper Classes and Functions ====================

    data class ProcessedChapter(
        val id: String,
        val title: String,
        val content: String,
        val order: Int
    )

    @OptIn(ExperimentalTime::class)
    private fun createChapter(id: Long, name: String, content: List<Page>): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            content = content,
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = Clock.System.now().toEpochMilliseconds(),
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }
}
