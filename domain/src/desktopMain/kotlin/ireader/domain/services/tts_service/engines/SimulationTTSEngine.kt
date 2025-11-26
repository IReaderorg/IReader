package ireader.domain.services.tts_service.engines

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSEngine
import ireader.domain.services.tts_service.TTSEngineCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Simulation TTS Engine
 * 
 * Simulates TTS by calculating reading time based on word count.
 * Used as a fallback when no real TTS engine is available.
 */
class SimulationTTSEngine : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentSpeed: Float = 1.0f
    private var isPaused = false
    
    override suspend fun speak(text: String, utteranceId: String) {
        scope.launch {
            try {
                callback?.onStart(utteranceId)
                
                // Calculate reading time based on word count and speech rate
                val words = text.split("\\s+".toRegex())
                val wordsPerMinute = 150 * currentSpeed // Base reading speed
                val readingTimeMs = (words.size / wordsPerMinute * 60 * 1000).toLong()
                
                Log.debug { "Simulating TTS: ${words.size} words, ${readingTimeMs}ms at ${currentSpeed}x speed" }
                
                // Simulate reading time
                delay(readingTimeMs)
                
                if (!isPaused) {
                    callback?.onDone(utteranceId)
                }
                
            } catch (e: Exception) {
                Log.error { "Simulation TTS error: ${e.message}" }
                callback?.onError(utteranceId, e.message ?: "Unknown error")
            }
        }
    }
    
    override fun stop() {
        isPaused = false
        // Cancel any ongoing simulation
    }
    
    override fun pause() {
        isPaused = true
    }
    
    override fun resume() {
        isPaused = false
    }
    
    override fun setSpeed(speed: Float) {
        currentSpeed = speed
    }
    
    override fun setPitch(pitch: Float) {
        // Simulation doesn't use pitch
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return true // Simulation is always ready
    }
    
    override fun cleanup() {
        // Nothing to clean up
    }
    
    override fun getEngineName(): String {
        return "Simulation (No TTS)"
    }
}
