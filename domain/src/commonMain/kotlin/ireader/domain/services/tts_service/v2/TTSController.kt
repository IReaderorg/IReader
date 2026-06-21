package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
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
    private val cacheUseCase: TTSCacheUseCase? = null
) {
    // Mutable Gradio config that can be updated at runtime
    private var gradioConfig: GradioConfig? = initialGradioConfig
    companion object {
        private const val TAG = "TTSController"

        // Bounds for the auto-next empty-content watch (issue #236). Poll loadChapter
        // with exponential backoff until the next chapter's content is fetched, giving
        // up after the timeout so the coroutine never leaks.
        private const val CHAPTER_CONTENT_WATCH_TIMEOUT_MS = 5 * 60 * 1000L
        private const val CHAPTER_CONTENT_WATCH_INITIAL_DELAY_MS = 500L
        private const val CHAPTER_CONTENT_WATCH_MAX_DELAY_MS = 5_000L
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ========== Validation Helpers ==========

    /** Validate that a book and chapter are loaded. Returns null with logged warning if not. */
    private fun requireBookAndChapter(): Pair<Book, Chapter>? {
        val currentState = _state.value
        val book = currentState.book
        val chapter = currentState.chapter
        if (book == null || chapter == null) {
            Log.warn { "$TAG: Operation requires loaded book/chapter, but none found" }
            return null
        }
        return book to chapter
    }

    /** Validate that content is loaded. Returns false with logged warning if not. */
    private fun requireContent(operation: String): Boolean {
        if (!_state.value.hasContent) {
            Log.warn { "$TAG: $operation requires content, but none loaded" }
            return false
        }
        return true
    }

    /** Validate paragraph index is within bounds. */
    private fun isValidParagraphIndex(index: Int): Boolean {
        val currentState = _state.value
        return index >= 0 && index < currentState.paragraphs.size
    }

    /** Validate chunk index is within bounds. */
    private fun isValidChunkIndex(index: Int): Boolean {
        val currentState = _state.value
        return index >= 0 && index < currentState.totalChunks
    }
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()

    // Job for observing chapter DB changes when waiting for next chapter to appear
    private var nextChapterWatchJob: kotlinx.coroutines.Job? = null
    
    // State - single source of truth
    private val _state = MutableStateFlow(TTSState())
    val state: StateFlow<TTSState> = _state.asStateFlow()
    
    // Events - one-time occurrences.
    // Buffered with DROP_OLDEST so emitting a one-time event can NEVER suspend the
    // command / engine-event processing coroutines. A zero-capacity (rendezvous) flow
    // would suspend emit() when a subscriber applies back-pressure, stalling the
    // controller mid-transition (e.g. chapter-end handler emitting ChapterCompleted right
    // before dispatching NextChapter).
    private val _events = MutableSharedFlow<TTSEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
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
        Log.debug { "$TAG: dispatch($command)" }
        
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
            is TTSCommand.RefreshContent -> refreshContent()
            
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
        Log.debug { "$TAG: initialize()" }
        
        if (engine == null) {
            val currentEngineType = _state.value.engineType
            engine = when (currentEngineType) {
                EngineType.NATIVE -> {
                    Log.debug { "$TAG: Creating native engine" }
                    nativeEngineFactory()
                }
                EngineType.GRADIO -> {
                    Log.debug { "$TAG: Creating Gradio engine" }
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
        Log.debug { "$TAG: stopAndRelease()" }
        
        pendingPlay = false
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = null
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
        Log.debug { "$TAG: cleanup()" }
        
        pendingPlay = false
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = null
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
        Log.debug { "$TAG: handleEngineEvent($event)" }
        
        when (event) {
            is EngineEvent.Ready -> {
                _state.update { it.copy(isEngineReady = true) }
                
                // If there's a pending play, execute it now.
                // Guard with the command mutex so this engine-driven play() cannot run
                // concurrently with an in-flight command's speak().
                if (pendingPlay) {
                    Log.debug { "$TAG: Engine ready, executing pending play" }
                    pendingPlay = false
                    commandMutex.withLock {
                        play()
                    }
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
            Log.debug { "$TAG: play() - Engine is null, reinitializing..." }
            initialize()
        }
        
        if (engine?.isReady() != true) {
            // Engine is initializing (async), queue the play for when it's ready
            Log.debug { "$TAG: play() - Engine not ready, queuing play" }
            pendingPlay = true
            _state.update { it.copy(playbackState = PlaybackState.LOADING) }
            return
        }
        
        // Use chunk mode if enabled
        if (currentState.chunkModeEnabled && mergeResult != null) {
            playChunk()
            return
        }
        
        val paragraphIndex = currentState.currentParagraphIndex
        
        // Mark this paragraph as loading
        _state.update { 
            it.copy(
                playbackState = PlaybackState.LOADING,
                loadingParagraphs = it.loadingParagraphs + paragraphIndex
            ) 
        }
        
        // Use displayContent which respects showTranslation setting
        val text = currentState.displayContent.getOrNull(paragraphIndex)
        if (text != null) {
            val utteranceId = "p_${paragraphIndex}"
            
            // Pre-cache next paragraphs in background
            precacheUpcomingParagraphs(paragraphIndex)
            
            engine?.speak(text, utteranceId)
        }
    }
    
    /**
     * Pre-cache upcoming paragraphs for smoother playback
     */
    private fun precacheUpcomingParagraphs(currentIndex: Int) {
        val currentState = _state.value
        val content = currentState.displayContent
        val prefetchCount = 3
        
        val itemsToPrecache = mutableListOf<Pair<String, String>>()
        for (i in 1..prefetchCount) {
            val nextIndex = currentIndex + i
            if (nextIndex >= content.size) break
            
            // Skip if already cached
            if (currentState.cachedParagraphs.contains(nextIndex)) continue
            
            val text = content.getOrNull(nextIndex) ?: continue
            if (text.isBlank()) continue
            
            itemsToPrecache.add("p_$nextIndex" to text)
        }
        
        if (itemsToPrecache.isNotEmpty()) {
            Log.debug { "$TAG: Precaching ${itemsToPrecache.size} upcoming paragraphs" }
            engine?.precacheNext(itemsToPrecache)
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
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = null
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
        val completedIndex = currentState.currentParagraphIndex
        
        // Mark paragraph as cached (it was just played, so it's now in cache)
        // and remove from loading set
        _state.update { 
            it.copy(
                cachedParagraphs = it.cachedParagraphs + completedIndex,
                loadingParagraphs = it.loadingParagraphs - completedIndex
            ) 
        }
        
        // Handle chunk mode completion
        if (currentState.chunkModeEnabled && mergeResult != null) {
            handleChunkCompleted()
            return
        }
        
        if (currentState.canGoNext && currentState.isPlaying) {
            // Auto-advance to next paragraph.
            // This runs on the engine-event collector (NOT under commandMutex), so guard it
            // with the mutex. Otherwise this engine-driven speak() can race a mutex-held
            // command (e.g. a NextChapter transition or a media-button Resume) and the engine
            // ends up with two concurrent speak() calls — which makes native TTS read only the
            // first syllable and then stop.
            commandMutex.withLock {
                _state.update {
                    it.copy(
                        previousParagraphIndex = it.currentParagraphIndex,
                        currentParagraphIndex = it.currentParagraphIndex + 1
                    )
                }
                play()
            }
        } else if (!currentState.canGoNext) {
            // Chapter finished
            _events.emit(TTSEvent.ChapterCompleted)
            
            if (currentState.autoNextChapter) {
                // Dispatch through commandMutex to prevent races with user-initiated commands.
                // Calling nextChapter() directly here bypasses the mutex, which can cause
                // the engine to receive concurrent speak() calls during the chapter transition,
                // resulting in TTS reading only the first syllable then stopping.
                dispatch(TTSCommand.NextChapter)
            } else {
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        }
    }
    
    private suspend fun handleChunkCompleted() {
        val currentState = _state.value
        val result = mergeResult ?: return
        
        Log.debug { "$TAG: handleChunkCompleted() - chunk ${currentState.currentChunkIndex}/${result.chunks.size}" }
        
        if (currentState.canGoNextChunk) {
            // Auto-advance to next chunk.
            // Guard with the command mutex for the same reason as paragraph auto-advance:
            // this runs on the engine-event collector and must not issue a concurrent speak().
            commandMutex.withLock {
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
            }
        } else {
            // All chunks finished - chapter complete
            _events.emit(TTSEvent.ChapterCompleted)
            
            if (currentState.autoNextChapter) {
                // Dispatch through commandMutex to prevent races with user-initiated commands
                dispatch(TTSCommand.NextChapter)
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
        if (!isValidParagraphIndex(index)) {
            Log.warn { "$TAG: jumpToParagraph($index) - invalid index, ignoring" }
            return
        }

        val currentState = _state.value
        Log.debug { "$TAG: jumpToParagraph($index) - chunkModeEnabled=${currentState.chunkModeEnabled}" }
        
        val wasPlaying = currentState.isPlaying
        engine?.stop()
        
        // If chunk mode is enabled, find the chunk containing this paragraph
        if (currentState.chunkModeEnabled && mergeResult != null) {
            val result = mergeResult!!
            val chunkIndex = result.paragraphToChunkMap[index]
            
            if (chunkIndex != null) {
                val chunk = result.chunks.getOrNull(chunkIndex)
                Log.debug { "$TAG: jumpToParagraph - found chunk $chunkIndex for paragraph $index" }
                
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
        val (book, chapter) = requireBookAndChapter() ?: return

        Log.debug { "$TAG: nextChapter() - current chapter: ${chapter.id}" }

        // If already loading a chapter (e.g., auto-next watch is fetching), skip to avoid race
        if (_state.value.playbackState == PlaybackState.LOADING) {
            Log.debug { "$TAG: nextChapter() - already loading, skipping" }
            return
        }

        // Cancel any previous chapter watch
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = null

        val wasPlaying = _state.value.isPlaying
        engine?.stop()

        // Give the native TTS engine time to fully process the stop command before the next
        // speak(). On many Android OEMs stop() is not synchronous; a bare yield() is not
        // enough and a speak() issued too soon is silently dropped after the first syllable,
        // leaving playback stuck in LOADING. A short real delay reliably lets it settle.
        kotlinx.coroutines.delay(150)

        _state.update { it.copy(playbackState = PlaybackState.LOADING) }

        try {
            val nextChapterId = contentLoader.getNextChapterId(book.id, chapter.id)

            if (nextChapterId != null) {
                Log.debug { "$TAG: Loading next chapter: $nextChapterId" }
                loadChapter(book.id, nextChapterId, 0)

                if (_state.value.hasContent) {
                    // Content is present (already in DB or fetched synchronously by
                    // loadChapter) — resume playback as before.
                    if (wasPlaying) { play() }
                } else if (_state.value.autoNextChapter) {
                    // The next chapter row exists but its content has not been fetched
                    // yet (empty paragraphs after loadChapter, e.g. the remote fetch is
                    // still in flight). Calling play() here would short-circuit on
                    // TTSError.NoContent and leave playback stuck with a perpetual
                    // loading bar (issue #236). Instead, watch this chapter for content
                    // and resume once it arrives.
                    Log.debug { "$TAG: Next chapter $nextChapterId has no content yet, watching for content (autoNext=true)" }
                    startChapterContentWatch(book.id, nextChapterId, wasPlaying)
                } else {
                    // Auto-next disabled and no content — settle cleanly to STOPPED
                    // rather than erroring or staying stuck in LOADING.
                    Log.debug { "$TAG: Next chapter $nextChapterId has no content and autoNext is off, stopping" }
                    _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
                }
            } else if (_state.value.autoNextChapter) {
                // Next chapter not in DB yet — watch for new chapters from remote fetch
                Log.debug { "$TAG: No next chapter in DB, watching for chapter DB changes (autoNext=true)" }
                startNextChapterWatch(book.id, chapter.id, wasPlaying)
            } else {
                Log.debug { "$TAG: No next chapter available" }
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load next chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load next chapter"))
        }
    }

    /**
     * Watch for new chapters appearing in the DB (e.g. from a remote fetch).
     * When a next chapter becomes available, dispatches LoadChapter and Play through the
     * commandMutex so all state mutations are serialized with user-initiated commands.
     * Times out after 5 minutes to avoid leaking the coroutine.
     */
    private fun startNextChapterWatch(bookId: Long, currentChapterId: Long, wasPlaying: Boolean) {
        nextChapterWatchJob?.cancel()
        var found = false
        nextChapterWatchJob = scope.launch {
            try {
                kotlinx.coroutines.withTimeout(5 * 60 * 1000L) {
                    contentLoader.subscribeChapters(bookId).collect { chapters ->
                        if (found) return@collect

                        val currentIdx = chapters.indexOfFirst { it.id == currentChapterId }
                        if (currentIdx == -1) return@collect

                        val nextIdx = currentIdx + 1
                        if (nextIdx < chapters.size) {
                            val nextId = chapters[nextIdx].id
                            Log.debug { "$TAG: Chapter DB changed, found next chapter: $nextId" }
                            found = true

                            // Dispatch through commandMutex to serialize with user commands.
                            // Read wasPlaying at resolution time (not watch-start) to respect
                            // any pause/stop the user issued while waiting.
                            dispatch(TTSCommand.LoadChapter(bookId, nextId, 0))
                            if (_state.value.isPlaying) {
                                dispatch(TTSCommand.Play)
                            } else if (wasPlaying) {
                                dispatch(TTSCommand.Play)
                            }
                        }
                    }
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                Log.debug { "$TAG: nextChapterWatch timed out, no new chapter appeared" }
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error { "$TAG: nextChapterWatch failed: ${e.message}" }
                handleError(TTSError.ContentLoadFailed(e.message ?: "Chapter watch failed"))
            } finally {
                nextChapterWatchJob = null
            }
        }
    }

    /**
     * Watch a chapter that already exists in the DB but whose content has not been
     * fetched yet, and resume playback once its content becomes available.
     *
     * This covers the auto-next case (issue #236) where getNextChapterId returns a
     * chapter row but loadChapter produced no paragraphs because the remote fetch is
     * still in flight. Without this, nextChapter() would call play() on empty content
     * and short-circuit on TTSError.NoContent, leaving playback stuck in a perpetual
     * loading state.
     *
     * Implementation note: we intentionally poll contentLoader.loadChapter rather than
     * subscribing to the chapter list. The production subscribeChapters stream is keyed
     * on chapter IDs (distinctUntilChangedBy { it.id }) and does NOT re-emit when an
     * existing row simply gains content, so it cannot detect this transition. loadChapter
     * itself re-attempts the remote fetch on empty content, so re-invoking it with backoff
     * both drives the fetch and observes when content lands. Resume is dispatched through
     * the commandMutex (via TTSCommand) so state mutations stay serialized with
     * user-initiated commands, and the current playback state is re-checked at resolution
     * time so a pause/stop issued while waiting is respected.
     */
    private fun startChapterContentWatch(bookId: Long, chapterId: Long, wasPlaying: Boolean) {
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = scope.launch {
            try {
                kotlinx.coroutines.withTimeout(CHAPTER_CONTENT_WATCH_TIMEOUT_MS) {
                    var delayMs = CHAPTER_CONTENT_WATCH_INITIAL_DELAY_MS
                    while (true) {
                        kotlinx.coroutines.delay(delayMs)

                        // If the user navigated elsewhere while waiting, abort the watch.
                        if (_state.value.chapter?.id != chapterId) {
                            Log.debug { "$TAG: chapterContentWatch - chapter changed while waiting, aborting" }
                            return@withTimeout
                        }

                        val content = contentLoader.loadChapter(bookId, chapterId)

                        // loadChapter suspends while fetching; re-check that the user is
                        // still on this chapter afterwards. If they navigated elsewhere
                        // during the fetch, drop the now-stale result instead of yanking
                        // playback back to the old auto-next chapter.
                        if (_state.value.chapter?.id != chapterId) {
                            Log.debug { "$TAG: chapterContentWatch - chapter changed during fetch, dropping stale result" }
                            return@withTimeout
                        }

                        if (content.paragraphs.isNotEmpty()) {
                            Log.debug { "$TAG: Chapter $chapterId content arrived, resuming playback" }

                            // Re-load to pull the now-available content into state, then
                            // resume only if playback wasn't paused/stopped while waiting.
                            // Read the live state at resolution time (not watch-start) so a
                            // pause issued during the wait (e.g. audio-focus loss) is honored.
                            dispatch(TTSCommand.LoadChapter(bookId, chapterId, 0))
                            val shouldResume = wasPlaying &&
                                _state.value.playbackState != PlaybackState.PAUSED &&
                                _state.value.playbackState != PlaybackState.STOPPED
                            if (shouldResume) {
                                dispatch(TTSCommand.Play)
                            }
                            return@withTimeout
                        }

                        // Exponential backoff, capped, to avoid hammering the source.
                        delayMs = (delayMs * 2).coerceAtMost(CHAPTER_CONTENT_WATCH_MAX_DELAY_MS)
                    }
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                Log.debug { "$TAG: chapterContentWatch timed out, content never arrived for $chapterId" }
                // Only settle to STOPPED if the user is still on the watched chapter — a
                // late timeout must not clobber playback the user has since started elsewhere.
                if (_state.value.chapter?.id == chapterId) {
                    _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error { "$TAG: chapterContentWatch failed: ${e.message}" }
                // Same guard: don't surface this watch's error onto an unrelated chapter
                // the user navigated to while the watch was pending.
                if (_state.value.chapter?.id == chapterId) {
                    handleError(TTSError.ContentLoadFailed(e.message ?: "Chapter content watch failed"))
                }
            } finally {
                nextChapterWatchJob = null
            }
        }
    }

    private suspend fun previousChapter() {
        val (book, chapter) = requireBookAndChapter() ?: return

        Log.debug { "$TAG: previousChapter() - current chapter: ${chapter.id}" }

        val wasPlaying = _state.value.isPlaying
        engine?.stop()

        // Give the native TTS engine time to settle after stop() before the next speak()
        // (see nextChapter() for the rationale — a bare yield() is not enough on many OEMs).
        kotlinx.coroutines.delay(150)

        _state.update { it.copy(playbackState = PlaybackState.LOADING) }

        try {
            val prevChapterId = contentLoader.getPreviousChapterId(book.id, chapter.id)

            if (prevChapterId != null) {
                Log.debug { "$TAG: Loading previous chapter: $prevChapterId" }
                loadChapter(book.id, prevChapterId, 0)
                if (wasPlaying) { play() }
            } else {
                Log.debug { "$TAG: No previous chapter available" }
                _state.update { it.copy(playbackState = PlaybackState.STOPPED) }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load previous chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load previous chapter"))
        }
    }
    
    // ========== Content ==========
    
    private suspend fun loadChapter(bookId: Long, chapterId: Long, startParagraph: Int) {
        Log.debug { "$TAG: loadChapter(bookId=$bookId, chapterId=$chapterId, startParagraph=$startParagraph)" }
        
        // Check if this EXACT chapter is already loaded with content - skip reload to preserve chunk mode
        // IMPORTANT: Must check BOTH bookId AND chapterId to avoid showing stale data from different book
        val currentState = _state.value
        val sameBook = currentState.book?.id == bookId
        val sameChapter = currentState.chapter?.id == chapterId
        val hasContent = currentState.paragraphs.isNotEmpty()
        
        if (sameBook && sameChapter && hasContent) {
            Log.debug { "$TAG: loadChapter - same book/chapter already loaded, skipping reload" }
            // Just ensure chunk mode is applied if pending
            val pendingWordCount = pendingChunkWordCount
            if (pendingWordCount != null && pendingWordCount > 0 && !currentState.chunkModeEnabled) {
                Log.debug { "$TAG: loadChapter - applying pending chunk mode" }
                enableChunkMode(pendingWordCount)
            }
            return
        }
        
        // Different book or chapter - must reload
        if (!sameBook || !sameChapter) {
            Log.debug { "$TAG: loadChapter - different book/chapter, clearing old state (old: book=${currentState.book?.id}, chapter=${currentState.chapter?.id})" }
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
                    allChunksParagraphIndices = emptyList(),
                    cachedChunks = cachedChunks,
                    isUsingCachedAudio = false
                )
            }
            
            Log.debug { "$TAG: Chapter loaded - ${content.paragraphs.size} paragraphs" }
            
            // Apply pending chunk mode if set
            val pendingWordCount = pendingChunkWordCount
            if (pendingWordCount != null && pendingWordCount > 0) {
                Log.debug { "$TAG: Applying pending chunk mode with $pendingWordCount words" }
                enableChunkMode(pendingWordCount)
            }
            
            // Update cached paragraphs state for UI
            updateCachedParagraphsState(content.paragraphs)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load chapter: ${e.message}" }
            handleError(TTSError.ContentLoadFailed(e.message ?: "Failed to load chapter"))
        }
    }
    
    /**
     * Update the cached paragraphs state by checking which paragraphs are in the cache
     */
    private suspend fun updateCachedParagraphsState(paragraphs: List<String>) {
        val cachedIndices = engine?.getCachedIndices(paragraphs) ?: emptySet()
        Log.debug { "$TAG: updateCachedParagraphsState - ${cachedIndices.size} paragraphs cached" }
        _state.update { it.copy(cachedParagraphs = cachedIndices) }
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
    
    /**
     * Refresh current chapter content (e.g., when content filter settings change).
     * Reloads the chapter from the content loader to apply new filter settings.
     */
    private suspend fun refreshContent() {
        val currentState = _state.value
        val bookId = currentState.book?.id
        val chapterId = currentState.chapter?.id
        val currentParagraph = currentState.currentParagraphIndex
        
        if (bookId == null || chapterId == null) {
            Log.debug { "$TAG: refreshContent - no chapter loaded, skipping" }
            return
        }
        
        Log.debug { "$TAG: refreshContent - reloading chapter $chapterId" }
        
        // Force reload by clearing the current state first
        _state.update { it.copy(paragraphs = emptyList()) }
        
        // Reload the chapter (content filter will be applied by contentLoader)
        loadChapter(bookId, chapterId, currentParagraph)
    }
    
    // ========== Translation ==========
    
    private fun setTranslatedContent(paragraphs: List<String>?) {
        Log.debug { "$TAG: setTranslatedContent(${paragraphs?.size ?: 0} paragraphs)" }
        _state.update { 
            it.copy(
                translatedParagraphs = paragraphs,
                isTranslationAvailable = paragraphs != null && paragraphs.isNotEmpty()
            )
        }
    }
    
    private fun toggleTranslation(show: Boolean) {
        Log.debug { "$TAG: toggleTranslation($show)" }
        _state.update { it.copy(showTranslation = show) }
    }
    
    private fun toggleBilingualMode(enabled: Boolean) {
        Log.debug { "$TAG: toggleBilingualMode($enabled)" }
        _state.update { it.copy(bilingualMode = enabled) }
    }
    
    // ========== Sentence Highlighting ==========
    
    private fun setSentenceHighlight(enabled: Boolean) {
        Log.debug { "$TAG: setSentenceHighlight($enabled)" }
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
        if (!enabled) {
            nextChapterWatchJob?.cancel()
            nextChapterWatchJob = null
        }
        _state.update { it.copy(autoNextChapter = enabled) }
    }
    
    private fun setEngine(type: EngineType) {
        val currentState = _state.value
        if (currentState.engineType == type) return
        
        Log.debug { "$TAG: setEngine($type) - switching from ${currentState.engineType}" }
        
        // Cancel chapter watch — engine is being replaced
        nextChapterWatchJob?.cancel()
        nextChapterWatchJob = null

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
        Log.debug { "$TAG: setGradioConfig(${config.name})" }
        
        gradioConfig = config
        
        // If currently using Gradio engine, reinitialize with new config
        if (_state.value.engineType == EngineType.GRADIO) {
            // Cancel chapter watch — engine is being replaced
            nextChapterWatchJob?.cancel()
            nextChapterWatchJob = null

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
            Log.debug { "$TAG: enableChunkMode - no content yet, storing pending word count: $targetWordCount" }
            return
        }
        
        Log.debug { "$TAG: enableChunkMode(targetWordCount=$targetWordCount)" }
        
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
                allChunksParagraphIndices = result.chunks.map { chunk -> chunk.paragraphIndices },
                cachedChunks = cachedChunks
            )
        }
        
        Log.debug { "$TAG: Chunk mode enabled - ${result.chunks.size} chunks, current=$currentChunkIndex, cached=${cachedChunks.size}" }
    }
    
    private fun disableChunkMode() {
        Log.debug { "$TAG: disableChunkMode()" }
        
        // Clear pending chunk word count so it won't be re-enabled on next loadChapter
        pendingChunkWordCount = null
        mergeResult = null
        
        _state.update {
            it.copy(
                chunkModeEnabled = false,
                currentChunkIndex = 0,
                totalChunks = 0,
                currentChunkParagraphs = emptyList(),
                allChunksParagraphIndices = emptyList(),
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
        
        Log.debug { "$TAG: nextChunk() -> $nextChunkIndex" }
        
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
        
        Log.debug { "$TAG: previousChunk() -> $prevChunkIndex" }
        
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
        
        Log.debug { "$TAG: jumpToChunk($index)" }
        
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
            Log.debug { "$TAG: playChunk() - Engine is null, reinitializing..." }
            initialize()
        }
        
        if (engine?.isReady() != true) {
            // Engine is initializing (async), queue the play for when it's ready
            Log.debug { "$TAG: playChunk() - Engine not ready, queuing play" }
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
            Log.debug { "$TAG: playChunk() - chunk $chunkIndex, cached=$isCached" }
            
            if (isCached) {
                val cachedAudio = cacheUseCase.getChunkAudio(chapterId, chunkIndex)
                if (cachedAudio != null) {
                    Log.debug { "$TAG: playChunk() - Playing cached audio: ${cachedAudio.size} bytes" }
                    _state.update { it.copy(isUsingCachedAudio = true) }
                    
                    // Play cached audio directly via engine
                    val played = engine?.playCachedAudio(cachedAudio, utteranceId) ?: false
                    if (played) {
                        return
                    }
                    Log.debug { "$TAG: playChunk() - Engine doesn't support cached audio playback, falling back to text" }
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
        
        Log.debug { "$TAG: playChunk() - chunk $chunkIndex, text length=${textToSpeak.length}, useTranslation=${currentState.showTranslation}" }
        
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
            Log.debug { "$TAG: precacheNextChunks() - precaching ${itemsToPrecache.size} chunks" }
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
    
    /**
     * Observe chapter list changes for a book.
     * Emits whenever chapters are added/updated in the database (e.g. after a remote fetch).
     */
    fun subscribeChapters(bookId: Long): kotlinx.coroutines.flow.Flow<List<Chapter>>
    
    data class ChapterContent(
        val book: Book?,
        val chapter: Chapter?,
        val paragraphs: List<String>
    )
}
