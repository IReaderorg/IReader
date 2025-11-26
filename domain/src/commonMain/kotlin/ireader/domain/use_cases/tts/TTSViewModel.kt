package ireader.domain.use_cases.tts

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.CommonTTSService
import ireader.domain.services.tts_service.TTSServiceAction
import ireader.domain.services.tts_service.TTSServiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Shared TTS ViewModel
 * Works on both Android and Desktop with platform-specific UI
 */
class TTSViewModel(
    private val ttsService: CommonTTSService
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Expose service state
    val state: TTSServiceState = ttsService.state
    
    // Convenience accessors
    val isPlaying: StateFlow<Boolean> = state.isPlaying
    val isLoading: StateFlow<Boolean> = state.isLoading
    val currentBook: StateFlow<Book?> = state.currentBook
    val currentChapter: StateFlow<Chapter?> = state.currentChapter
    val currentParagraph: StateFlow<Int> = state.currentParagraph
    val totalParagraphs: StateFlow<Int> = state.totalParagraphs
    val currentContent: StateFlow<List<String>> = state.currentContent
    val speechSpeed: StateFlow<Float> = state.speechSpeed
    val speechPitch: StateFlow<Float> = state.speechPitch
    val autoNextChapter: StateFlow<Boolean> = state.autoNextChapter
    val error: StateFlow<String?> = state.error
    val cachedParagraphs: StateFlow<Set<Int>> = state.cachedParagraphs
    val loadingParagraphs: StateFlow<Set<Int>> = state.loadingParagraphs
    
    init {
        ttsService.initialize()
    }
    
    // ========== Actions ==========
    
    fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean = false) {
        scope.launch {
            ttsService.startReading(bookId, chapterId, autoPlay)
        }
    }
    
    fun play() {
        scope.launch {
            ttsService.play()
        }
    }
    
    fun pause() {
        scope.launch {
            ttsService.pause()
        }
    }
    
    fun stop() {
        scope.launch {
            ttsService.stop()
        }
    }
    
    fun togglePlayPause() {
        if (isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    fun nextChapter() {
        scope.launch {
            ttsService.nextChapter()
        }
    }
    
    fun previousChapter() {
        scope.launch {
            ttsService.previousChapter()
        }
    }
    
    fun nextParagraph() {
        scope.launch {
            ttsService.nextParagraph()
        }
    }
    
    fun previousParagraph() {
        scope.launch {
            ttsService.previousParagraph()
        }
    }
    
    fun setSpeed(speed: Float) {
        ttsService.setSpeed(speed)
    }
    
    fun setPitch(pitch: Float) {
        ttsService.setPitch(pitch)
    }
    
    fun jumpToParagraph(index: Int) {
        scope.launch {
            ttsService.jumpToParagraph(index)
        }
    }
    
    fun handleAction(action: TTSServiceAction) {
        when (action) {
            is TTSServiceAction.StartReading -> startReading(action.bookId, action.chapterId, action.autoPlay)
            is TTSServiceAction.Play -> play()
            is TTSServiceAction.Pause -> pause()
            is TTSServiceAction.Stop -> stop()
            is TTSServiceAction.NextChapter -> nextChapter()
            is TTSServiceAction.PreviousChapter -> previousChapter()
            is TTSServiceAction.NextParagraph -> nextParagraph()
            is TTSServiceAction.PreviousParagraph -> previousParagraph()
            is TTSServiceAction.JumpToParagraph -> jumpToParagraph(action.index)
            is TTSServiceAction.SetSpeed -> setSpeed(action.speed)
            is TTSServiceAction.SetPitch -> setPitch(action.pitch)
        }
    }
    
    // ========== Computed Properties ==========
    
    /**
     * Get current paragraph text
     */
    fun getCurrentParagraphText(): String? {
        val content = currentContent.value
        val index = currentParagraph.value
        return content.getOrNull(index)
    }
    
    /**
     * Get progress percentage (0-100)
     */
    fun getProgress(): Float {
        val total = totalParagraphs.value
        if (total == 0) return 0f
        
        val current = currentParagraph.value
        return (current.toFloat() / total.toFloat()) * 100f
    }
    
    /**
     * Check if can go to next chapter
     */
    fun canGoToNextChapter(): Boolean {
        // This would need chapter list from service
        // For now, return true
        return true
    }
    
    /**
     * Check if can go to previous chapter
     */
    fun canGoToPreviousChapter(): Boolean {
        // This would need chapter list from service
        // For now, return true
        return true
    }
    
    /**
     * Check if can go to next paragraph
     */
    fun canGoToNextParagraph(): Boolean {
        return currentParagraph.value < totalParagraphs.value - 1
    }
    
    /**
     * Check if can go to previous paragraph
     */
    fun canGoToPreviousParagraph(): Boolean {
        return currentParagraph.value > 0
    }
    
    /**
     * Get cache status for a paragraph
     */
    fun getParagraphCacheStatus(index: Int): ParagraphCacheStatus {
        return when {
            cachedParagraphs.value.contains(index) -> ParagraphCacheStatus.CACHED
            loadingParagraphs.value.contains(index) -> ParagraphCacheStatus.LOADING
            else -> ParagraphCacheStatus.NOT_CACHED
        }
    }
    
    // ========== Cleanup ==========
    
    fun cleanup() {
        ttsService.cleanup()
    }
}

/**
 * Paragraph cache status
 */
enum class ParagraphCacheStatus {
    NOT_CACHED,
    LOADING,
    CACHED
}
