package ireader.domain.usecases.tts

import android.content.Context
import ireader.core.log.Log
import ireader.domain.services.tts.CoquiTTSService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Adapter for Coqui TTS Service to implement TTSPlayer interface
 * 
 * This adapter wraps CoquiTTSService to provide a consistent interface
 * with Native TTS, allowing them to be used interchangeably.
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
                Log.info { "Coqui TTS initialized successfully" }
                Result.success(Unit)
            } else {
                Log.error { "Coqui TTS service not available" }
                Result.failure(Exception("Coqui TTS service not available"))
            }
        } catch (e: Exception) {
            Log.error { "Failed to initialize Coqui TTS: ${e.message}" }
            Result.failure(e)
        }
    }
    
    override fun isReady(): Boolean = isInitialized && coquiService != null
    
    override suspend fun speak(text: String, utteranceId: String): Result<Unit> {
        return try {
            if (!isReady()) {
                callback?.onError(utteranceId, "Coqui TTS not initialized")
                return Result.failure(Exception("Coqui TTS not initialized"))
            }
            
            isSpeakingNow = true
            
            // Notify start immediately
            callback?.onStart(utteranceId)
            Log.info { "Coqui TTS speaking: $utteranceId" }
            
            val result = coquiService?.synthesize(
                text = text,
                voiceId = "default",
                speed = currentSpeechRate,
                pitch = currentPitch
            )
            
            if (result == null) {
                callback?.onError(utteranceId, "Coqui service not available")
                return Result.failure(Exception("Coqui service not available"))
            }
            
            // Wait for synthesis to complete
            val audioData = result.getOrElse { error ->
                Log.error { "Coqui TTS synthesis failed: ${error.message}" }
                callback?.onError(utteranceId, error.message ?: "Unknown error")
                return Result.failure(error)
            }
            
            Log.info { "Coqui TTS synthesized ${audioData.samples.size} bytes, duration: ${audioData.duration}" }
            
            // Play audio (non-blocking)
            coquiService?.playAudio(audioData)
            
            // Calculate duration
            val duration = if (audioData.duration.inWholeMilliseconds > 0) {
                audioData.duration.inWholeMilliseconds
            } else {
                // Estimate duration based on text length (rough estimate: 150 words per minute)
                val words = text.split(" ").size
                (words * 60000L / 150).coerceAtLeast(1000L)
            }
            
            Log.info { "Coqui TTS will play for ${duration}ms" }
            
            // Wait for playback to complete, then notify
            kotlinx.coroutines.delay(duration)
            Log.info { "Coqui TTS completed: $utteranceId" }
            
            isSpeakingNow = false
            callback?.onDone(utteranceId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "Failed to speak with Coqui TTS: ${e.message}" }
            isSpeakingNow = false
            callback?.onError(utteranceId, e.message ?: "Unknown error")
            Result.failure(e)
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
        Log.debug { "Coqui TTS isSpeaking: $speaking (flag=$isSpeakingNow, service=${coquiService?.isCurrentlyPlaying()})" }
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
    
    override fun getProviderName(): String = "Coqui TTS"
    
    override fun shutdown() {
        coquiService?.cleanup()
        coquiService = null
        isInitialized = false
        Log.info { "Coqui TTS shut down" }
    }
    
    /**
     * Get the underlying Coqui service for advanced features
     */
    fun getCoquiService(): CoquiTTSService? = coquiService
}
