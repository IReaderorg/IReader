package ireader.domain.services.tts_service.piper

import io.github.givimad.piperjni.PiperJNI
import io.github.givimad.piperjni.PiperVoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ireader.core.log.Log
import java.nio.file.Paths

/**
 * Piper TTS synthesizer using the piper-jni library.
 * This provides direct JNI integration with Piper TTS engine.
 * 
 * Uses GiviMAD/piper-jni library which wraps the MIT-licensed Piper (pre-1.3.0).
 */
class PiperJNISynthesizer {
    
    private var piperJNI: PiperJNI? = null
    private var piperVoice: PiperVoice? = null
    private var sampleRate: Int = 22050
    private var speechRate: Float = 1.0f
    
    /**
     * Initialize Piper with a voice model
     */
    suspend fun initialize(modelPath: String, configPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.info { "Initializing Piper JNI with model: $modelPath" }
            
            // Create PiperJNI instance
            val piper = PiperJNI()
            
            // Initialize with ESpeak phonemes support
            piper.initialize(true, false)
            
            // Load the voice model
            val modelPathObj = Paths.get(modelPath)
            val configPathObj = Paths.get(configPath)
            
            val voice = piper.loadVoice(modelPathObj, configPathObj)
            
            // Get sample rate from the loaded model
            sampleRate = voice.sampleRate
            
            piperJNI = piper
            piperVoice = voice
            
            Log.info { "Piper JNI initialized successfully with sample rate: $sampleRate Hz" }
            true
            
        } catch (e: Exception) {
            Log.error(e, "Failed to initialize Piper JNI")
            piperJNI?.close()
            piperJNI = null
            piperVoice = null
            false
        }
    }
    
    /**
     * Synthesize text to audio
     */
    suspend fun synthesize(text: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val piper = piperJNI
            val voice = piperVoice
            
            if (piper == null || voice == null) {
                Log.error { "Piper voice not initialized" }
                return@withContext ByteArray(0)
            }
            
            if (text.isBlank()) {
                return@withContext ByteArray(0)
            }
            
            // Synthesize audio using piper-jni
            // The textToAudio method returns short[] audio samples
            val audioSamples = piper.textToAudio(voice, text)
            
            if (audioSamples == null || audioSamples.isEmpty()) {
                Log.warn { "Synthesis produced no audio data" }
                return@withContext ByteArray(0)
            }
            
            // Convert short[] to ByteArray (16-bit PCM, little-endian)
            val audioBytes = ByteArray(audioSamples.size * 2)
            for (i in audioSamples.indices) {
                val sample = audioSamples[i]
                audioBytes[i * 2] = (sample.toInt() and 0xFF).toByte()
                audioBytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }
            
            audioBytes
            
        } catch (e: Exception) {
            Log.error(e, "Failed to synthesize text")
            ByteArray(0)
        }
    }
    
    /**
     * Set speech rate (0.5 = slower, 2.0 = faster)
     * Note: piper-jni doesn't support runtime speech rate adjustment
     * This is stored for future use if the API adds support
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }
    
    /**
     * Get sample rate of the loaded model
     */
    fun getSampleRate(): Int = sampleRate
    
    /**
     * Check if initialized
     */
    fun isInitialized(): Boolean = piperJNI != null && piperVoice != null
    
    /**
     * Shutdown and release resources
     */
    fun shutdown() {
        try {
            piperVoice?.close()
            piperJNI?.close()
            piperVoice = null
            piperJNI = null
            Log.info { "Piper JNI shutdown complete" }
        } catch (e: Exception) {
            Log.error(e, "Error during Piper JNI shutdown")
        }
    }
}
