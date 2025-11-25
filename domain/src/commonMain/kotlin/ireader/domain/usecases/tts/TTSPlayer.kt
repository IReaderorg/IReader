package ireader.domain.usecases.tts

import ireader.domain.models.entities.Chapter

/**
 * Unified TTS Player Interface
 * 
 * This interface provides a consistent API for all TTS implementations
 * (Native TTS, Coqui TTS, etc.) to ensure they can be used interchangeably.
 */
interface TTSPlayer {
    
    /**
     * Initialize the TTS player
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Check if player is initialized and ready
     */
    fun isReady(): Boolean
    
    /**
     * Start speaking text
     * 
     * @param text Text to speak
     * @param utteranceId Unique ID for this utterance
     * @return Result indicating success or failure
     */
    suspend fun speak(text: String, utteranceId: String): Result<Unit>
    
    /**
     * Stop speaking immediately
     */
    fun stop()
    
    /**
     * Pause speaking (if supported)
     */
    fun pause()
    
    /**
     * Resume speaking (if supported)
     */
    fun resume()
    
    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean
    
    /**
     * Set speech rate
     * 
     * @param rate Speech rate (0.5 - 2.0, where 1.0 is normal)
     */
    fun setSpeechRate(rate: Float)
    
    /**
     * Set pitch
     * 
     * @param pitch Pitch (0.5 - 2.0, where 1.0 is normal)
     */
    fun setPitch(pitch: Float)
    
    /**
     * Get current speech rate
     */
    fun getSpeechRate(): Float
    
    /**
     * Get current pitch
     */
    fun getPitch(): Float
    
    /**
     * Set callback for TTS events
     */
    fun setCallback(callback: TTSPlayerCallback)
    
    /**
     * Get the provider name
     */
    fun getProviderName(): String
    
    /**
     * Shutdown and cleanup resources
     */
    fun shutdown()
}

/**
 * Callback interface for TTS player events
 */
interface TTSPlayerCallback {
    /**
     * Called when TTS starts speaking an utterance
     */
    fun onStart(utteranceId: String)
    
    /**
     * Called when TTS finishes speaking an utterance
     */
    fun onDone(utteranceId: String)
    
    /**
     * Called when TTS encounters an error
     */
    fun onError(utteranceId: String, error: String)
    
    /**
     * Called when TTS is stopped
     */
    fun onStopped()
}

/**
 * Empty callback implementation for convenience
 */
open class TTSPlayerCallbackAdapter : TTSPlayerCallback {
    override fun onStart(utteranceId: String) {}
    override fun onDone(utteranceId: String) {}
    override fun onError(utteranceId: String, error: String) {}
    override fun onStopped() {}
}
