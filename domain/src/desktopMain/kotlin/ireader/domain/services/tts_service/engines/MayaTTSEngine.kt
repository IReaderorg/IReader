package ireader.domain.services.tts_service.engines

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSEngine
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.maya.MayaTTSAdapter
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Maya TTS Engine Wrapper
 * 
 * Wraps MayaTTSAdapter to implement the TTSEngine interface.
 */
class MayaTTSEngine(
    private val mayaAdapter: MayaTTSAdapter,
    private val audioEngine: AudioPlaybackEngine
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentSpeed: Float = 1.0f
    private var currentLanguage: String = "en" // Default language
    
    override suspend fun speak(text: String, utteranceId: String) {
        scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Synthesize audio with Maya
                val audioResult = mayaAdapter.synthesize(text, currentLanguage, currentSpeed)
                
                audioResult.onSuccess { audioData ->
                    // Play audio (blocking until complete)
                    audioEngine.play(audioData)
                    
                    callback?.onDone(utteranceId)
                }.onFailure { error ->
                    Log.error { "Maya synthesis failed: ${error.message}" }
                    callback?.onError(utteranceId, error.message ?: "Synthesis failed")
                }
                
            } catch (e: Exception) {
                Log.error { "Maya TTS error: ${e.message}" }
                callback?.onError(utteranceId, e.message ?: "Unknown error")
            }
        }
    }
    
    override fun stop() {
        audioEngine.stop()
    }
    
    override fun pause() {
        audioEngine.pause()
    }
    
    override fun resume() {
        audioEngine.resume()
    }
    
    override fun setSpeed(speed: Float) {
        currentSpeed = speed
    }
    
    override fun setPitch(pitch: Float) {
        // Maya doesn't support runtime pitch changes
        Log.debug { "Maya doesn't support runtime pitch changes" }
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return true // Maya is always ready if initialized
    }
    
    override fun cleanup() {
        mayaAdapter.shutdown()
    }
    
    override fun getEngineName(): String {
        return "Maya TTS"
    }
    
    /**
     * Set the language for Maya TTS
     */
    fun setLanguage(language: String) {
        currentLanguage = language
    }
}
