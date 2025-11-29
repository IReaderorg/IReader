//package ireader.domain.services.tts_service.player
//
//import ireader.core.log.Log
//import ireader.domain.models.entities.Book
//import ireader.domain.models.entities.Chapter
//import ireader.domain.services.tts_service.TTSServiceState
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.*
//
///**
// * Integration layer that connects GradioTTSPlayerManager with the TTS service layer.
// *
// * This class bridges the gap between the player and the service, handling:
// * - Book and chapter context
// * - Auto-next chapter functionality
// * - Sleep timer integration
// * - State synchronization with TTSServiceState
// *
// * Usage:
// * ```kotlin
// * val integration = GradioTTSServiceIntegration(
// *     playerManager = playerManager,
// *     onChapterFinished = { currentChapter -> loadNextChapter() },
// *     onSleepTimerExpired = { stop() }
// * )
// *
// * integration.startReading(book, chapter, paragraphs)
// * ```
// */
//class GradioTTSServiceIntegration(
//    private val playerManager: GradioTTSPlayerManager,
//    private val onChapterFinished: suspend (Chapter) -> Chapter?,
//    private val onSleepTimerExpired: suspend () -> Unit = {},
//    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
//) {
//    companion object {
//        private const val TAG = "GradioTTSIntegration"
//    }
//
//    private val scope = CoroutineScope(dispatcher + SupervisorJob())
//
//    // ==================== Book/Chapter Context ====================
//
//    private val _currentBook = MutableStateFlow<Book?>(null)
//    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()
//
//    private val _currentChapter = MutableStateFlow<Chapter?>(null)
//    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()
//
//    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
//    val currentContent: StateFlow<List<String>> = _currentContent.asStateFlow()
//
//    // ==================== Settings ====================
//
//    private val _autoNextChapter = MutableStateFlow(true)
//    val autoNextChapter: StateFlow<Boolean> = _autoNextChapter.asStateFlow()
//
//    private val _sleepTimerMinutes = MutableStateFlow(0)
//    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()
//
//    private val _sleepTimeRemaining = MutableStateFlow(0L)
//    val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining.asStateFlow()
//
//    private var sleepTimerJob: Job? = null
//
//    // ==================== Delegated State ====================
//
//    val isPlaying: StateFlow<Boolean> get() = playerManager.isPlaying
//    val isPaused: StateFlow<Boolean> get() = playerManager.isPaused
//    val isLoading: StateFlow<Boolean> get() = playerManager.isLoading
//    val currentParagraph: StateFlow<Int> get() = playerManager.currentParagraph
//    val totalParagraphs: StateFlow<Int> get() = playerManager.totalParagraphs
//    val cachedParagraphs: StateFlow<Set<Int>> get() = playerManager.cachedParagraphs
//    val speed: StateFlow<Float> get() = playerManager.speed
//    val pitch: StateFlow<Float> get() = playerManager.pitch
//    val error: StateFlow<String?> get() = playerManager.error
//    val hasContent: StateFlow<Boolean> get() = playerManager.hasContent
//    val currentEngine: StateFlow<String?> get() = playerManager.currentEngine
//    val availableEngines get() = playerManager.availableEngines
//
//    // ==================== Events ====================
//
//    private val _events = MutableSharedFlow<GradioTTSIntegrationEvent>(
//        replay = 0,
//        extraBufferCapacity = 16
//    )
//    val events: SharedFlow<GradioTTSIntegrationEvent> = _events.asSharedFlow()
//
//    init {
//        observePlayerEvents()
//    }
//
//    private fun observePlayerEvents() {
//        scope.launch {
//            playerManager.events.collect { event ->
//                when (event) {
//                    is GradioTTSPlayerManagerEvent.ContentFinished -> {
//                        handleContentFinished()
//                    }
//                    is GradioTTSPlayerManagerEvent.PlayerError -> {
//                        _events.emit(GradioTTSIntegrationEvent.Error(event.message, event.recoverable))
//                    }
//                    is GradioTTSPlayerManagerEvent.EngineChanged -> {
//                        _events.emit(GradioTTSIntegrationEvent.EngineChanged(event.engineId, event.engineName))
//                    }
//                    is GradioTTSPlayerManagerEvent.ManagerError -> {
//                        _events.emit(GradioTTSIntegrationEvent.Error(event.message, false))
//                    }
//                }
//            }
//        }
//    }
//
//    private suspend fun handleContentFinished() {
//        val chapter = _currentChapter.value ?: return
//
//        Log.info { "$TAG: Content finished for chapter ${chapter.id}" }
//        _events.emit(GradioTTSIntegrationEvent.ChapterFinished(chapter))
//
//        // Check if auto-next is enabled
//        if (_autoNextChapter.value) {
//            Log.info { "$TAG: Auto-next enabled, loading next chapter" }
//            val nextChapter = onChapterFinished(chapter)
//
//            if (nextChapter != null) {
//                _events.emit(GradioTTSIntegrationEvent.ChapterChanged(nextChapter))
//            } else {
//                Log.info { "$TAG: No more chapters, book finished" }
//                _events.emit(GradioTTSIntegrationEvent.BookFinished)
//            }
//        }
//    }
//
//    // ==================== Reading Control ====================
//
//    /**
//     * Start reading a chapter.
//     *
//     * @param book The book being read
//     * @param chapter The chapter to read
//     * @param paragraphs The paragraphs to read
//     * @param startIndex Starting paragraph index (default: 0)
//     * @param autoPlay Whether to start playing immediately (default: true)
//     */
//    fun startReading(
//        book: Book,
//        chapter: Chapter,
//        paragraphs: List<String>,
//        startIndex: Int = 0,
//        autoPlay: Boolean = true
//    ) {
//        _currentBook.value = book
//        _currentChapter.value = chapter
//        _currentContent.value = paragraphs
//
//        playerManager.setContent(paragraphs, startIndex)
//
//        Log.info { "$TAG: Starting reading: ${book.title} - ${chapter.name}" }
//
//        if (autoPlay) {
//            playerManager.play()
//        }
//
//        scope.launch {
//            _events.emit(GradioTTSIntegrationEvent.ReadingStarted(book, chapter))
//        }
//    }
//
//    /**
//     * Load a new chapter while keeping the same book context.
//     */
//    fun loadChapter(chapter: Chapter, paragraphs: List<String>, startIndex: Int = 0, autoPlay: Boolean = true) {
//        _currentChapter.value = chapter
//        _currentContent.value = paragraphs
//
//        playerManager.setContent(paragraphs, startIndex)
//
//        Log.info { "$TAG: Loaded chapter: ${chapter.name}" }
//
//        if (autoPlay) {
//            playerManager.play()
//        }
//    }
//
//    // ==================== Playback Controls ====================
//
//    fun play() {
//        playerManager.play()
//        startSleepTimerIfNeeded()
//    }
//
//    fun pause() {
//        playerManager.pause()
//        pauseSleepTimer()
//    }
//
//    fun stop() {
//        playerManager.stop()
//        cancelSleepTimer()
//    }
//
//    fun next() {
//        playerManager.next()
//    }
//
//    fun previous() {
//        playerManager.previous()
//    }
//
//    fun jumpTo(index: Int) {
//        playerManager.jumpTo(index)
//    }
//
//    // ==================== Settings ====================
//
//    fun setSpeed(speed: Float) {
//        playerManager.setSpeed(speed)
//    }
//
//    fun setPitch(pitch: Float) {
//        playerManager.setPitch(pitch)
//    }
//
//    fun setAutoNextChapter(enabled: Boolean) {
//        _autoNextChapter.value = enabled
//        Log.info { "$TAG: Auto-next chapter: $enabled" }
//    }
//
//    fun selectEngine(engineId: String) {
//        playerManager.selectEngine(engineId)
//    }
//
//    fun clearCache() {
//        playerManager.clearCache()
//    }
//
//    // ==================== Sleep Timer ====================
//
//    /**
//     * Set sleep timer in minutes.
//     * @param minutes Minutes until sleep (0 to disable)
//     */
//    fun setSleepTimer(minutes: Int) {
//        _sleepTimerMinutes.value = minutes
//
//        if (minutes <= 0) {
//            cancelSleepTimer()
//            return
//        }
//
//        _sleepTimeRemaining.value = minutes * 60 * 1000L
//
//        if (isPlaying.value) {
//            startSleepTimerIfNeeded()
//        }
//
//        Log.info { "$TAG: Sleep timer set to $minutes minutes" }
//    }
//
//    private fun startSleepTimerIfNeeded() {
//        if (_sleepTimerMinutes.value <= 0) return
//        if (sleepTimerJob?.isActive == true) return
//
//        sleepTimerJob = scope.launch {
//            while (_sleepTimeRemaining.value > 0 && isActive) {
//                delay(1000)
//                _sleepTimeRemaining.value = (_sleepTimeRemaining.value - 1000).coerceAtLeast(0)
//
//                if (_sleepTimeRemaining.value <= 0) {
//                    Log.info { "$TAG: Sleep timer expired" }
//                    onSleepTimerExpired()
//                    playerManager.pause()
//                    _events.emit(GradioTTSIntegrationEvent.SleepTimerExpired)
//                    break
//                }
//            }
//        }
//    }
//
//    private fun pauseSleepTimer() {
//        sleepTimerJob?.cancel()
//        sleepTimerJob = null
//    }
//
//    private fun cancelSleepTimer() {
//        sleepTimerJob?.cancel()
//        sleepTimerJob = null
//        _sleepTimerMinutes.value = 0
//        _sleepTimeRemaining.value = 0
//    }
//
//    // ==================== State ====================
//
//    /**
//     * Get current reading position for saving.
//     */
//    fun getCurrentPosition(): ReadingPosition? {
//        val book = _currentBook.value ?: return null
//        val chapter = _currentChapter.value ?: return null
//
//        return ReadingPosition(
//            bookId = book.id,
//            chapterId = chapter.id,
//            paragraphIndex = currentParagraph.value
//        )
//    }
//
//    // ==================== Lifecycle ====================
//
//    fun release() {
//        Log.info { "$TAG: Releasing integration" }
//        cancelSleepTimer()
//        playerManager.release()
//        scope.cancel()
//    }
//}
//
///**
// * Represents a reading position that can be saved/restored.
// */
//data class ReadingPosition(
//    val bookId: Long,
//    val chapterId: Long,
//    val paragraphIndex: Int
//)
//
///**
// * Events emitted by the GradioTTSServiceIntegration.
// */
//sealed class GradioTTSIntegrationEvent {
//    /** Reading has started */
//    data class ReadingStarted(val book: Book, val chapter: Chapter) : GradioTTSIntegrationEvent()
//
//    /** Chapter has finished */
//    data class ChapterFinished(val chapter: Chapter) : GradioTTSIntegrationEvent()
//
//    /** Chapter has changed (auto-next) */
//    data class ChapterChanged(val chapter: Chapter) : GradioTTSIntegrationEvent()
//
//    /** Book has finished (no more chapters) */
//    object BookFinished : GradioTTSIntegrationEvent()
//
//    /** Sleep timer has expired */
//    object SleepTimerExpired : GradioTTSIntegrationEvent()
//
//    /** Engine has changed */
//    data class EngineChanged(val engineId: String, val engineName: String) : GradioTTSIntegrationEvent()
//
//    /** Error occurred */
//    data class Error(val message: String, val recoverable: Boolean) : GradioTTSIntegrationEvent()
//}
