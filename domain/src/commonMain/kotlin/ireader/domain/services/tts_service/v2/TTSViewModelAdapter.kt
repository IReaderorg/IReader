package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * TTS ViewModel Adapter - Bridges TTSController with UI layer
 * 
 * Provides convenient properties and methods for Compose UI.
 * This adapter transforms the v2 state into UI-friendly formats.
 */
class TTSViewModelAdapter(
    private val controller: TTSController,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "TTSViewModelAdapter"
    }
    
    // Direct state access
    val state: StateFlow<TTSState> = controller.state
    val events = controller.events
    
    // Convenience properties derived from state
    val isPlaying: StateFlow<Boolean> = state.map { it.isPlaying }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val isPaused: StateFlow<Boolean> = state.map { it.isPaused }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val isLoading: StateFlow<Boolean> = state.map { it.isLoading }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val currentParagraph: StateFlow<Int> = state.map { it.currentParagraphIndex }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val totalParagraphs: StateFlow<Int> = state.map { it.totalParagraphs }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val progress: StateFlow<Float> = state.map { it.progress }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0f)
    
    val chapterTitle: StateFlow<String> = state.map { it.chapter?.name ?: "" }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), "")
    
    val bookTitle: StateFlow<String> = state.map { it.book?.title ?: "" }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), "")
    
    val speed: StateFlow<Float> = state.map { it.speed }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 1.0f)
    
    val hasError: StateFlow<Boolean> = state.map { it.error != null }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val errorMessage: StateFlow<String?> = state.map { error ->
        when (val e = error.error) {
            is TTSError.EngineInitFailed -> "Engine init failed: ${e.message}"
            is TTSError.ContentLoadFailed -> "Failed to load content: ${e.message}"
            is TTSError.SpeechFailed -> "Speech failed: ${e.message}"
            is TTSError.NetworkError -> "Network error: ${e.message}"
            is TTSError.NoContent -> "No content to read"
            is TTSError.EngineNotReady -> "TTS engine not ready"
            null -> null
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    
    val engineType: StateFlow<EngineType> = state.map { it.engineType }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), EngineType.NATIVE)
    
    val isEngineReady: StateFlow<Boolean> = state.map { it.isEngineReady }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val isUsingGradio: StateFlow<Boolean> = state.map { it.engineType == EngineType.GRADIO }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    // Chunk mode state
    val chunkModeEnabled: StateFlow<Boolean> = state.map { it.chunkModeEnabled }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val currentChunkIndex: StateFlow<Int> = state.map { it.currentChunkIndex }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val totalChunks: StateFlow<Int> = state.map { it.totalChunks }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    val chunkProgress: StateFlow<Float> = state.map { it.chunkProgress }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0f)
    
    val isCurrentChunkCached: StateFlow<Boolean> = state.map { it.isCurrentChunkCached }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val allChunksCached: StateFlow<Boolean> = state.map { it.allChunksCached }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    // Translation state
    val hasTranslation: StateFlow<Boolean> = state.map { it.hasTranslation }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val showTranslation: StateFlow<Boolean> = state.map { it.showTranslation }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val bilingualMode: StateFlow<Boolean> = state.map { it.bilingualMode }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val translatedParagraphs: StateFlow<List<String>?> = state.map { it.translatedParagraphs }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    
    // Sentence highlighting state
    val paragraphStartTime: StateFlow<Long> = state.map { it.paragraphStartTime }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0L)
    
    val sentenceHighlightEnabled: StateFlow<Boolean> = state.map { it.sentenceHighlightEnabled }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    val calibratedWPM: StateFlow<Float?> = state.map { it.calibratedWPM }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    
    val isCalibrated: StateFlow<Boolean> = state.map { it.isCalibrated }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
    
    // Previous paragraph for highlighting
    val previousParagraph: StateFlow<Int> = state.map { it.previousParagraphIndex }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    
    // ========== Actions ==========
    
    fun initialize() {
        Log.warn { "$TAG: initialize()" }
        controller.dispatch(TTSCommand.Initialize)
    }
    
    fun loadChapter(bookId: Long, chapterId: Long, startParagraph: Int = 0) {
        Log.warn { "$TAG: loadChapter(bookId=$bookId, chapterId=$chapterId, startParagraph=$startParagraph)" }
        controller.dispatch(TTSCommand.LoadChapter(bookId, chapterId, startParagraph))
    }
    
    fun play() {
        Log.warn { "$TAG: play()" }
        controller.dispatch(TTSCommand.Play)
    }
    
    fun pause() {
        Log.warn { "$TAG: pause()" }
        controller.dispatch(TTSCommand.Pause)
    }
    
    fun stop() {
        Log.warn { "$TAG: stop()" }
        controller.dispatch(TTSCommand.Stop)
    }
    
    fun togglePlayPause() {
        val currentState = state.value
        if (currentState.isPlaying) {
            pause()
        } else {
            play()
        }
    }
    
    fun nextParagraph() {
        Log.warn { "$TAG: nextParagraph()" }
        controller.dispatch(TTSCommand.NextParagraph)
    }
    
    fun previousParagraph() {
        Log.warn { "$TAG: previousParagraph()" }
        controller.dispatch(TTSCommand.PreviousParagraph)
    }
    
    fun jumpToParagraph(index: Int) {
        Log.warn { "$TAG: jumpToParagraph($index)" }
        controller.dispatch(TTSCommand.JumpToParagraph(index))
    }
    
    fun nextChapter() {
        Log.warn { "$TAG: nextChapter()" }
        controller.dispatch(TTSCommand.NextChapter)
    }
    
    fun previousChapter() {
        Log.warn { "$TAG: previousChapter()" }
        controller.dispatch(TTSCommand.PreviousChapter)
    }
    
    fun setSpeed(speed: Float) {
        Log.warn { "$TAG: setSpeed($speed)" }
        controller.dispatch(TTSCommand.SetSpeed(speed))
    }
    
    fun setPitch(pitch: Float) {
        Log.warn { "$TAG: setPitch($pitch)" }
        controller.dispatch(TTSCommand.SetPitch(pitch))
    }
    
    fun setAutoNextChapter(enabled: Boolean) {
        Log.warn { "$TAG: setAutoNextChapter($enabled)" }
        controller.dispatch(TTSCommand.SetAutoNextChapter(enabled))
    }
    
    fun setEngine(type: EngineType) {
        Log.warn { "$TAG: setEngine($type)" }
        controller.dispatch(TTSCommand.SetEngine(type))
    }
    
    fun setGradioConfig(config: GradioConfig) {
        Log.warn { "$TAG: setGradioConfig(${config.name})" }
        controller.dispatch(TTSCommand.SetGradioConfig(config))
    }
    
    fun useNativeTTS() {
        setEngine(EngineType.NATIVE)
    }
    
    fun useGradioTTS(config: GradioConfig) {
        setGradioConfig(config)
        setEngine(EngineType.GRADIO)
    }
    
    // ========== Translation Actions ==========
    
    fun setTranslatedContent(paragraphs: List<String>?) {
        Log.warn { "$TAG: setTranslatedContent(${paragraphs?.size ?: 0} paragraphs)" }
        controller.dispatch(TTSCommand.SetTranslatedContent(paragraphs))
    }
    
    fun toggleTranslation() {
        val current = state.value.showTranslation
        Log.warn { "$TAG: toggleTranslation() -> ${!current}" }
        controller.dispatch(TTSCommand.ToggleTranslation(!current))
    }
    
    fun setShowTranslation(show: Boolean) {
        Log.warn { "$TAG: setShowTranslation($show)" }
        controller.dispatch(TTSCommand.ToggleTranslation(show))
    }
    
    fun toggleBilingualMode() {
        val current = state.value.bilingualMode
        Log.warn { "$TAG: toggleBilingualMode() -> ${!current}" }
        controller.dispatch(TTSCommand.ToggleBilingualMode(!current))
    }
    
    fun setBilingualMode(enabled: Boolean) {
        Log.warn { "$TAG: setBilingualMode($enabled)" }
        controller.dispatch(TTSCommand.ToggleBilingualMode(enabled))
    }
    
    // ========== Sentence Highlighting Actions ==========
    
    fun setSentenceHighlight(enabled: Boolean) {
        Log.warn { "$TAG: setSentenceHighlight($enabled)" }
        controller.dispatch(TTSCommand.SetSentenceHighlight(enabled))
    }
    
    fun updateParagraphStartTime(timeMs: Long) {
        controller.dispatch(TTSCommand.UpdateParagraphStartTime(timeMs))
    }
    
    fun setCalibration(wpm: Float?, isCalibrated: Boolean) {
        controller.dispatch(TTSCommand.SetCalibration(wpm, isCalibrated))
    }
    
    // ========== Chunk Mode Actions ==========
    
    fun enableChunkMode(targetWordCount: Int = 50) {
        Log.warn { "$TAG: enableChunkMode($targetWordCount)" }
        controller.dispatch(TTSCommand.EnableChunkMode(targetWordCount))
    }
    
    fun disableChunkMode() {
        Log.warn { "$TAG: disableChunkMode()" }
        controller.dispatch(TTSCommand.DisableChunkMode)
    }
    
    fun nextChunk() {
        Log.warn { "$TAG: nextChunk()" }
        controller.dispatch(TTSCommand.NextChunk)
    }
    
    fun previousChunk() {
        Log.warn { "$TAG: previousChunk()" }
        controller.dispatch(TTSCommand.PreviousChunk)
    }
    
    fun jumpToChunk(index: Int) {
        Log.warn { "$TAG: jumpToChunk($index)" }
        controller.dispatch(TTSCommand.JumpToChunk(index))
    }
    
    /**
     * Enable Gradio TTS with chunk mode for efficient remote playback
     */
    fun useGradioTTSWithChunks(config: GradioConfig, targetWordCount: Int = 50) {
        useGradioTTS(config)
        enableChunkMode(targetWordCount)
    }
    
    /**
     * Generate audio data for text (for caching/download)
     * Only supported by remote TTS engines (Gradio).
     * 
     * @param text Text to convert to audio
     * @return Audio data as ByteArray, or null if not supported
     */
    suspend fun generateAudioForText(text: String): ByteArray? {
        return controller.generateAudioForText(text)
    }
    
    fun cleanup() {
        Log.warn { "$TAG: cleanup()" }
        controller.dispatch(TTSCommand.Cleanup)
    }
    
    fun destroy() {
        Log.warn { "$TAG: destroy()" }
        controller.destroy()
    }
}
