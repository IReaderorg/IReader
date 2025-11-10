package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.flow.Flow

/**
 * Interface for speech synthesis operations.
 * Implementations of this interface handle text-to-speech conversion using various TTS engines.
 */
interface SpeechSynthesizer {
    
    /**
     * Initialize the synthesizer with a voice model.
     * This must be called before any synthesis operations.
     * 
     * @param modelPath Absolute path to the voice model file
     * @param configPath Absolute path to the model configuration file
     * @return Result indicating success or failure with error details
     */
    suspend fun initialize(modelPath: String, configPath: String): Result<Unit>
    
    /**
     * Synthesize text to audio.
     * This is a blocking operation that generates the complete audio for the given text.
     * 
     * @param text The text to convert to speech
     * @return Result containing AudioData on success, or error on failure
     */
    suspend fun synthesize(text: String): Result<AudioData>
    
    /**
     * Synthesize text to audio in streaming mode.
     * This method splits text into manageable chunks and generates audio progressively,
     * allowing playback to begin before the entire text is synthesized.
     * 
     * @param text The text to convert to speech
     * @return Flow of AudioChunk objects representing progressive audio generation
     */
    fun synthesizeStream(text: String): Flow<AudioChunk>
    
    /**
     * Calculate word boundaries for the given text.
     * Word boundaries are used for text highlighting during playback.
     * 
     * @param text The text to analyze
     * @return List of WordBoundary objects with timing information
     */
    suspend fun getWordBoundaries(text: String): List<WordBoundary>
    
    /**
     * Set the speech rate (speed) for synthesis.
     * 
     * @param rate Speech rate multiplier (1.0 = normal speed, 0.5 = half speed, 2.0 = double speed)
     */
    fun setSpeechRate(rate: Float)
    
    /**
     * Shutdown the synthesizer and release all resources.
     * After calling this method, the synthesizer cannot be used until initialize is called again.
     */
    fun shutdown()
}
