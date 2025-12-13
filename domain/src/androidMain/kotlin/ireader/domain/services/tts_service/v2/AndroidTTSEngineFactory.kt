package ireader.domain.services.tts_service.v2

import android.content.Context
import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.domain.services.tts_service.AndroidGradioAudioPlayer
import ireader.domain.services.tts_service.GenericGradioTTSEngine
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.NativeTTSPlayer
import ireader.domain.services.tts_service.TTSCallback
import ireader.domain.services.tts_service.TTSEngineCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of TTS Engine Factory for v2 architecture
 */
actual object TTSEngineFactory : KoinComponent {
    private val context: Context by inject()
    private val httpClient: HttpClient by inject()
    
    // Shared audio cache instance (500MB limit by default)
    private val audioCache: TTSAudioCache by lazy {
        val cacheDir = context.cacheDir.absolutePath.toPath() / "tts_audio_cache"
        TTSAudioCache(
            fileSystem = FileSystem.SYSTEM,
            cacheDir = cacheDir,
            maxCacheSizeMB = 500
        )
    }
    
    actual fun createNativeEngine(): TTSEngine {
        return AndroidNativeTTSEngineV2(context)
    }
    
    actual fun createGradioEngine(config: GradioConfig): TTSEngine? {
        return if (config.spaceUrl.isNotEmpty() && config.enabled) {
            AndroidGradioTTSEngineV2(context, httpClient, config, audioCache)
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
 * Android Native TTS Engine wrapper for v2 architecture
 * Wraps the existing NativeTTSPlayer and converts callbacks to Flow events
 */
class AndroidNativeTTSEngineV2(context: Context) : TTSEngine {
    companion object {
        private const val TAG = "AndroidNativeTTSV2"
    }
    
    private val player = NativeTTSPlayer(context)
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    
    override val events: Flow<EngineEvent> = _events
    override val name: String = "Native Android TTS"
    
    init {
        player.setCallback(object : TTSCallback {
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
        Log.warn { "$TAG: speak($utteranceId)" }
        player.speak(text, utteranceId)
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        player.stop()
    }
    
    override fun pause() {
        Log.warn { "$TAG: pause()" }
        player.pause()
    }
    
    override fun resume() {
        Log.warn { "$TAG: resume()" }
        player.resume()
    }
    
    override fun setSpeed(speed: Float) = player.setSpeed(speed)
    override fun setPitch(pitch: Float) = player.setPitch(pitch)
    override fun isReady() = player.isReady()
    
    override fun release() {
        Log.warn { "$TAG: release()" }
        player.cleanup()
    }
}


/**
 * Android Gradio TTS Engine wrapper for v2 architecture
 * 
 * Features:
 * - Checks disk cache before making web requests
 * - Automatically caches generated audio
 * - Size-limited cache with LRU eviction
 */
class AndroidGradioTTSEngineV2(
    private val context: Context,
    private val httpClient: HttpClient,
    private val config: GradioConfig,
    private val audioCache: TTSAudioCache? = null
) : TTSEngine {
    companion object {
        private const val TAG = "AndroidGradioTTSV2"
    }
    
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    
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
    
    private val audioPlayer = AndroidGradioAudioPlayer(context)
    private val engine = GenericGradioTTSEngine(
        config = legacyConfig,
        httpClient = httpClient,
        audioPlayer = audioPlayer
    )
    
    // Track current text for caching
    private var currentText: String? = null
    
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
        currentText = text
        
        // Check disk cache first
        if (audioCache != null) {
            val cachedAudio = audioCache.get(text, config.id)
            if (cachedAudio != null) {
                Log.warn { "$TAG: Playing from CACHE (${cachedAudio.size} bytes)" }
                // Emit started event
                _events.tryEmit(EngineEvent.Started(utteranceId))
                // Play cached audio directly
                audioPlayer.play(cachedAudio) {
                    // Emit completed event when done
                    _events.tryEmit(EngineEvent.Completed(utteranceId))
                }
                return
            }
            Log.warn { "$TAG: Cache MISS, fetching from Gradio..." }
        }
        
        // Not in cache - fetch from Gradio, cache, then play
        if (audioCache != null) {
            // Fetch audio bytes first
            val audioData = engine.generateAudioBytes(text)
            if (audioData != null) {
                // Cache the audio
                audioCache.put(text, config.id, audioData)
                Log.warn { "$TAG: Cached new audio (${audioData.size} bytes)" }
                
                // Play the audio
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
    }
    
    /**
     * Generate audio data for text (for caching/download)
     * Checks cache first, then fetches from Gradio if needed.
     * Automatically caches the result.
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
     * Add audio data to the disk cache
     */
    suspend fun addToCache(text: String, audioData: ByteArray) {
        audioCache?.put(text, config.id, audioData)
    }
    
    /**
     * Check if audio is in the disk cache
     */
    suspend fun isInCache(text: String): Boolean {
        return audioCache?.isCached(text, config.id) == true
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): TTSAudioCache.CacheStats? {
        return audioCache?.getStats()
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
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
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
