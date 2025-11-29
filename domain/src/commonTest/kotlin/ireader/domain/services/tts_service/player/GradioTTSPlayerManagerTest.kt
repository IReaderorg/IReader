package ireader.domain.services.tts_service.player

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSManager
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Tests for GradioTTSPlayerManager.
 * 
 * Note: These tests use a real GradioTTSManager with mock HTTP client
 * since GradioTTSManager is a final class.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GradioTTSPlayerManagerTest {
    
    private lateinit var mockHttpClient: HttpClient
    private lateinit var mockAudioPlayer: MockGradioAudioPlayer
    private lateinit var gradioTTSManager: GradioTTSManager
    private lateinit var manager: GradioTTSPlayerManager
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        mockHttpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = """{"data": [{"url": "https://test.com/audio.wav"}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
        mockAudioPlayer = MockGradioAudioPlayer()
        gradioTTSManager = GradioTTSManager(
            httpClient = mockHttpClient,
            audioPlayerFactory = { MockGradioAudioPlayer() },
            saveConfigs = {},
            loadConfigs = { null } // Will load presets
        )
        manager = GradioTTSPlayerManager(
            httpClient = mockHttpClient,
            audioPlayer = mockAudioPlayer,
            gradioTTSManager = gradioTTSManager,
            dispatcher = testDispatcher
        )
    }
    
    @AfterTest
    fun teardown() {
        manager.release()
        mockHttpClient.close()
    }
    
    // ==================== Initialization Tests ====================
    
    @Test
    fun `manager loads available engines on init`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        assertTrue(manager.availableEngines.value.isNotEmpty())
    }
    
    @Test
    fun `manager auto-selects first engine if none selected`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        assertNotNull(manager.currentEngine.value)
    }
    
    // ==================== Engine Selection Tests ====================
    
    @Test
    fun `selectEngine updates current engine`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        // Use a valid preset engine ID
        manager.selectEngine("edge_tts")
        advanceUntilIdle()
        
        assertEquals("edge_tts", manager.currentEngine.value)
    }
    
    @Test
    fun `selectEngine with invalid id sets error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.selectEngine("non-existent-engine-id-12345")
        advanceUntilIdle()
        
        assertNotNull(manager.managerError.value)
    }
    
    // ==================== Content Management Tests ====================
    
    @Test
    fun `setContent sets content on player`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Para 1", "Para 2"))
        advanceUntilIdle()
        
        assertTrue(manager.hasContent.value)
        assertEquals(2, manager.totalParagraphs.value)
    }
    
    // Note: Cannot easily test "setContent without engine" since GradioTTSManager
    // always loads presets and auto-selects an engine
    
    @Test
    fun `setChapterContent splits content into paragraphs`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setChapterContent("Paragraph 1\n\nParagraph 2\n\nParagraph 3")
        advanceUntilIdle()
        
        assertEquals(3, manager.totalParagraphs.value)
    }
    
    // ==================== Playback Control Tests ====================
    
    @Test
    fun `play starts playback`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Test"))
        advanceUntilIdle()
        
        manager.play()
        advanceUntilIdle()
        
        assertTrue(manager.isPlaying.value)
    }
    
    @Test
    fun `pause pauses playback`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Test"))
        advanceUntilIdle()
        
        manager.play()
        advanceUntilIdle()
        
        manager.pause()
        advanceUntilIdle()
        
        assertTrue(manager.isPaused.value)
    }
    
    @Test
    fun `stop stops playback`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Test"))
        advanceUntilIdle()
        
        manager.play()
        advanceUntilIdle()
        
        manager.stop()
        advanceUntilIdle()
        
        assertFalse(manager.isPlaying.value)
    }
    
    // ==================== Navigation Tests ====================
    
    @Test
    fun `next moves to next paragraph`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Para 1", "Para 2", "Para 3"))
        advanceUntilIdle()
        
        manager.next()
        advanceUntilIdle()
        
        assertEquals(1, manager.currentParagraph.value)
    }
    
    @Test
    fun `previous moves to previous paragraph`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Para 1", "Para 2", "Para 3"), startIndex = 2)
        advanceUntilIdle()
        
        manager.previous()
        advanceUntilIdle()
        
        assertEquals(1, manager.currentParagraph.value)
    }
    
    @Test
    fun `jumpTo moves to specified paragraph`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Para 1", "Para 2", "Para 3", "Para 4", "Para 5"))
        advanceUntilIdle()
        
        manager.jumpTo(3)
        advanceUntilIdle()
        
        assertEquals(3, manager.currentParagraph.value)
    }
    
    // ==================== Settings Tests ====================
    
    @Test
    fun `setSpeed updates speed`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setSpeed(1.5f)
        advanceUntilIdle()
        
        assertEquals(1.5f, manager.speed.value)
    }
    
    @Test
    fun `setPitch updates pitch`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setPitch(1.2f)
        advanceUntilIdle()
        
        assertEquals(1.2f, manager.pitch.value)
    }
    
    // ==================== State Tests ====================
    
    @Test
    fun `getStateSnapshot returns current state`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.setContent(listOf("Para 1", "Para 2"))
        advanceUntilIdle()
        
        val snapshot = manager.getStateSnapshot()
        
        assertNotNull(snapshot)
        assertEquals(2, snapshot.totalParagraphs)
    }
    
    @Test
    fun `getCurrentPlayer returns player`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        val player = manager.getCurrentPlayer()
        
        assertNotNull(player)
    }
    
    @Test
    fun `getCurrentConfig returns config`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        val config = manager.getCurrentConfig()
        
        assertNotNull(config)
    }
    
    // ==================== Lifecycle Tests ====================
    
    @Test
    fun `release cleans up resources`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.release()
        advanceUntilIdle()
        
        assertNull(manager.currentEngine.value)
        assertFalse(manager.isInitialized.value)
    }
    
    @Test
    fun `refreshEngines reloads engines`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        manager.refreshEngines()
        advanceUntilIdle()
        
        assertTrue(manager.availableEngines.value.isNotEmpty())
    }
}
