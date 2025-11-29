package ireader.domain.services.tts_service

import android.content.Context
import io.ktor.client.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of TTS Engine Factory
 */
actual object TTSEngineFactory : KoinComponent {
    private val context: Context by inject()
    private val httpClient: HttpClient by inject()
    
    actual fun createNativeEngine(): TTSEngine {
        return AndroidNativeTTSEngine(context)
    }
    
    actual fun createCoquiEngine(spaceUrl: String, apiKey: String?): TTSEngine? {
        return if (spaceUrl.isNotEmpty()) {
            AndroidCoquiTTSEngine(context, spaceUrl, apiKey)
        } else {
            null
        }
    }
    
    /**
     * Create a generic Gradio TTS engine from configuration
     */
    actual fun createGradioEngine(config: GradioTTSConfig): TTSEngine? {
        return if (config.spaceUrl.isNotEmpty() && config.enabled) {
            AndroidGradioTTSEngine(context, httpClient, config)
        } else {
            null
        }
    }
    
    actual fun getAvailableEngines(): List<String> {
        return buildList {
            add("Native Android TTS")
            add("Gradio TTS (Online)")
            // Check if Coqui is configured
            // Could add more engines here
        }
    }
}

/**
 * Adapter to wrap NativeTTSPlayer as TTSEngine
 */
class AndroidNativeTTSEngine(context: Context) : TTSEngine {
    private val player = NativeTTSPlayer(context)
    
    override suspend fun speak(text: String, utteranceId: String) {
        player.speak(text, utteranceId)
    }
    
    override fun stop() = player.stop()
    override fun pause() = player.pause()
    override fun resume() = player.resume()
    override fun setSpeed(speed: Float) = player.setSpeed(speed)
    override fun setPitch(pitch: Float) = player.setPitch(pitch)
    override fun isReady() = player.isReady()
    override fun cleanup() = player.cleanup()
    override fun getEngineName() = "Native Android TTS"
    
    override fun setCallback(callback: TTSEngineCallback) {
        player.setCallback(object : TTSCallback {
            override fun onStart(utteranceId: String) = callback.onStart(utteranceId)
            override fun onDone(utteranceId: String) = callback.onDone(utteranceId)
            override fun onError(utteranceId: String, error: String) = callback.onError(utteranceId, error)
        })
    }
}

/**
 * Adapter to wrap CoquiTTSPlayer as TTSEngine with caching support
 */
class AndroidCoquiTTSEngine(
    context: Context,
    spaceUrl: String,
    apiKey: String?
) : TTSEngine {
    private val player = CoquiTTSPlayer(context, spaceUrl, apiKey)
    
    override suspend fun speak(text: String, utteranceId: String) {
        player.speak(text, utteranceId)
    }
    
    override fun stop() = player.stop()
    override fun pause() = player.pause()
    override fun resume() = player.resume()
    override fun setSpeed(speed: Float) = player.setSpeed(speed)
    override fun setPitch(pitch: Float) = player.setPitch(pitch)
    override fun isReady() = player.isReady()
    override fun cleanup() = player.cleanup()
    override fun getEngineName() = "Coqui TTS"
    
    override fun setCallback(callback: TTSEngineCallback) {
        player.setCallback(object : TTSCallback {
            override fun onStart(utteranceId: String) = callback.onStart(utteranceId)
            override fun onDone(utteranceId: String) = callback.onDone(utteranceId)
            override fun onError(utteranceId: String, error: String) = callback.onError(utteranceId, error)
        })
    }
    
    // Expose caching for Coqui
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) {
        player.precacheParagraphs(paragraphs)
    }
    
    fun getCacheStatus(utteranceId: String) = player.getCacheStatus(utteranceId)
}


/**
 * Android adapter for GenericGradioTTSEngine
 * Wraps the common GenericGradioTTSEngine with Android-specific audio player
 */
class AndroidGradioTTSEngine(
    context: Context,
    httpClient: HttpClient,
    config: GradioTTSConfig
) : TTSEngine {
    
    private val audioPlayer = AndroidCoquiAudioPlayer(context)
    private val engine = GenericGradioTTSEngine(
        config = config,
        httpClient = httpClient,
        audioPlayer = audioPlayer
    )
    
    override suspend fun speak(text: String, utteranceId: String) {
        engine.speak(text, utteranceId)
    }
    
    override fun stop() = engine.stop()
    override fun pause() = engine.pause()
    override fun resume() = engine.resume()
    override fun setSpeed(speed: Float) = engine.setSpeed(speed)
    override fun setPitch(pitch: Float) = engine.setPitch(pitch)
    override fun isReady() = engine.isReady()
    override fun cleanup() = engine.cleanup()
    override fun getEngineName() = engine.getEngineName()
    
    override fun setCallback(callback: TTSEngineCallback) {
        engine.setCallback(callback)
    }
    
    /**
     * Pre-cache paragraphs for smoother playback
     */
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) {
        engine.precacheParagraphs(paragraphs)
    }
    
    /**
     * Get cache status for a paragraph
     */
    fun getCacheStatus(utteranceId: String) = engine.getCacheStatus(utteranceId)
    
    /**
     * Get the underlying configuration
     */
    fun getConfig() = engine.getConfig()
}
