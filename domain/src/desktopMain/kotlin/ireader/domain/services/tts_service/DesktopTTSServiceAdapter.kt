package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    
    override fun cleanup() {
        service.shutdown()
    }
}

/**
 * Adapter that makes DesktopTTSState implement TTSServiceState
 * 
 * Converts mutable state properties to StateFlow for reactive UI updates.
 */
class DesktopTTSStateAdapter(
    private val desktopState: DesktopTTSState
) : TTSServiceState {
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentBook = MutableStateFlow<Book?>(null)
    override val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()
    
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    override val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()
    
    private val _currentParagraph = MutableStateFlow(0)
    override val currentParagraph: StateFlow<Int> = _currentParagraph.asStateFlow()
    
    private val _totalParagraphs = MutableStateFlow(0)
    override val totalParagraphs: StateFlow<Int> = _totalParagraphs.asStateFlow()
    
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    override val currentContent: StateFlow<List<String>> = _currentContent.asStateFlow()
    
    private val _speechSpeed = MutableStateFlow(1.0f)
    override val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()
    
    private val _speechPitch = MutableStateFlow(1.0f)
    override val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()
    
    private val _autoNextChapter = MutableStateFlow(false)
    override val autoNextChapter: StateFlow<Boolean> = _autoNextChapter.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs.asStateFlow()
    
    init {
        // Sync state from DesktopTTSState to StateFlows
        // In a real implementation, this would use coroutines to observe changes
        syncState()
    }
    
    private fun syncState() {
        _isPlaying.value = desktopState.isPlaying.value
        _currentBook.value = desktopState.ttsBook.value
        _currentChapter.value = desktopState.ttsChapter.value
        _currentParagraph.value = desktopState.currentReadingParagraph.value
        _currentContent.value = desktopState.ttsContent.value ?: emptyList()
        _totalParagraphs.value = desktopState.ttsContent.value?.size ?: 0
        _speechSpeed.value = desktopState.speechSpeed.value
        _speechPitch.value = desktopState.pitch.value
        _autoNextChapter.value = desktopState.autoNextChapter.value
        _isLoading.value = desktopState.isLoading.value
    }
    
    /**
     * Update state flows when desktop state changes
     * Should be called periodically or on state changes
     */
    fun updateStateFlows() {
        syncState()
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
