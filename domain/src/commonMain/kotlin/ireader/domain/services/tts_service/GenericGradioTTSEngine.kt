package ireader.domain.services.tts_service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Speech request for the queue
 */
private data class SpeechRequest(
    val text: String,
    val utteranceId: String
)

/**
 * Generic Gradio TTS Engine that works with any Gradio-based TTS space.
 * 
 * Features:
 * - Queue-based speech processing (one at a time, in order)
 * - Audio caching for pre-fetched paragraphs
 * - Automatic API endpoint detection and caching
 * - Support for multiple Gradio API versions
 */
class GenericGradioTTSEngine(
    private val config: GradioTTSConfig,
    private val httpClient: HttpClient,
    private val audioPlayer: GradioAudioPlayer
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private var speed: Float = config.defaultSpeed
    private var pitch: Float = 1.0f
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Speech queue - processes one request at a time
    private val speechQueue = Channel<SpeechRequest>(Channel.UNLIMITED)
    private var queueProcessorJob: Job? = null
    private var currentSpeechJob: Job? = null
    private var isPaused = false
    private val pauseMutex = Mutex()
    
    // Audio cache for pre-fetching
    private val audioCache = mutableMapOf<String, ByteArray>()
    private val loadingParagraphs = mutableSetOf<String>()
    private val cacheMutex = Mutex()
    
    // Pre-fetch job management
    private val prefetchJobs = mutableMapOf<String, Job>()
    private val prefetchMutex = Mutex()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphsFlow = MutableStateFlow<Set<Int>>(emptySet())
    val loadingParagraphsFlow: StateFlow<Set<Int>> = _loadingParagraphsFlow.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    init {
        // Start the queue processor
        startQueueProcessor()
    }
    
    companion object {
        private const val TAG = "GenericGradioTTS"
        private const val MAX_CACHE_SIZE = 20
        private const val MAX_TEXT_LENGTH = 5000
        private const val MAX_PREFETCH_CONCURRENT = 2
        
        // Cache of working API types per space URL - persists across engine instances
        private val workingApiCache = mutableMapOf<String, GradioApiType>()
        
        /**
         * Get cached working API type for a space URL
         */
        fun getCachedApiType(spaceUrl: String): GradioApiType? = workingApiCache[spaceUrl]
        
        /**
         * Cache a working API type for a space URL
         */
        fun cacheWorkingApiType(spaceUrl: String, apiType: GradioApiType) {
            workingApiCache[spaceUrl] = apiType
            Log.info { "$TAG: Cached working API type $apiType for $spaceUrl" }
        }
        
        /**
         * Clear the API type cache (useful if an endpoint stops working)
         */
        fun clearApiCache() {
            workingApiCache.clear()
            Log.info { "$TAG: Cleared API type cache" }
        }
    }
    
    /**
     * Start the queue processor that handles speech requests one at a time
     */
    private fun startQueueProcessor() {
        queueProcessorJob?.cancel()
        queueProcessorJob = scope.launch {
            for (request in speechQueue) {
                // Wait if paused
                while (isPaused) {
                    delay(100)
                }
                
                try {
                    processSpeechRequest(request)
                } catch (e: CancellationException) {
                    Log.info { "$TAG: Queue processor cancelled" }
                    break
                } catch (e: Exception) {
                    Log.error { "$TAG: Error processing speech request: ${e.message}" }
                    callback?.onError(request.utteranceId, e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Process a single speech request
     */
    private suspend fun processSpeechRequest(request: SpeechRequest) {
        val (text, utteranceId) = request
        
        _isPlaying.value = true
        callback?.onStart(utteranceId)
        
        try {
            // Check cache first
            val cachedAudio = cacheMutex.withLock { audioCache[utteranceId] }
            
            val audioData = if (cachedAudio != null) {
                Log.info { "$TAG: Playing cached audio for $utteranceId" }
                cachedAudio
            } else {
                // Generate audio from server
                Log.info { "$TAG: Generating audio for $utteranceId" }
                generateSpeech(text)
            }
            
            if (audioData != null) {
                // Cache the audio if not already cached
                if (cachedAudio == null) {
                    cacheAudio(utteranceId, audioData)
                }
                
                // Play the audio and wait for completion
                playAudioAndWait(audioData, utteranceId)
            } else {
                callback?.onError(utteranceId, "Failed to generate speech")
            }
        } catch (e: CancellationException) {
            Log.info { "$TAG: Speech cancelled for $utteranceId" }
            throw e
        } catch (e: Exception) {
            Log.error { "$TAG: Error speaking $utteranceId: ${e.message}" }
            callback?.onError(utteranceId, e.message ?: "Unknown error")
        } finally {
            _isPlaying.value = false
        }
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        // Add to queue instead of processing immediately
        Log.info { "$TAG: Queueing speech request: $utteranceId" }
        speechQueue.send(SpeechRequest(text, utteranceId))
    }
    
    /**
     * Clear the speech queue and stop current playback
     */
    fun clearQueue() {
        Log.info { "$TAG: Clearing speech queue" }
        
        // Cancel current speech
        currentSpeechJob?.cancel()
        
        // Drain the queue
        while (speechQueue.tryReceive().isSuccess) {
            // Keep draining
        }
        
        audioPlayer.stop()
        _isPlaying.value = false
    }
    
    /**
     * Pre-cache paragraphs for smoother playback
     * Limits concurrent prefetch to avoid overwhelming the server
     */
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) {
        scope.launch {
            // Filter out already cached or loading paragraphs
            val toFetch = paragraphs.filter { (utteranceId, _) ->
                cacheMutex.withLock {
                    !audioCache.containsKey(utteranceId) && !loadingParagraphs.contains(utteranceId)
                }
            }.take(MAX_PREFETCH_CONCURRENT) // Limit concurrent prefetch
            
            toFetch.forEach { (utteranceId, text) ->
                prefetchMutex.withLock {
                    if (prefetchJobs.containsKey(utteranceId)) return@forEach
                    
                    loadingParagraphs.add(utteranceId)
                    updateLoadingState()
                    
                    val job = scope.launch {
                        try {
                            val audioData = generateSpeech(text)
                            if (audioData != null) {
                                cacheAudio(utteranceId, audioData)
                                Log.info { "$TAG: Pre-cached audio for $utteranceId" }
                            }
                        } catch (e: CancellationException) {
                            Log.debug { "$TAG: Prefetch cancelled for $utteranceId" }
                        } catch (e: Exception) {
                            Log.warn { "$TAG: Error pre-caching $utteranceId: ${e.message}" }
                        } finally {
                            cacheMutex.withLock {
                                loadingParagraphs.remove(utteranceId)
                            }
                            prefetchMutex.withLock {
                                prefetchJobs.remove(utteranceId)
                            }
                            updateLoadingState()
                        }
                    }
                    prefetchJobs[utteranceId] = job
                }
            }
        }
    }
    
    /**
     * Cancel all prefetch jobs
     */
    fun cancelPrefetch() {
        scope.launch {
            prefetchMutex.withLock {
                prefetchJobs.values.forEach { it.cancel() }
                prefetchJobs.clear()
            }
            cacheMutex.withLock {
                loadingParagraphs.clear()
            }
            updateLoadingState()
        }
    }
    
    /**
     * Play audio and wait for completion
     */
    private suspend fun playAudioAndWait(audioData: ByteArray, utteranceId: String) {
        val completionDeferred = CompletableDeferred<Unit>()
        
        try {
            audioPlayer.play(audioData) {
                // Playback completed
                callback?.onDone(utteranceId)
                completionDeferred.complete(Unit)
            }
            
            // Wait for playback to complete
            completionDeferred.await()
        } catch (e: CancellationException) {
            audioPlayer.stop()
            throw e
        } catch (e: Exception) {
            Log.error { "$TAG: Error playing audio: ${e.message}" }
            callback?.onError(utteranceId, e.message ?: "Playback error")
        }
    }
    
    private suspend fun generateSpeech(text: String): ByteArray? {
        val truncatedText = if (text.length > MAX_TEXT_LENGTH) {
            text.take(MAX_TEXT_LENGTH)
        } else {
            text
        }
        
        Log.info { "$TAG: Generating speech for ${truncatedText.length} chars using ${config.name}" }
        Log.info { "$TAG: Space URL: ${config.spaceUrl}, API: ${config.apiName}, ApiType: ${config.apiType}" }
        
        // Build request body based on config parameters
        val requestBody = buildRequestBody(truncatedText)
        
        // Determine which API type to use
        val apiTypeToUse = when {
            // If config specifies a specific API type (not AUTO), use it
            config.apiType != GradioApiType.AUTO -> config.apiType
            // Check if we have a cached working API type for this space
            else -> getCachedApiType(config.spaceUrl)
        }
        
        // If we know which API works, use it directly
        if (apiTypeToUse != null && apiTypeToUse != GradioApiType.AUTO) {
            Log.info { "$TAG: Using known working API type: $apiTypeToUse" }
            val result = trySpecificApi(apiTypeToUse, requestBody)
            if (result != null) return result
            
            // If the cached API failed, clear cache and try all
            Log.warn { "$TAG: Cached API type $apiTypeToUse failed, trying all endpoints..." }
            workingApiCache.remove(config.spaceUrl)
        }
        
        // Try all API types and cache the one that works
        return tryAllApisAndCache(requestBody)
    }
    
    /**
     * Try a specific API type
     */
    private suspend fun trySpecificApi(apiType: GradioApiType, requestBody: String): ByteArray? {
        return when (apiType) {
            GradioApiType.GRADIO_API_CALL -> tryGradioApiCall(requestBody)
            GradioApiType.CALL -> tryCallApi(requestBody)
            GradioApiType.API_PREDICT -> tryApiPredict(requestBody)
            GradioApiType.RUN -> tryRunApi(requestBody)
            GradioApiType.QUEUE -> tryQueueApi(requestBody)
            GradioApiType.AUTO -> tryAllApisAndCache(requestBody)
        }
    }
    
    /**
     * Try all API types in order and cache the first one that works
     */
    private suspend fun tryAllApisAndCache(requestBody: String): ByteArray? {
        // Order of APIs to try
        val apiTypes = listOf(
            GradioApiType.GRADIO_API_CALL,
            GradioApiType.CALL,
            GradioApiType.API_PREDICT,
            GradioApiType.RUN,
            GradioApiType.QUEUE
        )
        
        for (apiType in apiTypes) {
            Log.info { "$TAG: Trying API type: $apiType" }
            val result = trySpecificApi(apiType, requestBody)
            if (result != null) {
                // Cache this working API type
                cacheWorkingApiType(config.spaceUrl, apiType)
                return result
            }
        }
        
        Log.error { "$TAG: All API types failed for ${config.spaceUrl}" }
        return null
    }
    
    /**
     * Try /gradio_api/call/{fn_name} endpoint (modern Gradio 4.x)
     */
    private suspend fun tryGradioApiCall(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/')
        val apiUrl = "$baseUrl/gradio_api/call/$apiName"
        return tryCallEndpoint(apiUrl, requestBody)
    }
    
    /**
     * Try /call/{fn_name} endpoint (older Gradio 4.x)
     */
    private suspend fun tryCallApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/')
        val apiUrl = "$baseUrl/call/$apiName"
        return tryCallEndpoint(apiUrl, requestBody)
    }
    
    /**
     * Try /api/predict endpoint (legacy Gradio 3.x)
     */
    private suspend fun tryApiPredict(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val legacyBody = requestBody.dropLast(1) + """, "fn_index": 0}"""
        
        // Try both /api/predict and /gradio_api/predict
        for (apiUrl in listOf("$baseUrl/api/predict", "$baseUrl/gradio_api/predict")) {
            try {
                Log.info { "$TAG: Trying predict API: $apiUrl" }
                
                val response = httpClient.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(legacyBody)
                }
                
                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    Log.debug { "$TAG: Predict response: ${responseText.take(300)}..." }
                    val result = parseGradioResponse(responseText)
                    if (result != null) return result
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Predict API error at $apiUrl: ${e.message}" }
            }
        }
        return null
    }
    
    /**
     * Try /run/{fn_name} endpoint
     */
    private suspend fun tryRunApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/').replace("/", "_")
        
        for (apiUrl in listOf("$baseUrl/run/$apiName", "$baseUrl/gradio_api/run/$apiName")) {
            try {
                Log.info { "$TAG: Trying run API: $apiUrl" }
                
                val response = httpClient.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(requestBody)
                }
                
                if (response.status.isSuccess()) {
                    val result = parseGradioResponse(response.bodyAsText())
                    if (result != null) return result
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Run API error at $apiUrl: ${e.message}" }
            }
        }
        return null
    }
    
    /**
     * Try queue-based API for long-running tasks
     */
    private suspend fun tryQueueApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val sessionHash = System.currentTimeMillis().toString()
        val queueBody = requestBody.dropLast(1) + """, "fn_index": 0, "session_hash": "$sessionHash"}"""
        
        for (queuePrefix in listOf("$baseUrl/queue", "$baseUrl/gradio_api/queue")) {
            try {
                val queueUrl = "$queuePrefix/join"
                Log.info { "$TAG: Trying queue API: $queueUrl" }
                
                val response = httpClient.post(queueUrl) {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(queueBody)
                }
                
                if (!response.status.isSuccess()) continue
                
                // Poll for result
                val dataUrl = "$queuePrefix/data?session_hash=$sessionHash"
                repeat(30) {
                    delay(1000)
                    val dataResponse = httpClient.get(dataUrl) {
                        config.apiKey?.let { header("Authorization", "Bearer $it") }
                    }
                    if (dataResponse.status.isSuccess()) {
                        val result = parseGradioResponse(dataResponse.bodyAsText())
                        if (result != null) return result
                    }
                }
            } catch (e: Exception) {
                Log.warn { "$TAG: Queue API error at $queuePrefix: ${e.message}" }
            }
        }
        return null
    }
    
    /**
     * Common logic for /call/ and /gradio_api/call/ endpoints with SSE streaming
     */
    private suspend fun tryCallEndpoint(apiUrl: String, requestBody: String): ByteArray? {
        return try {
            Log.info { "$TAG: Trying call API: $apiUrl" }
            Log.debug { "$TAG: Request body: $requestBody" }
            
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(requestBody)
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "$TAG: Call API returned ${response.status} at $apiUrl" }
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
            Log.warn { "$TAG: Call API error at $apiUrl: ${e.message}" }
            null
        }
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
        
        Log.debug { "$TAG: Extracting audio URL from response (${responseJson.length} chars)" }
        
        // Try different URL patterns
        val patterns = listOf(
            """"url":\s*"([^"]+)"""",           // Gradio 4.x format
            """"path":\s*"([^"]+)"""",          // Path format  
            """"data":\s*"file=([^"]+)"""",     // File= format
            """"data":\s*\[\s*"(/[^"]+)"""",    // Simple path in data array
            """"name":\s*"([^"]+\.(?:wav|mp3|ogg|flac))"""", // Audio file name
            """"orig_name":\s*"([^"]+\.(?:wav|mp3|ogg|flac))"""" // Original file name
        )
        
        for (pattern in patterns) {
            val regex = pattern.toRegex()
            val match = regex.find(responseJson)
            if (match != null) {
                val value = match.groupValues[1]
                Log.info { "$TAG: Pattern '$pattern' matched: $value" }
                return when {
                    value.startsWith("http") -> value
                    value.startsWith("/file=") -> "$baseUrl$value"
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
            Log.debug { "$TAG: Found data array: ${dataContent.take(200)}..." }
            
            // Split by comma but respect nested structures
            val items = splitDataArray(dataContent)
            Log.debug { "$TAG: Data array has ${items.size} items, looking for index ${config.audioOutputIndex}" }
            
            if (items.size > config.audioOutputIndex) {
                val audioItem = items[config.audioOutputIndex].trim()
                Log.debug { "$TAG: Audio item at index ${config.audioOutputIndex}: ${audioItem.take(200)}" }
                
                // Try to extract path from nested object
                val pathInObject = """"path":\s*"([^"]+)"""".toRegex().find(audioItem)
                if (pathInObject != null) {
                    val path = pathInObject.groupValues[1]
                    Log.info { "$TAG: Found path in object: $path" }
                    return if (path.startsWith("http")) path else "$baseUrl/file=$path"
                }
                
                // Try to extract URL from nested object
                val urlInObject = """"url":\s*"([^"]+)"""".toRegex().find(audioItem)
                if (urlInObject != null) {
                    val url = urlInObject.groupValues[1]
                    Log.info { "$TAG: Found URL in object: $url" }
                    return if (url.startsWith("http")) url else "$baseUrl$url"
                }
                
                // Check if it's a simple path string
                val pathMatch = """"([^"]+)"""".toRegex().find(audioItem)
                if (pathMatch != null) {
                    val path = pathMatch.groupValues[1]
                    if (path.contains(".wav") || path.contains(".mp3") || path.contains(".ogg") || path.contains(".flac")) {
                        Log.info { "$TAG: Found audio file path: $path" }
                        return if (path.startsWith("http")) path else "$baseUrl/file=$path"
                    }
                }
            }
        }
        
        Log.warn { "$TAG: Could not extract audio URL from response" }
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
    
    private suspend fun cacheAudio(utteranceId: String, audioData: ByteArray) {
        cacheMutex.withLock {
            // Remove oldest entries if cache is full
            while (audioCache.size >= MAX_CACHE_SIZE) {
                val oldestKey = audioCache.keys.firstOrNull()
                oldestKey?.let { audioCache.remove(it) }
            }
            audioCache[utteranceId] = audioData
        }
        updateCacheState()
    }
    
    private fun updateCacheState() {
        scope.launch {
            cacheMutex.withLock {
                _cachedParagraphs.value = audioCache.keys.mapNotNull { it.toIntOrNull() }.toSet()
            }
        }
    }
    
    private fun updateLoadingState() {
        scope.launch {
            cacheMutex.withLock {
                _loadingParagraphsFlow.value = loadingParagraphs.mapNotNull { it.toIntOrNull() }.toSet()
            }
        }
    }
    
    // TTSEngine interface implementation
    
    override fun stop() {
        Log.info { "$TAG: Stopping TTS" }
        clearQueue()
        cancelPrefetch()
    }
    
    override fun pause() {
        Log.info { "$TAG: Pausing TTS" }
        isPaused = true
        audioPlayer.pause()
    }
    
    override fun resume() {
        Log.info { "$TAG: Resuming TTS" }
        isPaused = false
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
        Log.info { "$TAG: Cleaning up TTS engine" }
        
        // Stop everything
        clearQueue()
        cancelPrefetch()
        
        // Cancel all coroutines
        queueProcessorJob?.cancel()
        scope.cancel()
        
        // Clear caches
        runBlocking {
            cacheMutex.withLock {
                audioCache.clear()
                loadingParagraphs.clear()
            }
        }
        
        // Release audio player
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


/**
 * Platform-specific audio player interface for Gradio-based TTS engines.
 * 
 * Each platform (Android, Desktop) provides its own implementation
 * to play audio data received from the TTS server.
 */
interface GradioAudioPlayer {
    /**
     * Play audio data
     * @param audioData Raw audio bytes (WAV, MP3, OGG, FLAC, etc.)
     * @param onComplete Callback when playback completes
     */
    suspend fun play(audioData: ByteArray, onComplete: () -> Unit)
    
    /**
     * Stop playback immediately
     */
    fun stop()
    
    /**
     * Pause playback
     */
    fun pause()
    
    /**
     * Resume paused playback
     */
    fun resume()
    
    /**
     * Release all resources
     */
    fun release()
}
