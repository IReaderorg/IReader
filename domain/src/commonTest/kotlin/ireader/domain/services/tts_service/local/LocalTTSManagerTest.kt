package ireader.domain.services.tts_service.local

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class LocalTTSManagerTest {

    private class MockAudioPlayer : GradioAudioPlayer {
        override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
            onComplete()
        }
        override fun stop() {}
        override fun pause() {}
        override fun resume() {}
        override fun release() {}
        override fun setSpeed(speed: Float) {}
    }

    private var savedConfigJson: String? = null

    private fun createManager(
        mockEngine: MockEngine,
        initialSavedJson: String? = null
    ): LocalTTSManager {
        savedConfigJson = initialSavedJson
        val client = HttpClient(mockEngine)
        return LocalTTSManager(
            httpClient = client,
            audioPlayerFactory = { MockAudioPlayer() },
            saveConfig = { json -> savedConfigJson = json },
            loadConfig = { savedConfigJson }
        )
    }

    @Test
    fun `initializes with default config when loadConfig returns null`() {
        val engine = MockEngine { respondOk() }
        val manager = createManager(engine, null)
        val config = manager.config.value

        assertEquals("http://127.0.0.1:8000", config.serverUrl)
        assertEquals("default", config.voice)
        assertEquals(LocalTTSApiFormat.SIMPLE_REST, config.apiFormat)
    }

    @Test
    fun `initializes with saved config when loadConfig returns json`() {
        val engine = MockEngine { respondOk() }
        val savedJson = """{"serverUrl":"http://192.168.1.50:8000","voice":"persian_female","speed":1.2,"pitch":1.0,"apiKey":"secret","apiFormat":"OPENAI_SPEECH","enabled":true,"name":"Custom Server","timeoutMs":30000}"""
        val manager = createManager(engine, savedJson)
        val config = manager.config.value

        assertEquals("http://192.168.1.50:8000", config.serverUrl)
        assertEquals("persian_female", config.voice)
        assertEquals(1.2f, config.speed)
        assertEquals("secret", config.apiKey)
        assertEquals(LocalTTSApiFormat.OPENAI_SPEECH, config.apiFormat)
    }

    @Test
    fun `updateConfig updates state and calls saveConfig`() {
        val engine = MockEngine { respondOk() }
        val manager = createManager(engine, null)

        val newConfig = LocalTTSConfig(
            serverUrl = "http://192.168.1.200:8000",
            voice = "persian_male",
            speed = 1.1f
        )
        manager.updateConfig(newConfig)

        assertEquals("http://192.168.1.200:8000", manager.config.value.serverUrl)
        assertNotNull(savedConfigJson)
        assertTrue(savedConfigJson!!.contains("192.168.1.200:8000"))
        assertTrue(savedConfigJson!!.contains("persian_male"))
    }

    @Test
    fun `testConnection successfully decodes health response`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/health", request.url.encodedPath)
            respond(
                content = """{"status":"ok","model":"Thomcles/Chatterbox-TTS-Persian-Farsi","device":"cuda","loaded":true,"sample_rate":24000,"error":null}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val manager = createManager(engine)
        val result = manager.testConnection("http://127.0.0.1:8000")

        assertTrue(result.isSuccess)
        val health = result.getOrThrow()
        assertEquals("ok", health.status)
        assertEquals("cuda", health.device)
        assertEquals("Thomcles/Chatterbox-TTS-Persian-Farsi", health.model)
        assertTrue(health.loaded)
    }

    @Test
    fun `testConnection returns failure on server error`() = runTest {
        val engine = MockEngine {
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val manager = createManager(engine)
        val result = manager.testConnection("http://127.0.0.1:8000")

        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchVoices returns list of voices from server`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/voices", request.url.encodedPath)
            respond(
                content = """[{"id":"default","name":"Persian Standard","language":"fa","gender":"female"},{"id":"persian_male","name":"Persian Male","language":"fa","gender":"male"}]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val manager = createManager(engine)
        val voices = manager.fetchVoices("http://127.0.0.1:8000")

        assertEquals(2, voices.size)
        assertEquals("default", voices[0].id)
        assertEquals("Persian Standard", voices[0].name)
        assertEquals("persian_male", voices[1].id)
    }

    @Test
    fun `fetchVoices falls back to default Persian voices on failure`() = runTest {
        val engine = MockEngine {
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }
        val manager = createManager(engine)
        val voices = manager.fetchVoices("http://127.0.0.1:8000")

        assertTrue(voices.isNotEmpty())
        assertTrue(voices.any { it.id == "default" })
    }
}
