package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.services.tts_service.*
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TTS Engine Factory for v2 architecture
 */
actual object TTSEngineFactory : KoinComponent {
    private val piperSynthesizer: PiperSpeechSynthesizer by inject()
    
    // Shared audio cache instance (500MB limit by default)
    private val audioCache: TTSAudioCache by lazy {
        val userHome = System.getProperty("user.home")
        val cacheDir = "$userHome/.ireader/cache/tts_audio".toPath()
        TTSAudioCache(
            fileSystem = FileSystem.SYSTEM,
            cacheDir = cacheDir,
            maxCacheSizeMB = 500
        )
    }
    
    actual fun createNativeEngine(): TTSEngine {
        return DesktopPiperTTSEngineV2(piperSynthesizer)
    }
    
    actual fun createGradioEngine(config: GradioConfig): TTSEngine? {
        return if (config.spaceUrl.isNotEmpty() && config.enabled) {
            DesktopGradioTTSEngineV2(config, audioCache)
        } else {
            null
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): TTSAudioCache.CacheStats {
        return audioCache.getStats()
    }
    
    /**
     * Clear all cached audio
     */
    suspend fun clearCache() {
        audioCache.clearAll()
    }
}

/**
 * Desktop Piper TTS Engine wrapper for v2 architecture
 */
class DesktopPiperTTSEngineV2(
    private val synthesizer: PiperSpeechSynthesizer
) : TTSEngine {
    companion object {
        private const val TAG = "DesktopPiperTTSV2"
    }
    
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    private val audioEngine: AudioPlaybackEngine by lazy { AudioPlaybackEngine() }
    
    override val events: Flow<EngineEvent> = _events
    override val name: String = "Piper TTS"
    
    override suspend fun speak(text: String, utteranceId: String) {
        Log.warn { "$TAG: speak($utteranceId)" }
        _events.tryEmit(EngineEvent.Started(utteranceId))
        
        try {
            val result = synthesizer.synthesize(text)
            
            result.onSuccess { audioData ->
                audioEngine.play(audioData)
                _events.tryEmit(EngineEvent.Completed(utteranceId))
            }.onFailure { error ->
                _events.tryEmit(EngineEvent.Error(utteranceId, error.message ?: "Piper synthesis failed"))
            }
        } catch (e: Exception) {
            _events.tryEmit(EngineEvent.Error(utteranceId, e.message ?: "Piper TTS error"))
        }
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        audioEngine.stop()
    }
    
    override fun pause() {
        Log.warn { "$TAG: pause()" }
        audioEngine.pause()
    }
    
    override fun resume() {
        Log.warn { "$TAG: resume()" }
        audioEngine.resume()
    }
    
    override fun setSpeed(speed: Float) = synthesizer.setSpeechRate(speed)
    override fun setPitch(pitch: Float) { /* Pitch not supported by Piper */ }
    override fun isReady() = synthesizer.isInitialized()
    
    override fun release() {
        Log.warn { "$TAG: release()" }
        stop()
    }
}

/**
 * Desktop Gradio TTS Engine wrapper for v2 architecture
 * 
 * Features:
 * - Checks disk cache before making web requests
 * - Automatically caches generated audio
 * - Size-limited cache with LRU eviction
 */
class DesktopGradioTTSEngineV2(
    private val config: GradioConfig,
    private val audioCache: TTSAudioCache? = null
) : TTSEngine {
    companion object {
        private const val TAG = "DesktopGradioTTSV2"
    }
    
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    private val httpClient = io.ktor.client.HttpClient()
    private val audioPlayer = DesktopGradioAudioPlayer()
    
    // Use original config if available, otherwise create a basic one
    private val legacyConfig: GradioTTSConfig = config.originalConfig ?: GradioTTSConfig(
        id = config.id,
        name = config.name,
        spaceUrl = config.spaceUrl,
        apiName = config.apiName,
        enabled = config.enabled
    )
    
    init {
        Log.warn { "$TAG: Created with config - id=${legacyConfig.id}, spaceUrl=${legacyConfig.spaceUrl}, apiName=${legacyConfig.apiName}, apiType=${legacyConfig.apiType}, params=${legacyConfig.parameters.size}" }
    }
    
    private val engine = GenericGradioTTSEngine(legacyConfig, httpClient, audioPlayer)
    
    override val events: Flow<EngineEvent> = _events
    override val name: String = config.name
    
    init {
        engine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {
                Log.warn { "$TAG: onStart($utteranceId)" }
                _events.tryEmit(EngineEvent.Started(utteranceId))
            }
            
            override fun onDone(utteranceId: String) {
                Log.warn { "$TAG: onDone($utteranceId)" }
                _events.tryEmit(EngineEvent.Completed(utteranceId))
            }
            
            override fun onError(utteranceId: String, error: String) {
                Log.warn { "$TAG: onError($utteranceId, $error)" }
                _events.tryEmit(EngineEvent.Error(utteranceId, error))
            }
            
            override fun onReady() {
                Log.warn { "$TAG: onReady()" }
                _events.tryEmit(EngineEvent.Ready)
            }
        })
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        Log.warn { "$TAG: speak($utteranceId) - text length=${text.length}" }
        
        // Check disk cache first
        if (audioCache != null) {
            val cachedAudio = audioCache.get(text, config.id)
            if (cachedAudio != null) {
                Log.warn { "$TAG: Playing from CACHE (${cachedAudio.size} bytes)" }
                _events.tryEmit(EngineEvent.Started(utteranceId))
                audioPlayer.play(cachedAudio) {
                    _events.tryEmit(EngineEvent.Completed(utteranceId))
                }
                return
            }
            Log.warn { "$TAG: Cache MISS, fetching from Gradio..." }
        }
        
        // Not in cache - fetch from Gradio, cache, then play
        if (audioCache != null) {
            val audioData = engine.generateAudioBytes(text)
            if (audioData != null) {
                audioCache.put(text, config.id, audioData)
                Log.warn { "$TAG: Cached new audio (${audioData.size} bytes)" }
                
                _events.tryEmit(EngineEvent.Started(utteranceId))
                audioPlayer.play(audioData) {
                    _events.tryEmit(EngineEvent.Completed(utteranceId))
                }
            } else {
                Log.error { "$TAG: Failed to generate audio from Gradio" }
                _events.tryEmit(EngineEvent.Error(utteranceId, "Failed to generate audio"))
            }
        } else {
            // No cache, use engine directly
            engine.speak(text, utteranceId)
        }
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        engine.stop()
    }
    
    override fun pause() {
        Log.warn { "$TAG: pause()" }
        engine.pause()
    }
    
    override fun resume() {
        Log.warn { "$TAG: resume()" }
        engine.resume()
    }
    
    override fun setSpeed(speed: Float) = engine.setSpeed(speed)
    override fun setPitch(pitch: Float) = engine.setPitch(pitch)
    override fun isReady() = engine.isReady()
    
    override fun release() {
        Log.warn { "$TAG: release()" }
        engine.cleanup()
        httpClient.close()
    }
    
    /**
     * Generate audio data for text (for caching/download)
     * Checks cache first, then fetches from Gradio if needed.
     */
    override suspend fun generateAudioForText(text: String): ByteArray? {
        Log.warn { "$TAG: generateAudioForText(${text.take(50)}...)" }
        
        // Check cache first
        if (audioCache != null) {
            val cached = audioCache.get(text, config.id)
            if (cached != null) {
                Log.warn { "$TAG: generateAudioForText - Cache HIT" }
                return cached
            }
        }
        
        // Fetch from Gradio
        val audioData = engine.generateAudioBytes(text)
        
        // Cache the result
        if (audioData != null && audioCache != null) {
            audioCache.put(text, config.id, audioData)
            Log.warn { "$TAG: generateAudioForText - Cached ${audioData.size} bytes" }
        }
        
        return audioData
    }
}
