package ireader.domain.services.tts_service.media_player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import ireader.core.log.Log
import ireader.domain.models.tts.AudioData
import ireader.domain.services.tts.AITTSManager
import ireader.domain.services.tts.AITTSProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Audio player for AI TTS synthesized audio
 * Plays PCM audio data from AI TTS services
 */
class AITTSPlayer(
    private val context: Context,
    private val aiTTSManager: AITTSManager
) {
    
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    
    /**
     * Speak text using AI TTS
     */
    suspend fun speak(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 0.0f,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            onStart()
            
            // Synthesize audio using AI TTS
            val result = aiTTSManager.synthesize(text, provider, voiceId, speed, pitch)
            
            result.onSuccess { audioData ->
                playAudio(audioData, onComplete, onError)
            }.onFailure { e ->
                Log.error { "AI TTS synthesis failed: ${e.message}" }
                onError(e as? Exception ?: Exception(e.message))
            }
            
        } catch (e: Exception) {
            Log.error { "AI TTS playback error: ${e.message}" }
            onError(e)
        }
    }
    
    /**
     * Play synthesized audio data
     */
    private suspend fun playAudio(
        audioData: AudioData,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Stop any existing playback
            stop()
            
            // Create AudioTrack
            val bufferSize = AudioTrack.getMinBufferSize(
                audioData.sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(audioData.sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            
            audioTrack?.play()
            isPlaying = true
            
            // Write audio data in chunks
            val chunkSize = 4096
            var offset = 0
            
            while (offset < audioData.samples.size && isPlaying) {
                val remaining = audioData.samples.size - offset
                val writeSize = minOf(chunkSize, remaining)
                
                val written = audioTrack?.write(
                    audioData.samples,
                    offset,
                    writeSize
                ) ?: 0
                
                if (written < 0) {
                    throw Exception("AudioTrack write error: $written")
                }
                
                offset += written
            }
            
            // Wait for playback to complete
            audioTrack?.stop()
            isPlaying = false
            
            onComplete()
            
        } catch (e: Exception) {
            Log.error { "Audio playback error: ${e.message}" }
            isPlaying = false
            onError(e)
        } finally {
            cleanup()
        }
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        isPlaying = false
        audioTrack?.stop()
        cleanup()
    }
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean = isPlaying
    
    /**
     * Clean up resources
     */
    private fun cleanup() {
        audioTrack?.release()
        audioTrack = null
    }
}
