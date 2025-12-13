package ireader.domain.services.tts_service.v2

import kotlinx.coroutines.flow.Flow

/**
 * TTS Engine Interface - Abstraction for text-to-speech engines
 * 
 * Each platform/engine type implements this interface.
 * The engine is ONLY responsible for converting text to speech.
 * It does NOT manage state, navigation, or caching.
 */
interface TTSEngine {
    /**
     * Speak the given text
     * @param text Text to speak
     * @param utteranceId Unique ID for this utterance (for tracking)
     */
    suspend fun speak(text: String, utteranceId: String)
    
    /**
     * Stop current speech immediately
     */
    fun stop()
    
    /**
     * Pause current speech (if supported)
     */
    fun pause()
    
    /**
     * Resume paused speech (if supported)
     */
    fun resume()
    
    /**
     * Set speech speed
     * @param speed Speed multiplier (0.5 to 2.0)
     */
    fun setSpeed(speed: Float)
    
    /**
     * Set speech pitch
     * @param pitch Pitch multiplier (0.5 to 2.0)
     */
    fun setPitch(pitch: Float)
    
    /**
     * Check if engine is ready to speak
     */
    fun isReady(): Boolean
    
    /**
     * Release all resources
     */
    fun release()
    
    /**
     * Generate audio data for text (for caching/download)
     * Only supported by remote TTS engines (Gradio).
     * Native TTS engines return null.
     * 
     * @param text Text to convert to audio
     * @return Audio data as ByteArray, or null if not supported
     */
    suspend fun generateAudioForText(text: String): ByteArray? = null
    
    /**
     * Play cached audio data directly
     * Used for offline playback of downloaded TTS audio.
     * 
     * @param audioData Raw audio bytes (WAV, MP3, etc.)
     * @param utteranceId Unique ID for this utterance (for tracking)
     * @return true if playback started successfully, false if not supported
     */
    suspend fun playCachedAudio(audioData: ByteArray, utteranceId: String): Boolean = false
    
    /**
     * Clear internal state (queue, cache, etc.)
     * Called when switching chapters to ensure clean state.
     */
    fun clearState() {}
    
    /**
     * Pre-cache upcoming text for smoother playback.
     * Only supported by remote TTS engines (Gradio).
     * Native TTS engines do nothing.
     * 
     * @param items List of (utteranceId, text) pairs to pre-cache
     */
    fun precacheNext(items: List<Pair<String, String>>) {}
    
    /**
     * Check if audio for the given text is cached.
     * Only supported by remote TTS engines (Gradio).
     * Native TTS engines return false.
     * 
     * @param text Text to check
     * @return true if audio is cached, false otherwise
     */
    suspend fun isTextCached(text: String): Boolean = false
    
    /**
     * Get the set of indices from a list of texts that are cached.
     * Useful for updating UI to show which paragraphs are cached.
     * 
     * @param texts List of texts to check
     * @return Set of indices that are cached
     */
    suspend fun getCachedIndices(texts: List<String>): Set<Int> = emptySet()
    
    /**
     * Engine events flow
     */
    val events: Flow<EngineEvent>
    
    /**
     * Engine name for display
     */
    val name: String
}

/**
 * Engine Events - Low-level events from the TTS engine
 */
sealed class EngineEvent {
    data class Started(val utteranceId: String) : EngineEvent()
    data class Completed(val utteranceId: String) : EngineEvent()
    data class Error(val utteranceId: String, val message: String) : EngineEvent()
    object Ready : EngineEvent()
}

/**
 * Factory for creating TTS engines
 */
expect object TTSEngineFactory {
    fun createNativeEngine(): TTSEngine
    fun createGradioEngine(config: GradioConfig): TTSEngine?
}

/**
 * Gradio TTS Configuration
 * 
 * Note: For full functionality, use the original GradioTTSConfig from presets.
 * This simplified config is used for basic engine creation.
 */
data class GradioConfig(
    val id: String,
    val name: String,
    val spaceUrl: String,
    val apiName: String = "predict",
    val enabled: Boolean = true,
    /** Original full config from presets - if provided, use this instead of building from fields */
    val originalConfig: ireader.domain.services.tts_service.GradioTTSConfig? = null
)
