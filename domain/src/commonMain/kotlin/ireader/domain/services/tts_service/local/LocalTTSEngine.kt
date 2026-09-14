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

    private data class SynthesisRequest(
        val utteranceId: String,
        val text: String,
        val deferred: CompletableDeferred<ByteArray?> = CompletableDeferred()
    )

    // In-memory cache for audio bytes (thread-safe LRU tracking)
    private val cacheLock = Any()
    private val audioCache = mutableMapOf<String, ByteArray>()
    private val accessOrder = mutableListOf<String>()

    // Sequential synthesis queue and active synthesis tracking
    private val queueLock = Any()
    private val synthesisQueue = mutableListOf<SynthesisRequest>()
    private val activeSynthesis = synchronizedMapOf<String, CompletableDeferred<ByteArray?>>()
    private var queueWorkerJob: Job? = null

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
        private const val MAX_CACHE_SIZE = 100
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
            // 1. Check if already present in cache
            var audioData = getAudio(utteranceId, trimmed)

            // 2. If not cached, check if background synthesis is already actively generating it
            if (audioData == null) {
                val inFlight = activeSynthesis[utteranceId] ?: activeSynthesis[trimmed]
                if (inFlight != null) {
                    Log.info { "$TAG: Awaiting in-flight synthesis for utterance $utteranceId" }
                    audioData = inFlight.await()
                }
            }

            // 3. If still null, synthesize immediately with priority (bypassing remaining queue)
            if (audioData == null) {
                synchronized(queueLock) {
                    synthesisQueue.removeAll { it.utteranceId == utteranceId || it.text == trimmed }
                }
                Log.info { "$TAG: Priority synthesizing speech for utterance $utteranceId (${trimmed.take(30)}...)" }
                audioData = generateAudio(trimmed)
                if (audioData != null && audioData.isNotEmpty()) {
                    cacheAudio(utteranceId, trimmed, audioData)
                    callback?.onCached(utteranceId)
                }
            }

            if (isStopped) {
                return
            }

            if (audioData != null && audioData.isNotEmpty()) {
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
     * Retrieves cached audio by utteranceId or text, updating access order.
     */
    fun getAudio(utteranceId: String, text: String): ByteArray? {
        val trimmed = text.trim()
        return synchronized(cacheLock) {
            val audio = audioCache[utteranceId] ?: audioCache[trimmed]
            if (audio != null) {
                accessOrder.remove(trimmed)
                accessOrder.add(trimmed)
            }
            audio
        }
    }

    /**
     * Caches audio bytes associated with both utteranceId and text.
     * Enforces LRU eviction when capacity is reached.
     */
    fun cacheAudio(utteranceId: String, text: String, bytes: ByteArray) {
        val trimmed = text.trim()
        synchronized(cacheLock) {
            while (accessOrder.size >= MAX_CACHE_SIZE && accessOrder.isNotEmpty()) {
                val oldest = accessOrder.removeAt(0)
                audioCache.remove(oldest)
            }
            audioCache[utteranceId] = bytes
            audioCache[trimmed] = bytes
            accessOrder.remove(trimmed)
            accessOrder.add(trimmed)
        }
    }

    /**
     * Empties the audio cache and access tracker.
     */
    fun clearCache() {
        synchronized(cacheLock) {
            audioCache.clear()
            accessOrder.clear()
        }
    }

    fun isTextCached(text: String): Boolean {
        val trimmed = text.trim()
        return synchronized(cacheLock) {
            audioCache.containsKey(trimmed)
        }
    }

    fun isUtteranceCached(utteranceId: String): Boolean {
        return synchronized(cacheLock) {
            audioCache.containsKey(utteranceId)
        }
    }

    private fun ensureQueueWorker(): Job? {
        return synchronized(queueLock) {
            if (queueWorkerJob?.isActive == true) return@synchronized queueWorkerJob
            val job = scope.launch {
                while (isActive && !isStopped) {
                    val request = synchronized(queueLock) {
                        if (synthesisQueue.isEmpty()) null else synthesisQueue.removeAt(0)
                    } ?: break

                    val (id, text, deferred) = request
                    if (!isActive || isStopped) {
                        deferred.complete(null)
                        break
                    }

                    // Check if already synthesized/cached (e.g. by priority speak)
                    val existing = getAudio(id, text)
                    if (existing != null) {
                        deferred.complete(existing)
                        callback?.onCached(id)
                        continue
                    }

                    activeSynthesis[id] = deferred
                    activeSynthesis[text] = deferred

                    try {
                        val audioData = generateAudio(text)
                        if (audioData != null && audioData.isNotEmpty()) {
                            cacheAudio(id, text, audioData)
                            deferred.complete(audioData)
                            callback?.onCached(id)
                            Log.info { "$TAG: Queue synthesized & cached $id (${audioData.size} bytes)" }
                        } else {
                            deferred.complete(null)
                        }
                    } catch (e: CancellationException) {
                        deferred.cancel(e)
                        throw e
                    } catch (e: Exception) {
                        Log.warn { "$TAG: Queue synthesis failed for $id: ${e.message}" }
                        deferred.complete(null)
                    } finally {
                        activeSynthesis.remove(id)
                        activeSynthesis.remove(text)
                    }
                }
            }
            queueWorkerJob = job
            job
        }
    }

    /**
     * Pre-cache an upcoming paragraph in background
     */
    fun precache(utteranceId: String, text: String): Job? {
        val trimmed = text.trim()
        if (!hasContent(trimmed) || isUtteranceCached(utteranceId) || isTextCached(trimmed)) {
            return null
        }
        val deferred = CompletableDeferred<ByteArray?>()
        synchronized(queueLock) {
            synthesisQueue.add(0, SynthesisRequest(utteranceId, trimmed, deferred))
        }
        ensureQueueWorker()
        return scope.launch {
            deferred.await()
        }
    }

    /**
     * Continuously queues upcoming items for sequential synthesis into cache.
     * Starts the background synthesis worker if not already active.
     */
    fun precacheNext(items: List<Pair<String, String>>): Job? {
        var addedAny = false
        synchronized(queueLock) {
            for ((id, text) in items) {
                val trimmed = text.trim()
                if (!hasContent(trimmed)) continue
                if (isUtteranceCached(id) || isTextCached(trimmed)) continue
                if (synthesisQueue.any { it.utteranceId == id || it.text == trimmed }) continue
                if (activeSynthesis.containsKey(id) || activeSynthesis.containsKey(trimmed)) continue

                synthesisQueue.add(SynthesisRequest(id, trimmed))
                addedAny = true
            }
        }
        if (addedAny) {
            ensureQueueWorker()
        }
        return synchronized(queueLock) { queueWorkerJob }
    }

    /**
     * Clears internal state (synthesis queue and cache) on chapter transition.
     */
    fun clearState() {
        Log.info { "$TAG: clearState() - clearing synthesis queue and cache" }
        synchronized(queueLock) {
            synthesisQueue.forEach { it.deferred.cancel() }
            synthesisQueue.clear()
        }
        queueWorkerJob?.cancel()
        queueWorkerJob = null
        clearCache()
    }

    override fun stop() {
        isStopped = true
        synchronized(queueLock) {
            synthesisQueue.forEach { it.deferred.cancel() }
            synthesisQueue.clear()
        }
        queueWorkerJob?.cancel()
        queueWorkerJob = null
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
        clearCache()
        scope.cancel()
        audioPlayer.release()
    }

    override fun getEngineName(): String = config.name
}
