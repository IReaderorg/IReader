package ireader.domain.services.tts

/**
 * iOS implementation of AITTSManager
 * 
 * TODO: Implement using AVSpeechSynthesizer or third-party TTS APIs
 */
actual class AITTSManager {
    actual suspend fun speak(text: String, voiceId: String?): Result<ByteArray> {
        return Result.failure(Exception("AI TTS not implemented on iOS"))
    }
    
    actual fun getAvailableVoices(): List<TTSVoice> {
        return emptyList()
    }
    
    actual fun stop() {
        // No-op
    }
}

data class TTSVoice(
    val id: String,
    val name: String,
    val language: String
)
