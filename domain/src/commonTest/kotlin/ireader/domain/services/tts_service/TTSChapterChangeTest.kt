package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.v2.EngineEvent
import ireader.domain.services.tts_service.v2.PlaybackState
import ireader.domain.services.tts_service.v2.TTSCommand
import ireader.domain.services.tts_service.v2.TTSContentLoader
import ireader.domain.services.tts_service.v2.TTSController
import ireader.domain.services.tts_service.v2.TTSEngine
import ireader.domain.services.tts_service.v2.TTSError
import ireader.core.source.model.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    /**
     * Regression test for issue #236 (and its duplicate #235).
     *
     * Auto-next to a chapter that already has content in the DB must play immediately
     * and must NOT route into the empty-content watch path or emit TTSError.NoContent.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `auto-next with content plays immediately without NoContent`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val loader = ContentAwareLoader()
            loader.contentByChapter[1L] = listOf("C1 P1", "C1 P2")
            loader.contentByChapter[2L] = listOf("C2 P1", "C2 P2") // next chapter already has content
            loader.nextChapterIdValue = 2L

            val engine = RecordingEngine()
            val controller = TTSController(
                contentLoader = loader,
                nativeEngineFactory = { engine }
            )

            controller.dispatch(TTSCommand.Initialize)
            controller.dispatch(TTSCommand.LoadChapter(bookId = 1, chapterId = 1, startParagraph = 0))
            controller.dispatch(TTSCommand.Play)
            testScheduler.advanceUntilIdle()

            controller.dispatch(TTSCommand.NextChapter)
            testScheduler.advanceUntilIdle()

            val state = controller.state.value
            assertEquals(2L, state.chapter?.id, "Should have advanced to chapter 2")
            assertTrue(state.hasContent, "Chapter 2 content should be loaded")
            assertFalse(state.error is TTSError.NoContent, "Must not emit NoContent on a chapter with content")
            assertEquals(2, state.totalParagraphs, "Chapter 2 paragraphs should be loaded")

            controller.destroy()
        } finally {
            Dispatchers.resetMain()
        }
    }

    /**
     * Regression test for issue #236: auto-next where the next chapter row exists in the
     * DB but its content has not been fetched yet (empty paragraphs after loadChapter).
     *
     * Before the fix, nextChapter() unconditionally called play() on the empty chapter,
     * which short-circuited on TTSError.NoContent and left playback stuck with a
     * perpetual loading bar. After the fix, the controller must NOT emit NoContent and
     * must resume playback once the content arrives in the DB.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `auto-next with empty next chapter does not emit NoContent and resumes when content arrives`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val loader = ContentAwareLoader()
            loader.contentByChapter[1L] = listOf("C1 P1", "C1 P2")
            // Chapter 2 exists but its content is not fetched yet: the first loadChapter
            // (from nextChapter) returns empty, then a later poll returns content,
            // modelling a remote fetch that lands while the watch is waiting.
            loader.pendingChapterId = 2L
            loader.emptyCallsBeforeContent = 1
            loader.pendingContent = listOf("C2 P1", "C2 P2")
            loader.nextChapterIdValue = 2L

            val engine = RecordingEngine()
            val controller = TTSController(
                contentLoader = loader,
                nativeEngineFactory = { engine }
            )
            controller.dispatch(TTSCommand.SetAutoNextChapter(true))
            controller.dispatch(TTSCommand.Initialize)
            controller.dispatch(TTSCommand.LoadChapter(bookId = 1, chapterId = 1, startParagraph = 0))
            controller.dispatch(TTSCommand.Play)
            testScheduler.advanceUntilIdle()

            controller.dispatch(TTSCommand.NextChapter)
            // Process the synchronous part of nextChapter (empty loadChapter + start watch)
            // without advancing virtual time, so we observe the pre-content-arrival state.
            testScheduler.runCurrent()

            // The next chapter has no content yet: must NOT have errored with NoContent.
            val watching = controller.state.value
            assertFalse(watching.error is TTSError.NoContent, "Must not emit NoContent while content is still being fetched")
            assertFalse(watching.playbackState == PlaybackState.ERROR, "Must not be in ERROR state while content is being fetched")

            // Let the content-watch poll run; content arrives on the next poll.
            testScheduler.advanceUntilIdle()

            val resumed = controller.state.value
            assertEquals(2L, resumed.chapter?.id, "Should have advanced to chapter 2 once content arrived")
            assertTrue(resumed.hasContent, "Chapter 2 content should now be loaded")
            assertFalse(resumed.error is TTSError.NoContent, "Must never emit NoContent for an auto-advanced chapter")

            controller.destroy()
        } finally {
            Dispatchers.resetMain()
        }
    }

    /**
     * Regression test for issue #236: auto-next disabled with an empty next chapter must
     * settle cleanly to STOPPED rather than erroring or staying stuck in LOADING.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `next chapter with empty content and autoNext off settles to stopped`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val loader = ContentAwareLoader()
            loader.contentByChapter[1L] = listOf("C1 P1", "C1 P2")
            loader.contentByChapter[2L] = emptyList()
            loader.nextChapterIdValue = 2L

            val engine = RecordingEngine()
            val controller = TTSController(
                contentLoader = loader,
                nativeEngineFactory = { engine }
            )
            controller.dispatch(TTSCommand.SetAutoNextChapter(false))
            controller.dispatch(TTSCommand.Initialize)
            controller.dispatch(TTSCommand.LoadChapter(bookId = 1, chapterId = 1, startParagraph = 0))
            controller.dispatch(TTSCommand.Play)
            testScheduler.advanceUntilIdle()

            controller.dispatch(TTSCommand.NextChapter)
            testScheduler.advanceUntilIdle()

            val state = controller.state.value
            assertFalse(state.error is TTSError.NoContent, "Must not emit NoContent with autoNext off")
            assertEquals(PlaybackState.STOPPED, state.playbackState, "Should settle to STOPPED")

            controller.destroy()
        } finally {
            Dispatchers.resetMain()
        }
    }

    /**
     * Content-aware mock loader: chapter content is looked up from a mutable map so a test
     * can model a chapter row that exists with empty content and later gains content.
     * subscribeChapters emits a MutableStateFlow the test can update to simulate a remote
     * fetch landing in the DB.
     */
    private class ContentAwareLoader : TTSContentLoader {
        val contentByChapter = mutableMapOf<Long, List<String>>()
        var nextChapterIdValue: Long? = null
        var previousChapterIdValue: Long? = null
        val chaptersFlow = MutableStateFlow<List<Chapter>>(emptyList())

        /**
         * Models a chapter whose remote content fetch is still in flight: loadChapter
         * for [pendingChapterId] returns empty until it has been called
         * [emptyCallsBeforeContent] times, then returns [pendingContent]. This mirrors
         * TTSContentLoaderImpl, where loadChapter itself drives the remote fetch and
         * eventually returns content once it lands.
         */
        var pendingChapterId: Long? = null
        var emptyCallsBeforeContent: Int = 0
        var pendingContent: List<String> = emptyList()
        private var pendingCalls = 0

        override suspend fun loadChapter(bookId: Long, chapterId: Long): TTSContentLoader.ChapterContent {
            val paragraphs = if (chapterId == pendingChapterId) {
                pendingCalls++
                if (pendingCalls > emptyCallsBeforeContent) pendingContent else emptyList()
            } else {
                contentByChapter[chapterId] ?: emptyList()
            }
            return TTSContentLoader.ChapterContent(
                book = Book(id = bookId, title = "Test Book", key = "test", sourceId = 1),
                chapter = Chapter(id = chapterId, bookId = bookId, key = "ch$chapterId", name = "Chapter $chapterId"),
                paragraphs = paragraphs
            )
        }

        override suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long? = nextChapterIdValue

        override suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long? = previousChapterIdValue

        override fun subscribeChapters(bookId: Long): Flow<List<Chapter>> = chaptersFlow
    }

    /**
     * Minimal recording TTS engine for controller-level tests. Uses replay = 1 so the
     * controller's event collector reliably observes the latest Started event regardless
     * of the order in which the collector subscribes relative to speak() under the test
     * dispatcher (with replay = 0 the Started could be emitted before the collector
     * subscribes and be lost, leaving the controller stuck in LOADING).
     */
    private class RecordingEngine : TTSEngine {
        private val _events = MutableSharedFlow<EngineEvent>(replay = 1, extraBufferCapacity = 10)
        override val events: Flow<EngineEvent> = _events
        override val name: String = "Recording Engine"

        override suspend fun speak(text: String, utteranceId: String) {
            _events.emit(EngineEvent.Started(utteranceId))
        }

        override fun stop() {}
        override fun pause() {}
        override fun resume() {}
        override fun setSpeed(speed: Float) {}
        override fun setPitch(pitch: Float) {}
        override fun isReady() = true
        override fun release() {}
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
