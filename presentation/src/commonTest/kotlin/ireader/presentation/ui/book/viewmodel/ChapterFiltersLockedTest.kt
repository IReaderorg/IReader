package ireader.presentation.ui.book.viewmodel

import ireader.domain.models.entities.Chapter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for locked chapter filtering functionality.
 * 
 * Locked chapters are identified by:
 * - Lock emoji (🔒) in chapter name
 * - "locked" text in chapter name (case-insensitive)
 */
class ChapterFiltersLockedTest {
    
    @Test
    fun `isLockedChapter should return true for chapter with lock emoji`() {
        // Arrange
        val chapter = Chapter(
            id = 1,
            bookId = 1,
            key = "ch1",
            name = "Chapter 1 🔒",
            read = false
        )
        
        // Act
        val result = chapter.isLockedChapter()
        
        // Assert
        assertTrue(result, "Chapter with 🔒 emoji should be detected as locked")
    }
    
    @Test
    fun `isLockedChapter should return true for chapter with locked text`() {
        // Arrange
        val chapter = Chapter(
            id = 2,
            bookId = 1,
            key = "ch2",
            name = "Chapter 2 [Locked]",
            read = false
        )
        
        // Act
        val result = chapter.isLockedChapter()
        
        // Assert
        assertTrue(result, "Chapter with 'locked' text should be detected as locked")
    }
    
    @Test
    fun `isLockedChapter should return true for chapter with LOCKED in uppercase`() {
        // Arrange
        val chapter = Chapter(
            id = 3,
            bookId = 1,
            key = "ch3",
            name = "Chapter 3 - LOCKED",
            read = false
        )
        
        // Act
        val result = chapter.isLockedChapter()
        
        // Assert
        assertTrue(result, "Chapter with 'LOCKED' text should be detected as locked")
    }
    
    @Test
    fun `isLockedChapter should return false for normal chapter`() {
        // Arrange
        val chapter = Chapter(
            id = 4,
            bookId = 1,
            key = "ch4",
            name = "Chapter 4 - Normal Chapter",
            read = false
        )
        
        // Act
        val result = chapter.isLockedChapter()
        
        // Assert
        assertFalse(result, "Normal chapter should not be detected as locked")
    }
    
    @Test
    fun `isLockedChapter should return false for empty chapter name`() {
        // Arrange
        val chapter = Chapter(
            id = 5,
            bookId = 1,
            key = "ch5",
            name = "",
            read = false
        )
        
        // Act
        val result = chapter.isLockedChapter()
        
        // Assert
        assertFalse(result, "Empty chapter name should not be detected as locked")
    }
    
    @Test
    fun `ChaptersFilters should include Locked type`() {
        // Arrange & Act
        val types = ChaptersFilters.types
        
        // Assert
        assertTrue(
            types.contains(ChaptersFilters.Type.Locked),
            "ChaptersFilters.Type should include Locked"
        )
    }
    
    @Test
    fun `getDefault should include Locked filter when includeAll is true`() {
        // Arrange & Act
        val filters = ChaptersFilters.getDefault(includeAll = true)
        
        // Assert
        assertTrue(
            filters.any { it.type == ChaptersFilters.Type.Locked },
            "Default filters should include Locked filter when includeAll is true"
        )
    }
}
