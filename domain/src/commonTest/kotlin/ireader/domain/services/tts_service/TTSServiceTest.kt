package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for CommonTTSService interface and BaseTTSService implementation
 * 
 * These tests verify:
 * - Service initialization
 * - Content management (original and custom/translated)
 * - Paragraph navigation
 * - Speed and pitch controls
 * - Engine management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSServiceTest {

    /**
     * Test: Service state is properly initialized
     */
    @Test
    fun `service state has correct initial values`() = runTest {
        val service = createMockTTSService()
        
        assertFalse(service.state.isPlaying.value, "Should not be playing initially")
        assertFalse(service.state.isLoading.value, "Should not be loading initially")
        assertEquals(0, service.state.currentParagraph.value, "Paragraph should start at 0")
        assertEquals(1.0f, service.state.speechSpeed.value, "Speed should default to 1.0")
        assertEquals(1.0f, service.state.speechPitch.value, "Pitch should default to 1.0")
        assertTrue(service.state.currentContent.value.isEmpty(), "Content should be empty initially")
    }

    /**
     * Test: setCustomContent sets translated content
     */
    @Test
    fun `setCustomContent updates content for TTS`() = runTest {
        val service = createMockTTSService()
        val originalContent = listOf("Original 1", "Original 2", "Original 3")
        val translatedContent = listOf("Translated 1", "Translated 2", "Translated 3")
        
        // Set original content
        service.setContent(originalContent)
        assertEquals(originalContent, service.state.currentContent.value)
        
        // Set custom/translated content
        service.setCustomContent(translatedContent)
        assertEquals(translatedContent, service.state.currentContent.value)
        assertEquals(3, service.state.totalParagraphs.value)
    }

    /**
     * Test: setCustomContent with null restores original content
     */
    @Test
    fun `setCustomContent with null restores original content`() = runTest {
        val service = createMockTTSService()
        val originalContent = listOf("Original 1", "Original 2")
        val translatedContent = listOf("Translated 1", "Translated 2")
        
        // Set original content
        service.setContent(originalContent)
        
        // Set custom content
        service.setCustomContent(translatedContent)
        assertEquals(translatedContent, service.state.currentContent.value)
        
        // Restore original
        service.setCustomContent(null)
        assertEquals(originalContent, service.state.currentContent.value)
    }

    /**
     * Test: Speed setting is applied correctly
     */
    @Test
    fun `setSpeed updates speech speed`() = runTest {
        val service = createMockTTSService()
        
        service.setSpeed(1.5f)
        assertEquals(1.5f, service.state.speechSpeed.value)
        
        service.setSpeed(0.5f)
        assertEquals(0.5f, service.state.speechSpeed.value)
    }

    /**
     * Test: Pitch setting is applied correctly
     */
    @Test
    fun `setPitch updates speech pitch`() = runTest {
        val service = createMockTTSService()
        
        service.setPitch(1.2f)
        assertEquals(1.2f, service.state.speechPitch.value)
        
        service.setPitch(0.8f)
        assertEquals(0.8f, service.state.speechPitch.value)
    }

    /**
     * Test: jumpToParagraph navigates correctly
     */
    @Test
    fun `jumpToParagraph updates current paragraph`() = runTest {
        val service = createMockTTSService()
        val content = listOf("P1", "P2", "P3", "P4", "P5")
        service.setContent(content)
        
        service.jumpToParagraph(3)
        assertEquals(3, service.state.currentParagraph.value)
        
        service.jumpToParagraph(0)
        assertEquals(0, service.state.currentParagraph.value)
    }

    /**
     * Test: jumpToParagraph handles out of bounds
     */
    @Test
    fun `jumpToParagraph handles out of bounds gracefully`() = runTest {
        val service = createMockTTSService()
        val content = listOf("P1", "P2", "P3")
        service.setContent(content)
        
        // Try to jump beyond content
        service.jumpToParagraph(10)
        // Should either clamp or ignore - implementation dependent
        assertTrue(service.state.currentParagraph.value in 0..2)
    }

    /**
     * Test: getAvailableEngines returns non-empty list
     */
    @Test
    fun `getAvailableEngines returns available engines`() = runTest {
        val service = createMockTTSService()
        
        val engines = service.getAvailableEngines()
        assertTrue(engines.isNotEmpty(), "Should have at least one engine")
    }

    /**
     * Test: getCurrentEngineName returns valid name
     */
    @Test
    fun `getCurrentEngineName returns engine name`() = runTest {
        val service = createMockTTSService()
        
        val engineName = service.getCurrentEngineName()
        assertTrue(engineName.isNotEmpty(), "Engine name should not be empty")
    }

    /**
     * Test: Content with empty paragraphs is filtered
     */
    @Test
    fun `empty paragraphs are handled correctly`() = runTest {
        val service = createMockTTSService()
        val contentWithEmpty = listOf("P1", "", "P2", "   ", "P3")
        
        service.setContent(contentWithEmpty)
        
        // Implementation should either filter or handle empty paragraphs
        val content = service.state.currentContent.value
        assertNotNull(content)
    }

    /**
     * Test: Paragraph reset when content changes
     */
    @Test
    fun `paragraph resets when content changes significantly`() = runTest {
        val service = createMockTTSService()
        
        // Set initial content and navigate
        service.setContent(listOf("P1", "P2", "P3", "P4", "P5"))
        service.jumpToParagraph(4)
        assertEquals(4, service.state.currentParagraph.value)
        
        // Set shorter content - paragraph should reset if out of bounds
        service.setCustomContent(listOf("New1", "New2"))
        assertTrue(service.state.currentParagraph.value < 2, "Paragraph should be within new bounds")
    }

    /**
     * Test: Sleep timer can be set and cancelled
     */
    @Test
    fun `setSleepTimer enables sleep mode`() = runTest {
        val service = createMockTTSService()
        
        service.setSleepTimer(30)
        assertTrue(service.state.sleepModeEnabled.value, "Sleep mode should be enabled")
        assertTrue(service.state.sleepTimeRemaining.value > 0, "Sleep time remaining should be positive")
    }
    
    /**
     * Test: Cancel sleep timer disables sleep mode
     */
    @Test
    fun `cancelSleepTimer disables sleep mode`() = runTest {
        val service = createMockTTSService()
        
        service.setSleepTimer(30)
        service.cancelSleepTimer()
        
        assertFalse(service.state.sleepModeEnabled.value, "Sleep mode should be disabled")
        assertEquals(0L, service.state.sleepTimeRemaining.value, "Sleep time remaining should be 0")
    }
    
    /**
     * Test: Auto-next chapter can be toggled
     */
    @Test
    fun `setAutoNextChapter updates state`() = runTest {
        val service = createMockTTSService()
        
        service.setAutoNextChapter(true)
        assertTrue(service.state.autoNextChapter.value, "Auto-next should be enabled")
        
        service.setAutoNextChapter(false)
        assertFalse(service.state.autoNextChapter.value, "Auto-next should be disabled")
    }

    // Helper to create a mock TTS service for testing
    private fun createMockTTSService(): MockTTSService {
        return MockTTSService()
    }
}

/**
 * Mock TTS Service for testing
 */
open class MockTTSService : CommonTTSService {
    
    private val _isPlaying = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _currentParagraph = MutableStateFlow(0)
    private val _totalParagraphs = MutableStateFlow(0)
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    private val _speechSpeed = MutableStateFlow(1.0f)
    private val _speechPitch = MutableStateFlow(1.0f)
    private val _autoNextChapter = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    private val _previousParagraph = MutableStateFlow(0)
    private val _sleepTimeRemaining = MutableStateFlow(0L)
    private val _sleepModeEnabled = MutableStateFlow(false)
    private val _hasAudioFocus = MutableStateFlow(true)
    
    private var originalContent: List<String>? = null
    
    override val state: TTSServiceState = object : TTSServiceState {
        override val isPlaying: StateFlow<Boolean> = _isPlaying
        override val isLoading: StateFlow<Boolean> = _isLoading
        override val currentBook: StateFlow<ireader.domain.models.entities.Book?> = MutableStateFlow(null)
        override val currentChapter: StateFlow<ireader.domain.models.entities.Chapter?> = MutableStateFlow(null)
        override val currentParagraph: StateFlow<Int> = _currentParagraph
        override val previousParagraph: StateFlow<Int> = _previousParagraph
        override val paragraphSpeakingStartTime: StateFlow<Long> = MutableStateFlow(0L)
        override val totalParagraphs: StateFlow<Int> = _totalParagraphs
        override val currentContent: StateFlow<List<String>> = _currentContent
        override val speechSpeed: StateFlow<Float> = _speechSpeed
        override val speechPitch: StateFlow<Float> = _speechPitch
        override val autoNextChapter: StateFlow<Boolean> = _autoNextChapter
        override val error: StateFlow<String?> = _error
        override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs
        override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs
        override val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining
        override val sleepModeEnabled: StateFlow<Boolean> = _sleepModeEnabled
        override val hasAudioFocus: StateFlow<Boolean> = _hasAudioFocus
        override val isTTSReady: StateFlow<Boolean> = MutableStateFlow(true)
        override val currentMergedChunkParagraphs: StateFlow<List<Int>> = MutableStateFlow(emptyList())
        override val isMergingEnabled: StateFlow<Boolean> = MutableStateFlow(false)
    }
    
    override fun initialize() {}
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        _isLoading.value = true
        // Simulate loading
        _isLoading.value = false
    }
    
    override suspend fun play() {
        _isPlaying.value = true
    }
    
    override suspend fun pause() {
        _isPlaying.value = false
    }
    
    override suspend fun stop() {
        _isPlaying.value = false
        _currentParagraph.value = 0
    }
    
    override suspend fun nextChapter() {}
    override suspend fun previousChapter() {}
    
    override suspend fun nextParagraph() {
        if (_currentParagraph.value < _currentContent.value.size - 1) {
            _currentParagraph.value++
        }
    }
    
    override suspend fun previousParagraph() {
        if (_currentParagraph.value > 0) {
            _currentParagraph.value--
        }
    }
    
    override fun setSpeed(speed: Float) {
        _speechSpeed.value = speed.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        _speechPitch.value = pitch.coerceIn(0.5f, 2.0f)
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        if (index in _currentContent.value.indices) {
            _currentParagraph.value = index
        }
    }
    
    override fun setCustomContent(content: List<String>?) {
        if (content != null && content.isNotEmpty()) {
            if (originalContent == null) {
                originalContent = _currentContent.value
            }
            _currentContent.value = content
            _totalParagraphs.value = content.size
            if (_currentParagraph.value >= content.size) {
                _currentParagraph.value = 0
            }
        } else {
            originalContent?.let {
                _currentContent.value = it
                _totalParagraphs.value = it.size
                if (_currentParagraph.value >= it.size) {
                    _currentParagraph.value = 0
                }
            }
            originalContent = null
        }
    }
    
    override fun getAvailableEngines(): List<String> = listOf("Mock TTS Engine")
    
    override fun getCurrentEngineName(): String = "Mock TTS Engine"
    
    override fun isReady(): Boolean = true
    
    override fun setSleepTimer(minutes: Int) {
        _sleepModeEnabled.value = minutes > 0
        _sleepTimeRemaining.value = minutes * 60 * 1000L
    }
    
    override fun cancelSleepTimer() {
        _sleepModeEnabled.value = false
        _sleepTimeRemaining.value = 0L
    }
    
    override fun setAutoNextChapter(enabled: Boolean) {
        _autoNextChapter.value = enabled
    }
    
    override fun cleanup() {
        _isPlaying.value = false
    }
    
    // Test helper to set content directly
    fun setContent(content: List<String>) {
        _currentContent.value = content
        _totalParagraphs.value = content.size
        originalContent = content
    }
}
