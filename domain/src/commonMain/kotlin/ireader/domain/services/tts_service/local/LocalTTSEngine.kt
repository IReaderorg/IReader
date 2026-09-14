package ireader.domain.services.tts_service.local

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import ireader.core.util.synchronizedMapOf
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.TTSEngine
import ireader.domain.services.tts_service.TTSEngineCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Local TTS Engine for connecting to locally hosted or cloud-hosted TTS servers
 * (such as Chatterbox-TTS-Persian-Farsi, Kokoro, LocalAI, etc.).
 *
 * Supports:
 * - Simple REST API (/api/tts)
 * - OpenAI Audio Speech API (/v1/audio/speech)
 * - In-memory pre-caching of upcoming paragraphs
 * - Platform audio playback via [GradioAudioPlayer]
 */
class LocalTTSEngine(
    val config: LocalTTSConfig,
    private val httpClient: HttpClient,
    private val audioPlayer: GradioAudioPlayer,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : TTSEngine {

    private var callback: TTSEngineCallback? = null
    private var speed: Float = config.speed
    private var pitch: Float = config.pitch
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    // In-memory cache for audio bytes (thread-safe)
    private val audioCache = synchronizedMapOf<String, ByteArray>()
    private val prefetchJobs = synchronizedMapOf<String, Job>()
    private var prefetchJob: Job? = null

    // Mutex to ensure sequential network synthesis requests to the local TTS server.
    // Deep learning autoregressive models (like Chatterbox Persian TTS) are non-reentrant
    // and fail on concurrent inference requests.
    private val networkMutex = Mutex()

    @Volatile
    private var isStopped = false

    @Volatile
    private var isPaused = false

    companion object {
        private const val TAG = "LocalTTSEngine"
        private const val MAX_CACHE_SIZE = 30
        private const val MAX_TEXT_LENGTH = 5000
    }

    /**
     * Checks if the text chunk contains valid Persian or alphanumeric characters
     * and has sufficient length for synthesis.
     * Prevents solitary punctuation or trailing marks (e.g., ".", "...", "؟", "« »")
     * from crashing the neural vocoder.
     */
    fun hasContent(sentence: String): Boolean {
        val trimmed = sentence.trim()
        if (trimmed.length < 2) return false
        return trimmed.any { it.isLetterOrDigit() }
    }

    override suspend fun speak(text: String, utteranceId: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            callback?.onDone(utteranceId)
            return
        }

        // Sanitize / filter punctuation-only or too-short chunks before requesting
        if (!hasContent(trimmed)) {
            Log.info { "$TAG: Skipping punctuation-only or too short chunk '$trimmed' (treating as 100ms pause)" }
            callback?.onStart(utteranceId)
            delay(100)
            callback?.onDone(utteranceId)
            return
        }

        isStopped = false
        isPaused = false
        callback?.onStart(utteranceId)

        try {
            // Cancel active prefetch for this utterance if running
            prefetchJob?.cancel()
            prefetchJobs.remove(utteranceId)?.cancel()

            val audioData = audioCache[utteranceId] ?: audioCache[trimmed] ?: run {
                Log.info { "$TAG: Synthesizing speech for utterance $utteranceId (${trimmed.take(30)}...)" }
                generateAudio(trimmed)
            }

            if (isStopped) {
                return
            }

            if (audioData != null && audioData.isNotEmpty()) {
                // Cache if not present
                if (!audioCache.containsKey(trimmed)) {
                    cacheAudio(trimmed, audioData)
                }

                val completionDeferred = CompletableDeferred<Unit>()
                audioPlayer.play(audioData) {
                    if (!isStopped) {
                        completionDeferred.complete(Unit)
                    }
                }
                completionDeferred.await()

                if (!isStopped) {
                    callback?.onDone(utteranceId)
                }
            } else {
                val errorMsg = "Local TTS server returned empty or null audio"
                Log.error { "$TAG: $errorMsg for utterance $utteranceId" }
                callback?.onError(utteranceId, errorMsg)
            }
        } catch (e: CancellationException) {
            Log.info { "$TAG: Speech cancelled for utterance $utteranceId" }
            throw e
        } catch (e: Exception) {
            Log.error { "$TAG: Error speaking utterance $utteranceId: ${e.message}" }
            callback?.onError(utteranceId, e.message ?: "Unknown error")
        }
    }

    /**
     * Synthesizes audio bytes from the server using the configured API format.
     * All network calls are serialized using a Mutex to prevent concurrent POST requests
     * that cause tensor shape collisions on non-reentrant TTS models (such as Chatterbox).
     */
    suspend fun generateAudio(text: String): ByteArray? {
        val trimmed = text.trim()
        if (!hasContent(trimmed)) {
            Log.info { "$TAG: generateAudio skipped for punctuation-only or too short chunk '$trimmed'" }
            return null
        }

        val cleanText = if (trimmed.length > MAX_TEXT_LENGTH) trimmed.take(MAX_TEXT_LENGTH) else trimmed

        return networkMutex.withLock {
            if (isStopped) return null
            try {
                when (config.apiFormat) {
                    LocalTTSApiFormat.SIMPLE_REST -> generateSimpleRest(cleanText)
                    LocalTTSApiFormat.OPENAI_SPEECH -> generateOpenAISpeech(cleanText)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error { "$TAG: Request failed: ${e.message}" }
                null
            }
        }
    }

    private suspend fun generateSimpleRest(text: String): ByteArray? {
        val url = "${config.normalizedUrl}/api/tts"
        val jsonPayload = buildJsonObject {
            put("text", text)
            put("voice", config.voice)
            put("speed", speed)
        }.toString()

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.parse("audio/*"))
            config.apiKey?.takeIf { it.isNotBlank() }?.let { key ->
                header(HttpHeaders.Authorization, "Bearer $key")
            }
            timeout {
                requestTimeoutMillis = config.timeoutMs
            }
            setBody(jsonPayload)
        }

        return if (response.status.isSuccess()) {
            response.body<ByteArray>()
        } else {
            Log.error { "$TAG: Simple REST returned status ${response.status.value}" }
            null
        }
    }

    private suspend fun generateOpenAISpeech(text: String): ByteArray? {
        val url = "${config.normalizedUrl}/v1/audio/speech"
        val jsonPayload = buildJsonObject {
            put("input", text)
            put("model", "chatterbox-persian")
            put("voice", config.voice)
            put("speed", speed)
            put("response_format", "wav")
        }.toString()

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.parse("audio/*"))
            config.apiKey?.takeIf { it.isNotBlank() }?.let { key ->
                header(HttpHeaders.Authorization, "Bearer $key")
            }
            timeout {
                requestTimeoutMillis = config.timeoutMs
            }
            setBody(jsonPayload)
        }

        return if (response.status.isSuccess()) {
            response.body<ByteArray>()
        } else {
            Log.error { "$TAG: OpenAI speech API returned status ${response.status.value}" }
            null
        }
    }

    /**
     * Pre-cache an upcoming paragraph in background
     */
    fun precache(utteranceId: String, text: String): Job? {
        val trimmed = text.trim()
        if (!hasContent(trimmed) || audioCache.containsKey(trimmed) || audioCache.containsKey(utteranceId)) {
            return null
        }

        prefetchJobs[utteranceId]?.cancel()
        val job = scope.launch {
            try {
                val audioData = generateAudio(trimmed)
                if (audioData != null && audioData.isNotEmpty()) {
                    cacheAudio(trimmed, audioData)
                    cacheAudio(utteranceId, audioData)
                    Log.info { "$TAG: Pre-cached paragraph $utteranceId (${audioData.size} bytes)" }
                }
            } catch (e: CancellationException) {
                // Ignore cancellation
            } catch (e: Exception) {
                Log.warn { "$TAG: Pre-cache failed for $utteranceId: ${e.message}" }
            } finally {
                prefetchJobs.remove(utteranceId)
            }
        }
        prefetchJobs[utteranceId] = job
        return job
    }

    /**
     * Pre-cache multiple upcoming items sequentially (1 sentence at a time).
     * Strictly awaits the current synthesis response before pre-fetching the immediate next sentence.
     * Never launches unbounded parallel HTTP calls to /api/tts.
     */
    fun precacheNext(items: List<Pair<String, String>>) {
        val validItems = items.take(2).filter { (id, text) ->
            val trimmed = text.trim()
            hasContent(trimmed) && !audioCache.containsKey(trimmed) && !audioCache.containsKey(id)
        }
        if (validItems.isEmpty()) return

        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            for ((id, text) in validItems) {
                if (!isActive || isStopped) break
                val trimmed = text.trim()
                try {
                    // Awaits current synthesis response before pre-fetching next sentence
                    val audioData = generateAudio(trimmed)
                    if (audioData != null && audioData.isNotEmpty()) {
                        cacheAudio(trimmed, audioData)
                        cacheAudio(id, audioData)
                        Log.info { "$TAG: Pre-cached sequential sentence $id (${audioData.size} bytes)" }
                    }
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Log.warn { "$TAG: Sequential pre-cache failed for $id: ${e.message}" }
                }
            }
        }
    }

    fun isTextCached(text: String): Boolean = audioCache.containsKey(text.trim())

    private fun cacheAudio(key: String, bytes: ByteArray) {
        if (audioCache.size >= MAX_CACHE_SIZE) {
            audioCache.keys.firstOrNull()?.let { audioCache.remove(it) }
        }
        audioCache[key] = bytes
    }

    override fun stop() {
        isStopped = true
        prefetchJob?.cancel()
        prefetchJob = null
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
        audioPlayer.stop()
    }

    override fun pause() {
        isPaused = true
        audioPlayer.pause()
    }

    override fun resume() {
        isPaused = false
        audioPlayer.resume()
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.5f, 2.5f)
        audioPlayer.setSpeed(this.speed)
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
    }

    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
        callback.onReady()
    }

    override fun isReady(): Boolean = true

    override fun cleanup() {
        stop()
        prefetchJob?.cancel()
        prefetchJob = null
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
        audioCache.clear()
        scope.cancel()
        audioPlayer.release()
    }

    override fun getEngineName(): String = config.name
}
