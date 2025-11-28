package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for TTSEngine interface and implementations
 * 
 * These tests verify:
 * - Engine initialization and readiness
 * - Speech rate and pitch controls
 * - Callback handling
 * - Engine lifecycle management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSEngineTest {

    /**
     * Test: Engine reports ready state correctly
     */
    @Test
    fun `engine reports ready state correctly`() = runTest {
        val engine = MockTTSEngine()
        
        assertFalse(engine.isReady(), "Should not be ready initially")
        
        engine.initialize()
        assertTrue(engine.isReady(), "Should be ready after initialization")
    }

    /**
     * Test: Engine speed setting is applied
     */
    @Test
    fun `engine speed setting is applied`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        engine.setSpeed(1.5f)
        assertEquals(1.5f, engine.currentSpeed, "Speed should be 1.5")
        
        engine.setSpeed(0.5f)
        assertEquals(0.5f, engine.currentSpeed, "Speed should be 0.5")
    }

    /**
     * Test: Engine pitch setting is applied
     */
    @Test
    fun `engine pitch setting is applied`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        engine.setPitch(1.2f)
        assertEquals(1.2f, engine.currentPitch, "Pitch should be 1.2")
        
        engine.setPitch(0.8f)
        assertEquals(0.8f, engine.currentPitch, "Pitch should be 0.8")
    }

    /**
     * Test: Engine callback is invoked on start
     */
    @Test
    fun `engine callback invoked on start`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        var startCalled = false
        var startUtteranceId = ""
        
        engine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {
                startCalled = true
                startUtteranceId = utteranceId
            }
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String, error: String) {}
        })
        
        engine.speak("Test text", "test_1")
        
        assertTrue(startCalled, "onStart should be called")
        assertEquals("test_1", startUtteranceId, "Utterance ID should match")
    }

    /**
     * Test: Engine callback is invoked on completion
     */
    @Test
    fun `engine callback invoked on completion`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        var doneCalled = false
        var doneUtteranceId = ""
        
        engine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) {
                doneCalled = true
                doneUtteranceId = utteranceId
            }
            override fun onError(utteranceId: String, error: String) {}
        })
        
        engine.speak("Test text", "test_1")
        engine.simulateCompletion()
        
        assertTrue(doneCalled, "onDone should be called")
        assertEquals("test_1", doneUtteranceId, "Utterance ID should match")
    }

    /**
     * Test: Engine callback is invoked on error
     */
    @Test
    fun `engine callback invoked on error`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        var errorCalled = false
        var errorMessage = ""
        
        engine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String, error: String) {
                errorCalled = true
                errorMessage = error
            }
        })
        
        engine.simulateError("Test error")
        
        assertTrue(errorCalled, "onError should be called")
        assertEquals("Test error", errorMessage, "Error message should match")
    }

    /**
     * Test: Engine stop cancels current speech
     */
    @Test
    fun `engine stop cancels current speech`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        engine.speak("Long text to speak", "test_1")
        assertTrue(engine.isSpeaking, "Should be speaking")
        
        engine.stop()
        assertFalse(engine.isSpeaking, "Should not be speaking after stop")
    }

    /**
     * Test: Engine pause suspends speech
     */
    @Test
    fun `engine pause suspends speech`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        engine.speak("Text to speak", "test_1")
        assertTrue(engine.isSpeaking, "Should be speaking")
        
        engine.pause()
        assertTrue(engine.isPaused, "Should be paused")
        assertFalse(engine.isSpeaking, "Should not be actively speaking when paused")
    }

    /**
     * Test: Engine resume continues speech
     */
    @Test
    fun `engine resume continues speech`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        engine.speak("Text to speak", "test_1")
        engine.pause()
        assertTrue(engine.isPaused, "Should be paused")
        
        engine.resume()
        assertFalse(engine.isPaused, "Should not be paused after resume")
        assertTrue(engine.isSpeaking, "Should be speaking after resume")
    }

    /**
     * Test: Engine cleanup releases resources
     */
    @Test
    fun `engine cleanup releases resources`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        engine.speak("Text", "test_1")
        
        engine.cleanup()
        
        assertFalse(engine.isReady(), "Should not be ready after cleanup")
        assertFalse(engine.isSpeaking, "Should not be speaking after cleanup")
    }

    /**
     * Test: Engine name is returned correctly
     */
    @Test
    fun `engine name is returned correctly`() = runTest {
        val engine = MockTTSEngine()
        
        assertEquals("Mock TTS Engine", engine.getEngineName())
    }

    /**
     * Test: Speed bounds are respected
     */
    @Test
    fun `speed bounds are respected`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        // Test lower bound
        engine.setSpeed(0.1f)
        assertTrue(engine.currentSpeed >= 0.5f, "Speed should be clamped to minimum 0.5")
        
        // Test upper bound
        engine.setSpeed(5.0f)
        assertTrue(engine.currentSpeed <= 2.0f, "Speed should be clamped to maximum 2.0")
    }

    /**
     * Test: Pitch bounds are respected
     */
    @Test
    fun `pitch bounds are respected`() = runTest {
        val engine = MockTTSEngine()
        engine.initialize()
        
        // Test lower bound
        engine.setPitch(0.1f)
        assertTrue(engine.currentPitch >= 0.5f, "Pitch should be clamped to minimum 0.5")
        
        // Test upper bound
        engine.setPitch(5.0f)
        assertTrue(engine.currentPitch <= 2.0f, "Pitch should be clamped to maximum 2.0")
    }
}

/**
 * Mock TTS Engine for testing
 */
class MockTTSEngine : TTSEngine {
    private var initialized = false
    private var callback: TTSEngineCallback? = null
    private var currentUtteranceId: String = ""
    
    var currentSpeed = 1.0f
        private set
    var currentPitch = 1.0f
        private set
    var isSpeaking = false
        private set
    var isPaused = false
        private set
    
    fun initialize() {
        initialized = true
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        currentUtteranceId = utteranceId
        isSpeaking = true
        isPaused = false
        callback?.onStart(utteranceId)
    }
    
    override fun stop() {
        isSpeaking = false
        isPaused = false
    }
    
    override fun pause() {
        if (isSpeaking) {
            isPaused = true
            isSpeaking = false
        }
    }
    
    override fun resume() {
        if (isPaused) {
            isPaused = false
            isSpeaking = true
        }
    }
    
    override fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean = initialized
    
    override fun cleanup() {
        initialized = false
        isSpeaking = false
        isPaused = false
        callback = null
    }
    
    override fun getEngineName(): String = "Mock TTS Engine"
    
    // Test helpers
    fun simulateCompletion() {
        isSpeaking = false
        callback?.onDone(currentUtteranceId)
    }
    
    fun simulateError(error: String) {
        isSpeaking = false
        callback?.onError(currentUtteranceId, error)
    }
}
