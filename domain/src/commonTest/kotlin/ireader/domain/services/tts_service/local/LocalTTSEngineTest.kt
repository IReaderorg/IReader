package ireader.domain.services.tts_service.local

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.v2.EngineEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Test
    fun `hasContent correctly classifies valid content vs solitary punctuation`() {
        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(MockEngine { respondOk() }), player)

        // Valid Persian & alphanumeric content
        assertTrue(ttsEngine.hasContent("سلام دنیا"))
        assertTrue(ttsEngine.hasContent("سلام"))
        assertTrue(ttsEngine.hasContent("Hello 123"))
        assertTrue(ttsEngine.hasContent("«متن داخل گیومه»"))
        assertTrue(ttsEngine.hasContent("آیا این یک سوال است؟"))

        // Punctuation-only or too short chunks (should be false)
        assertFalse(ttsEngine.hasContent(""))
        assertFalse(ttsEngine.hasContent("   "))
        assertFalse(ttsEngine.hasContent("."))
        assertFalse(ttsEngine.hasContent("..."))
        assertFalse(ttsEngine.hasContent("؟"))
        assertFalse(ttsEngine.hasContent("« »"))
        assertFalse(ttsEngine.hasContent("«»"))
        assertFalse(ttsEngine.hasContent("!"))
        assertFalse(ttsEngine.hasContent("!؟"))
        assertFalse(ttsEngine.hasContent("، ؛"))
        assertFalse(ttsEngine.hasContent("a")) // length < 2
        ttsEngine.cleanup()
    }

    @Test
    fun `generateAudio returns null for punctuation-only chunk without HTTP requests`() = runTest {
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

        val result1 = ttsEngine.generateAudio("...")
        val result2 = ttsEngine.generateAudio("؟")
        val result3 = ttsEngine.generateAudio("« »")

        assertNull(result1)
        assertNull(result2)
        assertNull(result3)
        assertEquals(0, requestCount, "No HTTP requests should be made for punctuation-only chunks")
        ttsEngine.cleanup()
    }

    @Test
    fun `speak treats punctuation-only chunks as pause without HTTP requests`() = runTest {
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

        ttsEngine.speak("...", "utt_punc")

        assertTrue(started)
        assertTrue(done)
        assertEquals(0, requestCount, "No HTTP synthesis request should be dispatched")
        assertNull(player.playedBytes, "No audio should be played for punctuation-only chunks")
        ttsEngine.cleanup()
    }

    @Test
    fun `concurrent synthesis requests are serialized and never overlap on the network`() = runTest {
        var concurrentCount = 0
        var maxConcurrent = 0

        val engine = MockEngine {
            concurrentCount++
            if (concurrentCount > maxConcurrent) {
                maxConcurrent = concurrentCount
            }
            kotlinx.coroutines.delay(20)
            concurrentCount--
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, Dispatchers.Default)

        // Launch 4 concurrent synthesis calls
        val results = coroutineScope {
            val j1 = async { ttsEngine.generateAudio("جمله اول تستی برای بررسی") }
            val j2 = async { ttsEngine.generateAudio("جمله دوم تستی برای بررسی") }
            val j3 = async { ttsEngine.generateAudio("جمله سوم تستی برای بررسی") }
            val j4 = async { ttsEngine.generateAudio("جمله چهارم تستی برای بررسی") }
            listOf(j1.await(), j2.await(), j3.await(), j4.await())
        }

        results.forEach { assertNotNull(it) }
        assertEquals(1, maxConcurrent, "Network requests must be serialized: max concurrent was $maxConcurrent")
        ttsEngine.cleanup()
    }

    @Test
    fun `precacheNext continuously synthesizes all queued items sequentially into cache`() = runTest {
        val synthesizedTexts = mutableListOf<String>()
        val cachedUtterances = mutableListOf<String>()

        val engine = MockEngine { request ->
            val body = request.body.toByteArray().decodeToString()
            synthesizedTexts.add(body)
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        ttsEngine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) {}
            override fun onError(utteranceId: String, error: String) {}
            override fun onCached(utteranceId: String) {
                cachedUtterances.add(utteranceId)
            }
        })

        val items = listOf(
            "p_1" to "پاراگراف شماره یک برای خوانش",
            "p_2" to "پاراگراف شماره دو برای خوانش",
            "p_3" to "پاراگراف شماره سه برای خوانش",
            "p_4" to "پاراگراف شماره چهار برای خوانش"
        )

        val job = ttsEngine.precacheNext(items)
        job?.join()

        assertEquals(4, synthesizedTexts.size, "All 4 items should be synthesized")
        assertEquals(listOf("p_1", "p_2", "p_3", "p_4"), cachedUtterances, "All 4 items should trigger onCached")
        assertTrue(ttsEngine.isTextCached("پاراگراف شماره یک برای خوانش"))
        assertTrue(ttsEngine.isTextCached("پاراگراف شماره چهار برای خوانش"))
        ttsEngine.cleanup()
    }

    @Test
    fun `speak does not cancel in-flight prefetch and plays cached audio immediately`() = runTest {
        var networkCalls = 0

        val engine = MockEngine {
            networkCalls++
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(engine), player, UnconfinedTestDispatcher())

        val items = listOf(
            "p_1" to "جمله اول برای پیش‌بارگذاری",
            "p_2" to "جمله دوم برای پیش‌بارگذاری"
        )

        ttsEngine.precacheNext(items)?.join()

        assertEquals(2, networkCalls, "Both items pre-cached")

        // Now speak p_1 (which is already cached)
        ttsEngine.speak("جمله اول برای پیش‌بارگذاری", "p_1")

        // Network calls should NOT increase because p_1 was cached
        assertEquals(2, networkCalls, "speak should use cached audio without re-requesting")
        assertNotNull(player.playedBytes)

        // Check that p_2 is still cached and not cancelled/cleared
        assertTrue(ttsEngine.isTextCached("جمله دوم برای پیش‌بارگذاری"))
        ttsEngine.cleanup()
    }

    @Test
    fun `clearState cancels pending queue and clears cache`() = runTest {
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

        ttsEngine.precacheNext(listOf("p_1" to "تست برای پاکسازی کش"))?.join()

        assertTrue(ttsEngine.isTextCached("تست برای پاکسازی کش"))

        ttsEngine.clearState()

        assertFalse(ttsEngine.isTextCached("تست برای پاکسازی کش"), "Cache should be cleared after clearState()")
        ttsEngine.cleanup()
    }

    @Test
    fun `cache retains items up to capacity and evicts oldest entries`() = runTest {
        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val player = TestAudioPlayer()
        val ttsEngine = LocalTTSEngine(config, HttpClient(MockEngine { respondOk() }), player)

        // Directly cache 105 entries (capacity is 100)
        for (i in 1..105) {
            ttsEngine.cacheAudio("p_$i", "متن شماره $i", sampleAudioBytes)
        }

        // The earliest 5 entries (1..5) should have been evicted
        for (i in 1..5) {
            assertFalse(ttsEngine.isTextCached("متن شماره $i"), "Entry $i should have been evicted")
        }

        // The remaining entries (6..105) should still be in cache
        for (i in 6..105) {
            assertTrue(ttsEngine.isTextCached("متن شماره $i"), "Entry $i should still be cached")
        }

        ttsEngine.cleanup()
    }

    @Test
    fun `LocalTTSEngineV2 emits EngineEvent Cached when utterance is synthesized`() = runTest {
        val engine = MockEngine {
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val localEngine = LocalTTSEngine(config, HttpClient(engine), TestAudioPlayer(), UnconfinedTestDispatcher())
        val v2Engine = localEngine.asV2()

        val events = mutableListOf<EngineEvent>()
        val collectJob = launch {
            v2Engine.events.collect {
                events.add(it)
            }
        }

        localEngine.precacheNext(listOf("p_0" to "متن برای بررسی اونت"))?.join()

        assertTrue(events.any { it is EngineEvent.Cached && it.utteranceId == "p_0" }, "Should emit EngineEvent.Cached for p_0")
        collectJob.cancel()
        v2Engine.release()
    }

    @Test
    fun `LocalTTSEngineV2 clearState delegates to local engine`() = runTest {
        val engine = MockEngine {
            respond(
                content = sampleAudioBytes,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "audio/wav")
            )
        }

        val config = LocalTTSConfig(serverUrl = "http://127.0.0.1:8000")
        val localEngine = LocalTTSEngine(config, HttpClient(engine), TestAudioPlayer(), UnconfinedTestDispatcher())
        val v2Engine = localEngine.asV2()

        localEngine.precacheNext(listOf("p_5" to "متن آزمایشی برای پاکسازی"))?.join()
        assertTrue(v2Engine.isTextCached("متن آزمایشی برای پاکسازی"))

        v2Engine.clearState()

        assertFalse(v2Engine.isTextCached("متن آزمایشی برای پاکسازی"), "Cache should be cleared after clearState()")
        v2Engine.release()
    }
}

