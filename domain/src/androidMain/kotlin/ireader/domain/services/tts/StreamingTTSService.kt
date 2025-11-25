package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel

/**
 * Abstract interface for streaming TTS services with advanced features
 * 
 * This interface defines the contract for TTS services that support:
 * - Sequential paragraph reading
 * - Preloading/caching
 * - Auto-advance features
 * - Playback controls
 * 
 * Implementations: CoquiTTSService, GoogleCloudTTSService, ElevenLabsTTSService, etc.
 */
interface StreamingTTSService {
    
    /**
     * Get available voices for this TTS service
     */
    suspend fun getAvailableVoices(): Result<List<VoiceModel>>
    
    /**
     * Check if service is available/reachable
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Get the provider name
     */
    fun getProviderName(): String
    
    /**
     * Start reading a list of paragraphs with auto-advance
     * 
     * @param paragraphs List of text paragraphs to read
     * @param startIndex Starting paragraph index
     * @param speed Speech speed multiplier
     * @param autoNext Enable auto-advance to next paragraph
     * @param onParagraphComplete Callback when each paragraph finishes
     * @param onChapterComplete Callback when all paragraphs finish
     */
    fun startReading(
        paragraphs: List<String>,
        startIndex: Int,
        speed: Float,
        autoNext: Boolean,
        onParagraphComplete: ((Int) -> Unit)?,
        onChapterComplete: (() -> Unit)?
    )
    
    /**
     * Pause reading (can be resumed)
     */
    fun pauseReading()
    
    /**
     * Resume reading from current position
     */
    fun resumeReading(
        paragraphs: List<String>,
        speed: Float,
        autoNext: Boolean
    )
    
    /**
     * Stop reading completely and clear cache
     */
    fun stopReading()
    
    /**
     * Seek to specific paragraph
     */
    fun seekToParagraph(
        paragraphs: List<String>,
        index: Int,
        speed: Float,
        autoNext: Boolean
    )
    
    /**
     * Get current playback state
     */
    fun isCurrentlyPlaying(): Boolean
    
    /**
     * Get current paragraph index
     */
    fun getCurrentParagraphIndex(): Int
    
    /**
     * Synthesize single text to audio (for basic usage)
     */
    suspend fun synthesize(
        text: String,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData>
    
    /**
     * Play audio data directly
     */
    fun playAudio(audioData: AudioData)
    
    /**
     * Stop audio playback
     */
    fun stopAudio()
    
    /**
     * Clean up resources
     */
    fun cleanup()
}
