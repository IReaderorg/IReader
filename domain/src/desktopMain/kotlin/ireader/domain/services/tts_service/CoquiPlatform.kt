package ireader.domain.services.tts_service

import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import javax.sound.sampled.*

/**
 * Desktop implementation of base64 decoding
 */
actual fun base64DecodeToBytes(base64: String): ByteArray {
    return Base64.getDecoder().decode(base64)
}

/**
 * Desktop implementation of CoquiAudioPlayer using Java Sound API
 */
class DesktopCoquiAudioPlayer : CoquiAudioPlayer {
    
    private var clip: Clip? = null
    private var tempFile: File? = null
    
    override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Stop any existing playback
                stop()
                
                // Write to temp file for playback
                tempFile = File.createTempFile("coqui_tts_", ".wav")
                FileOutputStream(tempFile).use { it.write(audioData) }
                
                // Play using Java Sound API
                val audioInputStream = AudioSystem.getAudioInputStream(tempFile)
                clip = AudioSystem.getClip().apply {
                    open(audioInputStream)
                    addLineListener { event ->
                        if (event.type == LineEvent.Type.STOP) {
                            onComplete()
                            cleanup()
                        }
                    }
                    start()
                }
            } catch (e: UnsupportedAudioFileException) {
                Log.error { "Unsupported audio format: ${e.message}" }
                // Try alternative playback method
                playWithSourceDataLine(audioData, onComplete)
            } catch (e: Exception) {
                Log.error { "Error playing audio: ${e.message}" }
                cleanup()
                onComplete()
            }
        }
    }
    
    private fun playWithSourceDataLine(audioData: ByteArray, onComplete: () -> Unit) {
        try {
            // Try to play raw PCM data
            val format = AudioFormat(22050f, 16, 1, true, false)
            val info = DataLine.Info(SourceDataLine::class.java, format)
            
            if (AudioSystem.isLineSupported(info)) {
                val line = AudioSystem.getLine(info) as SourceDataLine
                line.open(format)
                line.start()
                line.write(audioData, 0, audioData.size)
                line.drain()
                line.close()
                onComplete()
            } else {
                Log.error { "Audio line not supported" }
                onComplete()
            }
        } catch (e: Exception) {
            Log.error { "Error with alternative playback: ${e.message}" }
            onComplete()
        }
    }
    
    override fun stop() {
        try {
            clip?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        cleanup()
    }
    
    override fun pause() {
        try {
            clip?.stop()
        } catch (e: Exception) {
            Log.error { "Error pausing: ${e.message}" }
        }
    }
    
    override fun resume() {
        try {
            clip?.start()
        } catch (e: Exception) {
            Log.error { "Error resuming: ${e.message}" }
        }
    }
    
    override fun release() {
        cleanup()
    }
    
    private fun cleanup() {
        try {
            clip?.close()
            clip = null
            tempFile?.delete()
            tempFile = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}
