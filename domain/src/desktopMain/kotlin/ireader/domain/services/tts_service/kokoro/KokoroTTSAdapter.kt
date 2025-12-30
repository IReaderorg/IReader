package ireader.domain.services.tts_service.kokoro

import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.piper.AudioData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Adapter to integrate Kokoro TTS with the existing TTS service
 * 
 * This adapter converts Kokoro's WAV output to the AudioData format
 * used by the TTS service.
 */
class KokoroTTSAdapter(
    private val kokoroEngine: KokoroTTSEngine,
    private val appPreferences: AppPreferences? = null
) {
    
    /**
     * Initialize Kokoro engine
     */
    suspend fun initialize(): Result<Unit> {
        return kokoroEngine.initialize()
    }
    
    /**
     * Synthesize text and convert to AudioData
     */
    suspend fun synthesize(
        text: String,
        voice: String = currentVoice,
        speed: Float = 1.0f
    ): Result<AudioData> = withContext(Dispatchers.IO) {
        try {
            // Generate audio with Kokoro
            val wavResult = kokoroEngine.synthesize(text, voice, speed)
            
            if (wavResult.isFailure) {
                return@withContext Result.failure(
                    wavResult.exceptionOrNull() ?: Exception("Unknown error")
                )
            }
            
            val wavData = wavResult.getOrThrow()
            
            // Parse WAV file to extract PCM data
            val audioData = parseWavFile(wavData)
            
            Result.success(audioData)
            
        } catch (e: Exception) {
            Log.error { "Kokoro synthesis failed: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Get available voices
     */
    fun getAvailableVoices(): List<KokoroVoice> {
        return kokoroEngine.getAvailableVoices()
    }
    
    /**
     * Set the current voice (stored for future synthesis calls)
     */
    private var currentVoice: String = appPreferences?.selectedKokoroVoice()?.get() ?: "af_bella"
    
    fun setVoice(voiceId: String) {
        currentVoice = voiceId
        // Persist the voice selection to preferences
        appPreferences?.selectedKokoroVoice()?.set(voiceId)
        Log.info { "Kokoro voice set to: $voiceId" }
    }
    
    /**
     * Load the voice from preferences (call on initialization)
     */
    fun loadVoiceFromPreferences() {
        appPreferences?.selectedKokoroVoice()?.get()?.let { savedVoice ->
            currentVoice = savedVoice
            Log.info { "Loaded Kokoro voice from preferences: $savedVoice" }
        }
    }
    
    fun getCurrentVoice(): String = currentVoice
    
    /**
     * Check if Kokoro is available
     */
    fun isAvailable(): Boolean {
        return kokoroEngine.isAvailable()
    }
    
    /**
     * Get number of active processes (for monitoring)
     */
    fun getActiveProcessCount(): Int {
        return kokoroEngine.getActiveProcessCount()
    }
    
    /**
     * Parse WAV file and extract PCM data
     */
    private fun parseWavFile(wavData: ByteArray): AudioData {
        val inputStream = ByteArrayInputStream(wavData)
        val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
        
        val format = audioInputStream.format
        val pcmData = audioInputStream.readBytes()
        
        // Convert to our AudioData format
        return AudioData(
            samples = pcmData,
            sampleRate = format.sampleRate.toInt(),
            channels = format.channels,
            format = when (format.sampleSizeInBits) {
                16 -> AudioData.AudioFormat.PCM_16
                24 -> AudioData.AudioFormat.PCM_24
                32 -> AudioData.AudioFormat.PCM_32
                else -> AudioData.AudioFormat.PCM_16 // Default
            }
        )
    }
    
    /**
     * Shutdown
     */
    fun shutdown() {
        kokoroEngine.shutdown()
    }
}
