package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel

/**
 * Manager for AI TTS services
 * Handles multiple providers and voice selection
 */
expect class AITTSManager {
    
    /**
     * Get voices from a specific provider
     */
    suspend fun getVoicesFromProvider(provider: AITTSProvider): Result<List<VoiceModel>>
    
    /**
     * Synthesize text using specified provider and voice
     */
    suspend fun synthesize(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 0.0f
    ): Result<AudioData>
    
    /**
     * Synthesize and play audio directly
     */
    suspend fun synthesizeAndPlay(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 0.0f
    ): Result<Unit>
    
    /**
     * Configure Gradio TTS service
     */
    fun configureGradio(spaceUrl: String, apiKey: String? = null)
    
    /**
     * Download a Piper voice model
     */
    suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit>
    
    /**
     * Check if a voice is downloaded
     */
    fun isVoiceDownloaded(voiceId: String): Boolean
    
    /**
     * Delete a downloaded voice
     */
    fun deleteVoice(voiceId: String): Boolean
    
    /**
     * Get list of downloaded voices
     */
    fun getDownloadedVoices(): List<String>
    
    /**
     * Get total size of downloaded voices
     */
    fun getDownloadedVoicesSize(): Long
}
