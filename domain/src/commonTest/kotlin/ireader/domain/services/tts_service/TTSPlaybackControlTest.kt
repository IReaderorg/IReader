//package ireader.domain.services.tts_service
//
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
///**
// * Tests for TTS Playback Control functionality
// *
// * These tests verify:
// * - Play/Pause/Stop controls
// * - Navigation (next/previous paragraph and chapter)
// * - Speed and pitch adjustments
// * - Auto-next chapter behavior
// */
//@OptIn(ExperimentalCoroutinesApi::class)
//class TTSPlaybackControlTest {
//
//    /**
//     * Test: Play starts playback
//     */
//    @Test
//    fun `play starts playback`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3")
//
//        service.setContent(content)
//        assertFalse(service.state.isPlaying.value, "Should not be playing initially")
//
//        service.play()
//        assertTrue(service.state.isPlaying.value, "Should be playing after play()")
//    }
//
//    /**
//     * Test: Pause stops playback
//     */
//    @Test
//    fun `pause stops playback`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3")
//
//        service.setContent(content)
//        service.play()
//        assertTrue(service.state.isPlaying.value)
//
//        service.pause()
//        assertFalse(service.state.isPlaying.value, "Should not be playing after pause()")
//    }
//
//    /**
//     * Test: Stop resets playback state
//     */
//    @Test
//    fun `stop resets playback state`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3")
//
//        service.setContent(content)
//        service.jumpToParagraph(2)
//        service.play()
//
//        service.stop()
//
//        assertFalse(service.state.isPlaying.value, "Should not be playing after stop()")
//        assertEquals(0, service.state.currentParagraph.value, "Paragraph should reset to 0")
//    }
//
//    /**
//     * Test: Next paragraph advances correctly
//     */
//    @Test
//    fun `next paragraph advances correctly`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3", "P4")
//
//        service.setContent(content)
//        assertEquals(0, service.state.currentParagraph.value)
//
//        service.nextParagraph()
//        assertEquals(1, service.state.currentParagraph.value)
//
//        service.nextParagraph()
//        assertEquals(2, service.state.currentParagraph.value)
//    }
//
//    /**
//     * Test: Next paragraph at end stays at end
//     */
//    @Test
//    fun `next paragraph at end stays at end`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2")
//
//        service.setContent(content)
//        service.jumpToParagraph(1) // Last paragraph
//
//        service.nextParagraph()
//        assertEquals(1, service.state.currentParagraph.value, "Should stay at last paragraph")
//    }
//
//    /**
//     * Test: Previous paragraph goes back correctly
//     */
//    @Test
//    fun `previous paragraph goes back correctly`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3", "P4")
//
//        service.setContent(content)
//        service.jumpToParagraph(3)
//
//        service.previousParagraph()
//        assertEquals(2, service.state.currentParagraph.value)
//
//        service.previousParagraph()
//        assertEquals(1, service.state.currentParagraph.value)
//    }
//
//    /**
//     * Test: Previous paragraph at start stays at start
//     */
//    @Test
//    fun `previous paragraph at start stays at start`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2")
//
//        service.setContent(content)
//        assertEquals(0, service.state.currentParagraph.value)
//
//        service.previousParagraph()
//        assertEquals(0, service.state.currentParagraph.value, "Should stay at first paragraph")
//    }
//
//    /**
//     * Test: Speed adjustment works
//     */
//    @Test
//    fun `speed adjustment works`() = runTest {
//        val service = MockTTSService()
//
//        service.setSpeed(1.5f)
//        assertEquals(1.5f, service.state.speechSpeed.value)
//
//        service.setSpeed(0.75f)
//        assertEquals(0.75f, service.state.speechSpeed.value)
//    }
//
//    /**
//     * Test: Pitch adjustment works
//     */
//    @Test
//    fun `pitch adjustment works`() = runTest {
//        val service = MockTTSService()
//
//        service.setPitch(1.2f)
//        assertEquals(1.2f, service.state.speechPitch.value)
//
//        service.setPitch(0.9f)
//        assertEquals(0.9f, service.state.speechPitch.value)
//    }
//
//    /**
//     * Test: Auto-next chapter can be enabled
//     */
//    @Test
//    fun `auto next chapter can be enabled`() = runTest {
//        val service = MockTTSService()
//
//        assertFalse(service.state.autoNextChapter.value, "Should be disabled by default")
//
//        service.setAutoNextChapter(true)
//        assertTrue(service.state.autoNextChapter.value, "Should be enabled")
//    }
//
//    /**
//     * Test: Auto-next chapter can be disabled
//     */
//    @Test
//    fun `auto next chapter can be disabled`() = runTest {
//        val service = MockTTSService()
//
//        service.setAutoNextChapter(true)
//        assertTrue(service.state.autoNextChapter.value)
//
//        service.setAutoNextChapter(false)
//        assertFalse(service.state.autoNextChapter.value, "Should be disabled")
//    }
//
//    /**
//     * Test: Jump to paragraph works
//     */
//    @Test
//    fun `jump to paragraph works`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3", "P4", "P5")
//
//        service.setContent(content)
//
//        service.jumpToParagraph(3)
//        assertEquals(3, service.state.currentParagraph.value)
//
//        service.jumpToParagraph(0)
//        assertEquals(0, service.state.currentParagraph.value)
//
//        service.jumpToParagraph(4)
//        assertEquals(4, service.state.currentParagraph.value)
//    }
//
//    /**
//     * Test: Jump to invalid paragraph is handled
//     */
//    @Test
//    fun `jump to invalid paragraph is handled`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3")
//
//        service.setContent(content)
//        service.jumpToParagraph(1)
//
//        // Try to jump beyond bounds
//        service.jumpToParagraph(10)
//        assertTrue(service.state.currentParagraph.value in 0..2,
//            "Should stay within valid bounds")
//    }
//
//    /**
//     * Test: Playback state persists through navigation
//     */
//    @Test
//    fun `playback state persists through navigation`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2", "P3", "P4")
//
//        service.setContent(content)
//        service.play()
//        assertTrue(service.state.isPlaying.value)
//
//        service.nextParagraph()
//        assertTrue(service.state.isPlaying.value, "Should still be playing after next")
//
//        service.previousParagraph()
//        assertTrue(service.state.isPlaying.value, "Should still be playing after previous")
//
//        service.jumpToParagraph(3)
//        assertTrue(service.state.isPlaying.value, "Should still be playing after jump")
//    }
//
//    /**
//     * Test: Multiple play calls don't cause issues
//     */
//    @Test
//    fun `multiple play calls are idempotent`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2")
//
//        service.setContent(content)
//
//        service.play()
//        service.play()
//        service.play()
//
//        assertTrue(service.state.isPlaying.value, "Should be playing")
//        assertEquals(0, service.state.currentParagraph.value, "Paragraph should not change")
//    }
//
//    /**
//     * Test: Multiple pause calls don't cause issues
//     */
//    @Test
//    fun `multiple pause calls are idempotent`() = runTest {
//        val service = MockTTSService()
//        val content = listOf("P1", "P2")
//
//        service.setContent(content)
//        service.play()
//
//        service.pause()
//        service.pause()
//        service.pause()
//
//        assertFalse(service.state.isPlaying.value, "Should not be playing")
//    }
//
//    /**
//     * Test: Play without content doesn't crash
//     */
//    @Test
//    fun `play without content is handled gracefully`() = runTest {
//        val service = MockTTSService()
//
//        // Should not throw
//        service.play()
//
//        // State should be consistent
//        assertTrue(service.state.currentContent.value.isEmpty())
//    }
//}
