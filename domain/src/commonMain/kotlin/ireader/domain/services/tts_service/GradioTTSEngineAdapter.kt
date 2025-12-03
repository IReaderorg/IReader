package ireader.domain.services.tts_service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import ireader.domain.services.tts_service.player.GradioAudioGenerator
import ireader.domain.services.tts_service.player.GradioAudioPlayback
import kotlinx.coroutines.*
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Adapter that wraps Gradio TTS API calls and implements GradioAudioGenerator and GradioAudioPlayback
 * 
 * This is a simplified engine focused on:
 * 1. Generating audio from text via Gradio API
 * 2. Playing audio and waiting for completion
 * 3. Basic playback controls
 */
class GradioTTSEngineAdapter(
    private val config: GradioTTSConfig,
    private val httpClient: HttpClient,
    private val audioPlayer: GradioAudioPlayer
) : GradioAudioGenerator, GradioAudioPlayback {
    
    companion object {
        private const val TAG = "GradioTTSAdapter"
        private const val MAX_TEXT_LENGTH = 5000
        
        // Cache of working API types per space URL
        private val workingApiCache = mutableMapOf<String, GradioApiType>()
        
        fun getCachedApiType(spaceUrl: String): GradioApiType? = workingApiCache[spaceUrl]
        
        fun cacheWorkingApiType(spaceUrl: String, apiType: GradioApiType) {
            workingApiCache[spaceUrl] = apiType
        }
    }
    
    private var speed: Float = config.defaultSpeed
    private var currentPlaybackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override suspend fun generateAudio(text: String): ByteArray? {
        val truncatedText = if (text.length > MAX_TEXT_LENGTH) {
            text.take(MAX_TEXT_LENGTH)
        } else {
            text
        }
        
        val requestBody = buildRequestBody(truncatedText)
        
        // Determine which API type to use
        val apiTypeToUse = when {
            config.apiType != GradioApiType.AUTO -> config.apiType
            else -> getCachedApiType(config.spaceUrl)
        }
        
        // If we know which API works, use it directly
        if (apiTypeToUse != null && apiTypeToUse != GradioApiType.AUTO) {
            val result = trySpecificApi(apiTypeToUse, requestBody)
            if (result != null) return result
            
            // If failed, clear cache and try all
            workingApiCache.remove(config.spaceUrl)
        }
        
        // Try all API types
        return tryAllApis(requestBody)
    }
    
    @Volatile
    private var currentCompletion: CompletableDeferred<Boolean>? = null
    
    override suspend fun playAndWait(audioData: ByteArray): Boolean {
        val completion = CompletableDeferred<Boolean>()
        currentCompletion = completion
        
        try {
            audioPlayer.play(audioData) {
                completion.complete(true)
            }
            return completion.await()
        } catch (e: CancellationException) {
            audioPlayer.stop()
            throw e
        } catch (e: Exception) {
            Log.error { "$TAG: Playback error: ${e.message}" }
            return false
        } finally {
            currentCompletion = null
        }
    }
    
    override fun stop() {
        currentPlaybackJob?.cancel()
        audioPlayer.stop()
        // Reset pause state
        isPaused = false
        // Complete any pending playback wait to unblock the coroutine
        currentCompletion?.complete(false)
        currentCompletion = null
    }
    
    @Volatile
    private var isPaused = false
    
    override fun pause() {
        isPaused = true
        audioPlayer.pause()
        // Complete the playAndWait coroutine so it doesn't block
        // This allows the UI to respond to pause immediately
        currentCompletion?.complete(false)
    }
    
    override fun resume() {
        isPaused = false
        audioPlayer.resume()
    }
    
    fun isPaused(): Boolean = isPaused
    
    override fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        // Gradio TTS typically doesn't support pitch adjustment
        // This is a no-op but required by the interface
    }
    
    override fun release() {
        scope.cancel()
        audioPlayer.release()
    }
    
    // ==================== API Methods ====================
    
    private suspend fun tryAllApis(requestBody: String): ByteArray? {
        val apiTypes = listOf(
            GradioApiType.GRADIO_API_CALL,
            GradioApiType.CALL,
            GradioApiType.API_PREDICT,
            GradioApiType.RUN,
            GradioApiType.QUEUE
        )
        
        for (apiType in apiTypes) {
            val result = trySpecificApi(apiType, requestBody)
            if (result != null) {
                cacheWorkingApiType(config.spaceUrl, apiType)
                return result
            }
        }
        
        return null
    }
    
    private suspend fun trySpecificApi(apiType: GradioApiType, requestBody: String): ByteArray? {
        return when (apiType) {
            GradioApiType.GRADIO_API_CALL -> tryGradioApiCall(requestBody)
            GradioApiType.CALL -> tryCallApi(requestBody)
            GradioApiType.API_PREDICT -> tryApiPredict(requestBody)
            GradioApiType.RUN -> tryRunApi(requestBody)
            GradioApiType.QUEUE -> tryQueueApi(requestBody)
            GradioApiType.AUTO -> tryAllApis(requestBody)
        }
    }
    
    private suspend fun tryGradioApiCall(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/')
        return tryCallEndpoint("$baseUrl/gradio_api/call/$apiName", requestBody)
    }
    
    private suspend fun tryCallApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/')
        return tryCallEndpoint("$baseUrl/call/$apiName", requestBody)
    }
    
    private suspend fun tryCallEndpoint(apiUrl: String, requestBody: String): ByteArray? {
        return try {
            val response = httpClient.post(apiUrl) {
                contentType(ContentType.Application.Json)
                config.apiKey?.let { header("Authorization", "Bearer $it") }
                setBody(requestBody)
            }
            
            if (!response.status.isSuccess()) {
                return null
            }
            
            val responseText = response.bodyAsText()
            
            // Extract event_id for SSE streaming
            val eventIdRegex = """"event_id":\s*"([^"]+)"""".toRegex()
            val eventId = eventIdRegex.find(responseText)?.groupValues?.getOrNull(1)
            
            if (eventId.isNullOrEmpty()) {
                return parseGradioResponse(responseText)
            }
            
            // Get result using event_id - poll with retries
            var lastResult: ByteArray? = null
            repeat(30) { attempt ->
                if (!currentCoroutineContext().isActive) return null
                
                try {
                    val resultResponse = httpClient.get("$apiUrl/$eventId") {
                        config.apiKey?.let { header("Authorization", "Bearer $it") }
                        header("Accept", "text/event-stream")
                    }
                    
                    if (resultResponse.status.isSuccess()) {
                        val parsed = parseSSEResponse(resultResponse.bodyAsText())
                        if (parsed != null) {
                            return parsed
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Retry on timeout
                }
                
                delay(2000) // Wait 2 seconds between retries
            }
            
            lastResult
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun tryApiPredict(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val legacyBody = requestBody.dropLast(1) + """, "fn_index": 0}"""
        
        for (apiUrl in listOf("$baseUrl/api/predict", "$baseUrl/gradio_api/predict")) {
            try {
                val response = httpClient.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(legacyBody)
                }
                
                if (response.status.isSuccess()) {
                    val result = parseGradioResponse(response.bodyAsText())
                    if (result != null) return result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ignore errors, try next URL
            }
        }
        return null
    }
    
    private suspend fun tryRunApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val apiName = config.apiName.trimStart('/').replace("/", "_")
        
        for (apiUrl in listOf("$baseUrl/run/$apiName", "$baseUrl/gradio_api/run/$apiName")) {
            try {
                val response = httpClient.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(requestBody)
                }
                
                if (response.status.isSuccess()) {
                    val result = parseGradioResponse(response.bodyAsText())
                    if (result != null) return result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ignore errors, try next URL
            }
        }
        return null
    }
    
    private suspend fun tryQueueApi(requestBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        val sessionHash = currentTimeToLong().toString()
        val queueBody = requestBody.dropLast(1) + """, "fn_index": 0, "session_hash": "$sessionHash"}"""
        
        for (queuePrefix in listOf("$baseUrl/queue", "$baseUrl/gradio_api/queue")) {
            try {
                val response = httpClient.post("$queuePrefix/join") {
                    contentType(ContentType.Application.Json)
                    config.apiKey?.let { header("Authorization", "Bearer $it") }
                    setBody(queueBody)
                }
                
                if (!response.status.isSuccess()) continue
                
                // Poll for result
                repeat(30) {
                    if (!currentCoroutineContext().isActive) return null
                    delay(1000)
                    val dataResponse = httpClient.get("$queuePrefix/data?session_hash=$sessionHash") {
                        config.apiKey?.let { header("Authorization", "Bearer $it") }
                    }
                    if (dataResponse.status.isSuccess()) {
                        val result = parseGradioResponse(dataResponse.bodyAsText())
                        if (result != null) return result
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Ignore errors, try next URL
            }
        }
        return null
    }
    
    // ==================== Request/Response Parsing ====================
    
    private fun buildRequestBody(text: String): String {
        val formattedValues = config.parameters.map { param ->
            when {
                param.isTextInput -> "\"${escapeJson(text)}\""
                param.isSpeedInput -> speed.toString()
                else -> when (param.type) {
                    GradioParamType.STRING -> "\"${escapeJson(param.defaultValue ?: "")}\""
                    GradioParamType.FLOAT -> (param.defaultValue?.toFloatOrNull() ?: 1.0f).toString()
                    GradioParamType.INT -> (param.defaultValue?.toIntOrNull() ?: 0).toString()
                    GradioParamType.BOOLEAN -> (param.defaultValue?.toBooleanStrictOrNull() ?: false).toString()
                    GradioParamType.CHOICE -> "\"${escapeJson(param.defaultValue ?: param.choices?.firstOrNull() ?: "")}\""
                }
            }
        }
        return """{"data": [${formattedValues.joinToString(", ")}]}"""
    }
    
    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    private suspend fun parseSSEResponse(sseBody: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        
        // Collect all potential audio URLs from the SSE stream
        val audioUrls = mutableListOf<String>()
        
        for (line in sseBody.split("\n")) {
            if (line.startsWith("data:")) {
                val jsonData = line.substring(5).trim()
                
                if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                    // Look for audio file paths/URLs in the JSON
                    val pathRegex = """"path":\s*"([^"]+)"""".toRegex()
                    val urlRegex = """"url":\s*"([^"]+)"""".toRegex()
                    val nameRegex = """"name":\s*"([^"]+)"""".toRegex()
                    
                    // Find all matches
                    pathRegex.findAll(jsonData).forEach { match ->
                        val path = match.groupValues[1]
                        if (path.contains(".wav") || path.contains(".mp3") || 
                            path.contains(".ogg") || path.contains(".flac") ||
                            path.contains("audio") || path.contains("tmp")) {
                            // Construct URL - Gradio 4.x uses /file/ not /file=
                            val url = when {
                                path.startsWith("http") -> path
                                path.startsWith("/") -> "$baseUrl/file$path"
                                else -> "$baseUrl/file/$path"
                            }
                            audioUrls.add(url)
                        }
                    }
                    
                    urlRegex.findAll(jsonData).forEach { match ->
                        val url = match.groupValues[1]
                        if (url.contains(".wav") || url.contains(".mp3") || 
                            url.contains(".ogg") || url.contains(".flac") ||
                            url.contains("audio") || url.contains("file")) {
                            val fullUrl = if (url.startsWith("http")) url else "$baseUrl$url"
                            audioUrls.add(fullUrl)
                        }
                    }
                    
                    nameRegex.findAll(jsonData).forEach { match ->
                        val name = match.groupValues[1]
                        if (name.endsWith(".wav") || name.endsWith(".mp3") || 
                            name.endsWith(".ogg") || name.endsWith(".flac")) {
                            // Construct URL - Gradio 4.x uses /file/ not /file=
                            val url = when {
                                name.startsWith("/") -> "$baseUrl/file$name"
                                else -> "$baseUrl/file/$name"
                            }
                            audioUrls.add(url)
                        }
                    }
                }
            }
        }
        
        // Try to download from each URL until one succeeds
        for (url in audioUrls) {
            val audio = downloadAudio(url)
            if (audio != null && audio.size > 1000) {
                return audio
            }
        }
        
        return null
    }
    
    private suspend fun parseGradioResponse(responseJson: String): ByteArray? {
        val baseUrl = config.spaceUrl.trimEnd('/')
        
        // Try to extract audio URL from various patterns
        val patterns = listOf(
            """"url":\s*"([^"]+)"""",
            """"path":\s*"([^"]+)"""",
            """"name":\s*"([^"]+\.(?:wav|mp3|ogg|flac))""""
        )
        
        for (pattern in patterns) {
            val match = pattern.toRegex().find(responseJson)
            if (match != null) {
                val value = match.groupValues[1]
                // Gradio 4.x uses /file/ not /file=
                val url = when {
                    value.startsWith("http") -> value
                    value.startsWith("/file/") -> "$baseUrl$value"
                    value.startsWith("/") -> "$baseUrl/file$value"
                    else -> "$baseUrl/file/$value"
                }
                
                val audio = downloadAudio(url)
                if (audio != null && audio.size > 1000 && !isHtmlResponse(audio)) {
                    return audio
                }
            }
        }
        
        // Try base64 audio
        val base64Regex = """data:audio/[^;]+;base64,([A-Za-z0-9+/=]+)""".toRegex()
        base64Regex.find(responseJson)?.groupValues?.get(1)?.let { base64 ->
            return try {
                base64DecodeToBytes(base64)
            } catch (e: Exception) {
                null
            }
        }
        
        return null
    }
    
    /**
     * Check if the response is an HTML error page instead of audio
     */
    private fun isHtmlResponse(data: ByteArray): Boolean {
        if (data.size < 15) return false
        val header = String(data.take(15).toByteArray())
        return header.contains("<!doctype", ignoreCase = true) || 
               header.contains("<html", ignoreCase = true)
    }
    
    private suspend fun downloadAudio(url: String): ByteArray? {
        return try {
            val response = httpClient.get(url) {
                config.apiKey?.let { header("Authorization", "Bearer $it") }
            }
            
            if (!response.status.isSuccess()) {
                return null
            }
            
            val contentType = response.headers["Content-Type"]
            
            // Check if response is HTML (error page)
            if (contentType?.contains("text/html") == true) {
                return null
            }
            
            val data: ByteArray = response.body()
            
            // Verify it's not an HTML error page
            if (isHtmlResponse(data)) {
                return null
            }
            
            data
        } catch (e: Exception) {
            Log.error { "$TAG: Download error: ${e.message}" }
            null
        }
    }
}
