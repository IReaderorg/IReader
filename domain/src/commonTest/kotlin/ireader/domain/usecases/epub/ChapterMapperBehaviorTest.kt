package ireader.domain.usecases.epub

import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests that document and verify the behavior of chapter mappers.
 * 
 * This test class ensures that:
 * 1. The light mapper (chapterMapperLight) behavior is understood
 * 2. The full mapper (chapterMapper) behavior is understood
 * 3. The difference between them is clear and tested
 * 
 * These tests serve as living documentation for the mapper behavior
 * and prevent regressions in EPUB export functionality.
 */
class ChapterMapperBehaviorTest {

    // ==================== Light Mapper Behavior ====================

    @Test
    fun `light mapper - should not include actual content`() {
        // Document: chapterMapperLight does NOT include the content field
        // It only includes a placeholder to indicate download status
        
        // Given: Simulated light mapper output
        val lightMappedChapter = simulateLightMapper(
            id = 1L,
            bookId = 1L,
            url = "chapter-1",
            name = "Chapter 1",
            isDownloaded = true
        )
        
        // Then: Content should be empty or placeholder
        val actualContent = extractActualContent(lightMappedChapter)
        assertTrue(
            actualContent.isBlank(),
            "Light mapper should NOT include actual content"
        )
    }

    @Test
    fun `light mapper - should indicate download status via placeholder`() {
        // Document: Light mapper uses empty Text("") as placeholder for downloaded chapters
        
        // Given: Downloaded chapter from light mapper
        val downloadedChapter = simulateLightMapper(
            id = 1L,
            bookId = 1L,
            url = "chapter-1",
            name = "Chapter 1",
            isDownloaded = true
        )
        
        // Given: Non-downloaded chapter from light mapper
        val notDownloadedChapter = simulateLightMapper(
            id = 2L,
            bookId = 1L,
            url = "chapter-2",
            name = "Chapter 2",
            isDownloaded = false
        )
        
        // Then: Downloaded chapter has placeholder, non-downloaded has empty list
        assertTrue(
            downloadedChapter.content.isNotEmpty(),
            "Downloaded chapter should have placeholder"
        )
        assertTrue(
            notDownloadedChapter.content.isEmpty(),
            "Non-downloaded chapter should have empty content list"
        )
    }

    @Test
    fun `light mapper - should preserve all metadata fields`() {
        // Document: Light mapper preserves all fields except content
        
        // Given: Light mapped chapter
        val chapter = simulateLightMapper(
            id = 123L,
            bookId = 456L,
            url = "test-url",
            name = "Test Chapter",
            isDownloaded = true,
            read = true,
            bookmark = true,
            lastPageRead = 50L,
            chapterNumber = 5.5f,
            sourceOrder = 10L,
            dateFetch = 1000L,
            dateUpload = 2000L,
            type = 1L,
            scanlator = "Test Scanlator"
        )
        
        // Then: All metadata should be preserved
        assertEquals(123L, chapter.id)
        assertEquals(456L, chapter.bookId)
        assertEquals("test-url", chapter.key)
        assertEquals("Test Chapter", chapter.name)
        assertTrue(chapter.read)
        assertTrue(chapter.bookmark)
        assertEquals(50L, chapter.lastPageRead)
        assertEquals(5.5f, chapter.number)
        assertEquals(10L, chapter.sourceOrder)
        assertEquals(1000L, chapter.dateFetch)
        assertEquals(2000L, chapter.dateUpload)
        assertEquals(1L, chapter.type)
        assertEquals("Test Scanlator", chapter.translator)
    }

    // ==================== Full Mapper Behavior ====================

    @Test
    fun `full mapper - should include actual content`() {
        // Document: chapterMapper DOES include the content field
        
        // Given: Simulated full mapper output
        val fullMappedChapter = simulateFullMapper(
            id = 1L,
            bookId = 1L,
            url = "chapter-1",
            name = "Chapter 1",
            content = listOf(Text("This is the actual chapter content."))
        )
        
        // Then: Content should be present
        val actualContent = extractActualContent(fullMappedChapter)
        assertTrue(
            actualContent.isNotBlank(),
            "Full mapper MUST include actual content"
        )
        assertEquals(
            "This is the actual chapter content.",
            actualContent
        )
    }

    @Test
    fun `full mapper - should preserve all fields including content`() {
        // Document: Full mapper preserves ALL fields
        
        // Given: Full mapped chapter with all fields
        val content = listOf(Text("Content paragraph 1"), Text("Content paragraph 2"))
        val chapter = simulateFullMapper(
            id = 789L,
            bookId = 101L,
            url = "full-test-url",
            name = "Full Test Chapter",
            content = content,
            read = true,
            bookmark = false,
            lastPageRead = 100L,
            chapterNumber = 10.0f,
            sourceOrder = 20L,
            dateFetch = 3000L,
            dateUpload = 4000L,
            type = 2L,
            scanlator = "Full Scanlator"
        )
        
        // Then: All fields including content should be preserved
        assertEquals(789L, chapter.id)
        assertEquals(101L, chapter.bookId)
        assertEquals("full-test-url", chapter.key)
        assertEquals("Full Test Chapter", chapter.name)
        assertEquals(2, chapter.content.size)
        assertTrue(chapter.read)
        assertFalse(chapter.bookmark)
        assertEquals(100L, chapter.lastPageRead)
        assertEquals(10.0f, chapter.number)
        assertEquals(20L, chapter.sourceOrder)
        assertEquals(3000L, chapter.dateFetch)
        assertEquals(4000L, chapter.dateUpload)
        assertEquals(2L, chapter.type)
        assertEquals("Full Scanlator", chapter.translator)
    }

    // ==================== Comparison Tests ====================

    @Test
    fun `comparison - same chapter different mappers should differ only in content`() {
        // Document: The only difference between mappers is content
        
        // Given: Same chapter data mapped by both mappers
        val lightChapter = simulateLightMapper(
            id = 1L,
            bookId = 1L,
            url = "chapter-1",
            name = "Chapter 1",
            isDownloaded = true,
            read = true,
            bookmark = false,
            lastPageRead = 25L,
            chapterNumber = 1.0f,
            sourceOrder = 1L,
            dateFetch = 1000L,
            dateUpload = 500L,
            type = 0L,
            scanlator = "Translator"
        )
        
        val fullChapter = simulateFullMapper(
            id = 1L,
            bookId = 1L,
            url = "chapter-1",
            name = "Chapter 1",
            content = listOf(Text("Actual content here")),
            read = true,
            bookmark = false,
            lastPageRead = 25L,
            chapterNumber = 1.0f,
            sourceOrder = 1L,
            dateFetch = 1000L,
            dateUpload = 500L,
            type = 0L,
            scanlator = "Translator"
        )
        
        // Then: Metadata should be identical
        assertEquals(lightChapter.id, fullChapter.id)
        assertEquals(lightChapter.bookId, fullChapter.bookId)
        assertEquals(lightChapter.key, fullChapter.key)
        assertEquals(lightChapter.name, fullChapter.name)
        assertEquals(lightChapter.read, fullChapter.read)
        assertEquals(lightChapter.bookmark, fullChapter.bookmark)
        assertEquals(lightChapter.lastPageRead, fullChapter.lastPageRead)
        assertEquals(lightChapter.number, fullChapter.number)
        assertEquals(lightChapter.sourceOrder, fullChapter.sourceOrder)
        assertEquals(lightChapter.dateFetch, fullChapter.dateFetch)
        assertEquals(lightChapter.dateUpload, fullChapter.dateUpload)
        assertEquals(lightChapter.type, fullChapter.type)
        assertEquals(lightChapter.translator, fullChapter.translator)
        
        // But content should differ
        val lightContent = extractActualContent(lightChapter)
        val fullContent = extractActualContent(fullChapter)
        
        assertTrue(lightContent.isBlank(), "Light mapper content should be blank")
        assertTrue(fullContent.isNotBlank(), "Full mapper content should not be blank")
    }

    // ==================== EPUB Export Requirement Tests ====================

    @Test
    fun `epub requirement - must use full mapper for export`() {
        // Document: EPUB export MUST use full mapper to get content
        
        // Given: Chapters from both mappers
        val lightChapters = (1..3).map { i ->
            simulateLightMapper(i.toLong(), 1L, "ch-$i", "Chapter $i", true)
        }
        
        val fullChapters = (1..3).map { i ->
            simulateFullMapper(i.toLong(), 1L, "ch-$i", "Chapter $i", 
                listOf(Text("Content for chapter $i")))
        }
        
        // When: Checking exportability
        val lightExportable = lightChapters.all { extractActualContent(it).isNotBlank() }
        val fullExportable = fullChapters.all { extractActualContent(it).isNotBlank() }
        
        // Then: Only full mapper chapters are exportable
        assertFalse(
            lightExportable,
            "EPUB REQUIREMENT: Light mapper chapters are NOT exportable"
        )
        assertTrue(
            fullExportable,
            "EPUB REQUIREMENT: Full mapper chapters ARE exportable"
        )
    }

    @Test
    fun `epub requirement - content validation before export`() {
        // Document: EPUB export should validate content exists
        
        // Given: Mixed chapters (some with content, some without)
        val chapters = listOf(
            simulateFullMapper(1L, 1L, "ch-1", "Chapter 1", listOf(Text("Content"))),
            simulateLightMapper(2L, 1L, "ch-2", "Chapter 2", true),
            simulateFullMapper(3L, 1L, "ch-3", "Chapter 3", listOf(Text("More content")))
        )
        
        // When: Validating for export
        val validChapters = chapters.filter { extractActualContent(it).isNotBlank() }
        val invalidChapters = chapters.filter { extractActualContent(it).isBlank() }
        
        // Then: Should correctly identify valid/invalid chapters
        assertEquals(2, validChapters.size, "Should have 2 valid chapters")
        assertEquals(1, invalidChapters.size, "Should have 1 invalid chapter")
        assertEquals(2L, invalidChapters.first().id, "Invalid chapter should be the light-mapped one")
    }

    // ==================== Helper Functions ====================

    /**
     * Simulates the output of chapterMapperLight
     * This is what findChaptersByBookId returns
     */
    @OptIn(ExperimentalTime::class)
    private fun simulateLightMapper(
        id: Long,
        bookId: Long,
        url: String,
        name: String,
        isDownloaded: Boolean,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Long = 0L,
        chapterNumber: Float = id.toFloat(),
        sourceOrder: Long = id,
        dateFetch: Long = if (isDownloaded) Clock.System.now().toEpochMilliseconds() else 0L,
        dateUpload: Long = 0L,
        type: Long = 0L,
        scanlator: String = ""
    ): Chapter {
        // Light mapper: content is placeholder if downloaded, empty otherwise
        val content: List<Page> = if (isDownloaded) listOf(Text("")) else emptyList()
        
        return Chapter(
            id = id,
            bookId = bookId,
            key = url,
            name = name,
            content = content,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            number = chapterNumber,
            sourceOrder = sourceOrder,
            dateFetch = dateFetch,
            dateUpload = dateUpload,
            type = type,
            translator = scanlator
        )
    }

    /**
     * Simulates the output of chapterMapper
     * This is what findChaptersByBookIdWithContent returns
     */
    @OptIn(ExperimentalTime::class)
    private fun simulateFullMapper(
        id: Long,
        bookId: Long,
        url: String,
        name: String,
        content: List<Page>,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Long = 0L,
        chapterNumber: Float = id.toFloat(),
        sourceOrder: Long = id,
        dateFetch: Long = Clock.System.now().toEpochMilliseconds(),
        dateUpload: Long = 0L,
        type: Long = 0L,
        scanlator: String = ""
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = url,
            name = name,
            content = content,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            number = chapterNumber,
            sourceOrder = sourceOrder,
            dateFetch = dateFetch,
            dateUpload = dateUpload,
            type = type,
            translator = scanlator
        )
    }

    /**
     * Extracts actual text content from a chapter
     * Filters out empty/whitespace-only text
     */
    private fun extractActualContent(chapter: Chapter): String {
        return chapter.content.mapNotNull {
            when (it) {
                is Text -> it.text.takeIf { text -> text.isNotBlank() }
                else -> null
            }
        }.joinToString("\n\n")
    }
}
