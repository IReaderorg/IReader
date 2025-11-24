package ireader.domain.services.tts

import android.content.Context
import ireader.core.log.Log
import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel
import ireader.domain.preferences.prefs.AppPreferences

/**
 * Manager for AI TTS services
 * Handles multiple providers and voice selection
 */
actual class AITTSManager(
    private val context: Context,
    private val appPreferences: AppPreferences
) {
    
    private val providers = mutableMapOf<AITTSProvider, AITTSService>()
    
    init {
        initializeProviders()
    }
    
    private fun initializeProviders() {
        // Initialize Piper TTS (offline, free, high-quality)
        val httpClient = createHttpClient()
        providers[AITTSProvider.PIPER_TTS] = PiperTTSService(context, httpClient)
    }
    
    private fun createHttpClient(): io.ktor.client.HttpClient {
        return io.ktor.client.HttpClient(io.ktor.client.engine.okhttp.OkHttp) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                // JSON support if needed
            }
            
            // Timeout configuration
            engine {
                config {
                    connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
        }
    }
    
    /**
     * Get all available AI voices from all configured providers
     */
    suspend fun getAllAvailableVoices(): Result<Map<AITTSProvider, List<VoiceModel>>> {
        return runCatching {
            val voicesMap = mutableMapOf<AITTSProvider, List<VoiceModel>>()
            
            providers.forEach { (provider, service) ->
                if (service.isAvailable()) {
                    service.getAvailableVoices().onSuccess { voices ->
                        voicesMap[provider] = voices
                    }.onFailure { e ->
                        Log.error { "Failed to get voices from ${provider.name}: ${e.message}" }
                    }
                }
            }
            
            voicesMap
        }
    }
    
    /**
     * Get voices from a specific provider
     */
    suspend fun getVoicesFromProvider(provider: AITTSProvider): Result<List<VoiceModel>> {
        return providers[provider]?.getAvailableVoices() 
            ?: Result.failure(Exception("Provider $provider not configured"))
    }
    
    /**
     * Synthesize text using specified provider and voice
     */
    suspend fun synthesize(
        text: String,
        provider: AITTSProvider,
        voiceId: String,
        speed: Float = 1.0f,
        pitch: Float = 0.0f
    ): Result<AudioData> {
        val service = providers[provider] 
            ?: return Result.failure(Exception("Provider $provider not configured"))
        
        if (!service.isAvailable()) {
            return Result.failure(Exception("Provider $provider not available"))
        }
        
        return service.synthesize(text, voiceId, speed, pitch)
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
            runCatching { service.isAvailable() }.getOrDefault(false)
        }.keys.toList()
    }
    
    /**
     * Add or update a provider
     */
    fun addProvider(provider: AITTSProvider, service: AITTSService) {
        providers[provider] = service
    }
    
    /**
     * Remove a provider
     */
    fun removeProvider(provider: AITTSProvider) {
        providers.remove(provider)
    }
    
    /**
     * Download a Piper voice model
     */
    suspend fun downloadPiperVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit> {
        val piperService = providers[AITTSProvider.PIPER_TTS] as? PiperTTSService
            ?: return Result.failure(Exception("Piper TTS not available"))
        
        return piperService.downloadVoice(voiceModel, onProgress)
    }
    
    /**
     * Check if a voice is downloaded
     */
    fun isVoiceDownloaded(voiceId: String): Boolean {
        val piperService = providers[AITTSProvider.PIPER_TTS] as? PiperTTSService
            ?: return false
        
        return piperService.isVoiceDownloaded(voiceId)
    }
    
    /**
     * Delete a downloaded voice
     */
    fun deleteVoice(voiceId: String): Boolean {
        val piperService = providers[AITTSProvider.PIPER_TTS] as? PiperTTSService
            ?: return false
        
        return piperService.deleteVoice(voiceId)
    }
    
    /**
     * Get list of downloaded voices
     */
    fun getDownloadedVoices(): List<String> {
        val piperService = providers[AITTSProvider.PIPER_TTS] as? PiperTTSService
            ?: return emptyList()
        
        return piperService.getDownloadedVoices()
    }
    
    /**
     * Get total size of downloaded voices
     */
    fun getDownloadedVoicesSize(): Long {
        val piperService = providers[AITTSProvider.PIPER_TTS] as? PiperTTSService
            ?: return 0L
        
        return piperService.getDownloadedVoicesSize()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        providers.values.forEach { service ->
            when (service) {
                is PiperTTSService -> service.close()
            }
        }
        providers.clear()
    }
}

// Extension functions are not needed - use AppPreferences methods directly
