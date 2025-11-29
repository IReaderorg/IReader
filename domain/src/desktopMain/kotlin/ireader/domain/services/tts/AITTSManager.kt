package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel

/**
 * Desktop implementation of AI TTS Manager
 * Currently provides stub implementation
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
        return Result.failure(Exception("AI TTS not supported on desktop"))
    }
    
    actual suspend fun synthesizeAndPlay(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<Unit> {
        return Result.failure(Exception("AI TTS not supported on desktop"))
    }
    
    actual fun configureGradio(spaceUrl: String, apiKey: String?) {
        // No-op on desktop
    }
    
    actual suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit
    ): Result<Unit> {
        return Result.failure(Exception("Voice download not supported on desktop"))
    }
    
    actual fun isVoiceDownloaded(voiceId: String): Boolean {
        return false
    }
    
    actual fun deleteVoice(voiceId: String): Boolean {
        return false
    }
    
    actual fun getDownloadedVoices(): List<String> {
        return emptyList()
    }
    
    actual fun getDownloadedVoicesSize(): Long {
        return 0L
    }
}
