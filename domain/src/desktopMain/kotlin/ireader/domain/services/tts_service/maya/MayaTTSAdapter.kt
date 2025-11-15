package ireader.domain.services.tts_service.maya

import ireader.core.log.Log
import ireader.domain.services.tts_service.piper.AudioData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Adapter for Maya TTS Engine
 * Provides a unified interface for the TTS service
 */
class MayaTTSAdapter(
    private val engine: MayaTTSEngine
) {
    /**
     * Initialize Maya TTS
     */
    suspend fun initialize(): Result<Unit> {
        return engine.initialize()
    }
    
    /**
     * Synthesize text to audio
     */
    suspend fun synthesize(
        text: String,
        language: String = currentLanguage,
        speed: Float = 1.0f
    ): Result<AudioData> = withContext(Dispatchers.IO) {
        try {
            val result = engine.synthesize(text, language, speed)
            
            result.fold(
                onSuccess = { wavBytes ->
                    // Parse WAV file to extract audio data
                    val audioData = parseWavFile(wavBytes)
                    Result.success(audioData)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.error { "Maya synthesis error: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Parse WAV file bytes to AudioData
     */
    private fun parseWavFile(wavBytes: ByteArray): AudioData {
        val inputStream = ByteArrayInputStream(wavBytes)
        val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
        
        val format = audioInputStream.format
        val samples = audioInputStream.readBytes()
        
        return AudioData(
            samples = samples,
            sampleRate = format.sampleRate.toInt(),
            channels = format.channels,
            format = when (format.sampleSizeInBits) {
                16 -> AudioData.AudioFormat.PCM_16
                24 -> AudioData.AudioFormat.PCM_24
                32 -> AudioData.AudioFormat.PCM_32
                else -> AudioData.AudioFormat.PCM_16
            }
        )
    }
    
    /**
     * Get supported languages
     */
    fun getSupportedLanguages(): List<MayaLanguage> {
        return engine.getSupportedLanguages()
    }
    
    /**
     * Set the current language (stored for future synthesis calls)
     */
    private var currentLanguage: String = "en"
    
    fun setLanguage(languageCode: String) {
        currentLanguage = languageCode
        Log.info { "Maya language set to: $languageCode" }
    }
    
    fun getCurrentLanguage(): String = currentLanguage
    
    /**
     * Check if Maya is available
     */
    fun isAvailable(): Boolean {
        return engine.isAvailable()
    }
    
    /**
     * Shutdown Maya engine
     */
    fun shutdown() {
        engine.shutdown()
    }
}
