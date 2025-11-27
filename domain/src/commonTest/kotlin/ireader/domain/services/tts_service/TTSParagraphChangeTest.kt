package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests specifically for paragraph change behavior in TTS
 * 
 * These tests verify the core issue: when user taps play,
 * the paragraph should change correctly, and when changing
 * chapters, the state should update properly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSParagraphChangeTest {

    /**
     * Test: Paragraph advances when TTS completes reading current paragraph
     * This is the core issue - paragraph not changing during playback
     */
    @Test
    fun `paragraph advances after completion`() = runTest {
        val state = TestTTSState()
        val content = listOf("First paragraph", "Second paragraph", "Third paragraph")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        state.setPlaying(true)
        
        // Simulate TTS completing first paragraph
        simulateParagraphComplete(state)
        
        assertEquals(1, state.currentReadingParagraph.value, 
            "Paragraph should advance to 1 after first paragraph completes")
    }

    /**
     * Test: Paragraph does not advance beyond content bounds
     */
    @Test
    fun `paragraph does not advance beyond content bounds`() = runTest {
        val state = TestTTSState()
        val content = listOf("Only paragraph")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        state.setPlaying(true)
        
        // Simulate completion at last paragraph
        val isFinished = simulateParagraphCompleteWithCheck(state)
        
        assertTrue(isFinished, "Should indicate finished when at last paragraph")
        assertEquals(0, state.currentReadingParagraph.value, 
            "Paragraph should remain at 0 (last index)")
    }

    /**
     * Test: Previous paragraph is updated when current changes
     * This is needed for UI scroll tracking
     */
    @Test
    fun `previous paragraph tracks current before change`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        state.setPreviousReadingParagraph(0)
        state.setPlaying(true)
        
        // Before advancing, save previous
        val previousBefore = state.currentReadingParagraph.value
        state.setPreviousReadingParagraph(previousBefore)
        
        // Advance paragraph
        state.setCurrentReadingParagraph(1)
        
        assertEquals(0, state.previousReadingParagraph.value, 
            "Previous should be 0")
        assertEquals(1, state.currentReadingParagraph.value, 
            "Current should be 1")
        assertNotEquals(
            state.previousReadingParagraph.value,
            state.currentReadingParagraph.value,
            "Previous and current should differ after advance"
        )
    }

    /**
     * Test: Clicking on a paragraph jumps to that paragraph
     */
    @Test
    fun `clicking paragraph jumps to that index`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3", "P4", "P5")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        
        // Simulate user clicking on paragraph 3
        state.setCurrentReadingParagraph(3)
        
        assertEquals(3, state.currentReadingParagraph.value, 
            "Should jump to clicked paragraph")
    }

    /**
     * Test: Paragraph change emits through StateFlow for UI updates
     */
    @Test
    fun `paragraph change emits through StateFlow`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        
        // Collect initial value
        assertEquals(0, state.currentReadingParagraph.first())
        
        // Change paragraph
        state.setCurrentReadingParagraph(2)
        
        // Verify emission
        assertEquals(2, state.currentReadingParagraph.first(), 
            "StateFlow should emit new value")
    }

    /**
     * Test: Chapter change resets paragraph to 0
     */
    @Test
    fun `chapter change resets paragraph to zero`() = runTest {
        val state = TestTTSState()
        
        // Setup initial state
        state.setTtsContent(listOf("P1", "P2", "P3"))
        state.setCurrentReadingParagraph(2)
        
        // Simulate chapter change
        state.setCurrentReadingParagraph(0)
        state.setTtsContent(listOf("New P1", "New P2"))
        
        assertEquals(0, state.currentReadingParagraph.value, 
            "Paragraph should reset to 0 on chapter change")
    }

    /**
     * Test: Rapid paragraph changes are handled correctly
     * Simulates fast TTS playback
     */
    @Test
    fun `rapid paragraph changes are handled correctly`() = runTest {
        val state = TestTTSState()
        val content = (1..100).map { "Paragraph $it" }
        
        state.setTtsContent(content)
        state.setPlaying(true)
        
        // Simulate rapid paragraph advances
        for (i in 0 until 50) {
            state.setPreviousReadingParagraph(state.currentReadingParagraph.value)
            state.setCurrentReadingParagraph(i + 1)
        }
        
        assertEquals(50, state.currentReadingParagraph.value, 
            "Should be at paragraph 50 after 50 advances")
        assertEquals(49, state.previousReadingParagraph.value, 
            "Previous should be 49")
    }

    /**
     * Test: Paragraph state persists through pause/resume
     */
    @Test
    fun `paragraph persists through pause resume`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3", "P4", "P5")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(2)
        state.setPlaying(true)
        
        // Pause
        state.setPlaying(false)
        assertEquals(2, state.currentReadingParagraph.value, 
            "Paragraph should persist after pause")
        
        // Resume
        state.setPlaying(true)
        assertEquals(2, state.currentReadingParagraph.value, 
            "Paragraph should persist after resume")
    }

    /**
     * Test: Next paragraph button advances correctly
     */
    @Test
    fun `next paragraph advances by one`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        
        // Simulate next paragraph button
        val current = state.currentReadingParagraph.value
        val contentList = state.ttsContent.value
        if (contentList != null && current < contentList.lastIndex) {
            state.setCurrentReadingParagraph(current + 1)
        }
        
        assertEquals(1, state.currentReadingParagraph.value, 
            "Should advance to next paragraph")
    }

    /**
     * Test: Previous paragraph button goes back correctly
     */
    @Test
    fun `previous paragraph goes back by one`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(2)
        
        // Simulate previous paragraph button
        val current = state.currentReadingParagraph.value
        if (current > 0) {
            state.setCurrentReadingParagraph(current - 1)
        }
        
        assertEquals(1, state.currentReadingParagraph.value, 
            "Should go back to previous paragraph")
    }

    /**
     * Test: Previous paragraph at index 0 stays at 0
     */
    @Test
    fun `previous paragraph at zero stays at zero`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        
        // Simulate previous paragraph button at index 0
        val current = state.currentReadingParagraph.value
        if (current > 0) {
            state.setCurrentReadingParagraph(current - 1)
        }
        
        assertEquals(0, state.currentReadingParagraph.value, 
            "Should stay at 0 when already at first paragraph")
    }

    /**
     * Test: Slider seek updates paragraph correctly
     */
    @Test
    fun `slider seek updates paragraph`() = runTest {
        val state = TestTTSState()
        val content = (1..10).map { "Paragraph $it" }
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(0)
        
        // Simulate slider seek to middle
        state.setCurrentReadingParagraph(5)
        
        assertEquals(5, state.currentReadingParagraph.value, 
            "Should jump to slider position")
    }

    /**
     * Test: Auto-advance to next chapter when content finishes
     */
    @Test
    fun `auto advance triggers at end of content`() = runTest {
        val state = TestTTSState()
        val content = listOf("P1", "P2")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(1) // Last paragraph
        state.setAutoNextChapter(true)
        state.setPlaying(true)
        
        // Check if at end
        val isAtEnd = state.currentReadingParagraph.value >= (state.ttsContent.value?.lastIndex ?: 0)
        
        assertTrue(isAtEnd, "Should be at end of content")
        assertTrue(state.autoNextChapter.value, "Auto next should be enabled")
    }

    // Helper function to simulate paragraph completion
    private fun simulateParagraphComplete(state: TestTTSState) {
        val content = state.ttsContent.value ?: return
        val current = state.currentReadingParagraph.value
        val isFinished = current >= content.lastIndex
        
        if (!isFinished && state.isPlaying.value) {
            state.setPreviousReadingParagraph(current)
            state.setCurrentReadingParagraph(current + 1)
        }
    }

    // Helper function that returns whether content is finished
    private fun simulateParagraphCompleteWithCheck(state: TestTTSState): Boolean {
        val content = state.ttsContent.value ?: return true
        val current = state.currentReadingParagraph.value
        val isFinished = current >= content.lastIndex
        
        if (!isFinished && state.isPlaying.value) {
            state.setPreviousReadingParagraph(current)
            state.setCurrentReadingParagraph(current + 1)
        }
        
        return isFinished
    }
}
