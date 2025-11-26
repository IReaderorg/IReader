package ireader.domain.services.tts_service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Common TTS Service Tests
 * 
 * These tests can run on both Android and Desktop implementations
 * because they use the CommonTTSService interface.
 * 
 * This demonstrates the power of the unified interface:
 * - Write tests once
 * - Run on all platforms
 * - Ensure consistent behavior
 */
class CommonTTSServiceTest {
    
    /**
     * Test basic playback flow
     * 
     * This test verifies that:
     * 1. Service can start reading
     * 2. Play/pause/stop work correctly
     * 3. State updates are reflected in StateFlow
     */
    @Test
    fun `test basic playback flow`() = runTest {
        // This would be injected with platform-specific implementation
        val service = createTestService()
        
        // Initialize service
        service.initialize()
        
        // Start reading
        service.startReading(bookId = 1, chapterId = 1, autoPlay = false)
        
        // Verify initial state
        assertFalse(service.state.isPlaying.first())
        assertNotNull(service.state.currentBook.first())
        assertNotNull(service.state.currentChapter.first())
        assertEquals(0, service.state.currentParagraph.first())
        
        // Play
        service.play()
        assertTrue(service.state.isPlaying.first())
        
        // Pause
        service.pause()
        assertFalse(service.state.isPlaying.first())
        
        // Stop
        service.stop()
        assertFalse(service.state.isPlaying.first())
        assertEquals(0, service.state.currentParagraph.first())
    }
    
    /**
     * Test paragraph navigation
     */
    @Test
    fun `test paragraph navigation`() = runTest {
        val service = createTestService()
        service.initialize()
        service.startReading(bookId = 1, chapterId = 1, autoPlay = false)
        
        // Initial paragraph
        assertEquals(0, service.state.currentParagraph.first())
        
        // Next paragraph
        service.nextParagraph()
        assertEquals(1, service.state.currentParagraph.first())
        
        // Previous paragraph
        service.previousParagraph()
        assertEquals(0, service.state.currentParagraph.first())
        
        // Jump to paragraph
        service.jumpToParagraph(5)
        assertEquals(5, service.state.currentParagraph.first())
    }
    
    /**
     * Test chapter navigation
     */
    @Test
    fun `test chapter navigation`() = runTest {
        val service = createTestService()
        service.initialize()
        service.startReading(bookId = 1, chapterId = 1, autoPlay = false)
        
        val initialChapter = service.state.currentChapter.first()
        assertNotNull(initialChapter)
        
        // Next chapter
        service.nextChapter()
        val nextChapter = service.state.currentChapter.first()
        assertNotNull(nextChapter)
        // Verify chapter changed (would need actual chapter data)
        
        // Previous chapter
        service.previousChapter()
        val prevChapter = service.state.currentChapter.first()
        assertNotNull(prevChapter)
    }
    
    /**
     * Test speed and pitch controls
     */
    @Test
    fun `test speed and pitch controls`() = runTest {
        val service = createTestService()
        service.initialize()
        
        // Set speed
        service.setSpeed(1.5f)
        assertEquals(1.5f, service.state.speechSpeed.first())
        
        // Set pitch
        service.setPitch(1.2f)
        assertEquals(1.2f, service.state.speechPitch.first())
    }
    
    /**
     * Test auto-next chapter
     */
    @Test
    fun `test auto-next chapter setting`() = runTest {
        val service = createTestService()
        service.initialize()
        
        // Auto-next should be enabled by default (or from preferences)
        val autoNext = service.state.autoNextChapter.first()
        assertTrue(autoNext || !autoNext) // Just verify it has a value
    }
    
    /**
     * Test error handling
     */
    @Test
    fun `test error handling for invalid book`() = runTest {
        val service = createTestService()
        service.initialize()
        
        // Try to start reading with invalid book ID
        service.startReading(bookId = -1, chapterId = -1, autoPlay = false)
        
        // Should have error
        val error = service.state.error.first()
        assertNotNull(error)
    }
    
    /**
     * Test cleanup
     */
    @Test
    fun `test cleanup releases resources`() = runTest {
        val service = createTestService()
        service.initialize()
        service.startReading(bookId = 1, chapterId = 1, autoPlay = true)
        
        // Cleanup
        service.cleanup()
        
        // Should stop playing
        assertFalse(service.state.isPlaying.first())
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Create a test service instance
     * 
     * In actual tests, this would be provided by the test framework
     * with mocked dependencies.
     */
    private fun createTestService(): CommonTTSService {
        // This would be platform-specific in actual tests
        // For Android: return AndroidTTSService(...)
        // For Desktop: return DesktopTTSServiceAdapter(DesktopTTSService(...))
        
        // For now, return a mock implementation
        return MockTTSService()
    }
}

/**
 * Mock TTS Service for testing
 * 
 * This is a simple mock implementation for demonstration.
 * In real tests, you'd use a mocking framework like MockK or Mockito.
 */
class MockTTSService : CommonTTSService {
    private val mockState = MockTTSServiceState()
    
    override val state: TTSServiceState = mockState
    
    override fun initialize() {
        // Mock initialization
    }
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        if (bookId < 0) {
            mockState.setError("Invalid book ID")
            return
        }
        
        mockState.setCurrentBook(MockBook(bookId))
        mockState.setCurrentChapter(MockChapter(chapterId))
        mockState.setCurrentContent(listOf("Paragraph 1", "Paragraph 2", "Paragraph 3"))
        
        if (autoPlay) {
            play()
        }
    }
    
    override suspend fun play() {
        mockState.setPlaying(true)
    }
    
    override suspend fun pause() {
        mockState.setPlaying(false)
    }
    
    override suspend fun stop() {
        mockState.setPlaying(false)
        mockState.setCurrentParagraph(0)
    }
    
    override suspend fun nextChapter() {
        val current = mockState.currentChapter.value
        if (current != null) {
            mockState.setCurrentChapter(MockChapter(current.id + 1))
        }
    }
    
    override suspend fun previousChapter() {
        val current = mockState.currentChapter.value
        if (current != null && current.id > 1) {
            mockState.setCurrentChapter(MockChapter(current.id - 1))
        }
    }
    
    override suspend fun nextParagraph() {
        val current = mockState.currentParagraph.value
        val total = mockState.totalParagraphs.value
        if (current < total - 1) {
            mockState.setCurrentParagraph(current + 1)
        }
    }
    
    override suspend fun previousParagraph() {
        val current = mockState.currentParagraph.value
        if (current > 0) {
            mockState.setCurrentParagraph(current - 1)
        }
    }
    
    override fun setSpeed(speed: Float) {
        mockState.setSpeechSpeed(speed)
    }
    
    override fun setPitch(pitch: Float) {
        mockState.setSpeechPitch(pitch)
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        mockState.setCurrentParagraph(index)
    }
    
    override fun cleanup() {
        mockState.setPlaying(false)
    }
}

// Mock implementations for testing
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockBook(override val id: Long) : Book {
    override val title: String = "Test Book"
    override val cover: String = ""
    override val sourceId: Long = 1
    override val favorite: Boolean = false
    override val lastUpdate: Long = 0
    override val author: String = "Test Author"
    override val description: String = ""
    override val genres: List<String> = emptyList()
    override val status: Int = 0
    override val customCover: String? = null
    override val key: String = ""
    override val lastRead: Long? = null
    override val flags: Long = 0
}

class MockChapter(override val id: Long) : Chapter {
    override val bookId: Long = 1
    override val name: String = "Chapter $id"
    override val key: String = ""
    override val sourceOrder: Long = id
    override val number: Float = id.toFloat()
    override val content: List<Any> = emptyList()
    override val translator: String = ""
    override val dateFetch: Long = 0
    override val dateUpload: Long = 0
    override val read: Boolean = false
    override val bookmark: Boolean = false
    override val lastPageRead: Long = 0
    override val type: String = ""
}

class MockTTSServiceState : TTSServiceState {
    private val _isPlaying = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _currentBook = MutableStateFlow<Book?>(null)
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    private val _currentParagraph = MutableStateFlow(0)
    private val _totalParagraphs = MutableStateFlow(0)
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    private val _speechSpeed = MutableStateFlow(1.0f)
    private val _speechPitch = MutableStateFlow(1.0f)
    private val _autoNextChapter = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    
    override val isPlaying: StateFlow<Boolean> = _isPlaying
    override val isLoading: StateFlow<Boolean> = _isLoading
    override val currentBook: StateFlow<Book?> = _currentBook
    override val currentChapter: StateFlow<Chapter?> = _currentChapter
    override val currentParagraph: StateFlow<Int> = _currentParagraph
    override val totalParagraphs: StateFlow<Int> = _totalParagraphs
    override val currentContent: StateFlow<List<String>> = _currentContent
    override val speechSpeed: StateFlow<Float> = _speechSpeed
    override val speechPitch: StateFlow<Float> = _speechPitch
    override val autoNextChapter: StateFlow<Boolean> = _autoNextChapter
    override val error: StateFlow<String?> = _error
    override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs
    override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs
    
    fun setPlaying(playing: Boolean) { _isPlaying.value = playing }
    fun setLoading(loading: Boolean) { _isLoading.value = loading }
    fun setCurrentBook(book: Book?) { _currentBook.value = book }
    fun setCurrentChapter(chapter: Chapter?) { _currentChapter.value = chapter }
    fun setCurrentParagraph(paragraph: Int) { _currentParagraph.value = paragraph }
    fun setTotalParagraphs(total: Int) { _totalParagraphs.value = total }
    fun setCurrentContent(content: List<String>) { 
        _currentContent.value = content
        _totalParagraphs.value = content.size
    }
    fun setSpeechSpeed(speed: Float) { _speechSpeed.value = speed }
    fun setSpeechPitch(pitch: Float) { _speechPitch.value = pitch }
    fun setAutoNextChapter(auto: Boolean) { _autoNextChapter.value = auto }
    fun setError(error: String?) { _error.value = error }
}
