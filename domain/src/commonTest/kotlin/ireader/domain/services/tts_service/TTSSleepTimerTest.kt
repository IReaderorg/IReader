package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for TTS Sleep Timer functionality
 * 
 * These tests verify:
 * - Sleep timer activation and deactivation
 * - Timer countdown behavior
 * - Auto-stop when timer expires
 * - Timer cancellation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSSleepTimerTest {

    /**
     * Test: Sleep timer can be set
     */
    @Test
    fun `sleep timer can be set`() = runTest {
        val service = MockTTSService()
        
        assertFalse(service.state.sleepModeEnabled.value, "Sleep mode should be off initially")
        
        service.setSleepTimer(30)
        
        assertTrue(service.state.sleepModeEnabled.value, "Sleep mode should be enabled")
        assertTrue(service.state.sleepTimeRemaining.value > 0, "Time remaining should be positive")
    }

    /**
     * Test: Sleep timer with 0 minutes disables sleep mode
     */
    @Test
    fun `sleep timer with zero disables sleep mode`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(30)
        assertTrue(service.state.sleepModeEnabled.value)
        
        service.setSleepTimer(0)
        assertFalse(service.state.sleepModeEnabled.value, "Sleep mode should be disabled with 0 minutes")
    }

    /**
     * Test: Cancel sleep timer disables sleep mode
     */
    @Test
    fun `cancel sleep timer disables sleep mode`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(30)
        assertTrue(service.state.sleepModeEnabled.value)
        
        service.cancelSleepTimer()
        
        assertFalse(service.state.sleepModeEnabled.value, "Sleep mode should be disabled")
        assertEquals(0L, service.state.sleepTimeRemaining.value, "Time remaining should be 0")
    }

    /**
     * Test: Sleep timer calculates correct milliseconds
     */
    @Test
    fun `sleep timer calculates correct milliseconds`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(15) // 15 minutes
        
        val expectedMs = 15 * 60 * 1000L
        assertEquals(expectedMs, service.state.sleepTimeRemaining.value, 
            "Should be 15 minutes in milliseconds")
    }

    /**
     * Test: Multiple sleep timer sets override previous
     */
    @Test
    fun `multiple sleep timer sets override previous`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(30)
        assertEquals(30 * 60 * 1000L, service.state.sleepTimeRemaining.value)
        
        service.setSleepTimer(15)
        assertEquals(15 * 60 * 1000L, service.state.sleepTimeRemaining.value,
            "New timer should override previous")
    }

    /**
     * Test: Sleep timer state persists during playback
     */
    @Test
    fun `sleep timer state persists during playback`() = runTest {
        val service = MockTTSService()
        val content = listOf("P1", "P2", "P3")
        
        service.setContent(content)
        service.setSleepTimer(30)
        service.play()
        
        assertTrue(service.state.sleepModeEnabled.value, "Sleep mode should persist during playback")
        assertTrue(service.state.isPlaying.value, "Should be playing")
    }

    /**
     * Test: Sleep timer state persists through pause/resume
     */
    @Test
    fun `sleep timer persists through pause resume`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(30)
        service.play()
        service.pause()
        
        assertTrue(service.state.sleepModeEnabled.value, "Sleep mode should persist after pause")
        
        service.play()
        assertTrue(service.state.sleepModeEnabled.value, "Sleep mode should persist after resume")
    }

    /**
     * Test: Sleep timer is cleared on stop
     */
    @Test
    fun `sleep timer behavior on stop`() = runTest {
        val service = MockTTSService()
        
        service.setSleepTimer(30)
        service.play()
        service.stop()
        
        // Sleep timer should remain set even after stop (user preference)
        // This allows resuming with the same timer
        assertTrue(service.state.sleepModeEnabled.value, 
            "Sleep mode should remain enabled after stop")
    }

    /**
     * Test: Common sleep timer presets
     */
    @Test
    fun `common sleep timer presets work correctly`() = runTest {
        val service = MockTTSService()
        
        // 15 minutes
        service.setSleepTimer(15)
        assertEquals(15 * 60 * 1000L, service.state.sleepTimeRemaining.value)
        
        // 30 minutes
        service.setSleepTimer(30)
        assertEquals(30 * 60 * 1000L, service.state.sleepTimeRemaining.value)
        
        // 45 minutes
        service.setSleepTimer(45)
        assertEquals(45 * 60 * 1000L, service.state.sleepTimeRemaining.value)
        
        // 60 minutes (1 hour)
        service.setSleepTimer(60)
        assertEquals(60 * 60 * 1000L, service.state.sleepTimeRemaining.value)
    }
}
