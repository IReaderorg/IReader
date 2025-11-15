package ireader.domain.plugins

import ireader.domain.models.tts.VoiceModel
import kotlinx.serialization.Serializable

/**
 * Plugin interface for text-to-speech engines
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
interface TTSPlugin : Plugin {
    /**
     * Speak the given text using the specified voice configuration
     * @param text Text to speak
     * @param voice Voice configuration
     * @return Result containing audio stream or error
     */
    suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream>
    
    /**
     * Get available voices provided by this TTS plugin
     * @return List of available voice models
     */
    fun getAvailableVoices(): List<VoiceModel>
    
    /**
     * Check if this TTS plugin supports streaming audio
     * @return true if streaming is supported
     */
    fun supportsStreaming(): Boolean
    
    /**
     * Get the audio format produced by this TTS plugin
     * @return Audio format specification
     */
    fun getAudioFormat(): AudioFormat
}

/**
 * Voice configuration for TTS
 */
@Serializable
data class VoiceConfig(
    val voiceId: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f
)

/**
 * Audio format specification
 */
@Serializable
data class AudioFormat(
    val encoding: AudioEncoding,
    val sampleRate: Int,
    val channels: Int,
    val bitDepth: Int
)

/**
 * Audio encoding types
 */
@Serializable
enum class AudioEncoding {
    PCM,
    MP3,
    OGG,
    AAC,
    WAV
}

/**
 * Audio stream interface for TTS output
 */
interface AudioStream {
    /**
     * Read audio data
     * @param buffer Buffer to read into
     * @return Number of bytes read, or -1 if end of stream
     */
    suspend fun read(buffer: ByteArray): Int
    
    /**
     * Close the audio stream
     */
    fun close()
    
    /**
     * Get total duration in milliseconds (if known)
     */
    fun getDuration(): Long?
}
