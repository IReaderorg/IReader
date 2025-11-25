package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel

/**
 * Interface for AI-powered Text-to-Speech services
 * Supports multiple providers (Google Cloud TTS, Azure, etc.)
 */
interface AITTSService {
    
    /**
     * Get available AI voices from the provider
     */
    suspend fun getAvailableVoices(): Result<List<VoiceModel>>
    
    /**
     * Synthesize text to speech using AI voice
     * @param text The text to synthesize
     * @param voiceId The voice ID to use
     * @param speed Speech rate (0.25 to 4.0)
     * @param pitch Pitch adjustment (-20.0 to 20.0)
     * @return AudioData containing the synthesized audio
     */
    suspend fun synthesize(
        text: String,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 0.0f
    ): Result<AudioData>
    
    /**
     * Check if the service is available and configured
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Get the provider name (e.g., "Google Cloud TTS", "Azure TTS")
     */
    fun getProviderName(): String
}

/**
 * AI TTS Provider types
 */
enum class AITTSProvider {
    COQUI_TTS,           // Coqui TTS - high-quality open-source TTS (online, Gradio-based)
    KOKORO_TTS,          // Kokoro TTS - hexgrad/Kokoro-TTS (high-quality, fast, multiple voices)
    PIPER_TTS,           // High-quality neural TTS (offline, free)
    MELO_TTS,            // MyShell MeloTTS - multilingual, expressive (offline, free)
    HUGGING_FACE,        // Hugging Face Spaces TTS (online, API-based)
    NATIVE_ANDROID       // Android built-in TTS
}
