package ireader.domain.services.tts_service.engines

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSEngine
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Piper TTS Engine Wrapper
 * 
 * Wraps PiperSpeechSynthesizer and AudioPlaybackEngine to implement the TTSEngine interface.
 * This allows Piper to be used polymorphically with other TTS engines.
 */
class PiperTTSEngine(
    private val synthesizer: PiperSpeechSynthesizer,
    private val audioEngine: AudioPlaybackEngine
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentSpeed: Float = 1.0f
    
    override suspend fun speak(text: String, utteranceId: String) {
        scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Synthesize audio
                val audioResult = synthesizer.synthesize(text)
                
                audioResult.onSuccess { audioData ->
                    // Play audio (blocking until complete)
                    audioEngine.play(audioData)
                    
                    callback?.onDone(utteranceId)
                }.onFailure { error ->
                    Log.error { "Piper synthesis failed: ${error.message}" }
                    callback?.onError(utteranceId, error.message ?: "Synthesis failed")
                }
                
            } catch (e: Exception) {
                Log.error { "Piper TTS error: ${e.message}" }
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
        synthesizer.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        // Piper doesn't support runtime pitch changes
        // This is a limitation of the ONNX models
        Log.debug { "Piper doesn't support runtime pitch changes" }
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return synthesizer.isInitialized()
    }
    
    override fun cleanup() {
        audioEngine.shutdown()
        synthesizer.shutdown()
    }
    
    override fun getEngineName(): String {
        return "Piper TTS"
    }
}
