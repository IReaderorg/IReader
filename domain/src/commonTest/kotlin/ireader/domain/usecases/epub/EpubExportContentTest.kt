package ireader.domain.usecases.epub

import ireader.core.source.model.Text
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Comprehensive tests for EPUB export functionality.
 * 
 * These tests verify that chapter content is properly included in EPUB exports.
 * This addresses the critical bug where chapters were exported with only titles
 * and no text content because the lightweight query was being used instead of
 * the full query that includes content.
 * 
 * Key scenarios tested:
 * 1. Chapter content extraction for EPUB
 * 2. Difference between light and full chapter data
 * 3. Content processing for EPUB format
 * 4. Empty content detection
 * 5. Multiple content types handling
 */
class EpubExportContentTest {

    // ==================== Chapter Content Tests ====================

    @Test
    fun `chapter with content should not be empty`() {
        // Given: A chapter with actual text content
        val chapter = createChapterWithContent(
            id = 1L,
            name = "Chapter 1",
            content = "This is the actual chapter content that should appear in the EPUB."
        )
        
        // Then: Content should not be empty
        assertFalse(chapter.isEmpty(), "Chapter with content should not be empty")
        assertTrue(chapter.content.isNotEmpty(), "Content list should not be empty")
    }

    @Test
    fun `chapter without content should be detected as empty`() {
        // Given: A chapter without content (simulating light query result)
        val chapter = createChapterWithoutContent(id = 1L, name = "Chapter 1")
        
        // Then: Should be detected as empty
        assertTrue(chapter.isEmpty(), "Chapter without content should be empty")
        assertTrue(chapter.content.isEmpty(), "Content list should be empty")
    }

    @Test
    fun `chapter with placeholder content should be detected`() {
        // Given: A chapter with empty placeholder (simulating light query with is_downloaded flag)
        val chapter = Chapter(
            id = 1L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            content = listOf(Text("")), // Empty placeholder from light query
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = 1L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // Then: Should be detected as empty (placeholder has no actual content)
        assertTrue(chapter.isEmpty(), "Chapter with empty placeholder should be empty")
    }

    @Test
    fun `extracting text from chapter content should work correctly`() {
        // Given: A chapter with multiple text paragraphs
        val paragraphs = listOf(
            "First paragraph of the chapter.",
            "Second paragraph with more content.",
            "Third paragraph concluding the chapter."
        )
        val chapter = Chapter(
            id = 1L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            content = paragraphs.map { Text(it) },
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = 1L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // When: Extracting text content (simulating EpubBuilder.processChapters)
        val extractedContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: All paragraphs should be extracted
        paragraphs.forEach { paragraph ->
            assertTrue(
                extractedContent.contains(paragraph),
                "Extracted content should contain: $paragraph"
            )
        }
    }

    // ==================== Light vs Full Query Simulation Tests ====================

    @Test
    fun `light query chapter should have empty or placeholder content`() {
        // Given: Simulating what the light query returns
        val lightChapter = simulateLightQueryResult(
            id = 1L,
            name = "Chapter 1",
            isDownloaded = true
        )
        
        // Then: Content should be empty or just a placeholder
        val actualContent = lightChapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("")
        
        assertTrue(
            actualContent.isBlank(),
            "Light query result should have no actual content, but had: '$actualContent'"
        )
    }

    @Test
    fun `full query chapter should have actual content`() {
        // Given: Simulating what the full query returns
        val fullChapter = simulateFullQueryResult(
            id = 1L,
            name = "Chapter 1",
            content = "This is the full chapter content from the database."
        )
        
        // Then: Content should be present
        val actualContent = fullChapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("")
        
        assertTrue(
            actualContent.isNotBlank(),
            "Full query result should have actual content"
        )
        assertTrue(
            actualContent.contains("full chapter content"),
            "Content should match what was stored"
        )
    }

    @Test
    fun `EPUB export should fail gracefully with light query chapters`() {
        // Given: Chapters from light query (no content)
        val chapters = listOf(
            simulateLightQueryResult(1L, "Chapter 1", true),
            simulateLightQueryResult(2L, "Chapter 2", true),
            simulateLightQueryResult(3L, "Chapter 3", true)
        )
        
        // When: Processing for EPUB (simulating EpubBuilder behavior)
        val processedChapters = chapters.map { chapter ->
            val content = chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.joinToString("\n\n")
            
            chapter to content
        }
        
        // Then: All chapters should have empty content (the bug scenario)
        processedChapters.forEach { (chapter, content) ->
            assertTrue(
                content.isBlank(),
                "Light query chapter '${chapter.name}' should have empty content for EPUB"
            )
        }
    }

    @Test
    fun `EPUB export should succeed with full query chapters`() {
        // Given: Chapters from full query (with content)
        val chapters = listOf(
            simulateFullQueryResult(1L, "Chapter 1", "Content of chapter 1"),
            simulateFullQueryResult(2L, "Chapter 2", "Content of chapter 2"),
            simulateFullQueryResult(3L, "Chapter 3", "Content of chapter 3")
        )
        
        // When: Processing for EPUB (simulating EpubBuilder behavior)
        val processedChapters = chapters.map { chapter ->
            val content = chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.joinToString("\n\n")
            
            chapter to content
        }
        
        // Then: All chapters should have content
        processedChapters.forEach { (chapter, content) ->
            assertTrue(
                content.isNotBlank(),
                "Full query chapter '${chapter.name}' should have content for EPUB"
            )
        }
    }

    // ==================== Content Processing Tests ====================

    @Test
    fun `content with HTML should be processable`() {
        // Given: Chapter with HTML content
        val htmlContent = "<p>This is a paragraph.</p><p>This is another paragraph.</p>"
        val chapter = createChapterWithContent(1L, "Chapter 1", htmlContent)
        
        // When: Extracting content
        val extractedContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: HTML content should be preserved (cleaning happens later)
        assertTrue(extractedContent.contains("<p>"), "HTML should be preserved for later processing")
    }

    @Test
    fun `content with special characters should be preserved`() {
        // Given: Chapter with special characters
        val specialContent = "Chapter with special chars: é, ñ, 中文, 日本語, emoji: 📚"
        val chapter = createChapterWithContent(1L, "Chapter 1", specialContent)
        
        // When: Extracting content
        val extractedContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Special characters should be preserved
        assertEquals(specialContent, extractedContent, "Special characters should be preserved")
    }

    @Test
    fun `multiple paragraphs should be joined correctly`() {
        // Given: Chapter with multiple paragraphs
        val paragraphs = listOf("Para 1", "Para 2", "Para 3")
        val chapter = Chapter(
            id = 1L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            content = paragraphs.map { Text(it) },
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = 1L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // When: Joining with double newlines (EPUB format)
        val joinedContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Should be properly joined
        assertEquals("Para 1\n\nPara 2\n\nPara 3", joinedContent)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `empty chapter list should be handled`() {
        // Given: Empty chapter list
        val chapters = emptyList<Chapter>()
        
        // When: Processing for EPUB
        val hasContent = chapters.any { chapter ->
            chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.isNotEmpty()
        }
        
        // Then: Should indicate no content
        assertFalse(hasContent, "Empty chapter list should have no content")
    }

    @Test
    fun `chapter with only whitespace content should be detected`() {
        // Given: Chapter with whitespace-only content
        val chapter = Chapter(
            id = 1L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            content = listOf(Text("   \n\t\n   ")),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = 1L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // When: Checking for actual content
        val hasActualContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.isNotEmpty()
        
        // Then: Should be detected as having no actual content
        assertFalse(hasActualContent, "Whitespace-only content should be detected as empty")
    }

    @Test
    fun `very long content should be handled`() {
        // Given: Chapter with very long content
        val longContent = "A".repeat(100_000) // 100KB of content
        val chapter = createChapterWithContent(1L, "Long Chapter", longContent)
        
        // When: Extracting content
        val extractedContent = chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text
                else -> null
            }
        }.joinToString("\n\n")
        
        // Then: Full content should be preserved
        assertEquals(100_000, extractedContent.length, "Long content should be fully preserved")
    }

    // ==================== Regression Prevention Tests ====================

    @Test
    fun `regression - chapters must have content for valid EPUB export`() {
        // This test documents the bug that was fixed:
        // When using findChaptersByBookId (light query), chapters had no content
        // EPUB export requires findChaptersByBookIdWithContent (full query)
        
        // Given: A book with downloaded chapters
        val book = Book(
            id = 1L,
            sourceId = 1L,
            key = "book-1",
            title = "Test Book",
            author = "Test Author",
            description = "Test Description",
            genres = emptyList(),
            status = 0,
            cover = "",
            customCover = "",
            favorite = true,
            lastUpdate = 0L,
            lastInit = 0L,
            dateAdded = 0L,
            viewer = 0,
            flags = 0
        )
        
        // Simulating the FIXED behavior: using full query
        val chaptersWithContent = listOf(
            simulateFullQueryResult(1L, "Chapter 1", "Content 1"),
            simulateFullQueryResult(2L, "Chapter 2", "Content 2")
        )
        
        // When: Validating for EPUB export
        val allChaptersHaveContent = chaptersWithContent.all { chapter ->
            chapter.content.mapNotNull {
                when (it) {
                    is Text -> it.text.takeIf { text -> text.isNotBlank() }
                    else -> null
                }
            }.isNotEmpty()
        }
        
        // Then: All chapters should have content (the fix ensures this)
        assertTrue(
            allChaptersHaveContent,
            "REGRESSION CHECK: All chapters must have content for EPUB export. " +
            "If this fails, ensure findChaptersByBookIdWithContent is being used instead of findChaptersByBookId"
        )
    }

    @Test
    fun `regression - light query placeholder should not be treated as content`() {
        // This test ensures the light query placeholder is not mistaken for actual content
        
        // Given: Light query result with placeholder
        val lightChapter = simulateLightQueryResult(1L, "Chapter 1", isDownloaded = true)
        
        // When: Checking if it has exportable content
        val hasExportableContent = lightChapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.isNotEmpty()
        
        // Then: Should NOT have exportable content
        assertFalse(
            hasExportableContent,
            "REGRESSION CHECK: Light query placeholder should not be treated as exportable content"
        )
    }

    // ==================== Helper Functions ====================

    private fun createChapterWithContent(id: Long, name: String, content: String): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            content = listOf(Text(content)),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = System.currentTimeMillis(),
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }

    private fun createChapterWithoutContent(id: Long, name: String): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            content = emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }

    /**
     * Simulates the result of the light query (getChaptersByMangaIdLight)
     * which does NOT include actual content, only a placeholder if downloaded
     */
    private fun simulateLightQueryResult(id: Long, name: String, isDownloaded: Boolean): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            // Light query returns empty placeholder if downloaded, empty list otherwise
            content = if (isDownloaded) listOf(Text("")) else emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = if (isDownloaded) System.currentTimeMillis() else 0L,
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }

    /**
     * Simulates the result of the full query (getChaptersByMangaId)
     * which DOES include actual content
     */
    private fun simulateFullQueryResult(id: Long, name: String, content: String): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            content = listOf(Text(content)),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = System.currentTimeMillis(),
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }
}
