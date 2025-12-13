package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for text-to-speech engines.
 * TTS plugins provide custom speech synthesis capabilities.
 * 
 * Example:
 * ```kotlin
 * class CloudTTSPlugin : TTSPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.cloud-tts",
 *         name = "Cloud TTS",
 *         type = PluginType.TTS,
 *         permissions = listOf(PluginPermission.NETWORK),
 *         // ... other manifest fields
 *     )
 *     
 *     override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
 *         // Call cloud TTS API and return audio stream
 *     }
 *     
 *     // ... other implementations
 * }
 * ```
 */
interface TTSPlugin : Plugin {
    /**
     * Speak the given text using the specified voice configuration.
     * 
     * @param text Text to speak
     * @param voice Voice configuration (voice ID, speed, pitch, volume)
     * @return Result containing audio stream or error
     */
    suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream>
    
    /**
     * Get available voices provided by this TTS plugin.
     * 
     * @return List of available voice models
     */
    fun getAvailableVoices(): List<VoiceModel>
    
    /**
     * Check if this TTS plugin supports streaming audio.
     * Streaming allows playback to start before full audio is generated.
     * 
     * @return true if streaming is supported
     */
    fun supportsStreaming(): Boolean
    
    /**
     * Get the audio format produced by this TTS plugin.
     * 
     * @return Audio format specification
     */
    fun getAudioFormat(): AudioFormat
}

/**
 * Voice configuration for TTS.
 */
@Serializable
data class VoiceConfig(
    /** Voice identifier */
    val voiceId: String,
    /** Speech speed multiplier (1.0 = normal) */
    val speed: Float = 1.0f,
    /** Voice pitch multiplier (1.0 = normal) */
    val pitch: Float = 1.0f,
    /** Volume level (0.0 to 1.0) */
    val volume: Float = 1.0f
)

/**
 * Voice model information.
 */
@Serializable
data class VoiceModel(
    /** Unique voice identifier */
    val id: String,
    /** Display name of the voice */
    val name: String,
    /** Language code (e.g., "en-US") */
    val language: String,
    /** Gender of the voice */
    val gender: VoiceGender = VoiceGender.NEUTRAL,
    /** Whether this voice requires download */
    val requiresDownload: Boolean = false,
    /** Size in bytes if download required */
    val downloadSize: Long? = null
)

/**
 * Voice gender options.
 */
@Serializable
enum class VoiceGender {
    MALE,
    FEMALE,
    NEUTRAL
}

/**
 * Audio format specification.
 */
@Serializable
data class AudioFormat(
    /** Audio encoding type */
    val encoding: AudioEncoding,
    /** Sample rate in Hz */
    val sampleRate: Int,
    /** Number of audio channels (1 = mono, 2 = stereo) */
    val channels: Int,
    /** Bit depth (e.g., 16 for 16-bit audio) */
    val bitDepth: Int
)

/**
 * Audio encoding types.
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
 * Audio stream interface for TTS output.
 * Provides streaming access to generated audio data.
 */
interface AudioStream {
    /**
     * Read audio data into buffer.
     * 
     * @param buffer Buffer to read into
     * @return Number of bytes read, or -1 if end of stream
     */
    suspend fun read(buffer: ByteArray): Int
    
    /**
     * Close the audio stream and release resources.
     */
    fun close()
    
    /**
     * Get total duration in milliseconds (if known).
     * 
     * @return Duration in ms, or null if unknown
     */
    fun getDuration(): Long?
}
