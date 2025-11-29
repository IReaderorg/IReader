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
        
        // Escape special characters in text for JSON
        val escapedText = truncatedText
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        
        Log.info { "$TAG: Generating speech for ${truncatedText.length} chars at ${speed}x speed" }
        Log.info { "$TAG: Space URL: $spaceUrl" }
        
        // Try legacy /api/predict first (most common)
        var audioData = tryLegacyGradioApi(escapedText)
        
        // If that fails, try Gradio 4.x /call endpoint
        if (audioData == null) {
            Log.info { "$TAG: Trying Gradio 4.x call API..." }
            audioData = tryGradio4Api(escapedText)
        }
        
        // If both fail, try the /run endpoint
        if (audioData == null) {
            Log.info { "$TAG: Trying Gradio run API..." }
            audioData = tryGradioRunApi(escapedText)
        }
        
        // Try queue-based API (Gradio 4.x)
        if (audioData == null) {
            Log.info { "$TAG: Trying Gradio queue API..." }
            audioData = tryGradioQueueApi(escapedText)
        }
        
        return audioData
    }
    
    private suspend fun tryGradioQueueApi(text: String): ByteArray? {
        return try {
            val baseUrl = spaceUrl.trimEnd('/')
            
            // Step 1: Submit to queue
            val queueUrl = "$baseUrl/queue/join"
            Log.info { "$TAG: Trying Gradio queue API: $queueUrl" }
            
            val submitResponse = httpClient.post(queueUrl) {
                contentType(ContentType.Application.Json)
                apiKey?.let { header("Authorization", "Bearer $it") }
                setBody("""{"data": ["$text", $speed], "fn_index": 0, "session_hash": "${System.currentTimeMillis()}"}""")
            }
            
            if (!submitResponse.status.isSuccess()) {
                Log.warn { "$TAG: Queue submit failed: ${submitResponse.status}" }
                return null
            }
            
            // Step 2: Poll for result
            val responseText = submitResponse.bodyAsText()
            Log.debug { "$TAG: Queue response: $responseText" }
            
            parseGradioResponse(submitResponse)
        } catch (e: Exception) {
            Log.warn { "$TAG: Gradio queue API error: ${e.message}" }
            null
        }
    }
    
    private suspend fun tryGradio4Api(text: String): ByteArray? {
        return try {
            val apiUrl = buildGradioApiUrl()
            Log.info { "$TAG: Trying Gradio 4.x API: $apiUrl" }
            
            // Step 1: POST to get event_id
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(buildGradioRequest(text, speed))
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "$TAG: Gradio 4.x API returned ${response.status}" }
                return null
            }
            
            val responseText = response.bodyAsText()
            Log.debug { "$TAG: Call response: $responseText" }
            
            // Extract event_id
            val eventIdRegex = """"event_id":\s*"([^"]+)"""".toRegex()
            val eventIdMatch = eventIdRegex.find(responseText)
            val eventId = eventIdMatch?.groupValues?.getOrNull(1)
            
            if (eventId.isNullOrEmpty()) {
                Log.warn { "$TAG: No event_id in response" }
                return null
            }
            
            Log.info { "$TAG: Got event_id: $eventId" }
            
            // Step 2: GET result using event_id
            val resultUrl = "$apiUrl/$eventId"
            Log.info { "$TAG: Getting result from: $resultUrl" }
            
            val resultResponse = httpClient.get(resultUrl) {
                apiKey?.let { header("Authorization", "Bearer $it") }
                header("Accept", "text/event-stream")
            }
            
            if (!resultResponse.status.isSuccess()) {
                Log.warn { "$TAG: Result request failed: ${resultResponse.status}" }
                return null
            }
            
            val resultBody = resultResponse.bodyAsText()
            Log.debug { "$TAG: Result body: ${resultBody.take(500)}" }
            
            // Parse SSE response to get audio path
            parseSSEResponse(resultBody)
        } catch (e: Exception) {
            Log.warn { "$TAG: Gradio 4.x API error: ${e.message}" }
            null
        }
    }
    
    private suspend fun parseSSEResponse(sseBody: String): ByteArray? {
        val baseUrl = spaceUrl.trimEnd('/')
        var audioPath: String? = null
        var audioUrl: String? = null
        
        val lines = sseBody.split("\n")
        for (line in lines) {
            if (line.startsWith("data:")) {
                val jsonData = line.substring(5).trim()
                if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                    // Try to extract audio path/url from JSON
                    val pathRegex = """"path":\s*"([^"]+)"""".toRegex()
                    val urlRegex = """"url":\s*"([^"]+)"""".toRegex()
                    
                    pathRegex.find(jsonData)?.let { audioPath = it.groupValues[1] }
                    urlRegex.find(jsonData)?.let { audioUrl = it.groupValues[1] }
                    
                    if (!audioPath.isNullOrEmpty() || !audioUrl.isNullOrEmpty()) {
                        Log.info { "$TAG: Found audio - path: $audioPath, url: $audioUrl" }
                        break
                    }
                }
            }
        }
        
        if (audioPath.isNullOrEmpty() && audioUrl.isNullOrEmpty()) {
            Log.warn { "$TAG: No audio path or URL found in SSE response" }
            return null
        }
        
        // Download audio
        val downloadUrl = when {
            !audioUrl.isNullOrEmpty() -> if (audioUrl.startsWith("http")) audioUrl else "$baseUrl$audioUrl"
            !audioPath.isNullOrEmpty() -> "$baseUrl/file=$audioPath"
            else -> return null
        }
        
        Log.info { "$TAG: Downloading audio from: $downloadUrl" }
        return downloadAudio(downloadUrl)
    }
    
    private suspend fun tryLegacyGradioApi(text: String): ByteArray? {
        return try {
            val apiUrl = buildLegacyGradioApiUrl()
            Log.info { "$TAG: Trying legacy Gradio API: $apiUrl" }
            
            // Try with fn_index format (Gradio 3.x)
            val requestBody = """{"data": ["$text", $speed], "fn_index": 0}"""
            Log.debug { "$TAG: Request body: $requestBody" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(requestBody)
            }
            
            Log.info { "$TAG: Legacy API response status: ${response.status}" }
            
            if (response.status.isSuccess()) {
                parseGradioResponse(response)
            } else {
                val errorBody = response.bodyAsText()
                Log.warn { "$TAG: Legacy Gradio API returned ${response.status}: $errorBody" }
                null
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Legacy Gradio API error: ${e.message}" }
            null
        }
    }
    
    private suspend fun tryGradioRunApi(text: String): ByteArray? {
        return try {
            // Gradio 4.x uses /run endpoint with function name
            val baseUrl = spaceUrl.trimEnd('/')
            val apiUrl = "$baseUrl/run/text_to_speech"
            Log.info { "$TAG: Trying Gradio run API: $apiUrl" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                apiKey?.let { header("Authorization", "Bearer $it") }
                setBody("""{"data": ["$text", $speed]}""")
            }
            
            if (response.status.isSuccess()) {
                parseGradioResponse(response)
            } else {
                Log.error { "$TAG: Gradio run API returned ${response.status}" }
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Gradio run API error: ${e.message}" }
            null
        }
    }
    
    private fun buildGradioApiUrl(): String {
        // Gradio 4.x API endpoint format - correct path
        val baseUrl = spaceUrl.trimEnd('/')
        return "$baseUrl/gradio_api/call/text_to_speech"
    }
    
    private fun buildGradioRequest(text: String, speed: Float): String {
        // Gradio 4.x request format
        return """{"data": ["$text", $speed]}"""
    }
    
    /**
     * Alternative API URL for older Gradio versions
     */
    private fun buildLegacyGradioApiUrl(): String {
        val baseUrl = spaceUrl.trimEnd('/')
        return "$baseUrl/api/predict"
    }
    
    private suspend fun parseGradioResponse(response: HttpResponse): ByteArray? {
        return try {
            // Gradio returns JSON with audio data
            // The format depends on the Gradio interface configuration
            val responseText = response.bodyAsText()
            Log.debug { "$TAG: Response: ${responseText.take(500)}..." }
            
            // Try different extraction methods
            var audioData: ByteArray? = null
            
            // Method 1: Extract audio URL from Gradio 4.x format
            val audioUrl = extractAudioUrl(responseText)
            if (audioUrl != null) {
                Log.info { "$TAG: Found audio URL: $audioUrl" }
                audioData = downloadAudio(audioUrl)
            }
            
            // Method 2: Try to extract base64 audio data
            if (audioData == null) {
                audioData = extractBase64Audio(responseText)
                if (audioData != null) {
                    Log.info { "$TAG: Extracted base64 audio data" }
                }
            }
            
            // Method 3: Try numpy array format (Gradio audio output)
            if (audioData == null) {
                audioData = extractNumpyAudio(responseText)
                if (audioData != null) {
                    Log.info { "$TAG: Extracted numpy audio data" }
                }
            }
            
            audioData
        } catch (e: Exception) {
            Log.error { "$TAG: Error parsing response: ${e.message}" }
            null
        }
    }
    
    private fun extractAudioUrl(responseJson: String): String? {
        val baseUrl = spaceUrl.trimEnd('/')
        
        // Try Gradio 4.x format: {"data": [{"path": "...", "url": "..."}]}
        var regex = """"url":\s*"([^"]+)"""".toRegex()
        var match = regex.find(responseJson)
        if (match != null) {
            val url = match.groupValues[1]
            return if (url.startsWith("http")) url else "$baseUrl$url"
        }
        
        // Try path format
        regex = """"path":\s*"([^"]+)"""".toRegex()
        match = regex.find(responseJson)
        if (match != null) {
            val path = match.groupValues[1]
            return "$baseUrl/file=$path"
        }
        
        // Try file= format
        regex = """"data":\s*"file=([^"]+)"""".toRegex()
        match = regex.find(responseJson)
        if (match != null) {
            return "$baseUrl/file=${match.groupValues[1]}"
        }
        
        // Try simple file path in data array
        regex = """"data":\s*\[\s*"(/[^"]+)"""".toRegex()
        match = regex.find(responseJson)
        if (match != null) {
            return "$baseUrl/file=${match.groupValues[1]}"
        }
        
        return null
    }
    
    private fun extractBase64Audio(responseJson: String): ByteArray? {
        // Try to extract base64 encoded audio
        val regex = """data:audio/[^;]+;base64,([A-Za-z0-9+/=]+)""".toRegex()
        val match = regex.find(responseJson)
        return match?.groupValues?.getOrNull(1)?.let { base64 ->
            try {
                decodeBase64(base64)
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to decode base64: ${e.message}" }
                null
            }
        }
    }
    
    private fun extractNumpyAudio(responseJson: String): ByteArray? {
        // Gradio returns audio as tuple (sample_rate, numpy_array)
        // Format: {"data": [[22050, [0.1, 0.2, ...]]]}
        // This is complex to parse without proper JSON library
        // For now, return null and rely on URL extraction
        return null
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
