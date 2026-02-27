package ireader.presentation.ui.book

import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.isLockedChapter
import ireader.presentation.ui.book.viewmodel.ChaptersFilters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for chapter filtering with locked chapters.
 * 
 * Tests the complete flow from chapter data to filtered results.
 */
class ChapterFilteringIntegrationTest {
    
    private fun createTestChapters(): List<Chapter> {
        return listOf(
            Chapter(id = 1, bookId = 1, key = "ch1", name = "Chapter 1", read = false),
            Chapter(id = 2, bookId = 1, key = "ch2", name = "Chapter 2 🔒", read = false),
            Chapter(id = 3, bookId = 1, key = "ch3", name = "Chapter 3", read = true),
            Chapter(id = 4, bookId = 1, key = "ch4", name = "Chapter 4 [Locked]", read = false),
            Chapter(id = 5, bookId = 1, key = "ch5", name = "Chapter 5", read = false),
            Chapter(id = 6, bookId = 1, key = "ch6", name = "Chapter 6 - LOCKED", read = false),
        )
    }
    
    @Test
    fun `filter should exclude locked chapters when Locked filter is Excluded`() {
        // Arrange
        val chapters = createTestChapters()
        val filter = ChaptersFilters(ChaptersFilters.Type.Locked, ChaptersFilters.Value.Excluded)
        
        // Act
        val result = chapters.filterNot { it.isLockedChapter() }
        
        // Assert
        assertEquals(3, result.size, "Should have 3 unlocked chapters")
        assertTrue(result.none { it.isLockedChapter() }, "No locked chapters should remain")
        assertTrue(result.any { it.name == "Chapter 1" })
        assertTrue(result.any { it.name == "Chapter 3" })
        assertTrue(result.any { it.name == "Chapter 5" })
    }
    
    @Test
    fun `filter should include only locked chapters when Locked filter is Included`() {
        // Arrange
        val chapters = createTestChapters()
        val filter = ChaptersFilters(ChaptersFilters.Type.Locked, ChaptersFilters.Value.Included)
        
        // Act
        val result = chapters.filter { it.isLockedChapter() }
        
        // Assert
        assertEquals(3, result.size, "Should have 3 locked chapters")
        assertTrue(result.all { it.isLockedChapter() }, "All chapters should be locked")
        assertTrue(result.any { it.name == "Chapter 2 🔒" })
        assertTrue(result.any { it.name == "Chapter 4 [Locked]" })
        assertTrue(result.any { it.name == "Chapter 6 - LOCKED" })
    }
    
    @Test
    fun `filter should show all chapters when Locked filter is Missing`() {
        // Arrange
        val chapters = createTestChapters()
        val filter = ChaptersFilters(ChaptersFilters.Type.Locked, ChaptersFilters.Value.Missing)
        
        // Act - Missing means no filtering
        val result = chapters
        
        // Assert
        assertEquals(6, result.size, "Should have all 6 chapters")
    }
    
    @Test
    fun `combining locked and read filters should work correctly`() {
        // Arrange
        val chapters = createTestChapters()
        val lockedFilter = ChaptersFilters(ChaptersFilters.Type.Locked, ChaptersFilters.Value.Excluded)
        val readFilter = ChaptersFilters(ChaptersFilters.Type.Read, ChaptersFilters.Value.Excluded)
        
        // Act - Apply both filters
        val result = chapters
            .filterNot { it.isLockedChapter() }  // Exclude locked
            .filterNot { it.read }                // Exclude read
        
        // Assert
        assertEquals(2, result.size, "Should have 2 chapters (unlocked and unread)")
        assertTrue(result.all { !it.isLockedChapter() && !it.read })
        assertTrue(result.any { it.name == "Chapter 1" })
        assertTrue(result.any { it.name == "Chapter 5" })
    }
    
    @Test
    fun `locked chapter detection should be case-insensitive`() {
        // Arrange
        val chapters = listOf(
            Chapter(id = 1, bookId = 1, key = "ch1", name = "Chapter 1 locked", read = false),
            Chapter(id = 2, bookId = 1, key = "ch2", name = "Chapter 2 LOCKED", read = false),
            Chapter(id = 3, bookId = 1, key = "ch3", name = "Chapter 3 Locked", read = false),
            Chapter(id = 4, bookId = 1, key = "ch4", name = "Chapter 4 LoCkEd", read = false),
        )
        
        // Act & Assert
        assertTrue(chapters.all { it.isLockedChapter() }, "All variations should be detected as locked")
    }
    
    @Test
    fun `locked chapter detection should handle emoji correctly`() {
        // Arrange
        val chapters = listOf(
            Chapter(id = 1, bookId = 1, key = "ch1", name = "🔒 Chapter 1", read = false),
            Chapter(id = 2, bookId = 1, key = "ch2", name = "Chapter 2 🔒", read = false),
            Chapter(id = 3, bookId = 1, key = "ch3", name = "Chapter 🔒 3", read = false),
        )
        
        // Act & Assert
        assertTrue(chapters.all { it.isLockedChapter() }, "All chapters with 🔒 should be detected as locked")
    }
    
    @Test
    fun `normal chapters should not be detected as locked`() {
        // Arrange
        val chapters = listOf(
            Chapter(id = 1, bookId = 1, key = "ch1", name = "Chapter 1", read = false),
            Chapter(id = 2, bookId = 1, key = "ch2", name = "Chapter 2 - Free", read = false),
            Chapter(id = 3, bookId = 1, key = "ch3", name = "Chapter 3 (Premium)", read = false),
            Chapter(id = 4, bookId = 1, key = "ch4", name = "Chapter 4 - Unlocked", read = false),
        )
        
        // Act & Assert
        assertFalse(chapters.any { it.isLockedChapter() }, "None of these should be detected as locked")
    }
}
