package ireader.domain.services.tts_service

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.StateFlow

/**
 * Common TTS Service Interface
 * Shared logic for both Android and Desktop platforms
 */
interface CommonTTSService {
    /**
     * Current TTS state
     */
    val state: TTSServiceState
    
    /**
     * Initialize the service
     */
    fun initialize()
    
    /**
     * Start reading from a specific book and chapter
     */
    suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean = false)
    
    /**
     * Play/resume reading
     */
    suspend fun play()
    
    /**
     * Pause reading
     */
    suspend fun pause()
    
    /**
     * Stop reading and cleanup
     */
    suspend fun stop()
    
    /**
     * Skip to next chapter
     */
    suspend fun nextChapter()
    
    /**
     * Skip to previous chapter
     */
    suspend fun previousChapter()
    
    /**
     * Skip to next paragraph
     */
    suspend fun nextParagraph()
    
    /**
     * Skip to previous paragraph
     */
    suspend fun previousParagraph()
    
    /**
     * Set speech speed (0.5 - 2.0)
     */
    fun setSpeed(speed: Float)
    
    /**
     * Set speech pitch (0.5 - 2.0)
     */
    fun setPitch(pitch: Float)
    
    /**
     * Jump to specific paragraph
     */
    suspend fun jumpToParagraph(index: Int)
    
    /**
     * Set custom content to read (e.g., translated content)
     * If null, uses the original chapter content
     */
    fun setCustomContent(content: List<String>?)
    
    /**
     * Get available TTS engines for this platform
     */
    fun getAvailableEngines(): List<String>
    
    /**
     * Get current engine name
     */
    fun getCurrentEngineName(): String
    
    /**
     * Check if service is initialized and ready
     */
    fun isReady(): Boolean
    
    /**
     * Cleanup resources
     */
    fun cleanup()
    
    /**
     * Set sleep timer
     * @param minutes Duration in minutes (0 to disable)
     */
    fun setSleepTimer(minutes: Int)
    
    /**
     * Cancel sleep timer
     */
    fun cancelSleepTimer()
    
    /**
     * Toggle auto-next chapter
     */
    fun setAutoNextChapter(enabled: Boolean)
}

/**
 * TTS Service State
 * Shared state across platforms
 */
interface TTSServiceState {
    val isPlaying: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val currentBook: StateFlow<Book?>
    val currentChapter: StateFlow<Chapter?>
    val currentParagraph: StateFlow<Int>
    val previousParagraph: StateFlow<Int>  // For UI highlighting
    val totalParagraphs: StateFlow<Int>
    val currentContent: StateFlow<List<String>>
    val speechSpeed: StateFlow<Float>
    val speechPitch: StateFlow<Float>
    val autoNextChapter: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    // Caching state (for Coqui TTS)
    val cachedParagraphs: StateFlow<Set<Int>>
    val loadingParagraphs: StateFlow<Set<Int>>
    
    // Sleep timer state
    val sleepTimeRemaining: StateFlow<Long>  // Remaining time in milliseconds
    val sleepModeEnabled: StateFlow<Boolean>
    
    // Audio focus state
    val hasAudioFocus: StateFlow<Boolean>
}

/**
 * TTS Service Actions
 * Commands that can be sent to the service
 */
sealed class TTSServiceAction {
    data class StartReading(val bookId: Long, val chapterId: Long, val autoPlay: Boolean = false) : TTSServiceAction()
    object Play : TTSServiceAction()
    object Pause : TTSServiceAction()
    object Stop : TTSServiceAction()
    object NextChapter : TTSServiceAction()
    object PreviousChapter : TTSServiceAction()
    object NextParagraph : TTSServiceAction()
    object PreviousParagraph : TTSServiceAction()
    data class JumpToParagraph(val index: Int) : TTSServiceAction()
    data class SetSpeed(val speed: Float) : TTSServiceAction()
    data class SetPitch(val pitch: Float) : TTSServiceAction()
}

/**
 * TTS Service Events
 * Events emitted by the service
 */
sealed class TTSServiceEvent {
    object Started : TTSServiceEvent()
    object Paused : TTSServiceEvent()
    object Stopped : TTSServiceEvent()
    object Resumed : TTSServiceEvent()
    data class ChapterChanged(val chapter: Chapter) : TTSServiceEvent()
    data class ParagraphChanged(val index: Int) : TTSServiceEvent()
    data class Error(val message: String) : TTSServiceEvent()
    object ChapterFinished : TTSServiceEvent()
    object BookFinished : TTSServiceEvent()
}
