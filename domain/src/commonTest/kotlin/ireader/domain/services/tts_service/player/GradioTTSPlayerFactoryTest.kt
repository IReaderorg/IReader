package ireader.domain.services.tts_service.player

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.GradioTTSConfig
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Tests for GradioTTSPlayerFactory.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GradioTTSPlayerFactoryTest {
    
    private lateinit var mockHttpClient: HttpClient
    private lateinit var mockAudioPlayer: MockGradioAudioPlayer
    private lateinit var factory: GradioTTSPlayerFactory
    
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
        factory = GradioTTSPlayerFactory(mockHttpClient, mockAudioPlayer)
    }
    
    @AfterTest
    fun teardown() {
        mockHttpClient.close()
    }
    
    @Test
    fun `create returns a valid player`() {
        val config = createTestConfig()
        
        val player = factory.create(config)
        
        assertNotNull(player)
        player.release()
    }
    
    @Test
    fun `create with custom prefetch count`() {
        val config = createTestConfig()
        
        val player = factory.create(config, prefetchCount = 5)
        
        assertNotNull(player)
        player.release()
    }
    
    @Test
    fun `createWithCustomComponents uses provided components`() = runTest {
        val config = createTestConfig()
        val customGenerator = MockAudioGenerator()
        val customPlayback = MockAudioPlayback()
        val testDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
        
        val player = factory.createWithCustomComponents(
            config = config,
            audioGenerator = customGenerator,
            audioPlayback = customPlayback,
            prefetchCount = 2,
            dispatcher = testDispatcher
        )
        
        assertNotNull(player)
        
        // Verify custom components are used by setting speed
        // The player should call setSpeed on the generator
        player.setSpeed(1.5f)
        
        // Advance the test scheduler to process the command
        advanceUntilIdle()
        
        assertEquals(1.5f, customGenerator.speedSet, "Custom generator should receive speed setting")
        
        player.release()
    }
    
    @Test
    fun `extension function creates player from config`() {
        val config = createTestConfig()
        
        val player = config.createPlayer(mockHttpClient, mockAudioPlayer)
        
        assertNotNull(player)
        player.release()
    }
    
    private fun createTestConfig() = GradioTTSConfig(
        id = "test",
        name = "Test Engine",
        spaceUrl = "https://test.hf.space",
        apiName = "/test"
    )
}

/**
 * Mock GradioAudioPlayer for testing.
 */
class MockGradioAudioPlayer : GradioAudioPlayer {
    var playCalled = false
    var stopCalled = false
    var pauseCalled = false
    var resumeCalled = false
    var releaseCalled = false
    
    override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
        playCalled = true
        onComplete()
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
