package ireader.domain.services.tts

import android.content.Context
import ireader.core.log.Log

/**
 * Factory for creating TTS service instances
 * 
 * This factory provides a centralized way to create and configure
 * different TTS service implementations.
 * 
 * Currently supports:
 * - Native Android TTS (via existing TTSService)
 * - Coqui TTS (neural voice via Hugging Face Space)
 */
object TTSServiceFactory {
    
    /**
     * Create a TTS service based on provider type
     * 
     * @param provider The TTS provider to use
     * @param context Android context
     * @param config Configuration for the service
     * @return StreamingTTSService instance or null if creation fails
     */
    fun createService(
        provider: TTSProvider,
        context: Context,
        config: TTSServiceConfig
    ): StreamingTTSService? {
        return try {
            when (provider) {
                TTSProvider.COQUI_TTS -> {
                    if (config.spaceUrl.isEmpty()) {
                        Log.error { "Coqui TTS requires spaceUrl in config" }
                        return null
                    }
                    CoquiTTSService(
                        context = context,
                        spaceUrl = config.spaceUrl,
                        apiKey = config.apiKey
                    )
                }
                
                TTSProvider.NATIVE_ANDROID -> {
                    // Native Android TTS is handled by the existing TTSService
                    // This factory is primarily for streaming services like Coqui
                    Log.info { "Native Android TTS uses existing TTSService" }
                    null
                }
            }
        } catch (e: Exception) {
            Log.error { "Failed to create TTS service for $provider: ${e.message}" }
            null
        }
    }
}

/**
 * Supported TTS providers
 */
enum class TTSProvider {
    NATIVE_ANDROID,
    COQUI_TTS
}

/**
 * Configuration for TTS services
 */
data class TTSServiceConfig(
    val spaceUrl: String = "",
    val apiKey: String? = null,
    val region: String? = null,
    val endpoint: String? = null,
    val customHeaders: Map<String, String> = emptyMap(),
    val timeout: Long = 30000L,
    val preloadCount: Int = 3
)
