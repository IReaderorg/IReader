package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Adapter that makes DesktopTTSService implement CommonTTSService
 * 
 * This adapter bridges the gap between the existing DesktopTTSService implementation
 * and the CommonTTSService interface, enabling polymorphic usage and shared testing.
 */
class DesktopTTSServiceAdapter(
    private val service: DesktopTTSService
) : CommonTTSService {
    
    // Adapt DesktopTTSState to TTSServiceState
    override val state: TTSServiceState = DesktopTTSStateAdapter(service.state)
    
    override fun initialize() {
        service.initialize()
    }
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        service.startReading(bookId, chapterId, autoPlay)
    }
    
    override suspend fun play() {
        service.startService(DesktopTTSService.ACTION_PLAY)
    }
    
    override suspend fun pause() {
        service.startService(DesktopTTSService.ACTION_PAUSE)
    }
    
    override suspend fun stop() {
        service.startService(DesktopTTSService.ACTION_STOP)
    }
    
    override suspend fun nextChapter() {
        service.startService(DesktopTTSService.ACTION_SKIP_NEXT)
    }
    
    override suspend fun previousChapter() {
        service.startService(DesktopTTSService.ACTION_SKIP_PREV)
    }
    
    override suspend fun nextParagraph() {
        service.startService(DesktopTTSService.ACTION_NEXT_PAR)
    }
    
    override suspend fun previousParagraph() {
        service.startService(DesktopTTSService.ACTION_PREV_PAR)
    }
    
    override fun setSpeed(speed: Float) {
        service.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        service.setPitch(pitch)
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        service.state.setCurrentReadingParagraph(index)
        if (service.state.isPlaying.value) {
            pause()
            play()
        }
    }
    
    override fun getAvailableEngines(): List<String> {
        return service.getAvailableEngines().map { it.name }
    }
    
    override fun getCurrentEngineName(): String {
        return service.getCurrentEngine().name
    }
    
    override fun isReady(): Boolean {
        return service.synthesizer.isInitialized() || 
               service.kokoroAvailable || 
               service.mayaAvailable
    }
    
    override fun setCustomContent(content: List<String>?) {
        // Set custom content (e.g., translated text) to be read
        // Uses the translatedTTSContent property which is designed for this purpose
        service.state.setTranslatedTTSContent(content)
    }
    
    @OptIn(ExperimentalTime::class)
    override fun setSleepTimer(minutes: Int) {
        service.state.setSleepTime(minutes.toLong())
        service.state.setSleepMode(minutes > 0)
        if (minutes > 0) {
            service.state.setStartTime(kotlin.time.Clock.System.now())
        }
    }
    
    override fun cancelSleepTimer() {
        service.state.setSleepMode(false)
        service.state.setSleepTime(0)
    }
    
    override fun setAutoNextChapter(enabled: Boolean) {
        service.state.setAutoNextChapter(enabled)
    }
    
    override fun cleanup() {
        service.shutdown()
    }
}

/**
 * Adapter that makes DesktopTTSState implement TTSServiceState
 * 
 * Converts mutable state properties to StateFlow for reactive UI updates.
 * Uses coroutines to observe changes from the legacy state.
 */
@OptIn(ExperimentalTime::class)
class DesktopTTSStateAdapter(
    private val desktopState: DesktopTTSState
) : TTSServiceState {
    
    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob()
    )
    
    // Delegate directly to the legacy state flows
    override val isPlaying: StateFlow<Boolean>
        get() = desktopState.isPlaying
    
    override val isLoading: StateFlow<Boolean>
        get() = desktopState.isLoading
    
    override val currentBook: StateFlow<Book?>
        get() = desktopState.ttsBook
    
    override val currentChapter: StateFlow<Chapter?>
        get() = desktopState.ttsChapter
    
    override val currentParagraph: StateFlow<Int>
        get() = desktopState.currentReadingParagraph
    
    override val previousParagraph: StateFlow<Int>
        get() = desktopState.previousReadingParagraph
    
    // Timestamp when TTS actually starts speaking current paragraph (for highlighter sync)
    private val _paragraphSpeakingStartTime = MutableStateFlow(0L)
    override val paragraphSpeakingStartTime: StateFlow<Long> = _paragraphSpeakingStartTime.asStateFlow()
    
    // Derived state flows
    private val _totalParagraphs = MutableStateFlow(0)
    override val totalParagraphs: StateFlow<Int> = _totalParagraphs.asStateFlow()
    
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    override val currentContent: StateFlow<List<String>> = _currentContent.asStateFlow()
    
    override val speechSpeed: StateFlow<Float>
        get() = desktopState.speechSpeed
    
    override val speechPitch: StateFlow<Float>
        get() = desktopState.pitch
    
    override val autoNextChapter: StateFlow<Boolean>
        get() = desktopState.autoNextChapter
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs.asStateFlow()
    
    // Sleep timer state
    private val _sleepTimeRemaining = MutableStateFlow(0L)
    override val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining.asStateFlow()
    
    override val sleepModeEnabled: StateFlow<Boolean>
        get() = desktopState.sleepMode
    
    // Audio focus state (always true on desktop)
    private val _hasAudioFocus = MutableStateFlow(true)
    override val hasAudioFocus: StateFlow<Boolean> = _hasAudioFocus.asStateFlow()
    
    // TTS engine ready state (desktop engines are always ready after init)
    private val _isTTSReady = MutableStateFlow(true)
    override val isTTSReady: StateFlow<Boolean> = _isTTSReady.asStateFlow()
    
    init {
        // Observe ttsContent changes and update derived state
        scope.launch {
            desktopState.ttsContent.collect { content ->
                _currentContent.value = content ?: emptyList()
                _totalParagraphs.value = content?.size ?: 0
            }
        }
        
        // Periodically update sleep timer remaining
        scope.launch {
            while (true) {
                if (desktopState.sleepMode.value) {
                    val startTime = desktopState.startTime.value
                    val sleepMinutes = desktopState.sleepTime.value
                    if (startTime != null && sleepMinutes > 0) {
                        val now = kotlin.time.Clock.System.now()
                        val elapsed = now - startTime
                        val totalMs = sleepMinutes * 60 * 1000L
                        val remaining = (totalMs - elapsed.inWholeMilliseconds).coerceAtLeast(0)
                        _sleepTimeRemaining.value = remaining
                    }
                } else {
                    _sleepTimeRemaining.value = 0L
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    /**
     * Update state flows when desktop state changes
     * Should be called periodically or on state changes
     */
    fun updateStateFlows() {
        _currentContent.value = desktopState.ttsContent.value ?: emptyList()
        _totalParagraphs.value = desktopState.ttsContent.value?.size ?: 0
    }
    
    /**
     * Set error message
     */
    fun setError(message: String?) {
        _error.value = message
    }
}

/**
 * Adapter to wrap DesktopTTSService as a TTSEngine
 */
class DesktopTTSEngineAdapter(
    private val service: DesktopTTSService
) : TTSEngine {
    
    private var callback: TTSEngineCallback? = null
    
    override suspend fun speak(text: String, utteranceId: String) {
        callback?.onStart(utteranceId)
        
        try {
            // The service handles speaking internally
            // We just need to trigger it
            service.startService(DesktopTTSService.ACTION_PLAY)
            
            // Callback will be triggered by service state changes
        } catch (e: Exception) {
            ireader.core.log.Log.error { "Error speaking: ${e.message}" }
            callback?.onError(utteranceId, e.message ?: "Unknown error")
        }
    }
    
    override fun stop() {
        service.startService(DesktopTTSService.ACTION_STOP)
    }
    
    override fun pause() {
        service.startService(DesktopTTSService.ACTION_PAUSE)
    }
    
    override fun resume() {
        service.startService(DesktopTTSService.ACTION_PLAY)
    }
    
    override fun setSpeed(speed: Float) {
        service.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        service.setPitch(pitch)
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean {
        return service.synthesizer.isInitialized() || 
               service.kokoroAvailable || 
               service.mayaAvailable
    }
    
    override fun cleanup() {
        service.shutdown()
    }
    
    override fun getEngineName(): String {
        return service.getCurrentEngine().name
    }
}
