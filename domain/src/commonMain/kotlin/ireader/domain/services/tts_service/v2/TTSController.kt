package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.chapter.ChapterCommand
import ireader.domain.services.chapter.ChapterController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * TTS Controller - The central coordinator for all TTS operations
 * 
 * This is the SINGLE ENTRY POINT for all TTS interactions.
 * 
 * Responsibilities:
 * - Owns and manages the TTSState (single source of truth)
 * - Processes TTSCommands and updates state accordingly
 * - Coordinates between engine, content loading, and navigation
 * - Emits TTSEvents for one-time occurrences
 * 
 * NOT responsible for:
 * - Platform-specific implementations (delegated to TTSEngine)
 * - UI concerns (UI observes state, sends commands)
 * - Persistence (delegated to repositories)
 */
class TTSController(
    private val contentLoader: TTSContentLoader,
    private val nativeEngineFactory: () -> TTSEngine,
    private val gradioEngineFactory: ((GradioConfig) -> TTSEngine?)? = null,
    initialGradioConfig: GradioConfig? = null,
    private val cacheUseCase: TTSCacheUseCase? = null,
    private val chapterController: ChapterController? = null
) {
    // Mutable Gradio config that can be updated at runtime
    private var gradioConfig: GradioConfig? = initialGradioConfig
    companion object {
        private const val TAG = "TTSController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Track the last chapter ID we synced from ChapterController to avoid loops
    private var lastSyncedChapterId: Long? = null
    
    // Flag to indicate the current loadChapter was triggered by ChapterController sync
    // When true, we should NOT notify ChapterController back (would cause infinite loop)
    private var isLoadingFromChapterControllerSync: Boolean = false
    
    init {
        // Subscribe to ChapterController state changes for cross-screen sync
        subscribeToChapterControllerState()
    }
    
    /**
     * Subscribe to ChapterController state changes to keep TTS in sync with Reader screen.
     * When the Reader screen navigates to a different chapter, TTS should update accordingly.
     */
    private fun subscribeToChapterControllerState() {
        chapterController?.let { controller ->
            scope.launch {
                controller.state.collect { chapterState ->
                    val currentChapter = chapterState.currentChapter
                    val ttsState = _state.value
                    
                    // Only sync if:
                    // 1. ChapterController has a current chapter
                    // 2. TTS has content loaded (same book)
                    // 3. The chapter is different from what TTS currently has
                    // 4. We haven't just synced this chapter (avoid loops)
                    if (currentChapter != null && 
                        ttsState.book != null &&
                        ttsState.book?.id == chapterState.book?.id &&
                        currentChapter.id != ttsState.chapter?.id &&
                        currentChapter.id != lastSyncedChapterId) {
                        
                        Log.warn { "$TAG: ChapterController changed to chapter ${currentChapter.id}, syncing TTS" }
                        lastSyncedChapterId = currentChapter.id
                        
                        // Stop current playback before switching
                        val wasPlaying = ttsState.isPlaying
                        if (wasPlaying) {
                            engine?.stop()
                        }
                        
                        // Load the new chapter content for TTS
                        // Mark that this load is from ChapterController sync to prevent notifying back
                        isLoadingFromChapterControllerSync = true
                        try {
                            loadChapter(ttsState.book!!.id, currentChapter.id, chapterState.currentParagraphIndex)
                        } finally {
                            isLoadingFromChapterControllerSync = false
                        }
                    }
                }
            }
        }
    }
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    private val _state = MutableStateFlow(TTSState())
    val state: StateFlow<TTSState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    private val _events = MutableSharedFlow<TTSEvent>()
    val events: SharedFlow<TTSEvent> = _events.asSharedFlow()
    
    // Current engine instance
    private var engine: TTSEngine? = null
    
    // Flag to indicate a play is pending (waiting for engine ready)
    private var pendingPlay: Boolean = false
    
    // Pending chunk mode configuration (applied after content loads)
    private var pendingChunkWordCount: Int? = null
    
    /**
     * Process a command - ALL interactions go through here
     * Commands are processed sequentially using a mutex to prevent race conditions
     */
    fun dispatch(command: TTSCommand) {
        Log.warn { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error { "$TAG: Error processing command: ${e.message}" }
                    handleError(TTSError.SpeechFailed(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: TTSCommand) {
        when (command) {
            is TTSCommand.Initialize -> initialize()
            is TTSCommand.StopAndRelease -> stopAndRelease()
            is TTSCommand.Cleanup -> cleanup()
            
            is TTSCommand.Play -> play()
            is TTSCommand.Pause -> pause()
            is TTSCommand.Stop -> stop()
            is TTSCommand.Resume -> resume()
            
            is TTSCommand.NextParagraph -> nextParagraph()
            is TTSCommand.PreviousParagraph -> previousParagraph()
            is TTSCommand.JumpToParagraph -> jumpToParagraph(command.index)
            is TTSCommand.NextChapter -> nextChapter()
            is TTSCommand.PreviousChapter -> previousChapter()
            
            is TTSCommand.LoadChapter -> loadChapter(command.bookId, command.chapterId, command.startParagraph)
            is TTSCommand.SetContent -> setContent(command.paragraphs)
            
            is TTSCommand.SetTranslatedContent -> setTranslatedContent(command.paragraphs)
            is TTSCommand.ToggleTranslation -> toggleTranslation(command.show)
            is TTSCommand.ToggleBilingualMode -> toggleBilingualMode(command.enabled)
            
            is TTSCommand.SetSentenceHighlight -> setSentenceHighlight(command.enabled)
            is TTSCommand.UpdateParagraphStartTime -> updateParagraphStartTime(command.timeMs)
            is TTSCommand.SetCalibration -> setCalibration(command.wpm, command.isCalibrated)
            
            is TTSCommand.SetSpeed -> setSpeed(command.speed)
            is TTSCommand.SetPitch -> setPitch(command.pitch)
            is TTSCommand.SetAutoNextChapter -> setAutoNextChapter(command.enabled)
            is TTSCommand.SetEngine -> setEngine(command.type)
            is TTSCommand.SetGradioConfig -> setGradioConfig(command.config)
            
            is TTSCommand.EnableChunkMode -> enableChunkMode(command.targetWordCount)
            is TTSCommand.DisableChunkMode -> disableChunkMode()
            is TTSCommand.NextChunk -> nextChunk()
            is TTSCommand.PreviousChunk -> previousChunk()
            is TTSCommand.JumpToChunk -> jumpToChunk(command.index)
        }
    }
    
    // ========== Lifecycle ==========
    
    private fun initialize() {
        Log.warn { "$TAG: initialize()" }
        
        if (engine == null) {
            val currentEngineType = _state.value.engineType
            engine = when (currentEngineType) {
                EngineType.NATIVE -> {
                    Log.warn { "$TAG: Creating native engine" }
                    nativeEngineFactory()
                }
                EngineType.GRADIO -> {
                    Log.warn { "$TAG: Creating Gradio engine" }
                    val config = gradioConfig
                    if (config != null && gradioEngineFactory != null) {
                        gradioEngineFactory.invoke(config) ?: run {
                            Log.warn { "$TAG: Gradio engine creation failed, falling back to native" }
                            _state.update { it.copy(engineType = EngineType.NATIVE) }
                            nativeEngineFactory()
                        }
                    } else {
                        Log.warn { "$TAG: No Gradio config, falling back to native" }
                        _state.update { it.copy(engineType = EngineType.NATIVE) }
                        nativeEngineFactory()
                    }
                }
            }
            observeEngineEvents()
        }
        
        _state.update { it.copy(isEngineReady = engine?.isReady() == true) }
    }
    
    /**
     * Stop playback and release engine, but keep content (book, chapter, paragraphs).
     * User can tap play to reinitialize and resume.
     */
    private suspend fun stopAndRelease() {
        Log.warn { "$TAG: stopAndRelease()" }
        
        pendingPlay = false
        engine?.stop()
        engine?.release()
        engine = null
        
        // Keep content but reset playback state
        _state.update { 
            it.copy(
                playbackState = PlaybackState.STOPPED,
                isEngineReady = false,
                // Reset chunk mode state but keep the content
                currentChunkIndex = 0,
                cachedChunks = emptySet(),
                isUsingCachedAudio = false
            ) 
        }
        
        _events.emit(TTSEvent.PlaybackStopped)
    }
    
    /**
     * Full cleanup - resets everything including content.
     */
    private fun cleanup() {
        Log.warn { "$TAG: cleanup()" }
        
        pendingPlay = false
        engine?.stop()
        engine?.release()
        engine = null
        
        _state.update { TTSState() }
    }
    
    private fun observeEngineEvents() {
        engine?.let { eng ->
            scope.launch {
                eng.events.collect { event ->
                    handleEngineEvent(event)
                }
            }
        }
    }
    
    private suspend fun handleEngineEvent(event: EngineEvent) {
        Log.warn { "$TAG: handleEngineEvent($event)" }
        
        when (event) {
            is EngineEvent.Ready -> {
                _state.update { it.copy(isEngineReady = true) }
                
                // If there's a pending play, execute it now
                if (pendingPlay) {
                    Log.warn { "$TAG: Engine ready, executing pending play" }
                    pendingPlay = false
                    play()
                }
            }
            is EngineEvent.Started -> {
                // Track paragraph start time for sentence highlighting
                val currentTime = ireader.domain.utils.extensions.currentTimeToLong()
                _state.update { 
                    it.copy(
                        playbackState = PlaybackState.PLAYING,
                        paragraphStartTime = currentTime
                    ) 
                }
                _events.emit(TTSEvent.PlaybackStarted)
            }
            is EngineEvent.Completed -> {
                handleParagraphCompleted()
            }
            is EngineEvent.Error -> {
                handleError(TTSError.SpeechFailed(event.message))
            }
        }
    }
    
    // ========== Playback ==========
    
    private suspend fun play() {
        val currentState = _state.value
        
        if (!currentState.hasContent) {
            handleError(TTSError.NoContent)
            return
        }
        
        // Auto-initialize engine if it was released
        if (engine == null) {
            Log.warn { "$TAG: play() - Engine is null, reinitializing..." }
            initialize()
        }
        
        if (engine?.isReady() != true) {
            // Engine is initializing (async), queue the play for when it's ready
            Log.warn { "$TAG: play() - Engine not ready, queuing play" }
            pendingPlay = true
            _state.update { it.copy(playbackState = PlaybackState.LOADING) }
            return
        }
        
        // Use chunk mode if enabled
        if (currentState.chunkModeEnabled && mergeResult != null) {
            playChunk()
            return
        }
        
        _state.update { it.copy(playbackState = PlaybackState.LOADING) }
        
        // Use displayContent which respects showTranslation setting
        val text = currentState.displayContent.getOrNull(currentState.currentParagraphIndex)
        if (text != null) {
            val utteranceId = "p_${currentState.currentParagraphIndex}"
            engine?.speak(text, utteranceId)
        }
    }
    
    private suspend fun pause() {
        pendingPlay = false
        engine?.pause()
        _state.update { it.copy(playbackState = PlaybackState.PAUSED) }
        _events.emit(TTSEvent.PlaybackPaused)
    }
    
    private suspend fun stop() {
        pendingPlay = false
        engine?.stop()
        _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
        _events.emit(TTSEvent.PlaybackStopped)
    }
    
    private suspend fun resume() {
        val currentState = _state.value
        
        if (currentState.isPaused) {
            engine?.resume()
            _state.update { it.copy(playbackState = PlaybackState.PLAYING) }
        } else {
            play()
        }
    }
    
    private suspend fun handleParagraphCompleted() {
        _events.emit(TTSEvent.ParagraphCompleted)
        
        val currentState = _state.value
        
        // Handle chunk mode completion
        if (currentState.chunkModeEnabled && mergeResult != null) {
            handleChunkCompleted()
            return
        }
        
        if (currentState.canGoNext && currentState.isPlaying) {
            // Auto-advance to next paragraph
            _state.update { 
                it.copy(
                    previousParagraphIndex = it.currentParagraphIndex,
                    currentParagraphIndex = it.currentParagraphIndex + 1
                ) 
            }
            play()
        } else if (!currentState.canGoNext) {
            // Chapter finished
            _events.emit(TTSEvent.ChapterCompleted)
            
            if (currentState.autoNextChapter) {
                nextChapter()
            } else {
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        }
    }
    
    private suspend fun handleChunkCompleted() {
        val currentState = _state.value
        val result = mergeResult ?: return
        
        Log.warn { "$TAG: handleChunkCompleted() - chunk ${currentState.currentChunkIndex}/${result.chunks.size}" }
        
        if (currentState.canGoNextChunk) {
            // Auto-advance to next chunk
            val nextChunkIndex = currentState.currentChunkIndex + 1
            val nextChunk = result.chunks.getOrNull(nextChunkIndex)
            
            _state.update {
                it.copy(
                    currentChunkIndex = nextChunkIndex,
                    currentChunkParagraphs = nextChunk?.paragraphIndices ?: emptyList(),
                    currentParagraphIndex = nextChunk?.startParagraph ?: it.currentParagraphIndex
                )
            }
            
            playChunk()
        } else {
            // All chunks finished - chapter complete
            _events.emit(TTSEvent.ChapterCompleted)
            
            if (currentState.autoNextChapter) {
                nextChapter()
            } else {
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        }
    }
    
    // ========== Navigation ==========
    
    private suspend fun nextParagraph() {
        val currentState = _state.value
        if (!currentState.canGoNext) return
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        _state.update { 
            it.copy(
                previousParagraphIndex = it.currentParagraphIndex,
                currentParagraphIndex = it.currentParagraphIndex + 1
            ) 
        }
        
        if (wasPlaying) {
            play()
        }
    }
    
    private suspend fun previousParagraph() {
        val currentState = _state.value
        if (!currentState.canGoPrevious) return
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        _state.update { 
            it.copy(
                previousParagraphIndex = it.currentParagraphIndex,
                currentParagraphIndex = it.currentParagraphIndex - 1
            ) 
        }
        
        if (wasPlaying) {
            play()
        }
    }
    
    private suspend fun jumpToParagraph(index: Int) {
        val currentState = _state.value
        if (index < 0 || index >= currentState.paragraphs.size) return
        
        Log.warn { "$TAG: jumpToParagraph($index) - chunkModeEnabled=${currentState.chunkModeEnabled}" }
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        // If chunk mode is enabled, find the chunk containing this paragraph
        if (currentState.chunkModeEnabled && mergeResult != null) {
            val result = mergeResult!!
            val chunkIndex = result.paragraphToChunkMap[index]
            
            if (chunkIndex != null) {
                val chunk = result.chunks.getOrNull(chunkIndex)
                Log.warn { "$TAG: jumpToParagraph - found chunk $chunkIndex for paragraph $index" }
                
                _state.update {
                    it.copy(
                        previousParagraphIndex = it.currentParagraphIndex,
                        currentParagraphIndex = index,
                        currentChunkIndex = chunkIndex,
                        currentChunkParagraphs = chunk?.paragraphIndices ?: emptyList()
                    )
                }
                
                if (wasPlaying) {
                    playChunk()
                }
                return
            }
        }
        
        // Regular mode (no chunks)
        _state.update { 
            it.copy(
                previousParagraphIndex = it.currentParagraphIndex,
                currentParagraphIndex = index
            ) 
        }
        
        if (wasPlaying) {
            play()
        }
    }
    
    private suspend fun nextChapter() {
        val currentState = _state.value
        val book = currentState.book ?: return
        val chapter = currentState.chapter ?: return
        
        Log.warn { "$TAG: nextChapter() - current chapter: ${chapter.id}" }
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        _state.update { it.copy(playbackState = PlaybackState.LOADING) }
        
        try {
            // Get next chapter ID using contentLoader (TTS-specific content handling)
            val nextChapterId = contentLoader.getNextChapterId(book.id, chapter.id)
            
            if (nextChapterId != null) {
                Log.warn { "$TAG: Loading next chapter: $nextChapterId" }
                
                // Notify ChapterController about navigation (Requirements: 9.3, 9.4)
                // This keeps ChapterController in sync with TTS navigation
                chapterController?.dispatch(ChapterCommand.LoadChapter(nextChapterId, 0))
                
                // Load chapter content for TTS (with paragraph parsing)
                loadChapter(book.id, nextChapterId, 0)
                if (wasPlaying) {
                    play()
                }
            } else {
                Log.warn { "$TAG: No next chapter available" }
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load next chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load next chapter"))
        }
    }
    
    private suspend fun previousChapter() {
        val currentState = _state.value
        val book = currentState.book ?: return
        val chapter = currentState.chapter ?: return
        
        Log.warn { "$TAG: previousChapter() - current chapter: ${chapter.id}" }
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        _state.update { it.copy(playbackState = PlaybackState.LOADING) }
        
        try {
            // Get previous chapter ID using contentLoader (TTS-specific content handling)
            val prevChapterId = contentLoader.getPreviousChapterId(book.id, chapter.id)
            
            if (prevChapterId != null) {
                Log.warn { "$TAG: Loading previous chapter: $prevChapterId" }
                
                // Notify ChapterController about navigation (Requirements: 9.3, 9.4)
                // This keeps ChapterController in sync with TTS navigation
                chapterController?.dispatch(ChapterCommand.LoadChapter(prevChapterId, 0))
                
                // Load chapter content for TTS (with paragraph parsing)
                loadChapter(book.id, prevChapterId, 0)
                if (wasPlaying) {
                    play()
                }
            } else {
                Log.warn { "$TAG: No previous chapter available" }
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load previous chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load previous chapter"))
        }
    }
    
    // ========== Content ==========
    
    private suspend fun loadChapter(bookId: Long, chapterId: Long, startParagraph: Int) {
        Log.warn { "$TAG: loadChapter(bookId=$bookId, chapterId=$chapterId, startParagraph=$startParagraph)" }
        
        // Check if this chapter is already loaded with content - skip reload to preserve chunk mode
        val currentState = _state.value
        if (currentState.chapter?.id == chapterId && currentState.paragraphs.isNotEmpty()) {
            Log.warn { "$TAG: loadChapter - chapter already loaded, skipping reload" }
            // Just ensure chunk mode is applied if pending
            val pendingWordCount = pendingChunkWordCount
            if (pendingWordCount != null && pendingWordCount > 0 && !currentState.chunkModeEnabled) {
                Log.warn { "$TAG: loadChapter - applying pending chunk mode" }
                enableChunkMode(pendingWordCount)
            }
            return
        }
        
        // Stop current playback and clear engine state
        engine?.stop()
        
        // Clear engine's internal state (queue, cache) - IMPORTANT for Gradio engines
        engine?.clearState()
        
        // Clear chunk mode data - IMPORTANT: must be done before loading new content
        mergeResult = null
        
        _state.update { it.copy(playbackState = PlaybackState.LOADING, error = null) }
        
        try {
            val content = contentLoader.loadChapter(bookId, chapterId)
            
            // Check if we need to reload cached chunks for the new chapter
            val newChapterId = content.chapter?.id
            val cachedChunks = if (newChapterId != null && cacheUseCase != null) {
                cacheUseCase.getCachedChunkIndices(newChapterId)
            } else {
                emptySet()
            }
            
            _state.update { 
                it.copy(
                    book = content.book,
                    chapter = content.chapter,
                    paragraphs = content.paragraphs,
                    totalParagraphs = content.paragraphs.size,
                    currentParagraphIndex = startParagraph.coerceIn(0, content.paragraphs.lastIndex.coerceAtLeast(0)),
                    playbackState = PlaybackState.IDLE,
                    error = null,
                    // Reset translation state for new chapter
                    translatedParagraphs = null,
                    showTranslation = false,
                    isTranslationAvailable = false,
                    // Reset chunk mode state - will be re-enabled if needed
                    chunkModeEnabled = false,
                    currentChunkIndex = 0,
                    totalChunks = 0,
                    currentChunkParagraphs = emptyList(),
                    cachedChunks = cachedChunks,
                    isUsingCachedAudio = false
                )
            }
            
            Log.warn { "$TAG: Chapter loaded - ${content.paragraphs.size} paragraphs" }
            
            // Update lastSyncedChapterId to prevent sync loops
            if (content.chapter != null) {
                lastSyncedChapterId = content.chapter.id
            }
            
            // Notify ChapterController about chapter load (Requirements: 9.3, 9.4)
            // This keeps ChapterController in sync with TTS chapter state
            // BUT skip if this load was triggered BY ChapterController (would cause infinite loop)
            if (content.chapter != null && !isLoadingFromChapterControllerSync) {
                chapterController?.dispatch(ChapterCommand.LoadChapter(content.chapter.id, startParagraph))
            }
            
            // Apply pending chunk mode if set
            val pendingWordCount = pendingChunkWordCount
            if (pendingWordCount != null && pendingWordCount > 0) {
                Log.warn { "$TAG: Applying pending chunk mode with $pendingWordCount words" }
                enableChunkMode(pendingWordCount)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load chapter"))
        }
    }
    
    private fun setContent(paragraphs: List<String>) {
        _state.update { 
            it.copy(
                paragraphs = paragraphs,
                totalParagraphs = paragraphs.size,
                currentParagraphIndex = 0
            )
        }
    }
    
    // ========== Translation ==========
    
    private fun setTranslatedContent(paragraphs: List<String>?) {
        Log.warn { "$TAG: setTranslatedContent(${paragraphs?.size ?: 0} paragraphs)" }
        _state.update { 
            it.copy(
                translatedParagraphs = paragraphs,
                isTranslationAvailable = paragraphs != null && paragraphs.isNotEmpty()
            )
        }
    }
    
    private fun toggleTranslation(show: Boolean) {
        Log.warn { "$TAG: toggleTranslation($show)" }
        _state.update { it.copy(showTranslation = show) }
    }
    
    private fun toggleBilingualMode(enabled: Boolean) {
        Log.warn { "$TAG: toggleBilingualMode($enabled)" }
        _state.update { it.copy(bilingualMode = enabled) }
    }
    
    // ========== Sentence Highlighting ==========
    
    private fun setSentenceHighlight(enabled: Boolean) {
        Log.warn { "$TAG: setSentenceHighlight($enabled)" }
        _state.update { it.copy(sentenceHighlightEnabled = enabled) }
    }
    
    private fun updateParagraphStartTime(timeMs: Long) {
        _state.update { it.copy(paragraphStartTime = timeMs) }
    }
    
    private fun setCalibration(wpm: Float?, isCalibrated: Boolean) {
        _state.update { it.copy(calibratedWPM = wpm, isCalibrated = isCalibrated) }
    }
    
    // ========== Settings ==========
    
    private fun setSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        engine?.setSpeed(clampedSpeed)
        _state.update { it.copy(speed = clampedSpeed) }
    }
    
    private fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        engine?.setPitch(clampedPitch)
        _state.update { it.copy(pitch = clampedPitch) }
    }
    
    private fun setAutoNextChapter(enabled: Boolean) {
        _state.update { it.copy(autoNextChapter = enabled) }
    }
    
    private fun setEngine(type: EngineType) {
        val currentState = _state.value
        if (currentState.engineType == type) return
        
        Log.warn { "$TAG: setEngine($type) - switching from ${currentState.engineType}" }
        
        // Stop current engine
        engine?.stop()
        engine?.release()
        engine = null
        
        // Update state with new engine type
        _state.update { it.copy(engineType = type, isEngineReady = false) }
        
        // Create new engine will happen on next play() call via initialize()
        // Or we can initialize immediately
        initialize()
    }
    
    private fun setGradioConfig(config: GradioConfig) {
        Log.warn { "$TAG: setGradioConfig(${config.name})" }
        
        gradioConfig = config
        
        // If currently using Gradio engine, reinitialize with new config
        if (_state.value.engineType == EngineType.GRADIO) {
            engine?.stop()
            engine?.release()
            engine = null
            _state.update { it.copy(isEngineReady = false) }
            initialize()
        }
    }
    
    // ========== Chunk Mode ==========
    
    // Text merger for chunk mode
    private var textMerger: TTSTextMergerV2? = null
    private var mergeResult: TTSTextMergerV2.MergeResult? = null
    
    private fun enableChunkMode(targetWordCount: Int) {
        val currentState = _state.value
        
        // Always store the desired chunk word count - this ensures loadChapter can re-apply it
        pendingChunkWordCount = targetWordCount
        
        if (!currentState.hasContent) {
            Log.warn { "$TAG: enableChunkMode - no content yet, storing pending word count: $targetWordCount" }
            return
        }
        
        Log.warn { "$TAG: enableChunkMode(targetWordCount=$targetWordCount)" }
        
        // Clear engine's internal cache when re-chunking (chunks will be different)
        engine?.clearState()
        
        // Create text merger if needed
        if (textMerger == null) {
            textMerger = TTSTextMergerV2()
        }
        
        // Merge paragraphs into chunks
        val result = textMerger!!.mergeParagraphs(currentState.paragraphs, targetWordCount)
        mergeResult = result
        
        // Find the chunk containing current paragraph
        val currentChunkIndex = result.paragraphToChunkMap[currentState.currentParagraphIndex] ?: 0
        val currentChunk = result.chunks.getOrNull(currentChunkIndex)
        
        // Load cached chunks from persistent storage
        val chapterId = currentState.chapter?.id
        val cachedChunks = if (chapterId != null && cacheUseCase != null) {
            cacheUseCase.getCachedChunkIndices(chapterId)
        } else {
            emptySet()
        }
        
        _state.update {
            it.copy(
                chunkModeEnabled = true,
                currentChunkIndex = currentChunkIndex,
                totalChunks = result.chunks.size,
                currentChunkParagraphs = currentChunk?.paragraphIndices ?: emptyList(),
                cachedChunks = cachedChunks
            )
        }
        
        Log.warn { "$TAG: Chunk mode enabled - ${result.chunks.size} chunks, current=$currentChunkIndex, cached=${cachedChunks.size}" }
    }
    
    private fun disableChunkMode() {
        Log.warn { "$TAG: disableChunkMode()" }
        
        // Clear pending chunk word count so it won't be re-enabled on next loadChapter
        pendingChunkWordCount = null
        mergeResult = null
        
        _state.update {
            it.copy(
                chunkModeEnabled = false,
                currentChunkIndex = 0,
                totalChunks = 0,
                currentChunkParagraphs = emptyList(),
                cachedChunks = emptySet(),
                isUsingCachedAudio = false
            )
        }
    }
    
    private suspend fun nextChunk() {
        val currentState = _state.value
        if (!currentState.canGoNextChunk) return
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        val nextChunkIndex = currentState.currentChunkIndex + 1
        val nextChunk = mergeResult?.chunks?.getOrNull(nextChunkIndex)
        
        _state.update {
            it.copy(
                currentChunkIndex = nextChunkIndex,
                currentChunkParagraphs = nextChunk?.paragraphIndices ?: emptyList(),
                currentParagraphIndex = nextChunk?.startParagraph ?: it.currentParagraphIndex
            )
        }
        
        Log.warn { "$TAG: nextChunk() -> $nextChunkIndex" }
        
        if (wasPlaying) {
            playChunk()
        }
    }
    
    private suspend fun previousChunk() {
        val currentState = _state.value
        if (!currentState.canGoPreviousChunk) return
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        val prevChunkIndex = currentState.currentChunkIndex - 1
        val prevChunk = mergeResult?.chunks?.getOrNull(prevChunkIndex)
        
        _state.update {
            it.copy(
                currentChunkIndex = prevChunkIndex,
                currentChunkParagraphs = prevChunk?.paragraphIndices ?: emptyList(),
                currentParagraphIndex = prevChunk?.startParagraph ?: it.currentParagraphIndex
            )
        }
        
        Log.warn { "$TAG: previousChunk() -> $prevChunkIndex" }
        
        if (wasPlaying) {
            playChunk()
        }
    }
    
    private suspend fun jumpToChunk(index: Int) {
        val currentState = _state.value
        val result = mergeResult ?: return
        
        if (index < 0 || index >= result.chunks.size) return
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        val chunk = result.chunks[index]
        
        _state.update {
            it.copy(
                currentChunkIndex = index,
                currentChunkParagraphs = chunk.paragraphIndices,
                currentParagraphIndex = chunk.startParagraph
            )
        }
        
        Log.warn { "$TAG: jumpToChunk($index)" }
        
        if (wasPlaying) {
            playChunk()
        }
    }
    
    private suspend fun playChunk() {
        val currentState = _state.value
        val result = mergeResult
        
        if (!currentState.chunkModeEnabled || result == null) {
            // Fall back to regular play
            play()
            return
        }
        
        val chunk = result.chunks.getOrNull(currentState.currentChunkIndex)
        if (chunk == null) {
            play()
            return
        }
        
        // Auto-initialize engine if it was released
        if (engine == null) {
            Log.warn { "$TAG: playChunk() - Engine is null, reinitializing..." }
            initialize()
        }
        
        if (engine?.isReady() != true) {
            // Engine is initializing (async), queue the play for when it's ready
            Log.warn { "$TAG: playChunk() - Engine not ready, queuing play" }
            pendingPlay = true
            _state.update { it.copy(playbackState = PlaybackState.LOADING) }
            return
        }
        
        _state.update { it.copy(playbackState = PlaybackState.LOADING) }
        
        val chapterId = currentState.chapter?.id
        val chunkIndex = currentState.currentChunkIndex
        val utteranceId = "chunk_$chunkIndex"
        
        // Check if this chunk is cached (for offline playback)
        if (chapterId != null && cacheUseCase != null) {
            val isCached = cacheUseCase.isChunkCached(chapterId, chunkIndex)
            Log.warn { "$TAG: playChunk() - chunk $chunkIndex, cached=$isCached" }
            
            if (isCached) {
                val cachedAudio = cacheUseCase.getChunkAudio(chapterId, chunkIndex)
                if (cachedAudio != null) {
                    Log.warn { "$TAG: playChunk() - Playing cached audio: ${cachedAudio.size} bytes" }
                    _state.update { it.copy(isUsingCachedAudio = true) }
                    
                    // Play cached audio directly via engine
                    val played = engine?.playCachedAudio(cachedAudio, utteranceId) ?: false
                    if (played) {
                        return
                    }
                    Log.warn { "$TAG: playChunk() - Engine doesn't support cached audio playback, falling back to text" }
                }
            }
        }
        
        _state.update { it.copy(isUsingCachedAudio = false) }
        
        // Get the text to speak - use translated text if showTranslation is enabled
        val textToSpeak = if (currentState.showTranslation && currentState.hasTranslation) {
            // Re-merge the translated paragraphs for this chunk
            val translatedParagraphs = currentState.translatedParagraphs!!
            chunk.paragraphIndices
                .mapNotNull { translatedParagraphs.getOrNull(it) }
                .joinToString("\n\n")
        } else {
            chunk.text
        }
        
        Log.warn { "$TAG: playChunk() - chunk $chunkIndex, text length=${textToSpeak.length}, useTranslation=${currentState.showTranslation}" }
        
        engine?.speak(textToSpeak, utteranceId)
        
        // Pre-cache next chunk(s) for smoother playback
        precacheNextChunks(currentState, result)
    }
    
    /**
     * Pre-cache the next chunk(s) for smoother playback
     */
    private fun precacheNextChunks(currentState: TTSState, result: TTSTextMergerV2.MergeResult) {
        val nextChunkIndex = currentState.currentChunkIndex + 1
        if (nextChunkIndex >= result.chunks.size) return
        
        // Pre-cache up to 2 chunks ahead
        val itemsToPrecache = mutableListOf<Pair<String, String>>()
        
        for (i in nextChunkIndex until minOf(nextChunkIndex + 2, result.chunks.size)) {
            val chunk = result.chunks[i]
            val utteranceId = "chunk_$i"
            
            // Get the text - use translated if enabled
            val text = if (currentState.showTranslation && currentState.hasTranslation) {
                val translatedParagraphs = currentState.translatedParagraphs!!
                chunk.paragraphIndices
                    .mapNotNull { translatedParagraphs.getOrNull(it) }
                    .joinToString("\n\n")
            } else {
                chunk.text
            }
            
            if (text.isNotBlank()) {
                itemsToPrecache.add(utteranceId to text)
            }
        }
        
        if (itemsToPrecache.isNotEmpty()) {
            Log.warn { "$TAG: precacheNextChunks() - precaching ${itemsToPrecache.size} chunks" }
            engine?.precacheNext(itemsToPrecache)
        }
    }
    
    // ========== Error Handling ==========
    
    private suspend fun handleError(error: TTSError) {
        Log.error { "$TAG: handleError($error)" }
        
        _state.update { it.copy(playbackState = PlaybackState.ERROR, error = error) }
        _events.emit(TTSEvent.Error(error))
    }
    
    /**
     * Generate audio data for text (for caching/download)
     * Only supported by remote TTS engines (Gradio).
     * 
     * @param text Text to convert to audio
     * @return Audio data as ByteArray, or null if not supported
     */
    suspend fun generateAudioForText(text: String): ByteArray? {
        // Ensure engine is initialized
        if (engine == null) {
            initialize()
        }
        return engine?.generateAudioForText(text)
    }
    
    /**
     * Destroy the controller and release all resources
     */
    fun destroy() {
        cleanup()
        scope.cancel()
    }
}

/**
 * Content loader interface - abstracts chapter loading
 */
interface TTSContentLoader {
    suspend fun loadChapter(bookId: Long, chapterId: Long): ChapterContent
    
    /**
     * Get the next chapter ID for a book
     * @return Next chapter ID or null if at the end
     */
    suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long?
    
    /**
     * Get the previous chapter ID for a book
     * @return Previous chapter ID or null if at the beginning
     */
    suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long?
    
    data class ChapterContent(
        val book: Book?,
        val chapter: Chapter?,
        val paragraphs: List<String>
    )
}
