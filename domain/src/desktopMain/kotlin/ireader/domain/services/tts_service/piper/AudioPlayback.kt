package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Interface for audio playback operations
 * 
 * Provides methods for playing audio data, controlling playback,
 * and monitoring playback state.
 */
interface AudioPlayback {
    /**
     * Play audio data synchronously
     * 
     * @param audioData The audio data to play
     */
    suspend fun play(audioData: AudioData)
    
    /**
     * Play audio stream with buffering
     * 
     * @param audioFlow Flow of audio chunks to play
     * @return Job that can be used to control the playback
     */
    fun playStream(audioFlow: Flow<AudioChunk>): Job
    
    /**
     * Pause playback
     * Should respond within 200ms
     */
    fun pause()
    
    /**
     * Resume playback from paused position
     * Should respond within 200ms
     */
    fun resume()
    
    /**
     * Stop playback and clear buffers
     */
    fun stop()
    
    /**
     * Get current playback position in milliseconds
     * 
     * @return Current position in milliseconds
     */
    fun getCurrentPosition(): Long
    
    /**
     * Check if audio is currently playing
     * 
     * @return true if audio is playing, false otherwise
     */
    fun isPlaying(): Boolean
}
