package ireader.domain.services.tts_service.piper

import ireader.core.log.Log

/**
 * Piper TTS synthesizer stub.
 * 
 * NOTE: The Piper JNI library has been moved to an optional plugin
 * (io.github.ireaderorg.plugins.piper-tts) to reduce app size.
 * 
 * Users who need neural TTS should install the Piper TTS plugin
 * from the Feature Store.
 * 
 * This stub implementation returns empty results and logs warnings.
 */
class PiperJNISynthesizer {
    
    private var sampleRate: Int = 22050
    private var speechRate: Float = 1.0f
    
    /**
     * Initialize Piper with a voice model.
     * Always returns false since Piper is not bundled.
     */
    suspend fun initialize(modelPath: String, configPath: String): Boolean {
        Log.warn { 
            "Piper TTS is not available. Please install the 'Piper TTS' plugin " +
            "from the Feature Store to use neural text-to-speech."
        }
        return false
    }
    
    /**
     * Synthesize text to audio.
     * Returns empty array since Piper is not bundled.
     */
    suspend fun synthesize(text: String): ByteArray {
        Log.warn { "Piper TTS not available - synthesis skipped" }
        return ByteArray(0)
    }
    
    /**
     * Set speech rate (0.5 = slower, 2.0 = faster)
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }
    
    /**
     * Get sample rate of the loaded model
     */
    fun getSampleRate(): Int = sampleRate
    
    /**
     * Check if initialized.
     * Always returns false since Piper is not bundled.
     */
    fun isInitialized(): Boolean = false
    
    /**
     * Shutdown and release resources.
     * No-op since Piper is not bundled.
     */
    fun shutdown() {
        // No-op
    }
}
