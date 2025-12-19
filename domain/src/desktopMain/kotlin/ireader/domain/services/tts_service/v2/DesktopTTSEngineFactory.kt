package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.*
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.piper.PiperModelManager
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TTS Engine Factory for v2 architecture
 */
actual object TTSEngineFactory : KoinComponent {
    private val piperSynthesizer: PiperSpeechSynthesizer by inject()
    private val piperModelManager: PiperModelManager by inject()
    private val appPreferences: AppPreferences by inject()
    
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
        return DesktopPiperTTSEngineV2(piperSynthesizer, piperModelManager, appPreferences)
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
 * 
 * This engine automatically loads the selected voice model on initialization.
 * The speak() method is non-blocking - it starts playback in a background coroutine
 * and returns immediately, allowing other commands to be processed.
 */
class DesktopPiperTTSEngineV2(
    private val synthesizer: PiperSpeechSynthesizer,
    private val modelManager: PiperModelManager,
    private val appPreferences: AppPreferences
) : TTSEngine {
    companion object {
        private const val TAG = "DesktopPiperTTSV2"
    }
    
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    private val audioEngine: AudioPlaybackEngine by lazy { AudioPlaybackEngine() }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitializing = false
    
    // Current playback job - can be cancelled when stop() is called
    private var playbackJob: kotlinx.coroutines.Job? = null
    
    // Track the current utterance to prevent stale completions
    @Volatile
    private var currentUtteranceId: String? = null
    
    // Track if playback was stopped to prevent emitting Completed after stop
    @Volatile
    private var wasStopped = false
    
    override val events: Flow<EngineEvent> = _events
    override val name: String = "Piper TTS"
    
    init {
        // Auto-load the selected voice model on engine creation
        if (!synthesizer.isInitialized()) {
            loadSelectedVoiceModel()
        } else {
            // Already initialized, emit ready event
            _events.tryEmit(EngineEvent.Ready)
        }
    }
    
    /**
     * Load the selected voice model from preferences
     */
    private fun loadSelectedVoiceModel() {
        if (isInitializing) return
        isInitializing = true
        
        scope.launch {
            try {
                val selectedModelId = appPreferences.selectedPiperModel().get()
                Log.warn { "$TAG: Loading voice model: $selectedModelId" }
                
                if (selectedModelId.isEmpty()) {
                    Log.warn { "$TAG: No voice model selected" }
                    isInitializing = false
                    return@launch
                }
                
                val paths = modelManager.getModelPaths(selectedModelId)
                if (paths == null) {
                    Log.warn { "$TAG: Voice model paths not found for: $selectedModelId" }
                    isInitializing = false
                    return@launch
                }
                
                val result = synthesizer.initialize(paths.modelPath, paths.configPath)
                
                result.onSuccess {
                    Log.warn { "$TAG: Voice model loaded successfully" }
                    _events.tryEmit(EngineEvent.Ready)
                }.onFailure { error ->
                    Log.error { "$TAG: Failed to load voice model: ${error.message}" }
                }
                
                isInitializing = false
            } catch (e: Exception) {
                Log.error { "$TAG: Error loading voice model: ${e.message}" }
                isInitializing = false
            }
        }
    }
    
    /**
     * Speak text - NON-BLOCKING.
     * Starts playback in a background coroutine and returns immediately.
     * Completion/error events are emitted via the events flow.
     */
    override suspend fun speak(text: String, utteranceId: String) {
        Log.warn { "$TAG: speak($utteranceId)" }
        
        // Cancel any existing playback
        playbackJob?.cancel()
        audioEngine.stop()
        
        // Track this utterance and reset stopped flag
        currentUtteranceId = utteranceId
        wasStopped = false
        
        // Check if synthesizer is ready
        if (!synthesizer.isInitialized()) {
            Log.warn { "$TAG: Synthesizer not initialized, attempting to load voice model..." }
            loadSelectedVoiceModel()
            // Wait a bit for initialization
            delay(500)
            if (!synthesizer.isInitialized()) {
                _events.tryEmit(EngineEvent.Error(utteranceId, "Piper TTS not initialized. Please select a voice model."))
                return
            }
        }
        
        // Start playback in background coroutine (non-blocking)
        playbackJob = scope.launch {
            try {
                _events.tryEmit(EngineEvent.Started(utteranceId))
                
                val result = synthesizer.synthesize(text)
                
                result.onSuccess { audioData ->
                    // Check if we were stopped before playing
                    if (wasStopped || currentUtteranceId != utteranceId) {
                        Log.warn { "$TAG: speak($utteranceId) cancelled before playback" }
                        return@launch
                    }
                    
                    audioEngine.play(audioData)
                    
                    // Only emit Completed if this utterance is still current and wasn't stopped
                    if (currentUtteranceId == utteranceId && !wasStopped) {
                        Log.warn { "$TAG: speak($utteranceId) completed normally" }
                        _events.tryEmit(EngineEvent.Completed(utteranceId))
                    } else {
                        Log.warn { "$TAG: speak($utteranceId) skipping Completed (stopped=$wasStopped, current=$currentUtteranceId)" }
                    }
                }.onFailure { error ->
                    if (currentUtteranceId == utteranceId && !wasStopped) {
                        _events.tryEmit(EngineEvent.Error(utteranceId, error.message ?: "Piper synthesis failed"))
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.warn { "$TAG: speak($utteranceId) cancelled" }
                // Don't emit any event on cancellation
            } catch (e: Exception) {
                if (currentUtteranceId == utteranceId && !wasStopped) {
                    _events.tryEmit(EngineEvent.Error(utteranceId, e.message ?: "Piper TTS error"))
                }
            }
        }
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        wasStopped = true
        currentUtteranceId = null
        playbackJob?.cancel()
        playbackJob = null
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
        scope.cancel()
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
    
    // Track the current utterance to prevent stale completions
    @Volatile
    private var currentUtteranceId: String? = null
    
    // Track if playback was stopped to prevent emitting Completed after stop
    @Volatile
    private var wasStopped = false
    
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
                if (currentUtteranceId == utteranceId && !wasStopped) {
                    _events.tryEmit(EngineEvent.Started(utteranceId))
                }
            }
            
            override fun onDone(utteranceId: String) {
                Log.warn { "$TAG: onDone($utteranceId)" }
                if (currentUtteranceId == utteranceId && !wasStopped) {
                    _events.tryEmit(EngineEvent.Completed(utteranceId))
                } else {
                    Log.warn { "$TAG: onDone($utteranceId) skipped (stopped=$wasStopped, current=$currentUtteranceId)" }
                }
            }
            
            override fun onError(utteranceId: String, error: String) {
                Log.warn { "$TAG: onError($utteranceId, $error)" }
                if (currentUtteranceId == utteranceId && !wasStopped) {
                    _events.tryEmit(EngineEvent.Error(utteranceId, error))
                }
            }
            
            override fun onReady() {
                Log.warn { "$TAG: onReady()" }
                _events.tryEmit(EngineEvent.Ready)
            }
        })
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        Log.warn { "$TAG: speak($utteranceId) - text length=${text.length}" }
        
        // Track this utterance and reset stopped flag
        currentUtteranceId = utteranceId
        wasStopped = false
        
        // Check disk cache first
        if (audioCache != null) {
            val cachedAudio = audioCache.get(text, config.id)
            if (cachedAudio != null) {
                Log.warn { "$TAG: Playing from CACHE (${cachedAudio.size} bytes)" }
                _events.tryEmit(EngineEvent.Started(utteranceId))
                audioPlayer.play(cachedAudio) {
                    if (currentUtteranceId == utteranceId && !wasStopped) {
                        _events.tryEmit(EngineEvent.Completed(utteranceId))
                    }
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
                    if (currentUtteranceId == utteranceId && !wasStopped) {
                        _events.tryEmit(EngineEvent.Completed(utteranceId))
                    }
                }
            } else {
                Log.error { "$TAG: Failed to generate audio from Gradio" }
                if (!wasStopped) {
                    _events.tryEmit(EngineEvent.Error(utteranceId, "Failed to generate audio"))
                }
            }
        } else {
            // No cache, use engine directly
            engine.speak(text, utteranceId)
        }
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        wasStopped = true
        currentUtteranceId = null
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
        wasStopped = true
        currentUtteranceId = null
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
    
    /**
     * Play cached audio data directly (for offline playback)
     */
    override suspend fun playCachedAudio(audioData: ByteArray, utteranceId: String): Boolean {
        Log.warn { "$TAG: playCachedAudio($utteranceId) - ${audioData.size} bytes" }
        return try {
            _events.tryEmit(EngineEvent.Started(utteranceId))
            audioPlayer.play(audioData) {
                _events.tryEmit(EngineEvent.Completed(utteranceId))
            }
            true
        } catch (e: Exception) {
            Log.error { "$TAG: playCachedAudio error: ${e.message}" }
            _events.tryEmit(EngineEvent.Error(utteranceId, e.message ?: "Playback error"))
            false
        }
    }
    
    /**
     * Clear internal state (queue, cache) when switching chapters
     */
    override fun clearState() {
        Log.warn { "$TAG: clearState()" }
        engine.clearQueue()
        engine.clearCache()
    }
    
    /**
     * Pre-cache upcoming chunks/paragraphs for smoother playback
     * Uses disk cache for persistence across sessions
     */
    override fun precacheNext(items: List<Pair<String, String>>) {
        Log.warn { "$TAG: precacheNext(${items.size} items)" }
        
        // If we have disk cache, use it for prefetching
        if (audioCache != null) {
            GlobalScope.launch(Dispatchers.IO) {
                for ((utteranceId, text) in items) {
                    // Skip if already cached
                    if (audioCache.isCached(text, config.id)) {
                        Log.warn { "$TAG: precacheNext - $utteranceId already cached" }
                        continue
                    }
                    
                    try {
                        Log.warn { "$TAG: precacheNext - fetching $utteranceId" }
                        val audioData = engine.generateAudioBytes(text)
                        if (audioData != null) {
                            audioCache.put(text, config.id, audioData)
                            Log.warn { "$TAG: precacheNext - cached $utteranceId (${audioData.size} bytes)" }
                        }
                    } catch (e: Exception) {
                        Log.warn { "$TAG: precacheNext - failed $utteranceId: ${e.message}" }
                    }
                }
            }
        } else {
            // Fallback to in-memory cache
            engine.precacheParagraphs(items)
        }
    }
    
    /**
     * Check if audio for the given text is cached
     */
    override suspend fun isTextCached(text: String): Boolean {
        return audioCache?.isCached(text, config.id) ?: false
    }
    
    /**
     * Get the set of indices from a list of texts that are cached
     */
    override suspend fun getCachedIndices(texts: List<String>): Set<Int> {
        if (audioCache == null) return emptySet()
        
        return texts.mapIndexedNotNull { index, text ->
            if (audioCache.isCached(text, config.id)) index else null
        }.toSet()
    }
}
