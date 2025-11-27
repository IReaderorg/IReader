package ireader.domain.services.tts_service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts.CoquiTTSService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Unified TTS Player Interface
 * Abstracts Native TTS and Coqui TTS behind a single interface
 */
interface UnifiedTTSPlayer {
    fun speak(text: String, utteranceId: String)
    fun stop()
    fun pause()
    fun resume()
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
    fun setCallback(callback: TTSCallback)
    fun cleanup()
    fun isReady(): Boolean
}

/**
 * Callback interface for TTS events
 */
interface TTSCallback {
    fun onStart(utteranceId: String)
    fun onDone(utteranceId: String)
    fun onError(utteranceId: String, error: String)
}

/**
 * Native Android TTS implementation
 */
class NativeTTSPlayer(
    private val context: Context
) : UnifiedTTSPlayer {
    
    private var tts: TextToSpeech? = null
    private var callback: TTSCallback? = null
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            } else {
                Log.error { "Native TTS initialization failed with status: $status" }
            }
        }
    }
    
    override fun speak(text: String, utteranceId: String) {
        if (!isReady()) {
            scope.launch {
                callback?.onError(utteranceId, "TTS not initialized")
            }
            return
        }
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.let { id ->
                    scope.launch {
                        callback?.onStart(id)
                    }
                }
            }
            
            override fun onDone(utteranceId: String?) {
                utteranceId?.let { id ->
                    scope.launch {
                        callback?.onDone(id)
                    }
                }
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                utteranceId?.let { id ->
                    scope.launch {
                        callback?.onError(id, "TTS error")
                    }
                }
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.error { "Native TTS error: $utteranceId, code=$errorCode" }
                utteranceId?.let { id ->
                    scope.launch {
                        callback?.onError(id, "TTS error code: $errorCode")
                    }
                }
            }
        })
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
    
    override fun stop() {
        tts?.stop()
    }
    
    override fun pause() {
        tts?.stop()
    }
    
    override fun resume() {
        // Native TTS doesn't support resume, caller should re-speak
    }
    
    override fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }
    
    override fun setCallback(callback: TTSCallback) {
        this.callback = callback
    }
    
    override fun cleanup() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
    
    override fun isReady(): Boolean = isInitialized && tts != null
}

/**
 * Coqui TTS implementation
 */
class CoquiTTSPlayer(
    private val context: Context,
    private val spaceUrl: String,
    private val apiKey: String? = null
) : UnifiedTTSPlayer {
    
    private var coquiService: CoquiTTSService? = null
    private var callback: TTSCallback? = null
    private var currentSpeed = 1.0f
    private var currentPitch = 1.0f
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Cache for pre-synthesized audio
    private val audioCache = mutableMapOf<String, ireader.domain.models.tts.AudioData>()
    private val cacheSize = 3
    
    // Cache status tracking: utteranceId -> CacheStatus
    enum class CacheStatus { LOADING, CACHED, FAILED }
    private val cacheStatus = mutableMapOf<String, CacheStatus>()
    
    init {
        // Initialize synchronously using runBlocking to ensure it's ready before first use
        kotlinx.coroutines.runBlocking {
            try {
                coquiService = CoquiTTSService(context, spaceUrl, apiKey)
                if (coquiService?.isAvailable() == true) {
                    isInitialized = true
                } else {
                    Log.error { "Coqui TTS not available" }
                }
            } catch (e: Exception) {
                Log.error { "Coqui TTS initialization failed: ${e.message}" }
            }
        }
    }
    
    override fun speak(text: String, utteranceId: String) {
        if (!isReady()) {
            mainScope.launch {
                callback?.onError(utteranceId, "Coqui TTS not initialized")
            }
            return
        }
        
        scope.launch {
            try {
                mainScope.launch {
                    callback?.onStart(utteranceId)
                }
                
                Log.info { "Coqui TTS speaking: $utteranceId - ${text.take(50)}..." }
                
                // Check if audio is already cached
                val audioData = audioCache[utteranceId] ?: run {
                    Log.info { "Coqui TTS synthesizing (not cached): $utteranceId" }
                    val result = coquiService?.synthesize(
                        text = text,
                        voiceId = "default",
                        speed = currentSpeed,
                        pitch = currentPitch
                    )
                    
                    result?.getOrNull() ?: run {
                        mainScope.launch {
                            callback?.onError(utteranceId, result?.exceptionOrNull()?.message ?: "Synthesis failed")
                        }
                        return@launch
                    }
                }
                
                // Remove from cache after use
                audioCache.remove(utteranceId)
                cacheStatus.remove(utteranceId)
                
                Log.info { "Coqui TTS audio: ${audioData.samples.size} bytes, duration: ${audioData.duration}" }
                
                // Check if it's MP3 or PCM
                val isMp3 = audioData.samples.size > 3 &&
                           audioData.samples[0] == 0xFF.toByte() &&
                           (audioData.samples[1].toInt() and 0xE0) == 0xE0
                
                if (isMp3) {
                    // For MP3, use MediaPlayer with proper completion callback
                    val completionSignal = kotlinx.coroutines.CompletableDeferred<Unit>()
                    
                    playMp3WithCompletion(audioData.samples) {
                        completionSignal.complete(Unit)
                    }
                    
                    // Wait for actual completion with timeout
                    kotlinx.coroutines.withTimeoutOrNull(120000L) {
                        completionSignal.await()
                    }
                } else {
                    // For PCM, calculate actual duration from audio data
                    val bytesPerSample = audioData.bitsPerSample / 8
                    val totalSamples = if (bytesPerSample > 0) audioData.samples.size / bytesPerSample else 0
                    val calculatedDurationMs = if (audioData.sampleRate > 0 && audioData.channels > 0) {
                        (totalSamples * 1000L) / (audioData.sampleRate * audioData.channels)
                    } else {
                        audioData.duration.inWholeMilliseconds
                    }
                    
                    // Use the longer of calculated or reported duration
                    val duration = maxOf(calculatedDurationMs, audioData.duration.inWholeMilliseconds)
                        .coerceAtLeast(500L)
                    
                    Log.info { "Coqui TTS PCM duration: calculated=${calculatedDurationMs}ms, reported=${audioData.duration.inWholeMilliseconds}ms, using=${duration}ms" }
                    
                    // Play audio
                    coquiService?.playAudio(audioData)
                    
                    // Wait for actual duration plus buffer
                    kotlinx.coroutines.delay(duration + 300L)
                }
                
                Log.info { "Coqui TTS completed: $utteranceId" }
                
                mainScope.launch {
                    callback?.onDone(utteranceId)
                }
            } catch (e: Exception) {
                Log.error { "Coqui TTS error: ${e.message}" }
                mainScope.launch {
                    callback?.onError(utteranceId, e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Play MP3 audio with completion callback
     */
    private fun playMp3WithCompletion(mp3Bytes: ByteArray, onComplete: () -> Unit) {
        try {
            val tempFile = java.io.File.createTempFile("coqui_tts_", ".mp3", context.cacheDir)
            tempFile.writeBytes(mp3Bytes)
            
            val mediaPlayer = android.media.MediaPlayer()
            mediaPlayer.setDataSource(tempFile.absolutePath)
            mediaPlayer.setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                Log.info { "Coqui TTS MP3 playback started" }
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
                tempFile.delete()
                Log.info { "Coqui TTS MP3 playback completed" }
                onComplete()
            }
            
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.error { "Coqui TTS MediaPlayer error: what=$what, extra=$extra" }
                mp.release()
                tempFile.delete()
                onComplete()
                true
            }
            
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.error { "Failed to play MP3: ${e.message}" }
            onComplete()
        }
    }
    
    /**
     * Pre-cache the next N paragraphs for smoother playback
     */
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) {
        if (!isReady()) return
        
        scope.launch {
            paragraphs.take(cacheSize).forEach { (utteranceId, text) ->
                // Skip if already cached or loading
                if (audioCache.containsKey(utteranceId) || cacheStatus[utteranceId] == CacheStatus.LOADING) {
                    return@forEach
                }
                
                try {
                    cacheStatus[utteranceId] = CacheStatus.LOADING
                    
                    val result = coquiService?.synthesize(
                        text = text,
                        voiceId = "default",
                        speed = currentSpeed,
                        pitch = currentPitch
                    )
                    
                    result?.onSuccess { audioData ->
                        audioCache[utteranceId] = audioData
                        cacheStatus[utteranceId] = CacheStatus.CACHED
                    }?.onFailure { error ->
                        cacheStatus[utteranceId] = CacheStatus.FAILED
                        Log.error { "Coqui TTS cache failed: ${error.message}" }
                    }
                } catch (e: Exception) {
                    cacheStatus[utteranceId] = CacheStatus.FAILED
                    Log.error { "Coqui TTS cache error: ${e.message}" }
                }
            }
        }
    }
    
    /**
     * Get cache status for a specific utterance ID
     */
    fun getCacheStatus(utteranceId: String): CacheStatus? = cacheStatus[utteranceId]
    
    /**
     * Get all cached utterance IDs
     */
    fun getCachedParagraphs(): Set<String> = audioCache.keys.toSet()
    
    override fun stop() {
        coquiService?.stopAudio()
    }
    
    override fun pause() {
        coquiService?.pauseReading()
    }
    
    override fun resume() {
        // Coqui doesn't support resume, caller should re-speak
    }
    
    override fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
    }
    
    override fun setCallback(callback: TTSCallback) {
        this.callback = callback
    }
    
    override fun cleanup() {
        coquiService?.cleanup()
        coquiService = null
        isInitialized = false
    }
    
    override fun isReady(): Boolean = isInitialized && coquiService != null
}
