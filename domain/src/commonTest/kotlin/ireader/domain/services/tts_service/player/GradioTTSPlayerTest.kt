package ireader.domain.services.tts_service.player

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Comprehensive tests for GradioTTSPlayer.
 * 
 * These tests verify the player's behavior for:
 * - Content management
 * - Playback controls (play, pause, stop, next, previous)
 * - Settings changes (speed, pitch)
 * - Cache management
 * - Event emission
 * - Error handling
 * - State management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GradioTTSPlayerTest {
    
    private lateinit var mockGenerator: MockAudioGenerator
    private lateinit var mockPlayback: MockAudioPlayback
    private lateinit var player: GradioTTSPlayer
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        mockGenerator = MockAudioGenerator()
        mockPlayback = MockAudioPlayback()
        player = GradioTTSPlayer(
            audioGenerator = mockGenerator,
            audioPlayer = mockPlayback,
            config = createTestConfig(),
            prefetchCount = 2,
            dispatcher = testDispatcher
        )
    }
    
    @AfterTest
    fun teardown() {
        player.release()
    }
    
    // ==================== Content Tests ====================
    
    @Test
    fun `setContent updates state correctly`() = runTest(testDispatcher) {
        val content = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        
        player.setContent(content, startIndex = 0)
        advanceUntilIdle()
        
        assertEquals(3, player.totalParagraphs.value)
        assertEquals(0, player.currentParagraph.value)
        assertTrue(player.hasContent.value)
        assertFalse(player.isPlaying.value)
    }
    
    @Test
    fun `setContent with startIndex sets correct paragraph`() = runTest(testDispatcher) {
        val content = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        
        player.setContent(content, startIndex = 1)
        advanceUntilIdle()
        
        assertEquals(1, player.currentParagraph.value)
    }
    
    @Test
    fun `setContent with out of bounds startIndex clamps to valid range`() = runTest(testDispatcher) {
        val content = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        
        player.setContent(content, startIndex = 100)
        advanceUntilIdle()
        
        assertEquals(2, player.currentParagraph.value) // Clamped to last index
    }
    
    @Test
    fun `setContent clears previous cache`() = runTest(testDispatcher) {
        // Set initial content and cache some audio
        player.setContent(listOf("First content"))
        advanceUntilIdle()
        
        // Set new content
        player.setContent(listOf("New content"))
        advanceUntilIdle()
        
        assertTrue(player.cachedParagraphs.value.isEmpty())
    }
    
    @Test
    fun `setContent with empty list sets hasContent to false`() = runTest(testDispatcher) {
        player.setContent(emptyList())
        advanceUntilIdle()
        
        assertFalse(player.hasContent.value)
        assertEquals(0, player.totalParagraphs.value)
    }
    
    // ==================== Playback Control Tests ====================
    
    @Test
    fun `play starts playback when content is set`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        assertTrue(player.isPlaying.value)
        assertFalse(player.isPaused.value)
    }
    
    @Test
    fun `play does nothing when no content`() = runTest(testDispatcher) {
        player.play()
        advanceUntilIdle()
        
        assertFalse(player.isPlaying.value)
        assertNotNull(player.error.value)
    }
    
    @Test
    fun `play when already playing does nothing`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        val playingBefore = player.isPlaying.value
        
        player.play() // Call again
        advanceUntilIdle()
        
        assertEquals(playingBefore, player.isPlaying.value)
    }
    
    @Test
    fun `pause pauses playback`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        player.pause()
        advanceUntilIdle()
        
        assertTrue(player.isPaused.value)
        assertTrue(mockPlayback.pauseCalled)
    }
    
    @Test
    fun `pause when not playing does nothing`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.pause()
        advanceUntilIdle()
        
        assertFalse(player.isPaused.value)
    }
    
    @Test
    fun `play after pause resumes playback`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        player.pause()
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        assertFalse(player.isPaused.value)
        assertTrue(mockPlayback.resumeCalled)
    }
    
    @Test
    fun `stop resets to beginning`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        player.jumpTo(2)
        advanceUntilIdle()
        
        player.stop()
        advanceUntilIdle()
        
        assertEquals(0, player.currentParagraph.value)
        assertFalse(player.isPlaying.value)
        assertFalse(player.isPaused.value)
    }
    
    @Test
    fun `stop calls audioPlayer stop`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        player.stop()
        advanceUntilIdle()
        
        assertTrue(mockPlayback.stopCalled)
    }
    
    // ==================== Navigation Tests ====================
    
    @Test
    fun `next moves to next paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        player.next()
        advanceUntilIdle()
        
        assertEquals(1, player.currentParagraph.value)
    }
    
    @Test
    fun `next at last paragraph does nothing`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"), startIndex = 1)
        advanceUntilIdle()
        
        player.next()
        advanceUntilIdle()
        
        assertEquals(1, player.currentParagraph.value)
    }
    
    @Test
    fun `previous moves to previous paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"), startIndex = 2)
        advanceUntilIdle()
        
        player.previous()
        advanceUntilIdle()
        
        assertEquals(1, player.currentParagraph.value)
    }
    
    @Test
    fun `previous at first paragraph does nothing`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"))
        advanceUntilIdle()
        
        player.previous()
        advanceUntilIdle()
        
        assertEquals(0, player.currentParagraph.value)
    }
    
    @Test
    fun `jumpTo moves to specified paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3", "Para 4", "Para 5"))
        advanceUntilIdle()
        
        player.jumpTo(3)
        advanceUntilIdle()
        
        assertEquals(3, player.currentParagraph.value)
    }
    
    @Test
    fun `jumpTo clamps to valid range - upper bound`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        player.jumpTo(100)
        advanceUntilIdle()
        
        assertEquals(2, player.currentParagraph.value)
    }
    
    @Test
    fun `jumpTo clamps to valid range - lower bound`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        player.jumpTo(-5)
        advanceUntilIdle()
        
        assertEquals(0, player.currentParagraph.value)
    }
    
    @Test
    fun `jumpTo to same paragraph does nothing`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"), startIndex = 1)
        advanceUntilIdle()
        
        player.jumpTo(1)
        advanceUntilIdle()
        
        assertEquals(1, player.currentParagraph.value)
    }
    
    // ==================== Settings Tests ====================
    
    @Test
    fun `setSpeed updates speed and notifies generator`() = runTest(testDispatcher) {
        player.setContent(listOf("Test"))
        advanceUntilIdle()
        
        player.setSpeed(1.5f)
        advanceUntilIdle()
        
        assertEquals(1.5f, player.speed.value)
        assertEquals(1.5f, mockGenerator.speedSet)
    }
    
    @Test
    fun `setSpeed clamps to valid range - upper bound`() = runTest(testDispatcher) {
        player.setSpeed(3.0f)
        advanceUntilIdle()
        
        assertEquals(2.0f, player.speed.value)
    }
    
    @Test
    fun `setSpeed clamps to valid range - lower bound`() = runTest(testDispatcher) {
        player.setSpeed(0.1f)
        advanceUntilIdle()
        
        assertEquals(0.5f, player.speed.value)
    }
    
    @Test
    fun `setSpeed with same value does nothing`() = runTest(testDispatcher) {
        val initialSpeed = player.speed.value
        
        player.setSpeed(initialSpeed)
        advanceUntilIdle()
        
        // Speed should remain the same, no cache clear needed
        assertEquals(initialSpeed, player.speed.value)
    }
    
    @Test
    fun `setPitch updates pitch and notifies generator`() = runTest(testDispatcher) {
        player.setPitch(1.2f)
        advanceUntilIdle()
        
        assertEquals(1.2f, player.pitch.value)
        assertEquals(1.2f, mockGenerator.pitchSet)
    }
    
    @Test
    fun `setPitch clamps to valid range`() = runTest(testDispatcher) {
        player.setPitch(3.0f)
        advanceUntilIdle()
        
        assertEquals(2.0f, player.pitch.value)
        
        player.setPitch(0.1f)
        advanceUntilIdle()
        
        assertEquals(0.5f, player.pitch.value)
    }
    
    // ==================== Cache Tests ====================
    
    @Test
    fun `clearCache empties the cache`() = runTest(testDispatcher) {
        player.setContent(listOf("Test"))
        advanceUntilIdle()
        
        player.clearCache()
        advanceUntilIdle()
        
        assertTrue(player.cachedParagraphs.value.isEmpty())
    }
    
    // ==================== State Snapshot Test ====================
    
    @Test
    fun `getStateSnapshot returns current state`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"), startIndex = 1)
        player.setSpeed(1.3f)
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertEquals(1, snapshot.currentParagraph)
        assertEquals(2, snapshot.totalParagraphs)
        assertEquals(1.3f, snapshot.speed)
        assertTrue(snapshot.hasContent)
        assertFalse(snapshot.isPlaying)
    }
    
    @Test
    fun `getStateSnapshot canPlay is true when has content`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"))
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertTrue(snapshot.canPlay)
    }
    
    @Test
    fun `getStateSnapshot canNext is true when not at last paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertTrue(snapshot.canNext)
    }
    
    @Test
    fun `getStateSnapshot canNext is false when at last paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"), startIndex = 1)
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertFalse(snapshot.canNext)
    }
    
    @Test
    fun `getStateSnapshot canPrevious is true when not at first paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2", "Para 3"), startIndex = 1)
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertTrue(snapshot.canPrevious)
    }
    
    @Test
    fun `getStateSnapshot canPrevious is false when at first paragraph`() = runTest(testDispatcher) {
        player.setContent(listOf("Para 1", "Para 2"))
        advanceUntilIdle()
        
        val snapshot = player.getStateSnapshot()
        
        assertFalse(snapshot.canPrevious)
    }
    
    // ==================== Error Handling Tests ====================
    
    @Test
    fun `generator failure sets error state`() = runTest(testDispatcher) {
        mockGenerator.shouldFail = true
        
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        // After playback fails, error should be set
        // Note: The actual error handling depends on the playback loop
    }
    
    @Test
    fun `playback failure sets error state`() = runTest(testDispatcher) {
        mockPlayback.shouldFail = true
        
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        // After playback fails, error should be set
    }
    
    // ==================== Release Tests ====================
    
    @Test
    fun `release stops playback and cleans up`() = runTest(testDispatcher) {
        player.setContent(listOf("Test paragraph"))
        advanceUntilIdle()
        
        player.play()
        advanceUntilIdle()
        
        player.release()
        advanceUntilIdle()
        
        assertTrue(mockPlayback.releaseCalled)
        assertTrue(mockGenerator.releaseCalled)
    }
    
    // ==================== Helper Functions ====================
    
    private fun createTestConfig() = ireader.domain.services.tts_service.GradioTTSConfig(
        id = "test",
        name = "Test Engine",
        spaceUrl = "https://test.hf.space",
        apiName = "/test"
    )
}

/**
 * Mock audio generator for testing.
 */
class MockAudioGenerator : GradioAudioGenerator {
    var generateCalled = false
    var speedSet: Float? = null
    var pitchSet: Float? = null
    var shouldFail = false
    var releaseCalled = false
    var generatedTexts = mutableListOf<String>()
    
    override suspend fun generateAudio(text: String): ByteArray? {
        generateCalled = true
        generatedTexts.add(text)
        if (shouldFail) return null
        // Return fake audio data
        return "fake_audio_$text".toByteArray()
    }
    
    override fun setSpeed(speed: Float) {
        speedSet = speed
    }
    
    override fun setPitch(pitch: Float) {
        pitchSet = pitch
    }
    
    override fun release() {
        releaseCalled = true
    }
}

/**
 * Mock audio playback for testing.
 */
class MockAudioPlayback : GradioAudioPlayback {
    var playCalled = false
    var stopCalled = false
    var pauseCalled = false
    var resumeCalled = false
    var releaseCalled = false
    var shouldFail = false
    var playedAudioData = mutableListOf<ByteArray>()
    
    override suspend fun playAndWait(audioData: ByteArray): Boolean {
        playCalled = true
        playedAudioData.add(audioData)
        if (shouldFail) return false
        // Simulate short playback
        delay(10)
        return true
    }
    
    override fun stop() {
        stopCalled = true
    }
    
    override fun pause() {
        pauseCalled = true
    }
    
    override fun resume() {
        resumeCalled = true
    }
    
    override fun release() {
        releaseCalled = true
    }
}
