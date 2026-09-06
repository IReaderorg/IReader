package ireader.domain.services.tts_service.local

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.TTSEngineCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalTTSEngineTest {

    private class TestAudioPlayer : GradioAudioPlayer {
        var playedBytes: ByteArray? = null
        var isPlaying = false
        var stopped = false
        var paused = false
        var currentSpeed = 1.0f

        override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
            playedBytes = audioData
            isPlaying = true
            onComplete()
            isPlaying = false
        }

        override fun stop() {
            stopped = true
            isPlaying = false
        }

        override fun pause() {
            paused = true
        }

        override fun resume() {
            paused = false
        }

        override fun release() {
            stopped = true
        }

        override fun setSpeed(speed: Float) {
            currentSpeed = speed
        }
    }

    private val sampleAudioBytes = byteArrayOf(0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00) // RIFF...

    @Test
    fun `generateAudio sends correct payload for SIMPLE_REST`() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null

        val engine = MockEngine { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(
            serverUrl = "http://127.0.0.1:8000",
            voice = "default",
            speed = 1.0f,
            apiFormat = LocalTTSApiFormat.SIMPLE_REST
        )
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        val result = ttsEngine.generateAudio("سلام دنیا")

        assertNotNull(result)
        assertEquals("/api/tts", capturedPath)
        assertNotNull(capturedBody)
        val body1 = capturedBody
        assertNotNull(body1)
        assertTrue(body1.contains("سلام دنیا"))
        assertTrue(body1.contains("default"))
        ttsEngine.cleanup()
    }

    @Test
    fun `generateAudio sends correct payload for OPENAI_SPEECH`() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null

        val engine = MockEngine { request ->
            capturedPath = request.url.encodedPath
            capturedBody = request.body.toByteArray().decodeToString()
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(
            serverUrl = "http://127.0.0.1:8000",
            voice = "persian_female",
            speed = 1.25f,
            apiFormat = LocalTTSApiFormat.OPENAI_SPEECH
        )
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        val result = ttsEngine.generateAudio("آزمایش متن")

        assertNotNull(result)
        assertEquals("/v1/audio/speech", capturedPath)
        val body2 = capturedBody
        assertNotNull(body2)
        assertTrue(body2.contains("آزمایش متن"))
        assertTrue(body2.contains("persian_female"))
        assertTrue(body2.contains("input"))
        ttsEngine.cleanup()
    }

    @Test
    fun `speak triggers callback onStart and onDone and plays audio`() = runTest {
        val engine = MockEngine {
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        var started = false
        var done = false

        ttsEngine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {
                started = true
            }

            override fun onDone(utteranceId: String) {
                done = true
            }

            override fun onError(utteranceId: String, error: String) {
                fail("Should not error: $error")
            }
        })

        ttsEngine.speak("تست خواندن", "utt_1")

        assertTrue(started)
        assertTrue(done)
        assertNotNull(player.playedBytes)
        ttsEngine.cleanup()
    }

    @Test
    fun `precache populates cache and subsequent speak uses cached audio`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        val job = ttsEngine.precache("utt_cache", "متن ذخیره شده")
        job?.join()

        assertTrue(ttsEngine.isTextCached("متن ذخیره شده"))
        assertEquals(1, requestCount)

        // Now speak the cached text
        ttsEngine.speak("متن ذخیره شده", "utt_cache")

        // Request count should still be 1 (cache hit!)
        assertEquals(1, requestCount)
        assertNotNull(player.playedBytes)
        ttsEngine.cleanup()
    }
}
