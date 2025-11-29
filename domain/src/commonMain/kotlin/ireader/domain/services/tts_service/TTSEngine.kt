package ireader.domain.services.tts_service

/**
 * Cross-platform TTS Engine abstraction
 * Allows different TTS implementations on Android and Desktop
 */
interface TTSEngine {
    /**
     * Speak the given text with a unique utterance ID
     */
    suspend fun speak(text: String, utteranceId: String)
    
    /**
     * Stop current speech
     */
    fun stop()
    
    /**
     * Pause current speech
     */
    fun pause()
    
    /**
     * Resume paused speech
     */
    fun resume()
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeed(speed: Float)
    
    /**
     * Set speech pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float)
    
    /**
     * Set callback for TTS events
     */
    fun setCallback(callback: TTSEngineCallback)
    
    /**
     * Check if engine is ready to speak
     */
    fun isReady(): Boolean
    
    /**
     * Clean up resources
     */
    fun cleanup()
    
    /**
     * Get engine name for display
     */
    fun getEngineName(): String
}

/**
 * Callback interface for TTS engine events
 */
interface TTSEngineCallback {
    fun onStart(utteranceId: String)
    fun onDone(utteranceId: String)
    fun onError(utteranceId: String, error: String)
}

/**
 * TTS Engine configuration
 */
data class TTSEngineConfig(
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val language: String = "",
    val voice: String = ""
)

/**
 * Factory for creating platform-specific TTS engines
 */
expect object TTSEngineFactory {
    /**
     * Create native platform TTS engine (Android TTS or Desktop Piper TTS)
     */
    fun createNativeEngine(): TTSEngine
    
    /**
     * Create a Gradio TTS engine from configuration.
     * 
     * Use GradioTTSPresets for pre-configured engines:
     * - GradioTTSPresets.COQUI_IREADER - Coqui TTS (IReader Space)
     * - GradioTTSPresets.EDGE_TTS - Microsoft Edge TTS
     * - GradioTTSPresets.XTTS_V2 - Coqui XTTS v2
     * - etc.
     * 
     * @param config The Gradio TTS configuration
     * @return TTSEngine instance or null if creation fails
     */
    fun createGradioEngine(config: GradioTTSConfig): TTSEngine?
    
    /**
     * Get list of available engines on this platform
     */
    fun getAvailableEngines(): List<String>
}

/**
 * Desktop-specific engine creation
 * These are only available on Desktop platform
 */
expect object DesktopTTSEngines {
    fun createPiperEngine(): TTSEngine?
    fun createKokoroEngine(): TTSEngine?
    fun createMayaEngine(): TTSEngine?
}

/**
 * Platform-specific base64 decoding
 * 
 * Each platform (Android, Desktop) provides its own implementation.
 */
expect fun base64DecodeToBytes(base64: String): ByteArray
