package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel

/**
 * iOS implementation of AITTSManager
 * 
 * TODO: Full implementation using AVSpeechSynthesizer or third-party TTS APIs
 */
actual class AITTSManager {
    
    actual suspend fun getVoicesFromProvider(provider: AITTSProvider): Result<List<VoiceModel>> {
        return Result.success(emptyList())
    }
    
    actual suspend fun synthesize(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData> {
        return Result.failure(Exception("AI TTS not implemented on iOS"))
    }
    
    actual suspend fun synthesizeAndPlay(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<Unit> {
        return Result.failure(Exception("AI TTS not implemented on iOS"))
    }
    
    actual fun configureGradio(spaceUrl: String, apiKey: String?) {
        // No-op on iOS
    }
    
    actual suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit
    ): Result<Unit> {
        return Result.failure(Exception("Piper TTS not available on iOS"))
    }
    
    actual fun isVoiceDownloaded(voiceId: String): Boolean = false
    
    actual fun deleteVoice(voiceId: String): Boolean = false
    
    actual fun getDownloadedVoices(): List<String> = emptyList()
    
    actual fun getDownloadedVoicesSize(): Long = 0L
}
