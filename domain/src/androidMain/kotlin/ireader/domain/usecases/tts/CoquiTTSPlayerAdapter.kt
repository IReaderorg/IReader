package ireader.domain.usecases.tts

import android.content.Context
import ireader.core.log.Log
import ireader.domain.services.tts.CoquiTTSService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Adapter for Gradio TTS Service to implement TTSPlayer interface
 * 
 * This adapter wraps the Gradio-based TTS service (including Coqui TTS)
 * to provide a consistent interface with Native TTS, allowing them to be
 * used interchangeably.
 * 
 * Note: The class is named CoquiTTSPlayerAdapter for backward compatibility,
 * but it's aliased as GradioTTSPlayerAdapter for new code.
 */
class CoquiTTSPlayerAdapter(
    private val context: Context,
    private val spaceUrl: String,
    private val apiKey: String? = null
) : TTSPlayer {
    
    private var coquiService: CoquiTTSService? = null
    private var callback: TTSPlayerCallback? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentSpeechRate = 1.0f
    private var currentPitch = 1.0f
    private var isInitialized = false
    private var isSpeakingNow = false
    
    override suspend fun initialize(): Result<Unit> {
        return try {
            coquiService = CoquiTTSService(context, spaceUrl, apiKey)
            
            // Check if service is available
            val available = coquiService?.isAvailable() ?: false
            
            if (available) {
                isInitialized = true
                Log.info { "Gradio TTS initialized successfully" }
                Result.success(Unit)
            } else {
                Log.error { "Gradio TTS service not available" }
                Result.failure(Exception("Gradio TTS service not available"))
            }
        } catch (e: Exception) {
            Log.error { "Failed to initialize Gradio TTS: ${e.message}" }
            Result.failure(e)
        }
    }
    
    override fun isReady(): Boolean = isInitialized && coquiService != null
    
    override suspend fun speak(text: String, utteranceId: String): Result<Unit> {
        return try {
            if (!isReady()) {
                callback?.onError(utteranceId, "Gradio TTS not initialized")
                return Result.failure(Exception("Gradio TTS not initialized"))
            }
            
            isSpeakingNow = true
            
            // Notify start immediately
            callback?.onStart(utteranceId)
            Log.info { "Gradio TTS speaking: $utteranceId - text: ${text.take(50)}..." }
            
            val result = coquiService?.synthesize(
                text = text,
                voiceId = "default",
                speed = currentSpeechRate,
                pitch = currentPitch
            )
            
            if (result == null) {
                isSpeakingNow = false
                callback?.onError(utteranceId, "Gradio service not available")
                return Result.failure(Exception("Gradio service not available"))
            }
            
            // Wait for synthesis to complete
            val audioData = result.getOrElse { error ->
                Log.error { "Gradio TTS synthesis failed: ${error.message}" }
                isSpeakingNow = false
                callback?.onError(utteranceId, error.message ?: "Unknown error")
                return Result.failure(error)
            }
            
            Log.info { "Gradio TTS synthesized ${audioData.samples.size} bytes, duration: ${audioData.duration}" }
            
            // Use the blocking playback method from the TTS service
            // This will wait until audio playback is complete
            try {
                // Call the internal blocking playback
                coquiService?.let { service ->
                    // Use reflection or direct call to blocking method
                    // For now, use playAudio and wait properly
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        // Create a completion signal
                        val completionSignal = kotlinx.coroutines.CompletableDeferred<Unit>()
                        
                        // Check if it's MP3 or PCM
                        val isMp3 = audioData.samples.size > 3 &&
                                   audioData.samples[0] == 0xFF.toByte() &&
                                   (audioData.samples[1].toInt() and 0xE0) == 0xE0
                        
                        if (isMp3) {
                            // For MP3, use MediaPlayer with completion callback
                            playMp3WithCompletion(audioData.samples) {
                                completionSignal.complete(Unit)
                            }
                        } else {
                            // For PCM, calculate actual duration from audio data
                            val bytesPerSample = audioData.bitsPerSample / 8
                            val totalSamples = audioData.samples.size / bytesPerSample
                            val durationMs = (totalSamples * 1000L) / (audioData.sampleRate * audioData.channels)
                            
                            // Play audio
                            service.playAudio(audioData)
                            
                            // Wait for actual duration plus buffer
                            val waitTime = durationMs.coerceAtLeast(500L) + 200L
                            Log.info { "Gradio TTS waiting ${waitTime}ms for PCM playback (calculated: ${durationMs}ms)" }
                            kotlinx.coroutines.delay(waitTime)
                            completionSignal.complete(Unit)
                        }
                        
                        // Wait for completion with timeout
                        kotlinx.coroutines.withTimeoutOrNull(120000L) {
                            completionSignal.await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error { "Gradio TTS playback error: ${e.message}" }
            }
            
            Log.info { "Gradio TTS completed: $utteranceId" }
            
            isSpeakingNow = false
            callback?.onDone(utteranceId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "Failed to speak with Gradio TTS: ${e.message}" }
            isSpeakingNow = false
            callback?.onError(utteranceId, e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    /**
     * Play MP3 audio with completion callback
     */
    private fun playMp3WithCompletion(mp3Bytes: ByteArray, onComplete: () -> Unit) {
        try {
            val tempFile = java.io.File.createTempFile("gradio_tts_", ".mp3", context.cacheDir)
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
                Log.info { "Gradio TTS MP3 playback started" }
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
                tempFile.delete()
                Log.info { "Gradio TTS MP3 playback completed" }
                onComplete()
            }
            
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.error { "Gradio TTS MediaPlayer error: what=$what, extra=$extra" }
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
    
    override fun stop() {
        isSpeakingNow = false
        coquiService?.stopAudio()
        callback?.onStopped()
    }
    
    override fun pause() {
        coquiService?.pauseReading()
        callback?.onStopped()
    }
    
    override fun resume() {
        // Coqui TTS doesn't support simple resume
        // Caller needs to re-speak or use startReading
    }
    
    override fun isSpeaking(): Boolean {
        val speaking = isSpeakingNow || (coquiService?.isCurrentlyPlaying() ?: false)
        Log.debug { "Gradio TTS isSpeaking: $speaking (flag=$isSpeakingNow, service=${coquiService?.isCurrentlyPlaying()})" }
        return speaking
    }
    
    override fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
    }
    
    override fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        // Note: Coqui TTS may not support pitch adjustment
    }
    
    override fun getSpeechRate(): Float = currentSpeechRate
    
    override fun getPitch(): Float = currentPitch
    
    override fun setCallback(callback: TTSPlayerCallback) {
        this.callback = callback
    }
    
    override fun getProviderName(): String = "Gradio TTS"
    
    override fun shutdown() {
        coquiService?.cleanup()
        coquiService = null
        isInitialized = false
        Log.info { "Gradio TTS shut down" }
    }
    
    /**
     * Get the underlying Gradio service for advanced features
     * (Uses CoquiTTSService which is now aliased as GradioTTSService)
     */
    fun getGradioService(): CoquiTTSService? = coquiService
    
}

/**
 * Type alias for backward compatibility
 * GradioTTSPlayerAdapter is the new name for the Gradio-based TTS player adapter
 */
typealias GradioTTSPlayerAdapter = CoquiTTSPlayerAdapter
