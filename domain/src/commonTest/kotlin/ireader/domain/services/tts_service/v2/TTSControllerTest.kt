package ireader.domain.services.tts_service.v2

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TTSControllerTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // Mock content loader
    private class MockContentLoader : TTSContentLoader {
        var loadedBookId: Long? = null
        var loadedChapterId: Long? = null
        var shouldFail = false
        var mockParagraphs = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        var nextChapterId: Long? = null
        var previousChapterId: Long? = null
        
        override suspend fun loadChapter(bookId: Long, chapterId: Long): TTSContentLoader.ChapterContent {
            loadedBookId = bookId
            loadedChapterId = chapterId
            
            if (shouldFail) {
                throw IllegalStateException("Mock failure")
            }
            
            return TTSContentLoader.ChapterContent(
                book = Book(id = bookId, title = "Test Book", key = "test", sourceId = 1),
                chapter = Chapter(id = chapterId, bookId = bookId, key = "ch1", name = "Chapter 1"),
                paragraphs = mockParagraphs
            )
        }
        
        override suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long? {
            return nextChapterId
        }
        
        override suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long? {
            return previousChapterId
        }

        override fun subscribeChapters(bookId: Long): kotlinx.coroutines.flow.Flow<List<ireader.domain.models.entities.Chapter>> {
            return kotlinx.coroutines.flow.emptyFlow()
        }
    }
    
    // Mock TTS engine
    private class MockEngine : TTSEngine {
        private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
        override val events: Flow<EngineEvent> = _events
        override val name: String = "Mock Engine"
        
        var speakCalled = false
        var lastText: String? = null
        var lastUtteranceId: String? = null
        var stopCalled = false
        var pauseCalled = false
        var resumeCalled = false
        var speedTTS = 1.0f
        var ttsPitch = 1.0f
        var ready = true
        
        override suspend fun speak(text: String, utteranceId: String) {
            speakCalled = true
            lastText = text
            lastUtteranceId = utteranceId
            _events.emit(EngineEvent.Started(utteranceId))
        }
        
        override fun stop() { stopCalled = true }
        override fun pause() { pauseCalled = true }
        override fun resume() { resumeCalled = true }
        override fun setSpeed(speed: Float) { this.speedTTS = speed }
        override fun setPitch(pitch: Float) { this.ttsPitch = pitch }
        override fun isReady() = ready
        override fun release() {}
        
        // Helper to simulate completion
        suspend fun completeUtterance(utteranceId: String) {
            _events.emit(EngineEvent.Completed(utteranceId))
        }
        
        suspend fun emitReady() {
            _events.emit(EngineEvent.Ready)
        }
    }
    
    @Test
    fun `initial state is idle`() = runTest {
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { MockEngine() }
        )
        
        val state = controller.state.value
        assertEquals(PlaybackState.IDLE, state.playbackState)
        assertFalse(state.isPlaying)
        assertFalse(state.hasContent)
        
        controller.destroy()
    }
    
    @Test
    fun `initialize creates engine`() = runTest(testDispatcher) {
        var engineCreated = false
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { 
                engineCreated = true
                MockEngine()
            }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        
        // Advance the test dispatcher to process commands
        testScheduler.advanceUntilIdle()
        
        assertTrue(engineCreated)
        
        controller.destroy()
    }
    
    @Test
    fun `load chapter updates state`() = runTest(testDispatcher) {
        val contentLoader = MockContentLoader()
        val controller = TTSController(
            contentLoader = contentLoader,
            nativeEngineFactory = { MockEngine() }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(bookId = 1, chapterId = 2, startParagraph = 0))
        
        // Advance the test dispatcher to process commands
        testScheduler.advanceUntilIdle()
        
        val state = controller.state.value
        assertEquals(1L, contentLoader.loadedBookId)
        assertEquals(2L, contentLoader.loadedChapterId)
        assertEquals(3, state.totalParagraphs)
        assertEquals(0, state.currentParagraphIndex)
        assertTrue(state.hasContent)
        
        controller.destroy()
    }
    
    @Test
    fun `set speed updates state and engine`() = runTest(testDispatcher) {
        val engine = MockEngine()
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { engine }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.SetSpeed(1.5f))
        
        // Advance the test dispatcher to process commands
        testScheduler.advanceUntilIdle()
        
        assertEquals(1.5f, controller.state.value.speed)
        assertEquals(1.5f, engine.speedTTS)
        
        controller.destroy()
    }
    
    @Test
    fun `speed is clamped to valid range`() = runTest(testDispatcher) {
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { MockEngine() }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.SetSpeed(5.0f)) // Too high
        
        testScheduler.advanceUntilIdle()
        
        assertEquals(2.0f, controller.state.value.speed) // Clamped to max
        
        controller.dispatch(TTSCommand.SetSpeed(0.1f)) // Too low
        
        testScheduler.advanceUntilIdle()
        
        assertEquals(0.5f, controller.state.value.speed) // Clamped to min
        
        controller.destroy()
    }
    
    @Test
    fun `play without content emits error`() = runTest(testDispatcher) {
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { MockEngine() }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.Play)
        
        testScheduler.advanceUntilIdle()
        
        val state = controller.state.value
        assertEquals(PlaybackState.ERROR, state.playbackState)
        assertTrue(state.error is TTSError.NoContent)
        
        controller.destroy()
    }
    
    @Test
    fun `cleanup resets state`() = runTest(testDispatcher) {
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { MockEngine() }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(1, 1, 0))
        testScheduler.advanceUntilIdle()
        
        assertTrue(controller.state.value.hasContent)
        
        controller.dispatch(TTSCommand.Cleanup)
        testScheduler.advanceUntilIdle()
        
        assertFalse(controller.state.value.hasContent)
        assertEquals(PlaybackState.IDLE, controller.state.value.playbackState)
        
        controller.destroy()
    }
    
    @Test
    fun `stale utterance completion does not advance paragraph`() = runTest(testDispatcher) {
        val engine = MockEngine()
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { engine }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(1, 1, 0))
        testScheduler.advanceUntilIdle()
        
        controller.dispatch(TTSCommand.Play)
        testScheduler.advanceUntilIdle()
        
        assertEquals(0, controller.state.value.currentParagraphIndex)
        
        // Emit a stale completion event with wrong utteranceId
        engine.completeUtterance("stale_old_id")
        testScheduler.advanceUntilIdle()
        
        // Should NOT advance paragraph on stale utterance
        assertEquals(0, controller.state.value.currentParagraphIndex)
        
        // Emit matching completion event
        engine.completeUtterance("p_0")
        testScheduler.advanceUntilIdle()
        
        // Should advance to next paragraph
        assertEquals(1, controller.state.value.currentParagraphIndex)
        
        controller.destroy()
    }
    
    @Test
    fun `translation state is preserved across chapters when translated content is loaded`() = runTest(testDispatcher) {
        val controller = TTSController(
            contentLoader = MockContentLoader(),
            nativeEngineFactory = { MockEngine() }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(1, 1, 0))
        testScheduler.advanceUntilIdle()
        
        // User enables translation
        controller.dispatch(TTSCommand.ToggleTranslation(true))
        controller.dispatch(TTSCommand.SetTranslatedContent(listOf("Trans 1", "Trans 2", "Trans 3")))
        testScheduler.advanceUntilIdle()
        
        assertTrue(controller.state.value.showTranslation)
        assertTrue(controller.state.value.isTranslationAvailable)
        
        // Load next chapter
        controller.dispatch(TTSCommand.LoadChapter(1, 2, 0))
        testScheduler.advanceUntilIdle()
        
        // Immediately after loadChapter, showTranslation is false until translations arrive
        assertFalse(controller.state.value.showTranslation)
        
        // When new chapter translations arrive, showTranslation automatically turns back on!
        controller.dispatch(TTSCommand.SetTranslatedContent(listOf("Next Trans 1", "Next Trans 2", "Next Trans 3")))
        testScheduler.advanceUntilIdle()
        
        assertTrue(controller.state.value.showTranslation)
        assertTrue(controller.state.value.isTranslationAvailable)
        
        controller.destroy()
    }
    
    @Test
    fun `blank paragraphs are skipped automatically during playback`() = runTest(testDispatcher) {
        val mockLoader = MockContentLoader().apply {
            mockParagraphs = listOf("   ", "Valid Paragraph", "Third Paragraph")
        }
        val engine = MockEngine()
        val controller = TTSController(
            contentLoader = mockLoader,
            nativeEngineFactory = { engine }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(1, 1, 0))
        testScheduler.advanceUntilIdle()
        
        controller.dispatch(TTSCommand.Play)
        testScheduler.advanceUntilIdle()
        
        // Should have skipped the blank paragraph 0 and started playing paragraph 1
        assertEquals(1, controller.state.value.currentParagraphIndex)
        assertEquals("Valid Paragraph", engine.lastText)
        assertEquals("p_1", engine.lastUtteranceId)
        
        controller.destroy()
    }

    @Test
    fun `refreshContent does not interrupt playing audio when chapter already has content`() = runTest(testDispatcher) {
        val mockLoader = MockContentLoader()
        val engine = MockEngine()
        val controller = TTSController(
            contentLoader = mockLoader,
            nativeEngineFactory = { engine }
        )
        
        controller.dispatch(TTSCommand.Initialize)
        controller.dispatch(TTSCommand.LoadChapter(1, 1, 0))
        testScheduler.advanceUntilIdle()
        
        controller.dispatch(TTSCommand.Play)
        testScheduler.advanceUntilIdle()
        
        assertTrue(controller.state.value.isPlaying)
        assertEquals(0, controller.state.value.currentParagraphIndex)
        
        // Dispatch RefreshContent while playing
        controller.dispatch(TTSCommand.RefreshContent)
        testScheduler.advanceUntilIdle()
        
        // Playback should NOT be interrupted/reset
        assertTrue(controller.state.value.isPlaying)
        assertEquals(0, controller.state.value.currentParagraphIndex)
        
        controller.destroy()
    }
}

