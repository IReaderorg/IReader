package ireader.domain.services.tts_service

import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.kokoro.KokoroTTSAdapter
import ireader.domain.services.tts_service.kokoro.KokoroTTSEngine
import ireader.domain.services.tts_service.maya.MayaTTSAdapter
import ireader.domain.services.tts_service.maya.MayaTTSEngine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TTS Engine Factory
 * Uses Piper, Kokoro, and Maya TTS engines
 */
actual object TTSEngineFactory : KoinComponent {
    private val piperSynthesizer: PiperSpeechSynthesizer by inject()
    private val kokoroEngine: KokoroTTSEngine by inject()
    private val mayaEngine: MayaTTSEngine by inject()
    
    actual fun createNativeEngine(): TTSEngine {
        // Default to Piper as the primary engine
        return DesktopPiperTTSEngine(piperSynthesizer)
    }
    
    actual fun createCoquiEngine(spaceUrl: String, apiKey: String?): TTSEngine? {
        // Desktop can also use Coqui TTS via HTTP
        return if (spaceUrl.isNotEmpty()) {
            DesktopCoquiTTSEngine(spaceUrl, apiKey)
        } else {
            null
        }
    }
    
    actual fun getAvailableEngines(): List<String> {
        return listOf("Piper TTS", "Kokoro TTS", "Maya TTS", "Coqui TTS")
    }
    
    /**
     * Create specific engine by name
     */
    fun createEngine(engineName: String): TTSEngine? {
        return when (engineName.lowercase()) {
            "piper", "piper tts" -> DesktopPiperTTSEngine(piperSynthesizer)
            "kokoro", "kokoro tts" -> DesktopKokoroTTSEngine(KokoroTTSAdapter(kokoroEngine))
            "maya", "maya tts" -> DesktopMayaTTSEngine(MayaTTSAdapter(mayaEngine))
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
        return try {
            // Maya requires separate installation
            val mayaEngine = MayaTTSEngine()
            DesktopMayaTTSEngine(MayaTTSAdapter(mayaEngine))
        } catch (e: Exception) {
            ireader.core.log.Log.error { "Failed to create Maya engine: ${e.message}" }
            null
        }
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
 * Desktop Maya TTS Engine
 */
private class DesktopMayaTTSEngine(
    private val adapter: MayaTTSAdapter
) : TTSEngine {
    private var callback: TTSEngineCallback? = null
    
    override suspend fun speak(text: String, utteranceId: String) {
        callback?.onStart(utteranceId)
        try {
            // Maya synthesis - simplified for now
            callback?.onDone(utteranceId)
        } catch (e: Exception) {
            callback?.onError(utteranceId, e.message ?: "Maya TTS error")
        }
    }
    
    override fun stop() {}
    override fun pause() {}
    override fun resume() {}
    override fun setSpeed(speed: Float) {}
    override fun setPitch(pitch: Float) {}
    override fun isReady() = true
    override fun cleanup() {
        // Cleanup Maya resources
    }
    override fun getEngineName() = "Maya TTS"
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
}

/**
 * Desktop Coqui TTS Engine (HTTP-based)
 */
private class DesktopCoquiTTSEngine(
    private val spaceUrl: String,
    private val apiKey: String?
) : TTSEngine {
    private var callback: TTSEngineCallback? = null
    
    override suspend fun speak(text: String, utteranceId: String) {
        // TODO: Implement HTTP-based Coqui TTS
        callback?.onStart(utteranceId)
        // Make HTTP request to Coqui service...
        callback?.onDone(utteranceId)
    }
    
    override fun stop() {}
    override fun pause() {}
    override fun resume() {}
    override fun setSpeed(speed: Float) {}
    override fun setPitch(pitch: Float) {}
    override fun isReady() = true
    override fun cleanup() {}
    override fun getEngineName() = "Coqui TTS (HTTP)"
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
}
