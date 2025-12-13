package ireader.domain.services.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import ireader.core.log.Log
import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Enhanced Coqui TTS Service with preloading and auto-next features
 * - Preloads next 3 paragraphs for seamless playback
 * - Auto-advances to next paragraph
 * - Auto-continues to next chapter when enabled
 * - Full notification support via TTSService integration
 */
class CoquiTTSService(
    private val context: Context,
    private val spaceUrl: String,
    private val apiKey: String? = null
) : StreamingTTSService, AITTSService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var audioTrack: AudioTrack? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Preloading cache - persists until max size reached
    // Key: paragraph index, Value: audio data
    private val preloadCache = java.util.concurrent.ConcurrentHashMap<Int, AudioData>()
    private var preloadJob: Job? = null
    
    // Max cache size in bytes (50MB default)
    private val maxCacheSizeBytes = 50 * 1024 * 1024L
    
    // Playback state
    private var currentParagraphIndex = 0
    private var isPlaying = false
    private var onParagraphComplete: ((Int) -> Unit)? = null
    private var onChapterComplete: (() -> Unit)? = null
    
    /**
     * Get current cache size in bytes
     */
    private fun getCacheSizeBytes(): Long {
        return preloadCache.values.sumOf { it.samples.size.toLong() }
    }
    
    /**
     * Add audio to cache, evicting newest entries (highest paragraph numbers) if max size exceeded
     * This keeps older paragraphs cached so user can go back without re-generating
     */
    private fun addToCache(paragraph: Int, audioData: AudioData) {
        preloadCache[paragraph] = audioData
        
        // Check if we need to evict entries
        var currentSize = getCacheSizeBytes()
        if (currentSize > maxCacheSizeBytes) {
            // Evict newest entries first (highest paragraph numbers) - keep old ones for going back
            val sortedKeys = preloadCache.keys().toList().sortedDescending()
            for (key in sortedKeys) {
                if (key == paragraph) continue // Don't evict the one we just added
                if (currentSize <= maxCacheSizeBytes * 0.8) break // Keep 80% of max
                val removed = preloadCache.remove(key)
                if (removed != null) {
                    currentSize -= removed.samples.size
                    Log.debug { "Evicted paragraph $key from cache (${removed.samples.size} bytes)" }
                }
            }
        }
    }

    private val coquiVoices = listOf(
        VoiceModel(
            id = "default",
            name = "LJSpeech (Female, American)",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.HIGH,
            sampleRate = 22050,
            modelSize = 0L,
            downloadUrl = "",
            configUrl = "",
            checksum = "",
            license = "MPL 2.0",
            description = "Clear, natural female American voice from Coqui TTS",
            tags = listOf("neural", "online", "coqui", "american", "female")
        )
    )

    override suspend fun getAvailableVoices(): Result<List<VoiceModel>> {
        return Result.success(coquiVoices)
    }
    
    /**
     * Start reading chapter with auto-advance and preloading
     * @param paragraphs List of text paragraphs to read
     * @param startIndex Starting paragraph index
     * @param speed Speech speed multiplier
     * @param autoNext Enable auto-advance to next paragraph
     * @param onParagraphComplete Callback when paragraph finishes
     * @param onChapterComplete Callback when all paragraphs finish
     */
    override fun startReading(
        paragraphs: List<String>,
        startIndex: Int,
        speed: Float,
        autoNext: Boolean,
        onParagraphComplete: ((Int) -> Unit)?,
        onChapterComplete: (() -> Unit)?
    ) {
        this.currentParagraphIndex = startIndex
        this.onParagraphComplete = onParagraphComplete
        this.onChapterComplete = onChapterComplete
        this.isPlaying = true
        
        // Start preloading next 3 paragraphs
        startPreloading(paragraphs, startIndex, speed)
        
        // Play current paragraph
        serviceScope.launch {
            playParagraph(paragraphs, startIndex, speed, autoNext)
        }
    }
    
    /**
     * Preload next 3 paragraphs in background (parallel)
     */
    private fun startPreloading(paragraphs: List<String>, startIndex: Int, speed: Float) {
        preloadJob?.cancel()
        preloadJob = serviceScope.launch {
            Log.info { "Starting prefetch from paragraph ${startIndex + 1}, cache size: ${preloadCache.size}, cache bytes: ${getCacheSizeBytes() / 1024}KB" }
            
            // Launch all prefetch jobs in parallel
            val jobs = (1..3).mapNotNull { i ->
                val nextIndex = startIndex + i
                if (nextIndex >= paragraphs.size) return@mapNotNull null
                
                // Skip if already cached
                if (preloadCache.containsKey(nextIndex)) {
                    Log.debug { "Paragraph $nextIndex already cached, skipping" }
                    return@mapNotNull null
                }
                
                launch {
                    if (!isPlaying) return@launch
                    
                    try {
                        Log.info { "PREFETCH START: paragraph $nextIndex" }
                        val result = synthesize(paragraphs[nextIndex], "default", speed, 1.0f)
                        result.onSuccess { audioData ->
                            addToCache(nextIndex, audioData)
                            Log.info { "PREFETCH DONE: paragraph $nextIndex (${audioData.samples.size} bytes)" }
                        }.onFailure { error ->
                            Log.warn { "PREFETCH FAILED: paragraph $nextIndex - ${error.message}" }
                        }
                    } catch (e: Exception) {
                        Log.warn { "PREFETCH ERROR: paragraph $nextIndex - ${e.message}" }
                    }
                }
            }
            
            // Wait for all to complete
            jobs.forEach { it.join() }
        }
    }
    
    /**
     * Play a specific paragraph with auto-advance
     */
    private suspend fun playParagraph(
        paragraphs: List<String>,
        index: Int,
        speed: Float,
        autoNext: Boolean
    ) {
        if (index >= paragraphs.size || !isPlaying) {
            // Chapter complete
            onChapterComplete?.invoke()
            return
        }
        
        try {
            // Check if preloaded
            val audioData = preloadCache.remove(index) ?: run {
                // Not preloaded, synthesize now
                Log.info { "Synthesizing paragraph $index on-demand" }
                val result = synthesize(paragraphs[index], "default", speed, 1.0f)
                result.getOrThrow()
            }
            
            // Play audio
            playAudioBlocking(audioData)
            
            // Paragraph complete
            onParagraphComplete?.invoke(index)
            
            // Auto-advance to next paragraph
            if (autoNext && isPlaying) {
                currentParagraphIndex = index + 1
                
                // Preload next 3 paragraphs
                startPreloading(paragraphs, currentParagraphIndex, speed)
                
                // Play next paragraph
                playParagraph(paragraphs, currentParagraphIndex, speed, autoNext)
            }
            
        } catch (e: Exception) {
            Log.error { "Error playing paragraph $index: ${e.message}" }
            // Try to continue to next paragraph on error
            if (autoNext && isPlaying) {
                currentParagraphIndex = index + 1
                playParagraph(paragraphs, currentParagraphIndex, speed, autoNext)
            }
        }
    }
    
    /**
     * Stop reading (cache is preserved for going back)
     */
    override fun stopReading() {
        isPlaying = false
        preloadJob?.cancel()
        // Don't clear cache - keep it for going back to previous paragraphs
        stopAudio()
    }
    
    /**
     * Pause reading (can be resumed)
     */
    override fun pauseReading() {
        isPlaying = false
        stopAudio()
    }
    
    /**
     * Resume reading from current position
     */
    override fun resumeReading(paragraphs: List<String>, speed: Float, autoNext: Boolean) {
        isPlaying = true
        serviceScope.launch {
            playParagraph(paragraphs, currentParagraphIndex, speed, autoNext)
        }
    }
    
    /**
     * Skip to specific paragraph (cache is preserved)
     */
    override fun seekToParagraph(paragraphs: List<String>, index: Int, speed: Float, autoNext: Boolean) {
        stopAudio()
        currentParagraphIndex = index
        // Don't clear cache - keep it for going back to previous paragraphs
        
        if (isPlaying) {
            serviceScope.launch {
                startPreloading(paragraphs, index, speed)
                playParagraph(paragraphs, index, speed, autoNext)
            }
        }
    }

    override suspend fun synthesize(
        text: String,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData> = withContext(Dispatchers.IO) {
        try {
            Log.info { "Synthesizing with Coqui TTS: ${text.take(50)}... (speed: $speed)" }

            // Limit text length
            val limitedText = text.take(5000)

            // Step 1: Initiate the call to Gradio API
            val requestBody = JSONObject().apply {
                put("data", org.json.JSONArray().apply {
                    put(limitedText)
                    put(speed.toDouble())
                })
            }

            val request = Request.Builder()
                .url("$spaceUrl/gradio_api/call/text_to_speech")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .apply {
                    apiKey?.let { addHeader("Authorization", "Bearer $it") }
                    addHeader("Content-Type", "application/json")
                }
                .build()

            Log.debug { "Calling: $spaceUrl/gradio_api/call/text_to_speech" }
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.error { "TTS API failed: ${response.code} - $errorBody" }
                throw Exception("TTS API failed: ${response.code} - $errorBody")
            }

            val callResponseBody = response.body?.string()
                ?: throw Exception("Empty call response")

            Log.debug { "Call response: $callResponseBody" }

            val callJson = JSONObject(callResponseBody)
            val eventId = callJson.optString("event_id")

            if (eventId.isEmpty()) {
                throw Exception("No event_id in response")
            }

            Log.debug { "Got event_id: $eventId" }

            // Step 2: Poll for the result using SSE
            val resultRequest = Request.Builder()
                .url("$spaceUrl/gradio_api/call/text_to_speech/$eventId")
                .get()
                .apply {
                    apiKey?.let { addHeader("Authorization", "Bearer $it") }
                    addHeader("Accept", "text/event-stream")
                }
                .build()

            val resultResponse = client.newCall(resultRequest).execute()

            if (!resultResponse.isSuccessful) {
                throw Exception("Failed to get result: ${resultResponse.code}")
            }

            val resultBody = resultResponse.body?.string()
                ?: throw Exception("Empty result")

            Log.debug { "Result body: ${resultBody.take(500)}" }

            // Parse SSE response to get audio path
            var audioPath: String? = null
            var audioUrl: String? = null
            val lines = resultBody.split("\n")
            
            for (line in lines) {
                if (line.startsWith("data:")) {
                    val jsonData = line.substring(5).trim()
                    if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                        try {
                            // Try to parse as JSON array first (gTTS format)
                            if (jsonData.startsWith("[")) {
                                val dataArray = org.json.JSONArray(jsonData)
                                if (dataArray.length() > 0) {
                                    val audioResult = dataArray.get(0)
                                    
                                    when (audioResult) {
                                        is String -> audioPath = audioResult
                                        is JSONObject -> {
                                            audioPath = audioResult.optString("path")
                                            audioUrl = audioResult.optString("url")
                                            if (audioPath.isEmpty()) {
                                                audioPath = audioResult.optString("name")
                                            }
                                        }
                                    }
                                    
                                    if (!audioPath.isNullOrEmpty() || !audioUrl.isNullOrEmpty()) {
                                        Log.debug { "Found audio - path: $audioPath, url: $audioUrl" }
                                        break
                                    }
                                }
                            } else {
                                // Try event format
                                val eventJson = JSONObject(jsonData)
                                
                                if (eventJson.has("data")) {
                                    val dataArray = eventJson.getJSONArray("data")
                                    if (dataArray.length() > 0) {
                                        val audioResult = dataArray.get(0)
                                        
                                        when (audioResult) {
                                            is String -> audioPath = audioResult
                                            is JSONObject -> {
                                                audioPath = audioResult.optString("path")
                                                audioUrl = audioResult.optString("url")
                                                if (audioPath.isEmpty()) {
                                                    audioPath = audioResult.optString("name")
                                                }
                                            }
                                        }
                                        
                                        if (!audioPath.isNullOrEmpty() || !audioUrl.isNullOrEmpty()) {
                                            Log.debug { "Found audio - path: $audioPath, url: $audioUrl" }
                                            break
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.debug { "Failed to parse line: $line - ${e.message}" }
                        }
                    }
                }
            }

            if (audioPath.isNullOrEmpty() && audioUrl.isNullOrEmpty()) {
                throw Exception("No audio path or URL found in response")
            }

            Log.debug { "Audio path: $audioPath, URL: $audioUrl" }

            // Step 3: Download the audio file
            val downloadUrl = when {
                // If we have a full URL, use it
                !audioUrl.isNullOrEmpty() -> audioUrl
                // If path starts with http, use it directly
                audioPath?.startsWith("http") == true -> audioPath
                // Otherwise construct the file URL
                else -> "$spaceUrl/file=$audioPath"
            }

            Log.debug { "Downloading audio from: $downloadUrl" }

            val audioRequest = Request.Builder()
                .url(downloadUrl)
                .get()
                .apply {
                    apiKey?.let { addHeader("Authorization", "Bearer $it") }
                }
                .build()

            val audioResponse = client.newCall(audioRequest).execute()

            if (!audioResponse.isSuccessful) {
                throw Exception("Failed to download audio: ${audioResponse.code}")
            }

            val audioBytes = audioResponse.body?.bytes()
                ?: throw Exception("Empty audio file")

            Log.info { "Received ${audioBytes.size} bytes from TTS" }

            // Check if it's WAV or MP3
            val isWav = audioBytes.size > 4 && 
                       audioBytes[0] == 'R'.code.toByte() && 
                       audioBytes[1] == 'I'.code.toByte() &&
                       audioBytes[2] == 'F'.code.toByte() && 
                       audioBytes[3] == 'F'.code.toByte()
            
            val isMp3 = audioBytes.size > 3 &&
                       audioBytes[0] == 0xFF.toByte() &&
                       (audioBytes[1].toInt() and 0xE0) == 0xE0
            
            Log.info { "Audio format - WAV: $isWav, MP3: $isMp3, size: ${audioBytes.size}" }

            // Create AudioData based on format
            val audioData = if (isMp3) {
                // For MP3, save to temp file and use MediaPlayer
                Log.info { "MP3 detected - will use MediaPlayer for playback" }
                // Store the raw MP3 bytes - we'll handle playback differently
                AudioData(
                    samples = audioBytes,  // Store raw MP3 bytes
                    sampleRate = 22050,
                    channels = 1,
                    bitsPerSample = 16,
                    duration = 0.milliseconds  // Unknown duration for MP3
                )
            } else {
                // For WAV files, extract PCM data
                val pcmData = extractPCMFromWav(audioBytes)

                // Default sample rate (works for both Coqui and gTTS)
                val sampleRate = 22050
                val channels = 1
                val bitsPerSample = 16
                val bytesPerSample = bitsPerSample / 8
                val durationMs = (pcmData.size * 1000L) / (sampleRate * channels * bytesPerSample)

                AudioData(
                    samples = pcmData,
                    sampleRate = sampleRate,
                    channels = channels,
                    bitsPerSample = bitsPerSample,
                    duration = durationMs.milliseconds
                )
            }

            Result.success(audioData)
        } catch (e: Exception) {
            Log.error { "Failed to synthesize with Coqui TTS: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(spaceUrl)
                    .head()
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.error { "Coqui TTS Space not available: ${e.message}" }
            false
        }
    }

    override fun getProviderName(): String = "Coqui TTS"

    override fun playAudio(audioData: AudioData) {
        // Run playback in background thread to avoid blocking UI
        serviceScope.launch {
            playAudioBlocking(audioData)
        }
    }
    
    /**
     * Play audio and block until complete (for sequential playback)
     */
    private suspend fun playAudioBlocking(audioData: AudioData) = withContext(Dispatchers.IO) {
        try {
            stopAudio()

            // Check if it's MP3 (raw bytes) or PCM
            val isMp3 = audioData.samples.size > 3 &&
                       audioData.samples[0] == 0xFF.toByte() &&
                       (audioData.samples[1].toInt() and 0xE0) == 0xE0

            if (isMp3) {
                // Use MediaPlayer for MP3 (blocking)
                playMp3AudioBlocking(audioData.samples)
            } else {
                // Use AudioTrack for PCM (blocking)
                playPcmAudioBlocking(audioData)
            }
        } catch (e: Exception) {
            Log.error { "Failed to play audio: ${e.message}" }
            throw e
        }
    }

    private suspend fun playPcmAudioBlocking(audioData: AudioData) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                audioData.sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(audioData.sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack?) {
                    continuation.resume(Unit) {}
                }

                override fun onPeriodicNotification(track: AudioTrack?) {}
            })

            val totalFrames = audioData.samples.size / 2 // 16-bit = 2 bytes per sample
            audioTrack?.notificationMarkerPosition = totalFrames

            audioTrack?.play()
            audioTrack?.write(audioData.samples, 0, audioData.samples.size)
            
            // Fallback: resume after expected duration
            serviceScope.launch {
                delay(audioData.duration.inWholeMilliseconds + 500)
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }
            
        } catch (e: Exception) {
            Log.error { "PCM playback error: ${e.message}" }
            continuation.resumeWithException(e)
        }
    }

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentTempFile: File? = null
    
    /**
     * Play MP3 audio and block until complete
     */
    private suspend fun playMp3AudioBlocking(mp3Bytes: ByteArray) = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            // Clean up previous temp file if exists
            currentTempFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                }
            }
            
            // Create temp file
            val tempFile = File.createTempFile("coqui_tts_", ".mp3", context.cacheDir)
            currentTempFile = tempFile
            
            // Write MP3 data
            tempFile.writeBytes(mp3Bytes)

            // Use MediaPlayer to play MP3
            val mediaPlayer = MediaPlayer()
            currentMediaPlayer = mediaPlayer
            
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                Log.info { "Playing MP3 audio using MediaPlayer" }
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                // Clean up when playback completes
                mp.release()
                currentMediaPlayer = null
                
                // Delete temp file
                tempFile.delete()
                currentTempFile = null
                
                Log.info { "MP3 playback completed, temp file deleted" }
                
                // Resume coroutine
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }
            
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.error { "MediaPlayer error: what=$what, extra=$extra" }
                mp.release()
                currentMediaPlayer = null
                tempFile.delete()
                currentTempFile = null
                
                if (continuation.isActive) {
                    continuation.resumeWithException(Exception("MediaPlayer error: what=$what, extra=$extra"))
                }
                true
            }
            
            // Handle cancellation
            continuation.invokeOnCancellation {
                mediaPlayer.release()
                currentMediaPlayer = null
                tempFile.delete()
                currentTempFile = null
            }
            
            mediaPlayer.prepareAsync()
            
        } catch (e: Exception) {
            Log.error { "Failed to play MP3 audio: ${e.message}" }
            // Clean up on error
            currentTempFile?.delete()
            currentTempFile = null
            
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }

    override fun stopAudio() {
        // Stop AudioTrack
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                stop()
            }
            release()
        }
        audioTrack = null
        
        // Stop MediaPlayer and clean up temp file
        currentMediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        currentMediaPlayer = null
        
        // Delete temp file immediately
        currentTempFile?.let { file ->
            if (file.exists()) {
                file.delete()
                Log.info { "Temp file deleted on stop" }
            }
        }
        currentTempFile = null
    }

    private fun extractPCMFromWav(audioBytes: ByteArray): ByteArray {
        return if (audioBytes.size > 44 && 
                   audioBytes[0] == 'R'.code.toByte() && 
                   audioBytes[1] == 'I'.code.toByte() &&
                   audioBytes[2] == 'F'.code.toByte() && 
                   audioBytes[3] == 'F'.code.toByte()) {
            // WAV file - skip 44-byte header
            audioBytes.copyOfRange(44, audioBytes.size)
        } else {
            // Already PCM or unknown format
            audioBytes
        }
    }

    override fun cleanup() {
        stopReading()
        clearCache()
        serviceScope.cancel()
    }
    
    /**
     * Clear the audio cache (call when changing chapters)
     */
    fun clearCache() {
        preloadCache.clear()
        Log.info { "Audio cache cleared" }
    }
    
    /**
     * Get current playback state
     */
    override fun isCurrentlyPlaying(): Boolean = isPlaying
    
    /**
     * Get current paragraph index
     */
    override fun getCurrentParagraphIndex(): Int = currentParagraphIndex
}


/**
 * Type alias for backward compatibility
 * GradioTTSService is the new name for the generic Gradio-based TTS service
 * which includes Coqui TTS and other Hugging Face Space TTS engines
 */
typealias GradioTTSService = CoquiTTSService
