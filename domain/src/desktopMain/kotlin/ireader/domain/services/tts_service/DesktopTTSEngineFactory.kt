package ireader.domain.services.tts_service

import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.kokoro.KokoroTTSAdapter
import ireader.domain.services.tts_service.kokoro.KokoroTTSEngine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TTS Engine Factory
 * Uses Piper and Kokoro TTS engines
 */
actual object TTSEngineFactory : KoinComponent {
    private val piperSynthesizer: PiperSpeechSynthesizer by inject()
    private val kokoroEngine: KokoroTTSEngine by inject()
    
    actual fun createNativeEngine(): TTSEngine {
        // Default to Piper as the primary engine
        return DesktopPiperTTSEngine(piperSynthesizer)
    }
    
    /**
     * Create a Gradio TTS engine from configuration.
     * 
     * For Coqui TTS, use: GradioTTSPresets.COQUI_IREADER
     */
    actual fun createGradioEngine(config: GradioTTSConfig): TTSEngine? {
        return if (config.spaceUrl.isNotEmpty() && config.enabled) {
            DesktopGradioTTSEngine(config)
        } else {
            null
        }
    }
    
    actual fun getAvailableEngines(): List<String> {
        return listOf("Piper TTS", "Kokoro TTS", "Gradio TTS (Online)")
    }
    
    /**
     * Create specific engine by name
     */
    fun createEngine(engineName: String): TTSEngine? {
        return when (engineName.lowercase()) {
            "piper", "piper tts" -> DesktopPiperTTSEngine(piperSynthesizer)
            "kokoro", "kokoro tts" -> DesktopKokoroTTSEngine(KokoroTTSAdapter(kokoroEngine))
            else -> null
        }
    }
}

/**
 * Desktop-specific engine creation
 */
actual object DesktopTTSEngines : KoinComponent {
    private val piperSynthesizer: PiperSpeechSynthesizer by inject()
    
    actual fun createPiperEngine(): TTSEngine? {
        return try {
            DesktopPiperTTSEngine(piperSynthesizer)
        } catch (e: Exception) {
            ireader.core.log.Log.error { "Failed to create Piper engine: ${e.message}" }
            null
        }
    }
    
    actual fun createKokoroEngine(): TTSEngine? {
        return try {
            // Kokoro requires separate installation
            val kokoroEngine = KokoroTTSEngine()
            DesktopKokoroTTSEngine(KokoroTTSAdapter(kokoroEngine))
        } catch (e: Exception) {
            ireader.core.log.Log.error { "Failed to create Kokoro engine: ${e.message}" }
            null
        }
    }
    
    actual fun createMayaEngine(): TTSEngine? {
        // Maya TTS has been removed
        return null
    }
}

/**
 * Desktop Piper TTS Engine with Audio Playback
 */
private class DesktopPiperTTSEngine(
    private val synthesizer: PiperSpeechSynthesizer
) : TTSEngine {
    private var callback: TTSEngineCallback? = null
    private val audioEngine: ireader.domain.services.tts_service.piper.AudioPlaybackEngine by lazy {
        ireader.domain.services.tts_service.piper.AudioPlaybackEngine()
    }
    private var currentJob: kotlinx.coroutines.Job? = null
    
    override suspend fun speak(text: String, utteranceId: String) {
        callback?.onStart(utteranceId)
        try {
            // Synthesize audio
            val result = synthesizer.synthesize(text)
            
            result.onSuccess { audioData ->
                // Play audio
                audioEngine.play(audioData)
                callback?.onDone(utteranceId)
            }.onFailure { error ->
                callback?.onError(utteranceId, error.message ?: "Piper synthesis failed")
            }
        } catch (e: Exception) {
            callback?.onError(utteranceId, e.message ?: "Piper TTS error")
        }
    }
    
    override fun stop() {
        currentJob?.cancel()
        audioEngine.stop()
    }
    
    override fun pause() {
        audioEngine.pause()
    }
    
    override fun resume() {
        audioEngine.resume()
    }
    
    override fun setSpeed(speed: Float) {
        synthesizer.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        // Pitch not supported by Piper ONNX models
    }
    
    override fun isReady() = synthesizer.isInitialized()
    
    override fun cleanup() {
        stop()
        // AudioPlaybackEngine cleanup is handled by stop()
    }
    
    override fun getEngineName() = "Piper TTS"
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
}

/**
 * Desktop Kokoro TTS Engine
 */
private class DesktopKokoroTTSEngine(
    private val adapter: KokoroTTSAdapter
) : TTSEngine {
    private var callback: TTSEngineCallback? = null
    
    override suspend fun speak(text: String, utteranceId: String) {
        callback?.onStart(utteranceId)
        try {
            // Kokoro synthesis - simplified for now
            callback?.onDone(utteranceId)
        } catch (e: Exception) {
            callback?.onError(utteranceId, e.message ?: "Kokoro TTS error")
        }
    }
    
    override fun stop() {}
    override fun pause() {}
    override fun resume() {}
    override fun setSpeed(speed: Float) {}
    override fun setPitch(pitch: Float) {}
    override fun isReady() = true
    override fun cleanup() {
        // Cleanup Kokoro resources
    }
    override fun getEngineName() = "Kokoro TTS"
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
}

/**
 * Desktop Gradio TTS Engine (HTTP-based)
 * Uses the common GenericGradioTTSEngine with Desktop-specific audio player
 */
private class DesktopGradioTTSEngine(
    config: GradioTTSConfig
) : TTSEngine {
    private val httpClient = io.ktor.client.HttpClient()
    private val audioPlayer = DesktopGradioAudioPlayer()
    private val engine = GenericGradioTTSEngine(config, httpClient, audioPlayer)
    
    override suspend fun speak(text: String, utteranceId: String) = engine.speak(text, utteranceId)
    override fun stop() = engine.stop()
    override fun pause() = engine.pause()
    override fun resume() = engine.resume()
    override fun setSpeed(speed: Float) = engine.setSpeed(speed)
    override fun setPitch(pitch: Float) = engine.setPitch(pitch)
    override fun isReady() = engine.isReady()
    override fun cleanup() {
        engine.cleanup()
        httpClient.close()
    }
    override fun getEngineName() = engine.getEngineName()
    override fun setCallback(callback: TTSEngineCallback) = engine.setCallback(callback)
    
    // Expose caching for pre-fetching
    fun precacheParagraphs(paragraphs: List<Pair<String, String>>) = engine.precacheParagraphs(paragraphs)
    fun getCacheStatus(utteranceId: String) = engine.getCacheStatus(utteranceId)
    fun getConfig() = engine.getConfig()
}
