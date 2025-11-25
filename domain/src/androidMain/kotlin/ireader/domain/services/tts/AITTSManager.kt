package ireader.domain.services.tts

import android.content.Context
import ireader.core.log.Log
import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel
import ireader.domain.preferences.prefs.AppPreferences

/**
 * Manager for AI TTS services
 * Android implementation supports Hugging Face TTS and native Android TTS
 */
actual class AITTSManager(
    private val context: Context,
    private val appPreferences: AppPreferences
) {
    
    private val providers = mutableMapOf<AITTSProvider, AITTSService>()
    private var coquiService: CoquiTTSService? = null
    
    init {
        Log.info { "AITTSManager initialized - supports Coqui TTS, Kokoro TTS, Hugging Face TTS and native Android TTS" }
        
        // Auto-configure Coqui TTS if enabled
        if (appPreferences.useCoquiTTS().get()) {
            val spaceUrl = appPreferences.coquiSpaceUrl().get()
            val apiKey = appPreferences.coquiApiKey().get()
            if (spaceUrl.isNotEmpty()) {
                configureCoqui(spaceUrl, apiKey.ifEmpty { null })
                Log.info { "Auto-configured Coqui TTS: $spaceUrl" }
            }
        }
    }
    
    /**
     * Configure Coqui TTS service
     * High-quality open-source TTS with Gradio API
     * @param spaceUrl Your Hugging Face Space URL (e.g., "https://username-coqui-tts.hf.space")
     * @param apiKey Optional API key for private spaces
     */
    actual fun configureCoqui(spaceUrl: String, apiKey: String?) {
        coquiService = CoquiTTSService(context, spaceUrl, apiKey)
        providers[AITTSProvider.COQUI_TTS] = coquiService!!
        Log.info { "Coqui TTS configured with space: $spaceUrl" }
    }

    
    /**
     * Get all available AI voices from all configured providers
     */
    suspend fun getAllAvailableVoices(): Result<Map<AITTSProvider, List<VoiceModel>>> {
        val voicesMap = mutableMapOf<AITTSProvider, List<VoiceModel>>()
        
        providers.forEach { (provider, service) ->
            service.getAvailableVoices().onSuccess { voices ->
                voicesMap[provider] = voices
            }
        }
        
        return Result.success(voicesMap)
    }
    
    /**
     * Get voices from a specific provider
     */
    actual suspend fun getVoicesFromProvider(provider: AITTSProvider): Result<List<VoiceModel>> {
        val service = providers[provider]
            ?: return Result.failure(Exception("Provider $provider not configured"))
        
        return service.getAvailableVoices()
    }
    
    /**
     * Synthesize text using specified provider and voice
     */
    actual suspend fun synthesize(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData> {
        val service = providers[provider]
            ?: return Result.failure(Exception("Provider $provider not configured"))
        
        return service.synthesize(text, voiceId, speed, pitch)
    }
    
    /**
     * Synthesize and play audio directly
     */
    actual suspend fun synthesizeAndPlay(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<Unit> {
        return synthesize(text, provider, voiceId, speed, pitch).mapCatching { audioData ->
            when (provider) {
                AITTSProvider.COQUI_TTS -> coquiService?.playAudio(audioData)
                else -> { /* No playback for other providers */ }
            }
        }
    }
    
    /**
     * Stop audio playback
     */
    fun stopPlayback() {
        coquiService?.stopAudio()
    }
    
    /**
     * Get Coqui TTS service instance
     */
    fun getCoquiService(): CoquiTTSService? {
        return coquiService
    }
    
    /**
     * Check if any AI TTS provider is available
     */
    suspend fun hasAvailableProvider(): Boolean {
        return providers.values.any { it.isAvailable() }
    }
    
    /**
     * Get list of available providers
     */
    suspend fun getAvailableProviders(): List<AITTSProvider> {
        return providers.filter { (_, service) -> 
            service.isAvailable() 
        }.keys.toList()
    }
    
    /**
     * Download a voice model
     * Not available on Android - use native TTS or Sherpa TTS app
     */
    actual suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit
    ): Result<Unit> {
        return Result.failure(Exception("Voice download not available on Android - use native TTS or install Sherpa TTS app"))
    }
    
    /**
     * Check if a voice is downloaded
     * Always returns false on Android
     */
    actual fun isVoiceDownloaded(voiceId: String): Boolean {
        return false
    }
    
    /**
     * Delete a downloaded voice
     * Not available on Android
     */
    actual fun deleteVoice(voiceId: String): Boolean {
        return false
    }
    
    /**
     * Get list of downloaded voices
     * Returns empty list on Android
     */
    actual fun getDownloadedVoices(): List<String> {
        return emptyList()
    }
    
    /**
     * Get total size of downloaded voices
     * Returns 0 on Android
     */
    actual fun getDownloadedVoicesSize(): Long {
        return 0L
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        coquiService?.cleanup()
        providers.clear()
    }
}
