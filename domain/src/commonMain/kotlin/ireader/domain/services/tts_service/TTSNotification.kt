package ireader.domain.services.tts_service

/**
 * Cross-platform TTS Notification abstraction
 * Allows different notification implementations on Android and Desktop
 */
interface TTSNotification {
    /**
     * Show or update the TTS notification
     */
    fun show(data: TTSNotificationData)
    
    /**
     * Hide the notification
     */
    fun hide()
    
    /**
     * Update notification state (playing/paused)
     */
    fun updatePlaybackState(isPlaying: Boolean)
    
    /**
     * Update progress
     */
    fun updateProgress(current: Int, total: Int)
}

/**
 * Data for TTS notification display
 */
data class TTSNotificationData(
    val title: String,
    val subtitle: String,
    val coverUrl: String? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentParagraph: Int = 0,
    val totalParagraphs: Int = 0,
    val bookId: Long = -1,
    val chapterId: Long = -1,
    val sourceId: Long = -1
)

/**
 * Callback interface for notification actions
 */
interface TTSNotificationCallback {
    fun onPlay()
    fun onPause()
    fun onNext()
    fun onPrevious()
    fun onNextParagraph()
    fun onPreviousParagraph()
    fun onClose()
    fun onNotificationClick()
}

/**
 * Factory for creating platform-specific notifications
 */
expect object TTSNotificationFactory {
    /**
     * Create platform-specific TTS notification
     */
    fun create(callback: TTSNotificationCallback): TTSNotification
}
