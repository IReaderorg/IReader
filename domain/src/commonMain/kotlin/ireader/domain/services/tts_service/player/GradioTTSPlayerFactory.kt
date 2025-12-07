package ireader.domain.services.tts_service.player

import io.ktor.client.*
import ireader.domain.services.tts_service.GradioAudioPlayer
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSEngineAdapter

/**
 * Factory for creating GradioTTSPlayer instances.
 * 
 * This factory handles the creation of all necessary components:
 * - Audio generator (GradioTTSEngineAdapter)
 * - Audio playback (platform-specific)
 * - The player itself
 * 
 * Usage:
 * ```kotlin
 * val factory = GradioTTSPlayerFactory(httpClient, audioPlayer)
 * val player = factory.create(config)
 * 
 * player.setContent(paragraphs)
 * player.play()
 * ```
 */
class GradioTTSPlayerFactory(
    private val httpClient: HttpClient,
    private val audioPlayer: GradioAudioPlayer
) {
    /**
     * Create a new GradioTTSPlayer with the given configuration.
     * 
     * @param config The Gradio TTS configuration
     * @param prefetchCount Number of paragraphs to prefetch (default: 3)
     * @return A new GradioTTSPlayer instance
     */
    fun create(
        config: GradioTTSConfig,
        prefetchCount: Int = 3
    ): GradioTTSPlayer {
        // Create the adapter that implements both GradioAudioGenerator and GradioAudioPlayback
        val adapter = GradioTTSEngineAdapter(
            config = config,
            httpClient = httpClient,
            audioPlayer = audioPlayer
        )
        
        return GradioTTSPlayer(
            audioGenerator = adapter,
            audioPlayer = adapter,
            config = config,
            prefetchCount = prefetchCount
        )
    }
    
    /**
     * Create a new GradioTTSPlayer with custom generator and playback implementations.
     * 
     * This is useful for testing or when you need custom behavior.
     * 
     * @param config The Gradio TTS configuration
     * @param audioGenerator Custom audio generator
     * @param audioPlayback Custom audio playback
     * @param prefetchCount Number of paragraphs to prefetch (default: 3)
     * @param dispatcher Custom coroutine dispatcher (useful for testing)
     * @return A new GradioTTSPlayer instance
     */
    fun createWithCustomComponents(
        config: GradioTTSConfig,
        audioGenerator: GradioAudioGenerator,
        audioPlayback: GradioAudioPlayback,
        prefetchCount: Int = 3,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default
    ): GradioTTSPlayer {
        return GradioTTSPlayer(
            audioGenerator = audioGenerator,
            audioPlayer = audioPlayback,
            config = config,
            prefetchCount = prefetchCount,
            dispatcher = dispatcher
        )
    }
}

/**
 * Extension function to create a player directly from a config.
 */
fun GradioTTSConfig.createPlayer(
    httpClient: HttpClient,
    audioPlayer: GradioAudioPlayer,
    prefetchCount: Int = 3
): GradioTTSPlayer {
    val factory = GradioTTSPlayerFactory(httpClient, audioPlayer)
    return factory.create(this, prefetchCount)
}
