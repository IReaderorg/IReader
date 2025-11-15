package ireader.domain.services.tts

import ireader.domain.plugins.AudioStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Handler for processing audio streams from TTS plugins
 * Requirements: 5.3, 5.5
 */
class AudioStreamHandler {
    
    /**
     * Read entire audio stream into a byte array
     * Useful for non-streaming playback
     * Requirements: 5.3
     */
    suspend fun readFullStream(audioStream: AudioStream): Result<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(8192) // 8KB buffer
                
                var bytesRead: Int
                while (audioStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                audioStream.close()
                Result.success(outputStream.toByteArray())
            } catch (e: Exception) {
                audioStream.close()
                Result.failure(e)
            }
        }
    }
    
    /**
     * Stream audio in chunks for real-time playback
     * Requirements: 5.5
     */
    suspend fun streamAudio(
        audioStream: AudioStream,
        onChunk: suspend (ByteArray, Int) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val buffer = ByteArray(4096) // 4KB buffer for streaming
                
                var bytesRead: Int
                while (audioStream.read(buffer).also { bytesRead = it } != -1) {
                    onChunk(buffer, bytesRead)
                }
                
                audioStream.close()
                onComplete()
            } catch (e: Exception) {
                audioStream.close()
                onError(e)
            }
        }
    }
    
    /**
     * Get audio duration if available
     * Requirements: 5.3
     */
    fun getAudioDuration(audioStream: AudioStream): Long? {
        return audioStream.getDuration()
    }
    
    /**
     * Close an audio stream safely
     */
    fun closeStream(audioStream: AudioStream) {
        try {
            audioStream.close()
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
}

/**
 * Audio playback state for tracking TTS playback
 * Requirements: 5.3, 5.5
 */
sealed class AudioPlaybackState {
    object Idle : AudioPlaybackState()
    object Loading : AudioPlaybackState()
    data class Playing(
        val currentPosition: Long = 0,
        val duration: Long? = null,
        val isStreaming: Boolean = false
    ) : AudioPlaybackState()
    object Paused : AudioPlaybackState()
    object Completed : AudioPlaybackState()
    data class Error(val message: String) : AudioPlaybackState()
}

/**
 * Audio playback controller interface
 * Platform-specific implementations should implement this
 * Requirements: 5.3, 5.5
 */
interface AudioPlaybackController {
    /**
     * Play audio from a byte array
     */
    suspend fun playAudio(audioData: ByteArray): Result<Unit>
    
    /**
     * Play audio from a stream
     */
    suspend fun playStream(audioStream: AudioStream): Result<Unit>
    
    /**
     * Pause playback
     */
    fun pause()
    
    /**
     * Resume playback
     */
    fun resume()
    
    /**
     * Stop playback
     */
    fun stop()
    
    /**
     * Get current playback state
     */
    fun getState(): AudioPlaybackState
    
    /**
     * Seek to position (in milliseconds)
     */
    fun seekTo(position: Long)
    
    /**
     * Set playback speed
     */
    fun setSpeed(speed: Float)
    
    /**
     * Set volume
     */
    fun setVolume(volume: Float)
}
