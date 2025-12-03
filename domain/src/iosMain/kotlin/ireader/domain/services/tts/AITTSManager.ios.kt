package ireader.domain.services.tts

import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import platform.AVFAudio.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of AITTSManager
 * 
 * Uses AVSpeechSynthesizer for native iOS voices
 */
@OptIn(ExperimentalForeignApi::class)
actual class AITTSManager {
    
    private val synthesizer = AVSpeechSynthesizer()
    
    actual suspend fun getVoicesFromProvider(provider: AITTSProvider): Result<List<VoiceModel>> {
        return when (provider) {
            AITTSProvider.NATIVE_ANDROID -> getNativeVoices() // Reuse for iOS native
            else -> Result.failure(Exception("Provider not supported on iOS: $provider"))
        }
    }
    
    private fun getNativeVoices(): Result<List<VoiceModel>> {
        val voices = AVSpeechSynthesisVoice.speechVoices().mapNotNull { voice ->
            (voice as? AVSpeechSynthesisVoice)?.let {
                VoiceModel(
                    id = it.identifier,
                    name = it.name,
                    language = it.language.substringBefore("-"),
                    locale = it.language,
                    gender = VoiceGender.NEUTRAL,
                    quality = VoiceQuality.MEDIUM,
                    sampleRate = 22050,
                    modelSize = 0L,
                    downloadUrl = "",
                    configUrl = "",
                    checksum = "",
                    license = "Apple",
                    description = "iOS Native Voice: ${it.name}"
                )
            }
        }
        return Result.success(voices)
    }
    
    actual suspend fun synthesize(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData> {
        return Result.failure(Exception("Audio data export not supported. Use synthesizeAndPlay instead."))
    }
    
    actual suspend fun synthesizeAndPlay(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<Unit> {
        return try {
            val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
                rate = (speed * AVSpeechUtteranceDefaultSpeechRate).coerceIn(
                    AVSpeechUtteranceMinimumSpeechRate.toFloat(),
                    AVSpeechUtteranceMaximumSpeechRate.toFloat()
                )
                pitchMultiplier = pitch.coerceIn(0.5f, 2.0f)
                
                AVSpeechSynthesisVoice.voiceWithIdentifier(voiceId)?.let {
                    this.voice = it
                }
            }
            
            synthesizer.speakUtterance(utterance)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual fun configureGradio(spaceUrl: String, apiKey: String?) {
        // Not supported on iOS
    }
    
    actual suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit
    ): Result<Unit> {
        return Result.failure(Exception("Piper TTS not available on iOS"))
    }
    
    actual fun isVoiceDownloaded(voiceId: String): Boolean {
        return AVSpeechSynthesisVoice.voiceWithIdentifier(voiceId) != null
    }
    
    actual fun deleteVoice(voiceId: String): Boolean = false
    
    actual fun getDownloadedVoices(): List<String> {
        return AVSpeechSynthesisVoice.speechVoices().mapNotNull { voice ->
            (voice as? AVSpeechSynthesisVoice)?.identifier
        }
    }
    
    actual fun getDownloadedVoicesSize(): Long = 0L
    
    fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}
