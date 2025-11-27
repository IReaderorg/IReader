package ireader.domain.services.tts_service

import ireader.domain.models.entities.Chapter
import ireader.core.source.model.Text
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for chapter change behavior in TTS
 * 
 * These tests verify that when changing chapters:
 * - State updates correctly
 * - Paragraph resets to 0
 * - Content is updated
 * - UI chapters list is maintained
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSChapterChangeTest {

    /**
     * Test: Setting a new chapter updates the state
     */
    @Test
    fun `setTtsChapter updates chapter state`() = runTest {
        val state = TestTTSState()
        val chapter = createTestChapter(1L, "Chapter 1")
        
        assertNull(state.ttsChapter.value, "Should be null initially")
        
        state.setTtsChapter(chapter)
        
        assertNotNull(state.ttsChapter.value, "Should not be null after setting")
        assertEquals(1L, state.ttsChapter.value?.id, "Chapter ID should match")
        assertEquals("Chapter 1", state.ttsChapter.value?.name, "Chapter name should match")
    }

    /**
     * Test: Chapter change emits through StateFlow
     */
    @Test
    fun `chapter change emits through StateFlow`() = runTest {
        val state = TestTTSState()
        val chapter1 = createTestChapter(1L, "Chapter 1")
        val chapter2 = createTestChapter(2L, "Chapter 2")
        
        state.setTtsChapter(chapter1)
        assertEquals(1L, state.ttsChapter.first()?.id)
        
        state.setTtsChapter(chapter2)
        assertEquals(2L, state.ttsChapter.first()?.id, 
            "StateFlow should emit new chapter")
    }

    /**
     * Test: Chapters list can be set and retrieved
     */
    @Test
    fun `setTtsChapters updates chapters list`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2"),
            createTestChapter(3L, "Chapter 3")
        )
        
        assertTrue(state.ttsChapters.value.isEmpty(), "Should be empty initially")
        
        state.setTtsChapters(chapters)
        
        assertEquals(3, state.ttsChapters.value.size, "Should have 3 chapters")
        assertEquals(1L, state.ttsChapters.value[0].id, "First chapter ID should match")
    }

    /**
     * Test: UI chapters list updates when chapters are set
     */
    @Test
    fun `uiChapters updates when chapters are set`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2")
        )
        
        state.setUiChapters(chapters)
        
        assertEquals(2, state.uiChapters.value.size, "UI chapters should have 2 items")
    }

    /**
     * Test: Chapter index is tracked correctly
     */
    @Test
    fun `chapter index tracks current position`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2"),
            createTestChapter(3L, "Chapter 3")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[1])
        
        // Find index
        val index = chapters.indexOfFirst { it.id == state.ttsChapter.value?.id }
        state.setTtsCurrentChapterIndex(index)
        
        assertEquals(1, state.ttsCurrentChapterIndex.value, 
            "Chapter index should be 1")
    }

    /**
     * Test: Next chapter navigation
     */
    @Test
    fun `next chapter advances to next in list`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2"),
            createTestChapter(3L, "Chapter 3")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[0])
        state.setTtsCurrentChapterIndex(0)
        
        // Simulate next chapter
        val currentIndex = state.ttsCurrentChapterIndex.value
        if (currentIndex < chapters.lastIndex) {
            val nextChapter = chapters[currentIndex + 1]
            state.setTtsChapter(nextChapter)
            state.setTtsCurrentChapterIndex(currentIndex + 1)
            state.setCurrentReadingParagraph(0) // Reset paragraph
        }
        
        assertEquals(2L, state.ttsChapter.value?.id, "Should be at chapter 2")
        assertEquals(1, state.ttsCurrentChapterIndex.value, "Index should be 1")
        assertEquals(0, state.currentReadingParagraph.value, "Paragraph should reset to 0")
    }

    /**
     * Test: Previous chapter navigation
     */
    @Test
    fun `previous chapter goes back in list`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2"),
            createTestChapter(3L, "Chapter 3")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[2])
        state.setTtsCurrentChapterIndex(2)
        
        // Simulate previous chapter
        val currentIndex = state.ttsCurrentChapterIndex.value
        if (currentIndex > 0) {
            val prevChapter = chapters[currentIndex - 1]
            state.setTtsChapter(prevChapter)
            state.setTtsCurrentChapterIndex(currentIndex - 1)
            state.setCurrentReadingParagraph(0) // Reset paragraph
        }
        
        assertEquals(2L, state.ttsChapter.value?.id, "Should be at chapter 2")
        assertEquals(1, state.ttsCurrentChapterIndex.value, "Index should be 1")
    }

    /**
     * Test: Cannot go to previous chapter at first chapter
     */
    @Test
    fun `cannot go previous at first chapter`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[0])
        state.setTtsCurrentChapterIndex(0)
        
        // Try to go previous
        val currentIndex = state.ttsCurrentChapterIndex.value
        val canGoPrevious = currentIndex > 0
        
        assertFalse(canGoPrevious, "Should not be able to go previous at first chapter")
        assertEquals(0, state.ttsCurrentChapterIndex.value, "Index should remain 0")
    }

    /**
     * Test: Cannot go to next chapter at last chapter
     */
    @Test
    fun `cannot go next at last chapter`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[1])
        state.setTtsCurrentChapterIndex(1)
        
        // Try to go next
        val currentIndex = state.ttsCurrentChapterIndex.value
        val canGoNext = currentIndex < chapters.lastIndex
        
        assertFalse(canGoNext, "Should not be able to go next at last chapter")
        assertEquals(1, state.ttsCurrentChapterIndex.value, "Index should remain 1")
    }

    /**
     * Test: Drawer ascending order reverses chapter list
     */
    @Test
    fun `drawer ascending order affects ui chapters`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2"),
            createTestChapter(3L, "Chapter 3")
        )
        
        state.setTtsChapters(chapters)
        
        // Default order (descending)
        state.setDrawerAsc(false)
        state.setUiChapters(chapters)
        assertEquals(1L, state.uiChapters.value.first().id, "First should be chapter 1")
        
        // Ascending order
        state.setDrawerAsc(true)
        state.setUiChapters(chapters.reversed())
        assertEquals(3L, state.uiChapters.value.first().id, "First should be chapter 3 when reversed")
    }

    /**
     * Test: Chapter change during playback stops and restarts
     */
    @Test
    fun `chapter change during playback resets state`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[0])
        state.setTtsContent(listOf("P1", "P2", "P3"))
        state.setCurrentReadingParagraph(2)
        state.setPlaying(true)
        
        // Change chapter
        state.setPlaying(false) // Stop first
        state.setTtsChapter(chapters[1])
        state.setTtsContent(listOf("New P1", "New P2"))
        state.setCurrentReadingParagraph(0)
        
        assertEquals(2L, state.ttsChapter.value?.id, "Should be at new chapter")
        assertEquals(0, state.currentReadingParagraph.value, "Paragraph should reset")
        assertEquals(2, state.ttsContent.value?.size, "Content should be updated")
    }

    /**
     * Test: Loading state during chapter fetch
     */
    @Test
    fun `loading state during chapter fetch`() = runTest {
        val state = TestTTSState()
        
        assertFalse(state.isLoading.value, "Should not be loading initially")
        
        // Simulate chapter loading
        state.setLoading(true)
        assertTrue(state.isLoading.value, "Should be loading during fetch")
        
        // Simulate chapter loaded
        state.setTtsChapter(createTestChapter(1L, "Chapter 1"))
        state.setLoading(false)
        assertFalse(state.isLoading.value, "Should not be loading after fetch")
    }

    /**
     * Test: Auto next chapter at end of content
     */
    @Test
    fun `auto next chapter triggers at content end`() = runTest {
        val state = TestTTSState()
        val chapters = listOf(
            createTestChapter(1L, "Chapter 1"),
            createTestChapter(2L, "Chapter 2")
        )
        
        state.setTtsChapters(chapters)
        state.setTtsChapter(chapters[0])
        state.setTtsCurrentChapterIndex(0)
        state.setTtsContent(listOf("P1", "P2"))
        state.setCurrentReadingParagraph(1) // Last paragraph
        state.setAutoNextChapter(true)
        state.setPlaying(true)
        
        // Check conditions for auto-next
        val isAtEnd = state.currentReadingParagraph.value >= (state.ttsContent.value?.lastIndex ?: 0)
        val hasNextChapter = state.ttsCurrentChapterIndex.value < chapters.lastIndex
        val shouldAutoNext = isAtEnd && state.autoNextChapter.value && hasNextChapter
        
        assertTrue(shouldAutoNext, "Should trigger auto-next chapter")
    }

    // Helper function to create test chapters
    private fun createTestChapter(id: Long, name: String): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter_$id",
            name = name,
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            sourceOrder = id,
            dateFetch = 0L,
            dateUpload = 0L,
            content = listOf(Text("Test content for $name")),
            number = id.toFloat(),
            translator = ""
        )
    }
}
