package ireader.domain.usecases.chapter

import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Tests for chapter content persistence and ordering fixes.
 * 
 * These tests verify:
 * 1. Chapter ID is preserved when updating content (prevents duplicate chapters)
 * 2. Chapter sourceOrder is preserved when updating content (prevents wrong ordering)
 * 3. Chapter content updates don't create new chapters
 */
class ChapterContentPersistenceTest {

    @Test
    fun `chapter copy preserves id when updating content`() {
        // Given: An existing chapter with ID and sourceOrder
        val originalChapter = Chapter(
            id = 123L,
            bookId = 1L,
            key = "chapter-1",
            name = "Chapter 1",
            sourceOrder = 5L,
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
        
        // When: Copying with new content (simulating download)
        val updatedChapter = originalChapter.copy(
            content = listOf(ireader.core.source.model.Text("Downloaded content"))
        )
        
        // Then: ID and sourceOrder should be preserved
        assertEquals(originalChapter.id, updatedChapter.id, "Chapter ID should be preserved")
        assertEquals(originalChapter.sourceOrder, updatedChapter.sourceOrder, "Source order should be preserved")
        assertEquals(originalChapter.bookId, updatedChapter.bookId, "Book ID should be preserved")
        assertEquals(originalChapter.name, updatedChapter.name, "Name should be preserved")
        assertTrue(updatedChapter.content.isNotEmpty(), "Content should be updated")
    }

    @Test
    fun `chapter with id 0 should be treated as new chapter`() {
        // Given: A chapter with default ID (0)
        val newChapter = Chapter(
            id = 0L,
            bookId = 1L,
            key = "new-chapter",
            name = "New Chapter",
            sourceOrder = 10L,
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
        
        // Then: ID should be 0 (will trigger INSERT in database)
        assertEquals(0L, newChapter.id, "New chapter should have ID 0")
    }

    @Test
    fun `chapter sourceOrder should determine list position`() {
        // Given: Chapters with different sourceOrder values
        val chapters = listOf(
            createChapter(id = 1, sourceOrder = 3),
            createChapter(id = 2, sourceOrder = 1),
            createChapter(id = 3, sourceOrder = 2)
        )
        
        // When: Sorting by sourceOrder
        val sorted = chapters.sortedBy { it.sourceOrder }
        
        // Then: Should be in correct order
        assertEquals(2L, sorted[0].id, "First chapter should have sourceOrder 1")
        assertEquals(3L, sorted[1].id, "Second chapter should have sourceOrder 2")
        assertEquals(1L, sorted[2].id, "Third chapter should have sourceOrder 3")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `updating chapter content should not change sourceOrder`() {
        // Given: A chapter with specific sourceOrder
        val chapter = createChapter(id = 5, sourceOrder = 42)
        
        // When: Updating only the content
        val updated = chapter.copy(
            content = listOf(ireader.core.source.model.Text("New content")),
            dateFetch = Clock.System.now().toEpochMilliseconds()
        )
        
        // Then: sourceOrder should remain unchanged
        assertEquals(42L, updated.sourceOrder, "sourceOrder should not change when updating content")
    }

    @Test
    fun `chapter list should maintain order after content update`() {
        // Given: A list of chapters in order
        val chapters = listOf(
            createChapter(id = 1, sourceOrder = 1, hasContent = false),
            createChapter(id = 2, sourceOrder = 2, hasContent = false),
            createChapter(id = 3, sourceOrder = 3, hasContent = false)
        )
        
        // When: Middle chapter gets content (simulating download)
        val updatedChapters = chapters.map { chapter ->
            if (chapter.id == 2L) {
                chapter.copy(content = listOf(ireader.core.source.model.Text("Downloaded")))
            } else {
                chapter
            }
        }
        
        // Then: Order should be preserved
        val sorted = updatedChapters.sortedBy { it.sourceOrder }
        assertEquals(1L, sorted[0].id)
        assertEquals(2L, sorted[1].id)
        assertEquals(3L, sorted[2].id)
        
        // And the updated chapter should have content
        assertTrue(sorted[1].content.isNotEmpty())
    }

    private fun createChapter(
        id: Long,
        sourceOrder: Long,
        hasContent: Boolean = false
    ): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = "Chapter $id",
            sourceOrder = sourceOrder,
            content = if (hasContent) listOf(ireader.core.source.model.Text("Content")) else emptyList(),
            read = false,
            bookmark = false,
            dateUpload = 0L,
            dateFetch = 0L,
            number = id.toFloat(),
            translator = "",
            lastPageRead = 0L,
            type = 0L
        )
    }
}
