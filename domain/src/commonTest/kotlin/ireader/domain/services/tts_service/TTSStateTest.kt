package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for TTS State Management
 * 
 * These tests verify that state changes in the TTS system work correctly,
 * particularly focusing on:
 * - Paragraph state changes when playing
 * - Chapter state changes
 * - Play/pause state transitions
 * - State flow emissions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSStateTest {

    /**
     * Test: Initial state values are correct
     * Requirement: State should start with sensible defaults
     */
    @Test
    fun `initial state has correct default values`() = runTest {
        val state = createTestTTSState()
        
        assertEquals(0, state.currentReadingParagraph.value, "Initial paragraph should be 0")
        assertEquals(0, state.previousReadingParagraph.value, "Initial previous paragraph should be 0")
        assertFalse(state.isPlaying.value, "Initial playing state should be false")
        assertFalse(state.isLoading.value, "Initial loading state should be false")
        assertNull(state.ttsChapter.value, "Initial chapter should be null")
        assertNull(state.ttsBook.value, "Initial book should be null")
        assertNull(state.ttsContent.value, "Initial content should be null")
    }

    /**
     * Test: Setting current reading paragraph updates the state
     * Requirement: When user taps play, paragraph should change
     */
    @Test
    fun `setCurrentReadingParagraph updates state correctly`() = runTest {
        val state = createTestTTSState()
        
        state.setCurrentReadingParagraph(5)
        
        assertEquals(5, state.currentReadingParagraph.value, "Paragraph should be updated to 5")
    }

    /**
     * Test: Setting current reading paragraph emits new value via StateFlow
     * Requirement: UI should receive state updates
     */
    @Test
    fun `setCurrentReadingParagraph emits value through StateFlow`() = runTest {
        val state = createTestTTSState()
        
        // Set initial value
        state.setCurrentReadingParagraph(0)
        assertEquals(0, state.currentReadingParagraph.first())
        
        // Update value
        state.setCurrentReadingParagraph(3)
        assertEquals(3, state.currentReadingParagraph.first())
    }

    /**
     * Test: Previous paragraph is tracked separately from current
     * Requirement: Need to track previous for UI scroll behavior
     */
    @Test
    fun `previousReadingParagraph is tracked separately`() = runTest {
        val state = createTestTTSState()
        
        state.setPreviousReadingParagraph(2)
        state.setCurrentReadingParagraph(5)
        
        assertEquals(2, state.previousReadingParagraph.value, "Previous should remain 2")
        assertEquals(5, state.currentReadingParagraph.value, "Current should be 5")
        assertNotEquals(
            state.previousReadingParagraph.value,
            state.currentReadingParagraph.value,
            "Previous and current should be different"
        )
    }

    /**
     * Test: Playing state can be toggled
     * Requirement: Play/pause functionality
     */
    @Test
    fun `setPlaying toggles playing state`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.isPlaying.value, "Should start not playing")
        
        state.setPlaying(true)
        assertTrue(state.isPlaying.value, "Should be playing after setPlaying(true)")
        
        state.setPlaying(false)
        assertFalse(state.isPlaying.value, "Should not be playing after setPlaying(false)")
    }

    /**
     * Test: Content can be set and retrieved
     * Requirement: TTS needs content to read
     */
    @Test
    fun `setTtsContent updates content list`() = runTest {
        val state = createTestTTSState()
        val testContent = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        
        state.setTtsContent(testContent)
        
        assertEquals(testContent, state.ttsContent.value, "Content should match")
        assertEquals(3, state.ttsContent.value?.size, "Should have 3 paragraphs")
    }

    /**
     * Test: Paragraph index bounds checking
     * Requirement: Paragraph index should not exceed content size
     */
    @Test
    fun `paragraph index can be set within bounds`() = runTest {
        val state = createTestTTSState()
        val testContent = listOf("P1", "P2", "P3", "P4", "P5")
        state.setTtsContent(testContent)
        
        // Set to last valid index
        state.setCurrentReadingParagraph(4)
        assertEquals(4, state.currentReadingParagraph.value, "Should allow setting to last index")
        
        // Set to first index
        state.setCurrentReadingParagraph(0)
        assertEquals(0, state.currentReadingParagraph.value, "Should allow setting to first index")
    }

    /**
     * Test: Auto next chapter setting
     * Requirement: Auto-advance to next chapter when current finishes
     */
    @Test
    fun `autoNextChapter setting works correctly`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.autoNextChapter.value, "Should default to false")
        
        state.setAutoNextChapter(true)
        assertTrue(state.autoNextChapter.value, "Should be true after setting")
        
        state.setAutoNextChapter(false)
        assertFalse(state.autoNextChapter.value, "Should be false after unsetting")
    }

    /**
     * Test: Speech speed setting
     * Requirement: User can adjust speech rate
     */
    @Test
    fun `speechSpeed setting works correctly`() = runTest {
        val state = createTestTTSState()
        
        state.setSpeechSpeed(1.5f)
        assertEquals(1.5f, state.speechSpeed.value, "Speed should be 1.5")
        
        state.setSpeechSpeed(0.5f)
        assertEquals(0.5f, state.speechSpeed.value, "Speed should be 0.5")
    }

    /**
     * Test: Pitch setting
     * Requirement: User can adjust speech pitch
     */
    @Test
    fun `pitch setting works correctly`() = runTest {
        val state = createTestTTSState()
        
        state.setPitch(1.2f)
        assertEquals(1.2f, state.pitch.value, "Pitch should be 1.2")
        
        state.setPitch(0.8f)
        assertEquals(0.8f, state.pitch.value, "Pitch should be 0.8")
    }

    /**
     * Test: Loading state
     * Requirement: Show loading indicator when fetching content
     */
    @Test
    fun `loading state can be set`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.isLoading.value, "Should not be loading initially")
        
        state.setLoading(true)
        assertTrue(state.isLoading.value, "Should be loading after setLoading(true)")
        
        state.setLoading(false)
        assertFalse(state.isLoading.value, "Should not be loading after setLoading(false)")
    }

    /**
     * Test: Chapter index tracking
     * Requirement: Track current chapter position in chapter list
     */
    @Test
    fun `chapter index is tracked correctly`() = runTest {
        val state = createTestTTSState()
        
        assertEquals(-1, state.ttsCurrentChapterIndex.value, "Should default to -1")
        
        state.setTtsCurrentChapterIndex(5)
        assertEquals(5, state.ttsCurrentChapterIndex.value, "Should be 5 after setting")
    }

    /**
     * Test: Service connection state
     * Requirement: Track if TTS service is connected
     */
    @Test
    fun `service connection state is tracked`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.isServiceConnected.value, "Should not be connected initially")
        
        state.setServiceConnected(true)
        assertTrue(state.isServiceConnected.value, "Should be connected after setting")
    }

    /**
     * Test: Drawer ascending order toggle
     * Requirement: Chapter drawer can be sorted ascending/descending
     */
    @Test
    fun `drawer ascending order can be toggled`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.isDrawerAsc.value, "Should default to false (descending)")
        
        state.setDrawerAsc(true)
        assertTrue(state.isDrawerAsc.value, "Should be ascending after setting")
    }

    /**
     * Test: Sleep mode settings
     * Requirement: Sleep timer functionality
     */
    @Test
    fun `sleep mode settings work correctly`() = runTest {
        val state = createTestTTSState()
        
        assertFalse(state.sleepMode.value, "Sleep mode should be off by default")
        assertEquals(0L, state.sleepTime.value, "Sleep time should be 0 by default")
        
        state.setSleepMode(true)
        state.setSleepTime(30L)
        
        assertTrue(state.sleepMode.value, "Sleep mode should be on")
        assertEquals(30L, state.sleepTime.value, "Sleep time should be 30")
    }

    /**
     * Test: Utterance ID tracking
     * Requirement: Track current utterance for TTS callbacks
     */
    @Test
    fun `utterance ID is tracked correctly`() = runTest {
        val state = createTestTTSState()
        
        assertEquals("", state.utteranceId.value, "Should be empty initially")
        
        state.setUtteranceId("paragraph_5")
        assertEquals("paragraph_5", state.utteranceId.value, "Should match set value")
    }

    /**
     * Test: Multiple rapid state changes
     * Requirement: State should handle rapid updates correctly
     */
    @Test
    fun `handles rapid state changes correctly`() = runTest {
        val state = createTestTTSState()
        
        // Simulate rapid paragraph changes (like during playback)
        for (i in 0..10) {
            state.setCurrentReadingParagraph(i)
        }
        
        assertEquals(10, state.currentReadingParagraph.value, "Should end at paragraph 10")
    }

    /**
     * Test: State consistency during play/pause cycles
     * Requirement: State should remain consistent through play/pause
     */
    @Test
    fun `state remains consistent through play pause cycles`() = runTest {
        val state = createTestTTSState()
        val content = listOf("P1", "P2", "P3")
        
        state.setTtsContent(content)
        state.setCurrentReadingParagraph(1)
        
        // Play
        state.setPlaying(true)
        assertEquals(1, state.currentReadingParagraph.value)
        assertTrue(state.isPlaying.value)
        
        // Pause
        state.setPlaying(false)
        assertEquals(1, state.currentReadingParagraph.value, "Paragraph should not change on pause")
        assertFalse(state.isPlaying.value)
        
        // Resume
        state.setPlaying(true)
        assertEquals(1, state.currentReadingParagraph.value, "Paragraph should not change on resume")
        assertTrue(state.isPlaying.value)
    }

    // Helper function to create a test TTSState implementation
    private fun createTestTTSState(): TestTTSState {
        return TestTTSState()
    }
}
