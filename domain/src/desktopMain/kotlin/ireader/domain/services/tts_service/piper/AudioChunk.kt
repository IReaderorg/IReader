package ireader.domain.services.tts_service.piper

/**
 * Represents a chunk of audio data for streaming playback
 * 
 * @property data The audio data for this chunk
 * @property text The text that was synthesized to create this audio chunk
 * @property isLast Indicates if this is the last chunk in the stream
 */
data class AudioChunk(
    val data: AudioData,
    val text: String,
    val isLast: Boolean
)
