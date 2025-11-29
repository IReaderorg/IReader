package ireader.domain.services.tts_service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of base64 decoding
 */
actual fun base64DecodeToBytes(base64: String): ByteArray {
    return Base64.decode(base64, Base64.DEFAULT)
}

/**
 * Android implementation of CoquiAudioPlayer
 */
class AndroidGradioAudioPlayer(private val context: Context) : GradioAudioPlayer {
    
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null
    
    override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Stop any existing playback
                stop()
                
                // Write audio data to temp file
                tempFile = File.createTempFile("coqui_tts_", ".wav", context.cacheDir)
                FileOutputStream(tempFile).use { it.write(audioData) }
                
                withContext(Dispatchers.Main) {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(tempFile?.absolutePath)
                        setOnCompletionListener {
                            onComplete()
                            cleanup()
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.error { "MediaPlayer error: what=$what, extra=$extra" }
                            onComplete()
                            cleanup()
                            true
                        }
                        prepare()
                        start()
                    }
                }
            } catch (e: Exception) {
                Log.error { "Error playing audio: ${e.message}" }
                cleanup()
                onComplete()
            }
        }
    }
    
    override fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        cleanup()
    }
    
    override fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.error { "Error pausing: ${e.message}" }
        }
    }
    
    override fun resume() {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.error { "Error resuming: ${e.message}" }
        }
    }
    
    override fun release() {
        cleanup()
    }
    
    private fun cleanup() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            tempFile?.delete()
            tempFile = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}
