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
 * Desktop implementation of GradioAudioPlayer using Java Sound API
 * 
 * Supports multiple audio formats:
 * - WAV (native Java Sound support)
 * - MP3, OGG, FLAC (via format detection and conversion)
 */
class DesktopGradioAudioPlayer : GradioAudioPlayer {
    
    private var clip: Clip? = null
    private var currentLine: SourceDataLine? = null
    private var tempFile: File? = null
    @Volatile private var isPlaying = false
    @Volatile private var isPaused = false
    
    override suspend fun play(audioData: ByteArray, onComplete: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Stop any existing playback
                stop()
                isPlaying = true
                isPaused = false
                
                // Detect audio format from magic bytes
                val format = detectAudioFormat(audioData)
                
                when (format) {
                    AudioFileFormat.WAV -> playWavAudio(audioData, onComplete)
                    AudioFileFormat.MP3 -> playWithExternalDecoder(audioData, "mp3", onComplete)
                    AudioFileFormat.OGG -> playWithExternalDecoder(audioData, "ogg", onComplete)
                    AudioFileFormat.FLAC -> playWithExternalDecoder(audioData, "flac", onComplete)
                    AudioFileFormat.UNKNOWN -> {
                        // Try WAV first, then raw PCM
                        if (!tryPlayAsWav(audioData, onComplete)) {
                            playRawPcm(audioData, onComplete)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error { "Error playing audio: ${e.message}" }
                cleanup()
                onComplete()
            }
        }
    }
    
    private enum class AudioFileFormat {
        WAV, MP3, OGG, FLAC, UNKNOWN
    }
    
    private fun detectAudioFormat(data: ByteArray): AudioFileFormat {
        if (data.size < 12) return AudioFileFormat.UNKNOWN
        
        // WAV: starts with "RIFF" and contains "WAVE"
        if (data[0] == 'R'.code.toByte() && data[1] == 'I'.code.toByte() &&
            data[2] == 'F'.code.toByte() && data[3] == 'F'.code.toByte() &&
            data[8] == 'W'.code.toByte() && data[9] == 'A'.code.toByte() &&
            data[10] == 'V'.code.toByte() && data[11] == 'E'.code.toByte()) {
            return AudioFileFormat.WAV
        }
        
        // MP3: starts with ID3 tag or frame sync (0xFF 0xFB/0xFA/0xF3/0xF2)
        if ((data[0] == 'I'.code.toByte() && data[1] == 'D'.code.toByte() && data[2] == '3'.code.toByte()) ||
            (data[0] == 0xFF.toByte() && (data[1].toInt() and 0xE0) == 0xE0)) {
            return AudioFileFormat.MP3
        }
        
        // OGG: starts with "OggS"
        if (data[0] == 'O'.code.toByte() && data[1] == 'g'.code.toByte() &&
            data[2] == 'g'.code.toByte() && data[3] == 'S'.code.toByte()) {
            return AudioFileFormat.OGG
        }
        
        // FLAC: starts with "fLaC"
        if (data[0] == 'f'.code.toByte() && data[1] == 'L'.code.toByte() &&
            data[2] == 'a'.code.toByte() && data[3] == 'C'.code.toByte()) {
            return AudioFileFormat.FLAC
        }
        
        return AudioFileFormat.UNKNOWN
    }
    
    private fun playWavAudio(audioData: ByteArray, onComplete: () -> Unit) {
        try {
            // Write to temp file
            tempFile = File.createTempFile("gradio_tts_", ".wav")
            FileOutputStream(tempFile).use { it.write(audioData) }
            
            // Play using Java Sound API
            val audioInputStream = AudioSystem.getAudioInputStream(tempFile)
            
            // Get the format and check if we need to convert
            val baseFormat = audioInputStream.format
            
            // Convert to PCM if needed
            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
            )
            
            val decodedStream = if (baseFormat.encoding != AudioFormat.Encoding.PCM_SIGNED) {
                AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)
            } else {
                audioInputStream
            }
            
            clip = AudioSystem.getClip().apply {
                open(decodedStream)
                addLineListener { event ->
                    when (event.type) {
                        LineEvent.Type.STOP -> {
                            // Only call onComplete if audio finished naturally (reached end)
                            // Check if we're at the end of the clip
                            val clipRef = clip
                            if (clipRef != null && !isPaused && clipRef.framePosition >= clipRef.frameLength - 1) {
                                isPlaying = false
                                onComplete()
                                cleanup()
                            }
                        }
                        else -> { /* ignore other events */ }
                    }
                }
                start()
            }
        } catch (e: UnsupportedAudioFileException) {
            Log.error { "Unsupported WAV format: ${e.message}" }
            // Try raw PCM playback
            playRawPcm(audioData, onComplete)
        } catch (e: Exception) {
            Log.error { "Error playing WAV: ${e.message}" }
            cleanup()
            onComplete()
        }
    }
    
    private fun tryPlayAsWav(audioData: ByteArray, onComplete: () -> Unit): Boolean {
        return try {
            playWavAudio(audioData, onComplete)
            true
        } catch (e: Exception) {
            Log.warn { "Failed to play as WAV: ${e.message}" }
            false
        }
    }
    
    private fun playWithExternalDecoder(audioData: ByteArray, extension: String, onComplete: () -> Unit) {
        try {
            // Write to temp file with correct extension
            tempFile = File.createTempFile("gradio_tts_", ".$extension")
            FileOutputStream(tempFile).use { it.write(audioData) }
            
            // Try to use ffmpeg to convert to WAV
            val wavFile = File.createTempFile("gradio_tts_converted_", ".wav")
            
            val process = ProcessBuilder(
                "ffmpeg", "-y", "-i", tempFile!!.absolutePath,
                "-acodec", "pcm_s16le", "-ar", "22050", "-ac", "1",
                wavFile.absolutePath
            ).redirectErrorStream(true).start()
            
            val completed = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
            
            if (completed && process.exitValue() == 0 && wavFile.exists() && wavFile.length() > 0) {
                val wavData = wavFile.readBytes()
                wavFile.delete()
                playWavAudio(wavData, onComplete)
            } else {
                wavFile.delete()
                // Try direct playback (might work for some formats)
                tryDirectPlayback(onComplete)
            }
        } catch (e: Exception) {
            Log.error { "Error with external decoder: ${e.message}" }
            // ffmpeg not available, try direct playback
            tryDirectPlayback(onComplete)
        }
    }
    
    private fun tryDirectPlayback(onComplete: () -> Unit) {
        try {
            if (tempFile != null && tempFile!!.exists()) {
                val audioInputStream = AudioSystem.getAudioInputStream(tempFile)
                clip = AudioSystem.getClip().apply {
                    open(audioInputStream)
                    addLineListener { event ->
                        when (event.type) {
                            LineEvent.Type.STOP -> {
                                // Only call onComplete if audio finished naturally
                                val clipRef = clip
                                if (clipRef != null && !isPaused && clipRef.framePosition >= clipRef.frameLength - 1) {
                                    isPlaying = false
                                    onComplete()
                                    cleanup()
                                }
                            }
                            else -> { /* ignore other events */ }
                        }
                    }
                    start()
                }
            } else {
                Log.error { "No temp file for direct playback" }
                onComplete()
            }
        } catch (e: Exception) {
            Log.error { "Direct playback failed: ${e.message}" }
            cleanup()
            onComplete()
        }
    }
    
    private fun playRawPcm(audioData: ByteArray, onComplete: () -> Unit) {
        try {
            // Skip WAV header if present (44 bytes)
            val pcmData = if (audioData.size > 44 && 
                audioData[0] == 'R'.code.toByte() && 
                audioData[1] == 'I'.code.toByte()) {
                audioData.copyOfRange(44, audioData.size)
            } else {
                audioData
            }
            
            // Try different common audio formats
            val formats = listOf(
                AudioFormat(22050f, 16, 1, true, false),  // 22kHz mono
                AudioFormat(24000f, 16, 1, true, false),  // 24kHz mono
                AudioFormat(16000f, 16, 1, true, false),  // 16kHz mono
                AudioFormat(44100f, 16, 1, true, false),  // 44.1kHz mono
                AudioFormat(22050f, 16, 2, true, false),  // 22kHz stereo
            )
            
            for (format in formats) {
                val frameSize = format.frameSize
                // Ensure data size is a multiple of frame size
                val adjustedSize = (pcmData.size / frameSize) * frameSize
                
                if (adjustedSize <= 0) continue
                
                val info = DataLine.Info(SourceDataLine::class.java, format)
                if (AudioSystem.isLineSupported(info)) {
                    try {
                        val line = AudioSystem.getLine(info) as SourceDataLine
                        currentLine = line
                        line.open(format)
                        line.start()
                        line.write(pcmData, 0, adjustedSize)
                        line.drain()
                        line.close()
                        currentLine = null
                        isPlaying = false
                        onComplete()
                        return
                    } catch (e: Exception) {
                        Log.debug { "Format ${format.sampleRate}Hz failed: ${e.message}" }
                        continue
                    }
                }
            }
            
            Log.error { "No supported audio format found for raw PCM playback" }
            onComplete()
        } catch (e: Exception) {
            Log.error { "Error with raw PCM playback: ${e.message}" }
            onComplete()
        }
    }
    
    override fun stop() {
        isPlaying = false
        isPaused = false
        try {
            clip?.stop()
            currentLine?.stop()
            currentLine?.close()
        } catch (e: Exception) {
            // Ignore
        }
        cleanup()
    }
    
    override fun pause() {
        isPaused = true
        try {
            clip?.stop()
            currentLine?.stop()
        } catch (e: Exception) {
            Log.error { "Error pausing: ${e.message}" }
        }
    }
    
    override fun resume() {
        isPaused = false
        try {
            clip?.start()
            currentLine?.start()
        } catch (e: Exception) {
            Log.error { "Error resuming: ${e.message}" }
        }
    }
    
    override fun release() {
        stop()
        cleanup()
    }
    
    private fun cleanup() {
        try {
            clip?.close()
            clip = null
            currentLine?.close()
            currentLine = null
            tempFile?.delete()
            tempFile = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}
