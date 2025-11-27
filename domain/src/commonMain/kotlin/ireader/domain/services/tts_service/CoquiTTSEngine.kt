package ireader.domain.services.tts_service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Common Coqui TTS Engine implementation
 * 
 * This engine connects to a Coqui TTS server (Hugging Face Space or self-hosted)
 * and generates speech via HTTP requests. Works on both Android and Desktop.
 * 
 * The server should be running the Gradio-based Coqui TTS app.
 */
class CoquiTTSEngine(
    private val spaceUrl: String,
    private val apiKey: String? = null,
    private val httpClient: HttpClient,
    private val audioPlayer: CoquiAudioPlayer
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private var currentJob: Job? = null
    private var speed: Float = 1.0f
    private var pitch: Float = 1.0f
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Audio cache for pre-fetching
    private val audioCache = mutableMapOf<String, ByteArray>()
    private val loadingParagraphs = mutableSetOf<String>()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphsFlow = MutableStateFlow<Set<Int>>(emptySet())
    val loadingParagraphsFlow: StateFlow<Set<Int>> = _loadingParagraphsFlow.asStateFlow()
    
    private var isInitialized = false
    
    companion object {
        private const val TAG = "CoquiTTSEngine"
        private const val MAX_CACHE_SIZE = 10
        private const val MAX_TEXT_LENGTH = 5000
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        // Cancel any ongoing speech
        currentJob?.cancel()
        
        currentJob = scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Check cache first
                val cachedAudio = audioCache[utteranceId]
                if (cachedAudio != null) {
                    Log.info { "$TAG: Playing cached audio for utterance $utteranceId" }
                    playAudio(cachedAudio, utteranceId)
                    return@launch
                }
                
                // Generate audio from server
                val audioData = generateSpeech(text)
                if (audioData != null) {
                    // Cache the audio
                    cacheAudio(utteranceId, audioData)
                    playAudio(audioData, utteranceId)
                } else {
                    callback?.onError(utteranceId, "Failed to generate speech")
                }
            } catch (e: CancellationException) {
                Log.info { "$TAG: Speech cancelled for utterance $utteranceId" }
            } catch (e: Exception) {
                Log.error { "$TAG: Error speaking: ${e.message}" }
                callback?.onError(utteranceId, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Pre-cache paragraphs for smoother playback
     */
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) {
        paragraphs.forEach { (utteranceId, text) ->
            if (!audioCache.containsKey(utteranceId) && !loadingParagraphs.contains(utteranceId)) {
                loadingParagraphs.add(utteranceId)
                updateLoadingState()
                
                scope.launch {
                    try {
                        val audioData = generateSpeech(text)
                        if (audioData != null) {
                            cacheAudio(utteranceId, audioData)
                        }
                    } catch (e: Exception) {
                        Log.error { "$TAG: Error pre-caching paragraph $utteranceId: ${e.message}" }
                    } finally {
                        loadingParagraphs.remove(utteranceId)
                        updateLoadingState()
                    }
                }
            }
        }
    }
    
    /**
     * Get cache status for a paragraph
     */
    fun getCacheStatus(utteranceId: String): CacheStatus {
        return when {
            audioCache.containsKey(utteranceId) -> CacheStatus.CACHED
            loadingParagraphs.contains(utteranceId) -> CacheStatus.LOADING
            else -> CacheStatus.NOT_CACHED
        }
    }
    
    enum class CacheStatus {
        NOT_CACHED,
        LOADING,
        CACHED
    }
    
    private suspend fun generateSpeech(text: String): ByteArray? {
        val truncatedText = if (text.length > MAX_TEXT_LENGTH) {
            text.take(MAX_TEXT_LENGTH)
        } else {
            text
        }
        
        return try {
            // Gradio API endpoint
            val apiUrl = buildGradioApiUrl()
            
            Log.info { "$TAG: Generating speech for ${truncatedText.length} chars at ${speed}x speed" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                apiKey?.let { 
                    header("Authorization", "Bearer $it")
                }
                setBody(buildGradioRequest(truncatedText, speed))
            }
            
            if (response.status.isSuccess()) {
                parseGradioResponse(response)
            } else {
                Log.error { "$TAG: Server returned ${response.status}" }
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error generating speech: ${e.message}" }
            null
        }
    }
    
    private fun buildGradioApiUrl(): String {
        // Gradio API endpoint format
        val baseUrl = spaceUrl.trimEnd('/')
        return "$baseUrl/api/predict"
    }
    
    private fun buildGradioRequest(text: String, speed: Float): String {
        // Gradio request format
        return """{"data": ["$text", $speed]}"""
    }
    
    private suspend fun parseGradioResponse(response: HttpResponse): ByteArray? {
        return try {
            // Gradio returns JSON with audio data
            // The format depends on the Gradio interface configuration
            val responseText = response.bodyAsText()
            
            // Parse the response to extract audio URL or data
            // This is a simplified implementation - actual parsing depends on Gradio version
            val audioUrl = extractAudioUrl(responseText)
            
            if (audioUrl != null) {
                // Download the audio file
                downloadAudio(audioUrl)
            } else {
                // Try to extract base64 audio data
                extractBase64Audio(responseText)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error parsing response: ${e.message}" }
            null
        }
    }
    
    private fun extractAudioUrl(responseJson: String): String? {
        // Simple JSON parsing for audio URL
        // Format: {"data": [{"name": "...", "data": "file=..."}]}
        val regex = """"data":\s*\[\s*\{\s*"name":\s*"[^"]*",\s*"data":\s*"file=([^"]+)"""".toRegex()
        val match = regex.find(responseJson)
        return match?.groupValues?.getOrNull(1)?.let { 
            "${spaceUrl.trimEnd('/')}/file=$it"
        }
    }
    
    private fun extractBase64Audio(responseJson: String): ByteArray? {
        // Try to extract base64 encoded audio
        val regex = """"data":\s*"data:audio/[^;]+;base64,([^"]+)"""".toRegex()
        val match = regex.find(responseJson)
        return match?.groupValues?.getOrNull(1)?.let { base64 ->
            try {
                decodeBase64(base64)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private suspend fun downloadAudio(url: String): ByteArray? {
        return try {
            val response = httpClient.get(url) {
                apiKey?.let { 
                    header("Authorization", "Bearer $it")
                }
            }
            if (response.status.isSuccess()) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error downloading audio: ${e.message}" }
            null
        }
    }
    
    private fun decodeBase64(base64: String): ByteArray {
        // Platform-specific base64 decoding will be handled by expect/actual
        return base64DecodeToBytes(base64)
    }
    
    private suspend fun playAudio(audioData: ByteArray, utteranceId: String) {
        try {
            audioPlayer.play(audioData) {
                callback?.onDone(utteranceId)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error playing audio: ${e.message}" }
            callback?.onError(utteranceId, e.message ?: "Playback error")
        }
    }
    
    private fun cacheAudio(utteranceId: String, audioData: ByteArray) {
        // Limit cache size
        if (audioCache.size >= MAX_CACHE_SIZE) {
            val oldestKey = audioCache.keys.firstOrNull()
            oldestKey?.let { audioCache.remove(it) }
        }
        audioCache[utteranceId] = audioData
        updateCacheState()
    }
    
    private fun updateCacheState() {
        _cachedParagraphs.value = audioCache.keys.mapNotNull { it.toIntOrNull() }.toSet()
    }
    
    private fun updateLoadingState() {
        _loadingParagraphsFlow.value = loadingParagraphs.mapNotNull { it.toIntOrNull() }.toSet()
    }
    
    override fun stop() {
        currentJob?.cancel()
        audioPlayer.stop()
    }
    
    override fun pause() {
        audioPlayer.pause()
    }
    
    override fun resume() {
        audioPlayer.resume()
    }
    
    override fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
        // Note: Coqui TTS doesn't support pitch adjustment in the same way
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return spaceUrl.isNotEmpty()
    }
    
    override fun cleanup() {
        scope.cancel()
        currentJob?.cancel()
        audioCache.clear()
        loadingParagraphs.clear()
        audioPlayer.release()
    }
    
    override fun getEngineName(): String = "Coqui TTS"
    
    /**
     * Clear the audio cache
     */
    fun clearCache() {
        audioCache.clear()
        updateCacheState()
    }
}

/**
 * Platform-specific audio player interface for Coqui TTS
 */
interface CoquiAudioPlayer {
    suspend fun play(audioData: ByteArray, onComplete: () -> Unit)
    fun stop()
    fun pause()
    fun resume()
    fun release()
}

/**
 * Platform-specific base64 decoding
 */
expect fun base64DecodeToBytes(base64: String): ByteArray
