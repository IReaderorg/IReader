package ireader.domain.usecases.tts

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Interface for managing TTS playback notifications across platforms
 * 
 * This abstraction allows different platforms (Android, Desktop) to implement
 * their own notification systems while maintaining a consistent API.
 */
interface TTSNotificationManager {
    
    /**
     * Show or update the TTS notification
     * 
     * @param book Current book being read
     * @param chapter Current chapter being read
     * @param state Current playback state
     */
    fun showNotification(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    )
    
    /**
     * Update notification with new state
     * 
     * @param state New playback state
     */
    fun updateNotification(state: TTSNotificationState)
    
    /**
     * Hide/dismiss the notification
     */
    fun hideNotification()
    
    /**
     * Check if notification is currently showing
     */
    fun isNotificationShowing(): Boolean
    
    /**
     * Set callback for notification actions
     * 
     * @param callback Callback to handle user actions from notification
     */
    fun setNotificationCallback(callback: TTSNotificationCallback)
    
    /**
     * Clean up resources
     */
    fun cleanup()
}

/**
 * State of TTS playback for notification display
 */
data class TTSNotificationState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentParagraph: Int = 0,
    val totalParagraphs: Int = 0,
    val progress: Float = 0f,
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val bookCoverUrl: String? = null,
    val speed: Float = 1.0f,
    val ttsProvider: String = "Native TTS"
)

/**
 * Callback interface for notification actions
 */
interface TTSNotificationCallback {
    fun onPlayPause()
    fun onStop()
    fun onNext()
    fun onPrevious()
    fun onSeek(position: Int)
    fun onSpeedChange(speed: Float)
}

/**
 * Default no-op implementation
 */
class NoOpTTSNotificationManager : TTSNotificationManager {
    override fun showNotification(book: Book, chapter: Chapter, state: TTSNotificationState) {}
    override fun updateNotification(state: TTSNotificationState) {}
    override fun hideNotification() {}
    override fun isNotificationShowing(): Boolean = false
    override fun setNotificationCallback(callback: TTSNotificationCallback) {}
    override fun cleanup() {}
}
