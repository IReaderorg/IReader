package ireader.domain.services.tts_service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Generic Gradio TTS Engine that works with any Gradio-based TTS space.
 * 
 * Supports multiple Gradio API versions:
 * - Gradio 3.x: /api/predict with fn_index
 * - Gradio 4.x: /call/{api_name} with event streaming
 * - Queue-based: /queue/join for long-running tasks
 */
class GenericGradioTTSEngine(
    private val config: GradioTTSConfig,
    private val httpClient: HttpClient,
    private val audioPlayer: CoquiAudioPlayer
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private var currentJob: Job? = null
    private var speed: Float = config.defaultSpeed
    private var pitch: Float = 1.0f
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Audio cache for pre-fetching
    private val audioCache = mutableMapOf<String, ByteArray>()
    private val loadingParagraphs = mutableSetOf<String>()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphsFlow = MutableStateFlow<Set<Int>>(emptySet())
    val loadingParagraphsFlow: StateFlow<Set<Int>> = _loadingParagraphsFlow.asStateFlow()
    
    companion object {
        private const val TAG = "GenericGradioTTS"
        private const val MAX_CACHE_SIZE = 10
        private const val MAX_TEXT_LENGTH = 5000
        
        // Base64 decoding characters
        private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        
        /**
         * Decode base64 string to byte array
         */
        fun base64DecodeToBytes(base64: String): ByteArray {
            val cleanBase64 = base64.replace("\\s".toRegex(), "")
            val padding = when {
                cleanBase64.endsWith("==") -> 2
                cleanBase64.endsWith("=") -> 1
                else -> 0
            }
            val noPadding = cleanBase64.trimEnd('=')
            val outputLength = (noPadding.length * 3) / 4
            val output = ByteArray(outputLength)
            
            var outputIndex = 0
            var i = 0
            while (i < noPadding.length) {
                val b0 = BASE64_CHARS.indexOf(noPadding[i])
                val b1 = if (i + 1 < noPadding.length) BASE64_CHARS.indexOf(noPadding[i + 1]) else 0
                val b2 = if (i + 2 < noPadding.length) BASE64_CHARS.indexOf(noPadding[i + 2]) else 0
                val b3 = if (i + 3 < noPadding.length) BASE64_CHARS.indexOf(noPadding[i + 3]) else 0
                
                if (outputIndex < output.size) output[outputIndex++] = ((b0 shl 2) or (b1 shr 4)).toByte()
                if (outputIndex < output.size) output[outputIndex++] = (((b1 and 0x0F) shl 4) or (b2 shr 2)).toByte()
                if (outputIndex < output.size) output[outputIndex++] = (((b2 and 0x03) shl 6) or b3).toByte()
                
                i += 4
            }
            
            return output.copyOf(outputLength - padding)
        }
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        currentJob?.cancel()
        
        currentJob = scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Check cache first
                val cachedAudio = audioCache[utteranceId]
                if (cachedAudio != null) {
                    Log.info { "$TAG: Playing cached audio for $utteranceId" }
                    playAudio(cachedAudio, utteranceId)
                    return@launch
                }
                
                // Generate audio from server
                val audioData = generateSpeech(text)
                if (audioData != null) {
                    cacheAudio(utteranceId, audioData)
                    playAudio(audioData, utteranceId)
                } else {
                    callback?.onError(utteranceId, "Failed to generate speech")
                }
            } catch (e: CancellationException) {
                Log.info { "$TAG: Speech cancelled for $utteranceId" }
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
                        Log.error { "$TAG: Error pre-caching $utteranceId: ${e.message}" }
                    } finally {
                        loadingParagraphs.remove(utteranceId)
                        updateLoadingState()
                    }
                }
            }
        }
    }

    
    private suspend fun generateSpeech(text: String): ByteArray? {
        val truncatedText = if (text.length > MAX_TEXT_LENGTH) {
            text.take(MAX_TEXT_LENGTH)
        } else {
            text
        }
        
        Log.info { "$TAG: Generating speech for ${truncatedText.length} chars using ${config.name}" }
        Log.info { "$TAG: Space URL: ${config.spaceUrl}, API: ${config.apiName}" }
        
        // Build request body based on config parameters
        val requestBody = buildRequestBody(truncatedText)
        
        // Try different Gradio API versions
        var audioData: ByteArray? = null
        
        // Try Gradio 4.x call API first (most modern)
        audioData = tryGradio4CallApi(requestBody)
        
        // Try legacy /api/predict (Gradio 3.x)
        if (audioData == null) {
            Log.info { "$TAG: Trying legacy Gradio API..." }
            audioData = tryLegacyGradioApi(requestBody)
        }
        
        // Try /run/{api_name} endpoint
        if (audioData == null) {
            Log.info { "$TAG: Trying Gradio run API..." }
            audioData = tryGradioRunApi(requestBody)
        }
        
        // Try queue-based API
        if (audioData == null) {
            Log.info { "$TAG: Trying Gradio queue API..." }
            audioData = tryGradioQueueApi(requestBody)
        }
        
        return audioData
    }
    
    /**
     * Build the request body JSON based on config parameters
     */
    private fun buildRequestBody(text: String): String {
        val dataValues = config.parameters.map { param ->
            when {
                param.isTextInput -> escapeJsonString(text)
                param.isSpeedInput -> speed.toString()
                else -> when (param.type) {
                    GradioParamType.STRING -> "\"${escapeJsonString(param.defaultValue ?: "")}\""
                    GradioParamType.FLOAT -> (param.defaultValue?.toFloatOrNull() ?: 1.0f).toString()
                    GradioParamType.INT -> (param.defaultValue?.toIntOrNull() ?: 0).toString()
                    GradioParamType.BOOLEAN -> (param.defaultValue?.toBooleanStrictOrNull() ?: false).toString()
                    GradioParamType.CHOICE -> "\"${escapeJsonString(param.defaultValue ?: param.choices?.firstOrNull() ?: "")}\""
                }
            }
        }
        
        // Handle text input specially - it needs quotes
        val formattedValues = config.parameters.mapIndexed { index, param ->
            if (param.isTextInput) {
                "\"${escapeJsonString(text)}\""
            } else if (param.isSpeedInput) {
                speed.toString()
            } else {
                dataValues[index]
            }
        }
        
        return """{"data": [${formattedValues.joinToString(", ")}]}"""
    }
    
    private fun escapeJsonString(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    /**
     * Try Gradio 4.x /call/{api_name} endpoint
     */
    private suspend fun tryGradio4CallApi(requestBody: String): ByteArray? {
        return try {
            val baseUrl = config.spaceUrl.trimEnd('/')
            val apiName = config.apiName.trimStart('/')
            val apiUrl = "$baseUrl/call/$apiName"
            
            Log.info { "$TAG: Trying Gradio 4.x API: $apiUrl" }
            Log.debug { "$TAG: Request body: $requestBody" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(requestBody)
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "$TAG: Gradio 4.x API returned ${response.status}" }
                return null
            }
            
            val responseText = response.bodyAsText()
            Log.debug { "$TAG: Call response: $responseText" }
            
            // Extract event_id for SSE streaming
            val eventIdRegex = """"event_id":\s*"([^"]+)"""".toRegex()
            val eventId = eventIdRegex.find(responseText)?.groupValues?.getOrNull(1)
            
            if (eventId.isNullOrEmpty()) {
                // Maybe direct response without streaming
                return parseGradioResponse(responseText)
            }
            
            Log.info { "$TAG: Got event_id: $eventId" }
            
            // Get result using event_id
            val resultUrl = "$apiUrl/$eventId"
            val resultResponse = httpClient.get(resultUrl) {
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                header("Accept", "text/event-stream")
            }
            
            if (!resultResponse.status.isSuccess()) {
                Log.warn { "$TAG: Result request failed: ${resultResponse.status}" }
                return null
            }
            
            parseSSEResponse(resultResponse.bodyAsText())
        } catch (e: Exception) {
            Log.warn { "$TAG: Gradio 4.x API error: ${e.message}" }
            null
        }
    }
    
    /**
     * Try legacy Gradio 3.x /api/predict endpoint
     */
    private suspend fun tryLegacyGradioApi(requestBody: String): ByteArray? {
        return try {
            val baseUrl = config.spaceUrl.trimEnd('/')
            val apiUrl = "$baseUrl/api/predict"
            
            // Add fn_index for legacy API
            val legacyBody = requestBody.dropLast(1) + """, "fn_index": 0}"""
            
            Log.info { "$TAG: Trying legacy API: $apiUrl" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(legacyBody)
            }
            
            if (response.status.isSuccess()) {
                parseGradioResponse(response.bodyAsText())
            } else {
                Log.warn { "$TAG: Legacy API returned ${response.status}" }
                null
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Legacy API error: ${e.message}" }
            null
        }
    }
    
    /**
     * Try Gradio /run/{api_name} endpoint
     */
    private suspend fun tryGradioRunApi(requestBody: String): ByteArray? {
        return try {
            val baseUrl = config.spaceUrl.trimEnd('/')
            val apiName = config.apiName.trimStart('/').replace("/", "_")
            val apiUrl = "$baseUrl/run/$apiName"
            
            Log.info { "$TAG: Trying run API: $apiUrl" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(requestBody)
            }
            
            if (response.status.isSuccess()) {
                parseGradioResponse(response.bodyAsText())
            } else {
                Log.warn { "$TAG: Run API returned ${response.status}" }
                null
            }
        } catch (e: Exception) {
            Log.warn { "$TAG: Run API error: ${e.message}" }
            null
        }
    }
    
    /**
     * Try Gradio queue-based API for long-running tasks
     */
    private suspend fun tryGradioQueueApi(requestBody: String): ByteArray? {
        return try {
            val baseUrl = config.spaceUrl.trimEnd('/')
            val queueUrl = "$baseUrl/queue/join"
            val sessionHash = System.currentTimeMillis().toString()
            
            // Add session_hash and fn_index
            val queueBody = requestBody.dropLast(1) + """, "fn_index": 0, "session_hash": "$sessionHash"}"""
            
            Log.info { "$TAG: Trying queue API: $queueUrl" }
            
            val response = httpClient.post(queueUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(queueBody)
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "$TAG: Queue submit failed: ${response.status}" }
                return null
            }
            
            // Poll for result
            val dataUrl = "$baseUrl/queue/data?session_hash=$sessionHash"
            var attempts = 0
            val maxAttempts = 30
            
            while (attempts < maxAttempts) {
                delay(1000)
                attempts++
                
                val dataResponse = httpClient.get(dataUrl) {
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                }
                
                if (dataResponse.status.isSuccess()) {
                    val result = parseGradioResponse(dataResponse.bodyAsText())
                    if (result != null) return result
                }
            }
            
            Log.warn { "$TAG: Queue polling timed out" }
            null
        } catch (e: Exception) {
            Log.warn { "$TAG: Queue API error: ${e.message}" }
            null
        }
    }

    
    /**
     * Parse SSE (Server-Sent Events) response to extract audio
     */
    private suspend fun parseSSEResponse(sseBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
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
            !audioUrl.isNullOrEmpty() -> if (audioUrl!!.startsWith("http")) audioUrl!! else "$baseUrl$audioUrl"
            !audioPath.isNullOrEmpty() -> "$baseUrl/file=$audioPath"
            else -> return null
        }
        
        Log.info { "$TAG: Downloading audio from: $downloadUrl" }
        return downloadAudio(downloadUrl)
    }
    
    /**
     * Parse Gradio JSON response to extract audio
     */
    private suspend fun parseGradioResponse(responseJson: String): ByteArray? {
        return try {
            Log.debug { "$TAG: Parsing response: ${responseJson.take(500)}..." }
            
            // Try to extract audio URL
            val audioUrl = extractAudioUrl(responseJson)
            if (audioUrl != null) {
                Log.info { "$TAG: Found audio URL: $audioUrl" }
                return downloadAudio(audioUrl)
            }
            
            // Try base64 encoded audio
            val base64Audio = extractBase64Audio(responseJson)
            if (base64Audio != null) {
                Log.info { "$TAG: Extracted base64 audio" }
                return base64Audio
            }
            
            Log.warn { "$TAG: Could not extract audio from response" }
            null
        } catch (e: Exception) {
            Log.error { "$TAG: Error parsing response: ${e.message}" }
            null
        }
    }
    
    /**
     * Extract audio URL from various Gradio response formats
     */
    private fun extractAudioUrl(responseJson: String): String? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        
        // Try different URL patterns
        val patterns = listOf(
            """"url":\s*"([^"]+)"""",           // Gradio 4.x format
            """"path":\s*"([^"]+)"""",          // Path format
            """"data":\s*"file=([^"]+)"""",     // File= format
            """"data":\s*\[\s*"(/[^"]+)"""",    // Simple path in data array
            """"name":\s*"([^"]+\.(?:wav|mp3|ogg|flac))"""" // Audio file name
        )
        
        for (pattern in patterns) {
            val regex = pattern.toRegex()
            val match = regex.find(responseJson)
            if (match != null) {
                val value = match.groupValues[1]
                return when {
                    value.startsWith("http") -> value
                    value.startsWith("/") -> "$baseUrl/file=$value"
                    else -> "$baseUrl/file=$value"
                }
            }
        }
        
        // Try to find audio in data array at the configured output index
        val dataArrayRegex = """"data":\s*\[(.*?)\]""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val dataMatch = dataArrayRegex.find(responseJson)
        if (dataMatch != null) {
            val dataContent = dataMatch.groupValues[1]
            // Split by comma but respect nested structures
            val items = splitDataArray(dataContent)
            if (items.size > config.audioOutputIndex) {
                val audioItem = items[config.audioOutputIndex].trim()
                // Check if it's a path or URL
                val pathMatch = """"([^"]+)"""".toRegex().find(audioItem)
                if (pathMatch != null) {
                    val path = pathMatch.groupValues[1]
                    if (path.contains(".wav") || path.contains(".mp3") || path.contains(".ogg") || path.contains(".flac")) {
                        return if (path.startsWith("http")) path else "$baseUrl/file=$path"
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Split data array content respecting nested structures
     */
    private fun splitDataArray(content: String): List<String> {
        val items = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()
        
        for (char in content) {
            when (char) {
                '[', '{' -> {
                    depth++
                    current.append(char)
                }
                ']', '}' -> {
                    depth--
                    current.append(char)
                }
                ',' -> {
                    if (depth == 0) {
                        items.add(current.toString())
                        current = StringBuilder()
                    } else {
                        current.append(char)
                    }
                }
                else -> current.append(char)
            }
        }
        if (current.isNotEmpty()) {
            items.add(current.toString())
        }
        
        return items
    }
    
    /**
     * Extract base64 encoded audio from response
     */
    private fun extractBase64Audio(responseJson: String): ByteArray? {
        val regex = """data:audio/[^;]+;base64,([A-Za-z0-9+/=]+)""".toRegex()
        val match = regex.find(responseJson)
        return match?.groupValues?.getOrNull(1)?.let { base64 ->
            try {
                base64DecodeToBytes(base64)
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to decode base64: ${e.message}" }
                null
            }
        }
    }
    
    /**
     * Download audio from URL
     */
    private suspend fun downloadAudio(url: String): ByteArray? {
        return try {
            val response = httpClient.get(url) {
                config.apiKey?.let { header("Authorization", "Bearer $it") }
            }
            if (response.status.isSuccess()) {
                response.body()
            } else {
                Log.error { "$TAG: Failed to download audio: ${response.status}" }
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Error downloading audio: ${e.message}" }
            null
        }
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
    
    // TTSEngine interface implementation
    
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
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return config.spaceUrl.isNotEmpty() && config.enabled
    }
    
    override fun cleanup() {
        scope.cancel()
        currentJob?.cancel()
        audioCache.clear()
        loadingParagraphs.clear()
        audioPlayer.release()
    }
    
    override fun getEngineName(): String = config.name
    
    /**
     * Get the current configuration
     */
    fun getConfig(): GradioTTSConfig = config
    
    /**
     * Clear the audio cache
     */
    fun clearCache() {
        audioCache.clear()
        updateCacheState()
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
}
