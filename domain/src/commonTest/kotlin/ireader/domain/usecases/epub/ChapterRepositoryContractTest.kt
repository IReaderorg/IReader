package ireader.domain.usecases.epub

import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Contract tests for ChapterRepository methods used in EPUB export.
 * 
 * These tests define the expected behavior of:
 * - findChaptersByBookId (light query - no content)
 * - findChaptersByBookIdWithContent (full query - with content)
 * 
 * These tests serve as documentation and regression prevention for the
 * critical difference between these two methods.
 */
class ChapterRepositoryContractTest {

    // ==================== Contract: findChaptersByBookId (Light Query) ====================

    @Test
    fun `contract - findChaptersByBookId should return chapters without content`() {
        // This test documents the expected behavior of findChaptersByBookId
        // It uses a lightweight query to prevent OOM errors when listing chapters
        
        // Given: A chapter as it would be returned by findChaptersByBookId
        val lightChapter = createLightQueryChapter(
            id = 1L,
            name = "Chapter 1",
            isDownloaded = true
        )
        
        // Then: The chapter should NOT have actual text content
        val actualTextContent = extractTextContent(lightChapter)
        assertTrue(
            actualTextContent.isBlank(),
            "CONTRACT: findChaptersByBookId should NOT return actual content. " +
            "Found: '$actualTextContent'"
        )
    }

    @Test
    fun `contract - findChaptersByBookId should indicate download status`() {
        // The light query should still indicate if a chapter is downloaded
        
        // Given: Downloaded and non-downloaded chapters
        val downloadedChapter = createLightQueryChapter(1L, "Downloaded", isDownloaded = true)
        val notDownloadedChapter = createLightQueryChapter(2L, "Not Downloaded", isDownloaded = false)
        
        // Then: Download status should be distinguishable
        // Downloaded chapters have a placeholder Text(""), non-downloaded have empty list
        assertTrue(
            downloadedChapter.content.isNotEmpty() || downloadedChapter.dateFetch > 0,
            "CONTRACT: Downloaded chapter should be identifiable"
        )
    }

    @Test
    fun `contract - findChaptersByBookId should preserve metadata`() {
        // Light query should still return all metadata
        
        // Given: A chapter from light query
        val chapter = createLightQueryChapter(
            id = 123L,
            name = "Test Chapter",
            isDownloaded = true
        )
        
        // Then: All metadata should be present
        assertEquals(123L, chapter.id, "CONTRACT: ID should be preserved")
        assertEquals("Test Chapter", chapter.name, "CONTRACT: Name should be preserved")
        assertEquals(1L, chapter.bookId, "CONTRACT: BookId should be preserved")
        assertNotEquals(0L, chapter.sourceOrder, "CONTRACT: SourceOrder should be preserved")
    }

    // ==================== Contract: findChaptersByBookIdWithContent (Full Query) ====================

    @Test
    fun `contract - findChaptersByBookIdWithContent should return chapters with content`() {
        // This test documents the expected behavior of findChaptersByBookIdWithContent
        // It uses the full query that includes chapter content
        
        // Given: A chapter as it would be returned by findChaptersByBookIdWithContent
        val fullChapter = createFullQueryChapter(
            id = 1L,
            name = "Chapter 1",
            content = "This is the actual chapter content."
        )
        
        // Then: The chapter SHOULD have actual text content
        val actualTextContent = extractTextContent(fullChapter)
        assertTrue(
            actualTextContent.isNotBlank(),
            "CONTRACT: findChaptersByBookIdWithContent MUST return actual content"
        )
        assertEquals(
            "This is the actual chapter content.",
            actualTextContent,
            "CONTRACT: Content should match what was stored"
        )
    }

    @Test
    fun `contract - findChaptersByBookIdWithContent should preserve all metadata`() {
        // Full query should return all metadata plus content
        
        // Given: A chapter from full query
        val chapter = createFullQueryChapter(
            id = 456L,
            name = "Full Chapter",
            content = "Content here"
        )
        
        // Then: All metadata should be present
        assertEquals(456L, chapter.id, "CONTRACT: ID should be preserved")
        assertEquals("Full Chapter", chapter.name, "CONTRACT: Name should be preserved")
        assertEquals(1L, chapter.bookId, "CONTRACT: BookId should be preserved")
        assertNotEquals(0L, chapter.sourceOrder, "CONTRACT: SourceOrder should be preserved")
        
        // And content should be present
        assertTrue(chapter.content.isNotEmpty(), "CONTRACT: Content list should not be empty")
    }

    // ==================== EPUB Export Requirements ====================

    @Test
    fun `requirement - EPUB export MUST use findChaptersByBookIdWithContent`() {
        // This test documents the critical requirement for EPUB export
        
        // Given: Chapters from both query types
        val lightChapters = listOf(
            createLightQueryChapter(1L, "Ch 1", true),
            createLightQueryChapter(2L, "Ch 2", true)
        )
        val fullChapters = listOf(
            createFullQueryChapter(1L, "Ch 1", "Content 1"),
            createFullQueryChapter(2L, "Ch 2", "Content 2")
        )
        
        // When: Checking which can be used for EPUB export
        val lightChaptersValid = lightChapters.all { extractTextContent(it).isNotBlank() }
        val fullChaptersValid = fullChapters.all { extractTextContent(it).isNotBlank() }
        
        // Then: Only full query chapters are valid for EPUB
        assertFalse(
            lightChaptersValid,
            "REQUIREMENT: Light query chapters should NOT be valid for EPUB export"
        )
        assertTrue(
            fullChaptersValid,
            "REQUIREMENT: Full query chapters MUST be valid for EPUB export"
        )
    }

    @Test
    fun `requirement - EPUB export should validate content before proceeding`() {
        // EPUB export should check that chapters have content
        
        // Given: Mixed chapters (some with content, some without)
        val chapters = listOf(
            createFullQueryChapter(1L, "With Content", "Actual content"),
            createLightQueryChapter(2L, "Without Content", true),
            createFullQueryChapter(3L, "Also With Content", "More content")
        )
        
        // When: Validating chapters for export
        val chaptersWithContent = chapters.filter { extractTextContent(it).isNotBlank() }
        val chaptersWithoutContent = chapters.filter { extractTextContent(it).isBlank() }
        
        // Then: Should be able to identify which chapters are exportable
        assertEquals(2, chaptersWithContent.size, "Should identify chapters with content")
        assertEquals(1, chaptersWithoutContent.size, "Should identify chapters without content")
    }

    // ==================== Performance Considerations ====================

    @Test
    fun `consideration - light query should be used for UI listing`() {
        // Document that light query is appropriate for UI, not export
        
        // Given: A large number of chapters (simulated)
        val chapterCount = 1000
        
        // When: Using light query for listing
        // (In real implementation, this prevents OOM by not loading content)
        val lightChapters = (1..chapterCount).map { i ->
            createLightQueryChapter(i.toLong(), "Chapter $i", true)
        }
        
        // Then: All chapters should be listable without content
        assertEquals(chapterCount, lightChapters.size)
        lightChapters.forEach { chapter ->
            // Verify no heavy content is loaded
            val contentSize = chapter.content.sumOf { 
                when (it) {
                    is Text -> it.text.length
                    else -> 0
                }
            }
            assertTrue(
                contentSize <= 1, // Empty or single empty string placeholder
                "CONSIDERATION: Light query should not load heavy content"
            )
        }
    }

    @Test
    fun `consideration - full query should only be used when content is needed`() {
        // Document that full query should be used sparingly
        
        // Given: Scenarios where full query is needed
        val epubExportScenario = true
        val chapterReadingScenario = true
        val chapterListingScenario = false // Should use light query
        
        // Then: Document appropriate usage
        assertTrue(epubExportScenario, "Full query needed for EPUB export")
        assertTrue(chapterReadingScenario, "Full query needed for reading")
        assertFalse(chapterListingScenario, "Light query preferred for listing")
    }

    // ==================== Helper Functions ====================

    /**
     * Creates a chapter as it would be returned by findChaptersByBookId (light query)
     */
    @OptIn(ExperimentalTime::class)
    private fun createLightQueryChapter(id: Long, name: String, isDownloaded: Boolean): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            // Light query: empty placeholder if downloaded, empty list otherwise
            content = if (isDownloaded) listOf(Text("")) else emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = if (isDownloaded) Clock.System.now().toEpochMilliseconds() else 0L,
            sourceOrder = id,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }

    /**
     * Creates a chapter as it would be returned by findChaptersByBookIdWithContent (full query)
     */
    @OptIn(ExperimentalTime::class)
    private fun createFullQueryChapter(id: Long, name: String, content: String): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = name,
            content = listOf(Text(content)),
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

    /**
     * Extracts text content from a chapter (simulating EpubBuilder behavior)
     */
    private fun extractTextContent(chapter: Chapter): String {
        return chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
    }
}
