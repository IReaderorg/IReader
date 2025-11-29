//package ireader.domain.services.tts_service.player
//
//import io.ktor.client.*
//import ireader.core.log.Log
//import ireader.domain.services.tts_service.GradioAudioPlayer
//import ireader.domain.services.tts_service.GradioTTSConfig
//import ireader.domain.services.tts_service.GradioTTSManager
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.*
//
///**
// * Manager for Gradio TTS Player that handles:
// * - Player lifecycle management
// * - Engine switching
// * - Chapter/content management
// * - Integration with the TTS service layer
// *
// * This is the main entry point for using Gradio TTS in the application.
// * It provides a high-level API that abstracts away the complexity of
// * managing players, engines, and content.
// */
//class GradioTTSPlayerManager(
//    private val httpClient: HttpClient,
//    private val audioPlayer: GradioAudioPlayer,
//    private val gradioTTSManager: GradioTTSManager,
//    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
//) {
//    companion object {
//        private const val TAG = "GradioTTSPlayerManager"
//    }
//
//    private val scope = CoroutineScope(dispatcher + SupervisorJob())
//    private var currentPlayer: GradioTTSPlayer? = null
//    private var currentConfig: GradioTTSConfig? = null
//
//    // ==================== State Flows ====================
//
//    private val _currentEngine = MutableStateFlow<String?>(null)
//    val currentEngine: StateFlow<String?> = _currentEngine.asStateFlow()
//
//    private val _availableEngines = MutableStateFlow<List<GradioTTSConfig>>(emptyList())
//    val availableEngines: StateFlow<List<GradioTTSConfig>> = _availableEngines.asStateFlow()
//
//    private val _isInitialized = MutableStateFlow(false)
//    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
//
//    private val _managerError = MutableStateFlow<String?>(null)
//    val managerError: StateFlow<String?> = _managerError.asStateFlow()
//
//    // ==================== Player State Delegation ====================
//
//    val isPlaying: StateFlow<Boolean> get() = currentPlayer?.isPlaying ?: MutableStateFlow(false)
//    val isPaused: StateFlow<Boolean> get() = currentPlayer?.isPaused ?: MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> get() = currentPlayer?.isLoading ?: MutableStateFlow(false)
//    val currentParagraph: StateFlow<Int> get() = currentPlayer?.currentParagraph ?: MutableStateFlow(0)
//    val totalParagraphs: StateFlow<Int> get() = currentPlayer?.totalParagraphs ?: MutableStateFlow(0)
//    val cachedParagraphs: StateFlow<Set<Int>> get() = currentPlayer?.cachedParagraphs ?: MutableStateFlow(emptySet())
//    val speed: StateFlow<Float> get() = currentPlayer?.speed ?: MutableStateFlow(1.0f)
//    val pitch: StateFlow<Float> get() = currentPlayer?.pitch ?: MutableStateFlow(1.0f)
//    val error: StateFlow<String?> get() = currentPlayer?.error ?: MutableStateFlow(null)
//    val hasContent: StateFlow<Boolean> get() = currentPlayer?.hasContent ?: MutableStateFlow(false)
//
//    // ==================== Events ====================
//
//    private val _events = MutableSharedFlow<GradioTTSPlayerManagerEvent>(
//        replay = 0,
//        extraBufferCapacity = 16
//    )
//    val events: SharedFlow<GradioTTSPlayerManagerEvent> = _events.asSharedFlow()
//
//    init {
//        loadAvailableEngines()
//    }
//
//    // ==================== Initialization ====================
//
//    private fun loadAvailableEngines() {
//        scope.launch {
//            try {
//                val engines = gradioTTSManager.getAllConfigs()
//                _availableEngines.value = engines
//                Log.info { "$TAG: Loaded ${engines.size} available engines" }
//
//                // Auto-select first engine if none selected
//                if (_currentEngine.value == null && engines.isNotEmpty()) {
//                    selectEngine(engines.first().id)
//                }
//            } catch (e: Exception) {
//                Log.error { "$TAG: Failed to load engines: ${e.message}" }
//                _managerError.value = "Failed to load TTS engines: ${e.message}"
//            }
//        }
//    }
//
//    /**
//     * Select a TTS engine by ID.
//     * This will create a new player with the selected engine configuration.
//     */
//    fun selectEngine(engineId: String) {
//        scope.launch {
//            try {
//                val config = gradioTTSManager.getConfigById(engineId)
//                if (config == null) {
//                    Log.error { "$TAG: Engine not found: $engineId" }
//                    _managerError.value = "Engine not found: $engineId"
//                    return@launch
//                }
//
//                // Release current player if exists
//                currentPlayer?.release()
//
//                // Create new player
//                val factory = GradioTTSPlayerFactory(httpClient, audioPlayer)
//                currentPlayer = factory.create(config)
//                currentConfig = config
//                _currentEngine.value = engineId
//                _isInitialized.value = true
//                _managerError.value = null
//
//                // Forward player events
//                forwardPlayerEvents()
//
//                Log.info { "$TAG: Selected engine: ${config.name}" }
//                _events.emit(GradioTTSPlayerManagerEvent.EngineChanged(engineId, config.name))
//
//            } catch (e: Exception) {
//                Log.error { "$TAG: Failed to select engine: ${e.message}" }
//                _managerError.value = "Failed to select engine: ${e.message}"
//            }
//        }
//    }
//
//    private fun forwardPlayerEvents() {
//        currentPlayer?.let { player ->
//            scope.launch {
//                player.events.collect { event ->
//                    // Forward relevant events
//                    when (event) {
//                        is GradioTTSPlayerEvent.Error -> {
//                            _events.emit(GradioTTSPlayerManagerEvent.PlayerError(event.message, event.recoverable))
//                        }
//                        is GradioTTSPlayerEvent.ChapterFinished -> {
//                            _events.emit(GradioTTSPlayerManagerEvent.ContentFinished)
//                        }
//                        else -> {
//                            // Other events are handled through state flows
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // ==================== Content Management ====================
//
//    /**
//     * Set content to be read.
//     *
//     * @param paragraphs List of paragraphs to read
//     * @param startIndex Starting paragraph index (default: 0)
//     */
//    fun setContent(paragraphs: List<String>, startIndex: Int = 0) {
//        val player = currentPlayer
//        if (player == null) {
//            Log.error { "$TAG: No player initialized" }
//            _managerError.value = "No TTS engine selected"
//            return
//        }
//
//        player.setContent(paragraphs, startIndex)
//        Log.info { "$TAG: Set content: ${paragraphs.size} paragraphs, starting at $startIndex" }
//    }
//
//    /**
//     * Set content from a chapter.
//     *
//     * @param chapterContent Raw chapter content (will be split into paragraphs)
//     * @param startIndex Starting paragraph index (default: 0)
//     */
//    fun setChapterContent(chapterContent: String, startIndex: Int = 0) {
//        val paragraphs = splitIntoParagraphs(chapterContent)
//        setContent(paragraphs, startIndex)
//    }
//
//    private fun splitIntoParagraphs(content: String): List<String> {
//        return content
//            .split("\n\n", "\r\n\r\n")
//            .map { it.trim() }
//            .filter { it.isNotBlank() }
//    }
//
//    // ==================== Playback Controls ====================
//
//    fun play() {
//        currentPlayer?.play() ?: run {
//            _managerError.value = "No TTS engine selected"
//        }
//    }
//
//    fun pause() {
//        currentPlayer?.pause()
//    }
//
//    fun stop() {
//        currentPlayer?.stop()
//    }
//
//    fun next() {
//        currentPlayer?.next()
//    }
//
//    fun previous() {
//        currentPlayer?.previous()
//    }
//
//    fun jumpTo(index: Int) {
//        currentPlayer?.jumpTo(index)
//    }
//
//    // ==================== Settings ====================
//
//    fun setSpeed(speed: Float) {
//        currentPlayer?.setSpeed(speed)
//    }
//
//    fun setPitch(pitch: Float) {
//        currentPlayer?.setPitch(pitch)
//    }
//
//    fun clearCache() {
//        currentPlayer?.clearCache()
//    }
//
//    // ==================== State ====================
//
//    /**
//     * Get current state snapshot.
//     */
//    fun getStateSnapshot(): GradioTTSPlayerState? {
//        return currentPlayer?.getStateSnapshot()
//    }
//
//    /**
//     * Get current player (for advanced usage).
//     */
//    fun getCurrentPlayer(): GradioTTSPlayer? = currentPlayer
//
//    /**
//     * Get current config.
//     */
//    fun getCurrentConfig(): GradioTTSConfig? = currentConfig
//
//    // ==================== Lifecycle ====================
//
//    /**
//     * Release all resources.
//     */
//    fun release() {
//        Log.info { "$TAG: Releasing manager" }
//        currentPlayer?.release()
//        currentPlayer = null
//        currentConfig = null
//        _currentEngine.value = null
//        _isInitialized.value = false
//        scope.cancel()
//    }
//
//    /**
//     * Refresh available engines.
//     */
//    fun refreshEngines() {
//        loadAvailableEngines()
//    }
//}
//
///**
// * Events emitted by the GradioTTSPlayerManager.
// */
//sealed class GradioTTSPlayerManagerEvent {
//    /** Engine has been changed */
//    data class EngineChanged(val engineId: String, val engineName: String) : GradioTTSPlayerManagerEvent()
//
//    /** Content has finished playing */
//    object ContentFinished : GradioTTSPlayerManagerEvent()
//
//    /** Player error occurred */
//    data class PlayerError(val message: String, val recoverable: Boolean) : GradioTTSPlayerManagerEvent()
//
//    /** Manager error occurred */
//    data class ManagerError(val message: String) : GradioTTSPlayerManagerEvent()
//}
