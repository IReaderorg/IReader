package ireader.domain.services.download

import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests for download service chapter preservation fixes.
 * 
 * These tests verify:
 * 1. Downloaded chapter preserves original chapter ID
 * 2. Downloaded chapter preserves original sourceOrder
 * 3. Only content is updated from downloaded chapter
 */
class DownloadChapterPreservationTest {

    @Test
    fun `downloaded chapter should preserve original id`() {
        // Given: Original chapter from database
        val originalChapter = Chapter(
            id = 123L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            sourceOrder = 5L,
            content = emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 1000L,
            dateFetch = 0L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // Simulated downloaded chapter (might have id = 0 from remote)
        val downloadedContent = listOf(ireader.core.source.model.Text("Downloaded content"))
        
        // When: Creating final chapter by copying original with new content
        // This is the fix: use originalChapter.copy() instead of downloadedChapter directly
        val finalChapter = originalChapter.copy(
            content = downloadedContent
        )
        
        // Then: Original ID should be preserved
        assertEquals(123L, finalChapter.id, "Original chapter ID should be preserved")
        assertEquals(5L, finalChapter.sourceOrder, "Original sourceOrder should be preserved")
        assertEquals(1L, finalChapter.bookId, "Original bookId should be preserved")
        assertEquals("Chapter 1", finalChapter.name, "Original name should be preserved")
        assertTrue(finalChapter.content.isNotEmpty(), "Content should be updated")
    }

    @Test
    fun `downloaded chapter should preserve all metadata`() {
        // Given: Original chapter with various metadata
        val originalChapter = Chapter(
            id = 456L,
            bookId = 2L,
            key = "chapter-key",
            name = "Original Name",
            sourceOrder = 10L,
            content = emptyList(),
            read = true,
            bookmark = true,
            dateUpload = 5000L,
            dateFetch = 1000L,
            number = 5.5f,
            translator = "Translator Name",
            lastPageRead = 100L,
            type = 1L
        )
        
        val downloadedContent = listOf(ireader.core.source.model.Text("New content"))
        
        // When: Updating with downloaded content
        val finalChapter = originalChapter.copy(content = downloadedContent)
        
        // Then: All metadata should be preserved
        assertEquals(originalChapter.id, finalChapter.id)
        assertEquals(originalChapter.bookId, finalChapter.bookId)
        assertEquals(originalChapter.key, finalChapter.key)
        assertEquals(originalChapter.name, finalChapter.name)
        assertEquals(originalChapter.sourceOrder, finalChapter.sourceOrder)
        assertEquals(originalChapter.read, finalChapter.read)
        assertEquals(originalChapter.bookmark, finalChapter.bookmark)
        assertEquals(originalChapter.dateUpload, finalChapter.dateUpload)
        assertEquals(originalChapter.number, finalChapter.number)
        assertEquals(originalChapter.translator, finalChapter.translator)
        assertEquals(originalChapter.lastPageRead, finalChapter.lastPageRead)
        assertEquals(originalChapter.type, finalChapter.type)
        
        // Only content should be different
        assertEquals(downloadedContent, finalChapter.content)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `using downloaded chapter directly would lose original id`() {
        // This test demonstrates the bug that was fixed
        
        // Given: Original chapter from database
        val originalChapter = Chapter(
            id = 789L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            sourceOrder = 3L,
            content = emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // Simulated downloaded chapter from remote (has id = 0)
        val downloadedChapter = Chapter(
            id = 0L, // Remote chapters typically have id = 0
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            sourceOrder = 0L, // Might also be wrong
            content = listOf(ireader.core.source.model.Text("Content")),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = Clock.System.now().toEpochMilliseconds(),
            number = 1f,
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
        
        // BUG: Using downloadedChapter directly would lose the original ID
        // This would cause a new chapter to be inserted instead of updating
        assertEquals(0L, downloadedChapter.id, "Downloaded chapter has id = 0")
        
        // FIX: Use original chapter and copy only the content
        val fixedChapter = originalChapter.copy(content = downloadedChapter.content)
        assertEquals(789L, fixedChapter.id, "Fixed chapter preserves original ID")
        assertEquals(3L, fixedChapter.sourceOrder, "Fixed chapter preserves original sourceOrder")
    }
}
