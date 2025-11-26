package ireader.domain.services.tts_service.engines

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSEngine
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.kokoro.KokoroTTSAdapter
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Kokoro TTS Engine Wrapper
 * 
 * Wraps KokoroTTSAdapter to implement the TTSEngine interface.
 */
class KokoroTTSEngine(
    private val kokoroAdapter: KokoroTTSAdapter,
    private val audioEngine: AudioPlaybackEngine
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentSpeed: Float = 1.0f
    private var currentVoice: String = "af_bella" // Default voice
    
    override suspend fun speak(text: String, utteranceId: String) {
        scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Synthesize audio with Kokoro
                val audioResult = kokoroAdapter.synthesize(text, currentVoice, currentSpeed)
                
                audioResult.onSuccess { audioData ->
                    // Play audio (blocking until complete)
                    audioEngine.play(audioData)
                    
                    callback?.onDone(utteranceId)
                }.onFailure { error ->
                    Log.error { "Kokoro synthesis failed: ${error.message}" }
                    callback?.onError(utteranceId, error.message ?: "Synthesis failed")
                }
                
            } catch (e: Exception) {
                Log.error { "Kokoro TTS error: ${e.message}" }
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
        // Kokoro doesn't support runtime pitch changes
        Log.debug { "Kokoro doesn't support runtime pitch changes" }
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return true // Kokoro is always ready if initialized
    }
    
    override fun cleanup() {
        kokoroAdapter.shutdown()
    }
    
    override fun getEngineName(): String {
        return "Kokoro TTS"
    }
    
    /**
     * Set the voice for Kokoro TTS
     */
    fun setVoice(voice: String) {
        currentVoice = voice
    }
}
