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
    
    companion object {
        private const val TAG = "GradioAudioPlayer"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null
    private var currentOnComplete: (() -> Unit)? = null
    private var playId: Int = 0 // Track play requests for logging
    
    // Flag to prevent completion callback when stopped manually
    @Volatile
    private var isStopped: Boolean = false
    
    override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
        val thisPlayId = ++playId
        Log.info { "$TAG: play() START - playId=$thisPlayId, audioSize=${audioData.size}, isStopped=$isStopped" }
        
        // Reset stopped flag for new playback BEFORE stopping old playback
        isStopped = false
        currentOnComplete = onComplete
        
        withContext(Dispatchers.Main) {
            try {
                // Stop and release any existing player
                mediaPlayer?.let { player ->
                    Log.info { "$TAG: play() - Releasing old player (playId=$thisPlayId)" }
                    try {
                        player.setOnCompletionListener(null)
                        player.setOnErrorListener(null)
                        player.stop()
                        player.release()
                    } catch (e: Exception) {
                        Log.warn { "$TAG: play() - Error releasing old player: ${e.message}" }
                    }
                }
                mediaPlayer = null
                
                // Delete old temp file
                tempFile?.delete()
                tempFile = null
            } catch (e: Exception) {
                Log.warn { "$TAG: play() - Cleanup error: ${e.message}" }
            }
        }
        
        withContext(Dispatchers.IO) {
            try {
                // Write audio data to temp file
                Log.info { "$TAG: play() - Writing audio to temp file (playId=$thisPlayId)" }
                val newTempFile = File.createTempFile("gradio_tts_", ".wav", context.cacheDir)
                FileOutputStream(newTempFile).use { it.write(audioData) }
                
                withContext(Dispatchers.Main) {
                    // Check if stopped while writing file
                    if (isStopped) {
                        Log.info { "$TAG: play() - ABORTED: isStopped=true after file write (playId=$thisPlayId)" }
                        newTempFile.delete()
                        return@withContext
                    }
                    
                    tempFile = newTempFile
                    
                    Log.info { "$TAG: play() - Creating MediaPlayer (playId=$thisPlayId)" }
                    val player = MediaPlayer()
                    mediaPlayer = player
                    
                    player.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    player.setDataSource(newTempFile.absolutePath)
                    player.setOnCompletionListener {
                        Log.info { "$TAG: onCompletion - playId=$thisPlayId, isStopped=$isStopped" }
                        // Only call onComplete if not manually stopped
                        if (!isStopped) {
                            Log.info { "$TAG: onCompletion - Calling onComplete callback (playId=$thisPlayId)" }
                            currentOnComplete?.invoke()
                        } else {
                            Log.info { "$TAG: onCompletion - SKIPPED: isStopped=true (playId=$thisPlayId)" }
                        }
                        cleanupPlayer()
                    }
                    player.setOnErrorListener { _, what, extra ->
                        Log.error { "$TAG: onError - what=$what, extra=$extra, playId=$thisPlayId, isStopped=$isStopped" }
                        // Only call onComplete if not manually stopped
                        if (!isStopped) {
                            Log.info { "$TAG: onError - Calling onComplete callback (playId=$thisPlayId)" }
                            currentOnComplete?.invoke()
                        } else {
                            Log.info { "$TAG: onError - SKIPPED: isStopped=true (playId=$thisPlayId)" }
                        }
                        cleanupPlayer()
                        true
                    }
                    player.prepare()
                    
                    // Final check before starting
                    if (!isStopped) {
                        Log.info { "$TAG: play() - Starting playback (playId=$thisPlayId)" }
                        player.start()
                    } else {
                        Log.info { "$TAG: play() - ABORTED: isStopped=true before start (playId=$thisPlayId)" }
                        cleanupPlayer()
                    }
                }
            } catch (e: Exception) {
                Log.error { "$TAG: play() - Exception: ${e.message} (playId=$thisPlayId)" }
                withContext(Dispatchers.Main) {
                    cleanupPlayer()
                }
                // Only call onComplete if not manually stopped
                if (!isStopped) {
                    Log.info { "$TAG: play() - Calling onComplete after exception (playId=$thisPlayId)" }
                    onComplete()
                }
            }
        }
        Log.info { "$TAG: play() END - playId=$thisPlayId" }
    }
    
    override fun stop() {
        Log.info { "$TAG: stop() START - playId=$playId, isStopped=$isStopped, hasPlayer=${mediaPlayer != null}" }
        // Set stopped flag BEFORE stopping to prevent race condition
        isStopped = true
        currentOnComplete = null
        
        try {
            mediaPlayer?.let { player ->
                Log.info { "$TAG: stop() - Removing listeners and releasing player" }
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                try {
                    player.stop()
                } catch (e: Exception) {
                    Log.warn { "$TAG: stop() - Error stopping player: ${e.message}" }
                }
                player.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.warn { "$TAG: stop() - Exception: ${e.message}" }
        }
        
        try {
            tempFile?.delete()
            tempFile = null
        } catch (e: Exception) {
            // Ignore
        }
        Log.info { "$TAG: stop() END" }
    }
    
    override fun pause() {
        Log.info { "$TAG: pause()" }
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.error { "$TAG: pause() error: ${e.message}" }
        }
    }
    
    override fun resume() {
        Log.info { "$TAG: resume()" }
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.error { "$TAG: resume() error: ${e.message}" }
        }
    }
    
    override fun release() {
        Log.info { "$TAG: release()" }
        stop()
    }
    
    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get total duration in milliseconds
     */
    fun getDuration(): Long {
        return try {
            mediaPlayer?.duration?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            Log.error { "$TAG: seekTo() error: ${e.message}" }
        }
    }
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun cleanupPlayer() {
        Log.info { "$TAG: cleanupPlayer()" }
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            tempFile?.delete()
            tempFile = null
        } catch (e: Exception) {
            Log.warn { "$TAG: cleanupPlayer() error: ${e.message}" }
        }
    }
}
