package ireader.domain.services.tts_service.player

import ireader.core.log.Log
import ireader.domain.services.tts_service.GradioTTSConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gradio TTS Player - A complete media player for Gradio-based TTS.
 * 
 * This player follows clean architecture principles and provides:
 * - Queue-based paragraph reading (one at a time, in order)
 * - Pre-caches next N paragraphs for smooth playback
 * - Handles play/pause/stop/skip controls
 * - Handles chapter changes (pause, restart from first)
 * - Handles settings changes (pause, apply, restart from current)
 * - Proper cleanup on stop/close
 * - Notification integration support via callbacks
 * 
 * Thread Safety:
 * - All state mutations go through a command channel
 * - Audio cache is protected by mutex
 * - Coroutines are properly scoped and cancelled
 */
class GradioTTSPlayer(
    private val audioGenerator: GradioAudioGenerator,
    private val audioPlayer: GradioAudioPlayback,
    private val config: GradioTTSConfig,
    private val prefetchCount: Int = 3,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CachingTTSPlayerState {
    
    companion object {
        private const val TAG = "GradioTTSPlayer"
        private const val MAX_CACHE_SIZE = 20
    }
    
    // ==================== State Flows ====================
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentParagraph = MutableStateFlow(0)
    override val currentParagraph: StateFlow<Int> = _currentParagraph.asStateFlow()
    
    private val _totalParagraphs = MutableStateFlow(0)
    override val totalParagraphs: StateFlow<Int> = _totalParagraphs.asStateFlow()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs.asStateFlow()
    
    private val _speed = MutableStateFlow(config.defaultSpeed)
    override val speed: StateFlow<Float> = _speed.asStateFlow()
    
    private val _pitch = MutableStateFlow(1.0f)
    override val pitch: StateFlow<Float> = _pitch.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _hasContent = MutableStateFlow(false)
    override val hasContent: StateFlow<Boolean> = _hasContent.asStateFlow()
    
    private val _engineName = MutableStateFlow(config.name)
    override val engineName: StateFlow<String> = _engineName.asStateFlow()
    
    // ==================== Events ====================
    
    private val _events = MutableSharedFlow<GradioTTSPlayerEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<GradioTTSPlayerEvent> = _events.asSharedFlow()
    
    // ==================== Internal State ====================
    
    private var paragraphs: List<String> = emptyList()
    private val audioCache = mutableMapOf<Int, ByteArray>()
    private val loadingSet = mutableSetOf<Int>()
    private val cacheMutex = Mutex()
    
    // Coroutine management
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val commandChannel = Channel<GradioTTSPlayerCommand>(Channel.UNLIMITED)
    private var commandProcessorJob: Job? = null
    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
    
    init {
        startCommandProcessor()
    }
    
    // ==================== Public API ====================
    
    /**
     * Set content to be read.
     * This will stop current playback and clear the cache.
     */
    fun setContent(content: List<String>, startIndex: Int = 0) {
        sendCommand(GradioTTSPlayerCommand.SetContent(content, startIndex))
    }
    
    /**
     * Start or resume playback.
     */
    fun play() {
        sendCommand(GradioTTSPlayerCommand.Play)
    }
    
    /**
     * Pause playback.
     */
    fun pause() {
        sendCommand(GradioTTSPlayerCommand.Pause)
    }
    
    /**
     * Stop playback and reset to beginning.
     */
    fun stop() {
        sendCommand(GradioTTSPlayerCommand.Stop)
    }
    
    /**
     * Skip to next paragraph.
     */
    fun next() {
        sendCommand(GradioTTSPlayerCommand.Next)
    }
    
    /**
     * Skip to previous paragraph.
     */
    fun previous() {
        sendCommand(GradioTTSPlayerCommand.Previous)
    }
    
    /**
     * Jump to specific paragraph.
     */
    fun jumpTo(index: Int) {
        sendCommand(GradioTTSPlayerCommand.JumpTo(index))
    }
    
    /**
     * Set speech speed. Will pause, clear cache, and restart if playing.
     */
    fun setSpeed(newSpeed: Float) {
        sendCommand(GradioTTSPlayerCommand.SetSpeed(newSpeed))
    }
    
    /**
     * Set speech pitch. Will pause, clear cache, and restart if playing.
     */
    fun setPitch(newPitch: Float) {
        sendCommand(GradioTTSPlayerCommand.SetPitch(newPitch))
    }
    
    /**
     * Clear audio cache.
     */
    fun clearCache() {
        sendCommand(GradioTTSPlayerCommand.ClearCache)
    }
    
    /**
     * Release all resources. Player cannot be used after this.
     */
    fun release() {
        sendCommand(GradioTTSPlayerCommand.Release)
    }
    
    /**
     * Get current state snapshot.
     */
    fun getStateSnapshot(): GradioTTSPlayerState {
        return GradioTTSPlayerState(
            isPlaying = _isPlaying.value,
            isPaused = _isPaused.value,
            isLoading = _isLoading.value,
            currentParagraph = _currentParagraph.value,
            totalParagraphs = _totalParagraphs.value,
            cachedParagraphs = _cachedParagraphs.value,
            loadingParagraphs = _loadingParagraphs.value,
            speed = _speed.value,
            pitch = _pitch.value,
            error = _error.value,
            hasContent = _hasContent.value,
            engineName = _engineName.value
        )
    }
    
    // ==================== Command Processing ====================
    
    private fun sendCommand(command: GradioTTSPlayerCommand) {
        scope.launch {
            commandChannel.send(command)
        }
    }
    
    private fun startCommandProcessor() {
        commandProcessorJob = scope.launch {
            for (command in commandChannel) {
                try {
                    processCommand(command)
                } catch (e: CancellationException) {
                    Log.info { "$TAG: Command processor cancelled" }
                    break
                } catch (e: Exception) {
                    Log.error { "$TAG: Error processing command $command: ${e.message}" }
                    _error.value = e.message
                    emitEvent(GradioTTSPlayerEvent.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: GradioTTSPlayerCommand) {
        Log.debug { "$TAG: Processing command: $command" }
        
        when (command) {
            is GradioTTSPlayerCommand.SetContent -> handleSetContent(command.paragraphs, command.startIndex)
            is GradioTTSPlayerCommand.Play -> handlePlay()
            is GradioTTSPlayerCommand.Pause -> handlePause()
            is GradioTTSPlayerCommand.Stop -> handleStop()
            is GradioTTSPlayerCommand.Next -> handleNext()
            is GradioTTSPlayerCommand.Previous -> handlePrevious()
            is GradioTTSPlayerCommand.JumpTo -> handleJumpTo(command.index)
            is GradioTTSPlayerCommand.SetSpeed -> handleSetSpeed(command.speed)
            is GradioTTSPlayerCommand.SetPitch -> handleSetPitch(command.pitch)
            is GradioTTSPlayerCommand.ClearCache -> handleClearCache()
            is GradioTTSPlayerCommand.Release -> handleRelease()
        }
    }
    
    // ==================== Command Handlers ====================
    
    private suspend fun handleSetContent(content: List<String>, startIndex: Int) {
        Log.info { "$TAG: Setting content: ${content.size} paragraphs, starting at $startIndex" }
        
        // Stop current playback
        stopPlayback()
        
        // Clear cache (new content)
        clearCacheInternal()
        
        // Set new content
        paragraphs = content
        val validIndex = startIndex.coerceIn(0, (content.size - 1).coerceAtLeast(0))
        
        _currentParagraph.value = validIndex
        _totalParagraphs.value = content.size
        _hasContent.value = content.isNotEmpty()
        _isPlaying.value = false
        _isPaused.value = false
        _error.value = null
        
        emitEvent(GradioTTSPlayerEvent.ContentLoaded(content.size))
    }
    
    private suspend fun handlePlay() {
        if (paragraphs.isEmpty()) {
            Log.warn { "$TAG: Cannot play - no content" }
            _error.value = "No content to play"
            return
        }
        
        if (_isPlaying.value && !_isPaused.value) {
            Log.debug { "$TAG: Already playing" }
            return
        }
        
        // If paused, resume
        if (_isPaused.value) {
            Log.info { "$TAG: Resuming playback" }
            _isPaused.value = false
            audioPlayer.resume()
            emitEvent(GradioTTSPlayerEvent.PlaybackResumed)
            return
        }
        
        Log.info { "$TAG: Starting playback from paragraph ${_currentParagraph.value}" }
        
        _isPlaying.value = true
        _isPaused.value = false
        _error.value = null
        
        emitEvent(GradioTTSPlayerEvent.PlaybackStarted)
        
        // Start prefetching
        startPrefetch()
        
        // Start playback loop
        startPlaybackLoop()
    }
    
    private suspend fun handlePause() {
        if (!_isPlaying.value) {
            Log.debug { "$TAG: Not playing, cannot pause" }
            return
        }
        
        Log.info { "$TAG: Pausing playback" }
        
        _isPaused.value = true
        audioPlayer.pause()
        
        emitEvent(GradioTTSPlayerEvent.PlaybackPaused)
    }
    
    private suspend fun handleStop() {
        Log.info { "$TAG: Stopping playback" }
        
        stopPlayback()
        
        // Reset to beginning
        _currentParagraph.value = 0
        _isPlaying.value = false
        _isPaused.value = false
        _error.value = null
        
        emitEvent(GradioTTSPlayerEvent.PlaybackStopped)
    }
    
    private suspend fun handleNext() {
        val current = _currentParagraph.value
        val total = _totalParagraphs.value
        
        if (current >= total - 1) {
            Log.debug { "$TAG: Already at last paragraph" }
            return
        }
        
        Log.info { "$TAG: Skipping to next paragraph" }
        
        // Stop current audio
        audioPlayer.stop()
        
        // Move to next
        _currentParagraph.value = current + 1
        
        val text = paragraphs.getOrNull(_currentParagraph.value) ?: ""
        emitEvent(GradioTTSPlayerEvent.ParagraphChanged(_currentParagraph.value, text))
        
        // If playing, restart playback loop
        if (_isPlaying.value && !_isPaused.value) {
            startPrefetch()
            startPlaybackLoop()
        }
    }
    
    private suspend fun handlePrevious() {
        val current = _currentParagraph.value
        
        if (current <= 0) {
            Log.debug { "$TAG: Already at first paragraph" }
            return
        }
        
        Log.info { "$TAG: Skipping to previous paragraph" }
        
        // Stop current audio
        audioPlayer.stop()
        
        // Move to previous
        _currentParagraph.value = current - 1
        
        val text = paragraphs.getOrNull(_currentParagraph.value) ?: ""
        emitEvent(GradioTTSPlayerEvent.ParagraphChanged(_currentParagraph.value, text))
        
        // If playing, restart playback loop
        if (_isPlaying.value && !_isPaused.value) {
            startPrefetch()
            startPlaybackLoop()
        }
    }
    
    private suspend fun handleJumpTo(index: Int) {
        val targetIndex = index.coerceIn(0, (_totalParagraphs.value - 1).coerceAtLeast(0))
        
        if (targetIndex == _currentParagraph.value) {
            Log.debug { "$TAG: Already at paragraph $targetIndex" }
            return
        }
        
        Log.info { "$TAG: Jumping to paragraph $targetIndex" }
        
        // Stop current audio
        audioPlayer.stop()
        
        // Clear cache for paragraphs that are no longer needed
        clearUnneededCache(targetIndex)
        
        // Move to target
        _currentParagraph.value = targetIndex
        
        val text = paragraphs.getOrNull(targetIndex) ?: ""
        emitEvent(GradioTTSPlayerEvent.ParagraphChanged(targetIndex, text))
        
        // Restart prefetch from new position
        startPrefetch()
        
        // If playing, restart playback loop
        if (_isPlaying.value && !_isPaused.value) {
            startPlaybackLoop()
        }
    }
    
    private suspend fun handleSetSpeed(newSpeed: Float) {
        val validSpeed = newSpeed.coerceIn(0.5f, 2.0f)
        
        if (validSpeed == _speed.value) {
            return
        }
        
        Log.info { "$TAG: Setting speed to $validSpeed" }
        
        val wasPlaying = _isPlaying.value && !_isPaused.value
        
        // Stop current playback
        if (wasPlaying) {
            audioPlayer.stop()
        }
        
        // Clear cache (speed affects audio generation)
        clearCacheInternal()
        
        // Apply new speed
        _speed.value = validSpeed
        audioGenerator.setSpeed(validSpeed)
        
        emitEvent(GradioTTSPlayerEvent.SpeedChanged(validSpeed))
        
        // Resume if was playing
        if (wasPlaying) {
            startPrefetch()
            startPlaybackLoop()
        }
    }
    
    private suspend fun handleSetPitch(newPitch: Float) {
        val validPitch = newPitch.coerceIn(0.5f, 2.0f)
        
        if (validPitch == _pitch.value) {
            return
        }
        
        Log.info { "$TAG: Setting pitch to $validPitch" }
        
        val wasPlaying = _isPlaying.value && !_isPaused.value
        
        // Stop current playback
        if (wasPlaying) {
            audioPlayer.stop()
        }
        
        // Clear cache (pitch affects audio generation)
        clearCacheInternal()
        
        // Apply new pitch
        _pitch.value = validPitch
        audioGenerator.setPitch(validPitch)
        
        emitEvent(GradioTTSPlayerEvent.PitchChanged(validPitch))
        
        // Resume if was playing
        if (wasPlaying) {
            startPrefetch()
            startPlaybackLoop()
        }
    }
    
    private suspend fun handleClearCache() {
        Log.info { "$TAG: Clearing cache" }
        clearCacheInternal()
        emitEvent(GradioTTSPlayerEvent.CacheCleared)
    }
    
    private suspend fun handleRelease() {
        Log.info { "$TAG: Releasing player" }
        
        emitEvent(GradioTTSPlayerEvent.Releasing)
        
        // Stop everything
        stopPlayback()
        clearCacheInternal()
        
        // Cancel command processor
        commandProcessorJob?.cancel()
        
        // Release resources
        audioPlayer.release()
        audioGenerator.release()
        
        // Cancel scope
        scope.cancel()
    }
    
    // ==================== Playback Loop ====================
    
    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        
        playbackJob = scope.launch {
            while (isActive && _currentParagraph.value < _totalParagraphs.value) {
                // Wait if paused
                while (_isPaused.value && isActive) {
                    delay(100)
                }
                
                // Check if still active and playing
                if (!isActive || !_isPlaying.value) break
                
                try {
                    val success = playCurrentParagraph()
                    
                    if (!success) {
                        Log.error { "$TAG: Failed to play paragraph ${_currentParagraph.value}" }
                        _error.value = "Failed to play paragraph"
                        emitEvent(GradioTTSPlayerEvent.Error("Failed to play paragraph ${_currentParagraph.value}"))
                        break
                    }
                    
                    // Move to next paragraph
                    val nextIndex = _currentParagraph.value + 1
                    
                    if (nextIndex < _totalParagraphs.value) {
                        _currentParagraph.value = nextIndex
                        
                        val text = paragraphs.getOrNull(nextIndex) ?: ""
                        emitEvent(GradioTTSPlayerEvent.ParagraphChanged(nextIndex, text))
                        
                        // Trigger prefetch for upcoming paragraphs
                        startPrefetch()
                    } else {
                        // Finished all paragraphs
                        break
                    }
                    
                } catch (e: CancellationException) {
                    Log.info { "$TAG: Playback cancelled" }
                    break
                } catch (e: Exception) {
                    Log.error { "$TAG: Playback error: ${e.message}" }
                    _error.value = e.message
                    emitEvent(GradioTTSPlayerEvent.Error(e.message ?: "Unknown error"))
                    break
                }
            }
            
            // Check if we finished all paragraphs
            if (_currentParagraph.value >= _totalParagraphs.value - 1 && _isPlaying.value) {
                Log.info { "$TAG: Chapter finished" }
                _isPlaying.value = false
                _isPaused.value = false
                emitEvent(GradioTTSPlayerEvent.ChapterFinished)
            }
        }
    }
    
    private suspend fun playCurrentParagraph(): Boolean {
        val index = _currentParagraph.value
        val text = paragraphs.getOrNull(index) ?: return false
        
        // Skip blank paragraphs
        if (text.isBlank()) {
            Log.debug { "$TAG: Skipping blank paragraph $index" }
            return true
        }
        
        Log.info { "$TAG: Playing paragraph $index (${text.take(50)}...)" }
        _isLoading.value = true
        
        try {
            // Check cache first
            var audioData = cacheMutex.withLock { audioCache[index] }
            
            if (audioData == null) {
                // Generate audio
                Log.info { "$TAG: Generating audio for paragraph $index" }
                audioData = audioGenerator.generateAudio(text)
                
                if (audioData != null) {
                    // Cache it
                    cacheAudio(index, audioData)
                }
            } else {
                Log.info { "$TAG: Using cached audio for paragraph $index" }
            }
            
            _isLoading.value = false
            
            if (audioData == null) {
                return false
            }
            
            // Play and wait for completion
            return audioPlayer.playAndWait(audioData)
            
        } catch (e: Exception) {
            Log.error { "$TAG: Error playing paragraph $index: ${e.message}" }
            _isLoading.value = false
            throw e
        }
    }
    
    // ==================== Prefetching ====================
    
    private fun startPrefetch() {
        prefetchJob?.cancel()
        
        prefetchJob = scope.launch {
            val startIndex = _currentParagraph.value + 1
            val endIndex = (startIndex + prefetchCount - 1).coerceAtMost(_totalParagraphs.value - 1)
            
            for (i in startIndex..endIndex) {
                if (!isActive) break
                
                // Check if already cached or loading
                val shouldFetch = cacheMutex.withLock {
                    !audioCache.containsKey(i) && !loadingSet.contains(i)
                }
                
                if (shouldFetch) {
                    prefetchParagraph(i)
                }
            }
        }
    }
    
    private suspend fun prefetchParagraph(index: Int) {
        val text = paragraphs.getOrNull(index) ?: return
        
        if (text.isBlank()) return
        
        // Mark as loading
        cacheMutex.withLock {
            loadingSet.add(index)
        }
        updateLoadingState()
        
        try {
            Log.debug { "$TAG: Prefetching paragraph $index" }
            val audioData = audioGenerator.generateAudio(text)
            
            if (audioData != null) {
                cacheAudio(index, audioData)
                emitEvent(GradioTTSPlayerEvent.ParagraphCached(index))
                Log.debug { "$TAG: Prefetched paragraph $index" }
            }
        } catch (e: CancellationException) {
            Log.debug { "$TAG: Prefetch cancelled for paragraph $index" }
        } catch (e: Exception) {
            Log.warn { "$TAG: Failed to prefetch paragraph $index: ${e.message}" }
        } finally {
            cacheMutex.withLock {
                loadingSet.remove(index)
            }
            updateLoadingState()
        }
    }
    
    // ==================== Cache Management ====================
    
    private suspend fun cacheAudio(index: Int, audioData: ByteArray) {
        cacheMutex.withLock {
            // Remove oldest entries if cache is full
            while (audioCache.size >= MAX_CACHE_SIZE) {
                val oldestKey = audioCache.keys.minOrNull()
                oldestKey?.let { audioCache.remove(it) }
            }
            audioCache[index] = audioData
        }
        updateCacheState()
    }
    
    private suspend fun clearCacheInternal() {
        cacheMutex.withLock {
            audioCache.clear()
            loadingSet.clear()
        }
        updateCacheState()
        updateLoadingState()
    }
    
    private suspend fun clearUnneededCache(newIndex: Int) {
        cacheMutex.withLock {
            // Keep cache for paragraphs around the new index
            val keepRange = (newIndex - 1)..(newIndex + prefetchCount)
            val toRemove = audioCache.keys.filter { it !in keepRange }
            toRemove.forEach { audioCache.remove(it) }
        }
        updateCacheState()
    }
    
    private fun updateCacheState() {
        scope.launch {
            cacheMutex.withLock {
                _cachedParagraphs.value = audioCache.keys.toSet()
            }
        }
    }
    
    private fun updateLoadingState() {
        scope.launch {
            cacheMutex.withLock {
                _loadingParagraphs.value = loadingSet.toSet()
            }
        }
    }
    
    // ==================== Helpers ====================
    
    private fun stopPlayback() {
        playbackJob?.cancel()
        prefetchJob?.cancel()
        audioPlayer.stop()
        _isLoading.value = false
    }
    
    private suspend fun emitEvent(event: GradioTTSPlayerEvent) {
        _events.emit(event)
    }
}

/**
 * Interface for audio generation (Gradio API calls).
 * Platform-specific implementations handle the actual HTTP requests.
 */
interface GradioAudioGenerator {
    /**
     * Generate audio for the given text.
     * @return Audio data as ByteArray, or null if failed
     */
    suspend fun generateAudio(text: String): ByteArray?
    
    /**
     * Set speech speed for generation.
     */
    fun setSpeed(speed: Float)
    
    /**
     * Set speech pitch for generation.
     */
    fun setPitch(pitch: Float)
    
    /**
     * Release resources.
     */
    fun release()
}

/**
 * Interface for audio playback.
 * Platform-specific implementations handle the actual audio output.
 */
interface GradioAudioPlayback {
    /**
     * Play audio and wait for completion.
     * @return true if playback completed successfully
     */
    suspend fun playAndWait(audioData: ByteArray): Boolean
    
    /**
     * Stop current playback.
     */
    fun stop()
    
    /**
     * Pause playback.
     */
    fun pause()
    
    /**
     * Resume playback.
     */
    fun resume()
    
    /**
     * Release resources.
     */
    fun release()
}
