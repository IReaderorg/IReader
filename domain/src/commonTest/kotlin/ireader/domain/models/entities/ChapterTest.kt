package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for Chapter entity
 * Tests chapter data model and business logic
 */
class ChapterTest {
    
    @Test
    fun `chapter should be created with required fields`() {
        // Given & When
        val chapter = Chapter(
            id = 1L,
            bookId = 100L,
            key = "chapter-1",
            name = "Chapter 1: The Beginning",
            dateUpload = System.currentTimeMillis(),
            number = 1.0f,
            sourceOrder = 1,
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            dateFetch = System.currentTimeMillis(),
            translator = null,
            content = emptyList()
        )
        
        // Then
        assertEquals(1L, chapter.id)
        assertEquals(100L, chapter.bookId)
        assertEquals("Chapter 1: The Beginning", chapter.name)
        assertFalse(chapter.read)
        assertFalse(chapter.bookmark)
    }
    
    @Test
    fun `chapter copy should preserve unchanged fields`() {
        // Given
        val original = createTestChapter()
        
        // When
        val copy = original.copy(read = true)
        
        // Then
        assertEquals(original.id, copy.id)
        assertEquals(original.bookId, copy.bookId)
        assertEquals(original.name, copy.name)
        assertTrue(copy.read)
        assertEquals(original.bookmark, copy.bookmark)
    }
    
    @Test
    fun `chapter with content should store text`() {
        // Given
        val content = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        
        // When
        val chapter = createTestChapter().copy(content = content)
        
        // Then
        assertEquals(3, chapter.content.size)
        assertEquals("Paragraph 1", chapter.content[0])
    }
    
    @Test
    fun `chapter number should support decimal values`() {
        // Given & When
        val chapter1 = createTestChapter().copy(number = 1.5f)
        val chapter2 = createTestChapter().copy(number = 2.0f)
        
        // Then
        assertEquals(1.5f, chapter1.number)
        assertEquals(2.0f, chapter2.number)
        assertTrue(chapter1.number < chapter2.number)
    }
    
    @Test
    fun `chapter should support translator information`() {
        // Given
        val translator = "Translation Team"
        
        // When
        val chapter = createTestChapter().copy(translator = translator)
        
        // Then
        assertEquals(translator, chapter.translator)
    }
    
    @Test
    fun `chapter should track last page read`() {
        // Given
        val lastPage = 42L
        
        // When
        val chapter = createTestChapter().copy(lastPageRead = lastPage)
        
        // Then
        assertEquals(lastPage, chapter.lastPageRead)
    }
    
    @Test
    fun `chapter should support bookmarking`() {
        // Given
        val chapter = createTestChapter()
        
        // When
        val bookmarked = chapter.copy(bookmark = true)
        
        // Then
        assertTrue(bookmarked.bookmark)
        assertFalse(chapter.bookmark)
    }
    
    private fun createTestChapter(): Chapter {
        return Chapter(
            id = 1L,
            bookId = 100L,
            key = "chapter-1",
            name = "Test Chapter",
            dateUpload = System.currentTimeMillis(),
            number = 1.0f,
            sourceOrder = 1,
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            dateFetch = System.currentTimeMillis(),
            translator = null,
            content = emptyList()
        )
    }
}
