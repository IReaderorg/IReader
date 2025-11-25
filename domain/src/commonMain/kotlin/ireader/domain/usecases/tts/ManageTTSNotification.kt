package ireader.domain.usecases.tts

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Use case for managing TTS notifications
 * 
 * This use case provides a clean API for showing, updating, and hiding
 * TTS playback notifications across different platforms.
 * 
 * Usage:
 * ```
 * val useCase = ManageTTSNotification(notificationManager)
 * 
 * // Show notification
 * useCase.show(book, chapter, state)
 * 
 * // Update state
 * useCase.update(newState)
 * 
 * // Hide notification
 * useCase.hide()
 * ```
 */
class ManageTTSNotification(
    private val notificationManager: TTSNotificationManager
) {
    
    /**
     * Show TTS notification
     */
    operator fun invoke(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        notificationManager.showNotification(book, chapter, state)
    }
    
    /**
     * Show notification with builder pattern
     */
    fun show(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        notificationManager.showNotification(book, chapter, state)
    }
    
    /**
     * Update notification state
     */
    fun update(state: TTSNotificationState) {
        notificationManager.updateNotification(state)
    }
    
    /**
     * Update specific fields
     */
    fun updatePlaybackState(
        isPlaying: Boolean,
        isPaused: Boolean = false
    ) {
        if (!notificationManager.isNotificationShowing()) return
        
        // Get current state and update only playback fields
        val currentState = getCurrentState() ?: return
        val newState = currentState.copy(
            isPlaying = isPlaying,
            isPaused = isPaused
        )
        notificationManager.updateNotification(newState)
    }
    
    /**
     * Update progress
     */
    fun updateProgress(
        currentParagraph: Int,
        totalParagraphs: Int
    ) {
        if (!notificationManager.isNotificationShowing()) return
        
        val currentState = getCurrentState() ?: return
        val progress = if (totalParagraphs > 0) {
            currentParagraph.toFloat() / totalParagraphs.toFloat()
        } else {
            0f
        }
        
        val newState = currentState.copy(
            currentParagraph = currentParagraph,
            totalParagraphs = totalParagraphs,
            progress = progress
        )
        notificationManager.updateNotification(newState)
    }
    
    /**
     * Update speed
     */
    fun updateSpeed(speed: Float) {
        if (!notificationManager.isNotificationShowing()) return
        
        val currentState = getCurrentState() ?: return
        val newState = currentState.copy(speed = speed)
        notificationManager.updateNotification(newState)
    }
    
    /**
     * Hide notification
     */
    fun hide() {
        notificationManager.hideNotification()
    }
    
    /**
     * Check if notification is showing
     */
    fun isShowing(): Boolean {
        return notificationManager.isNotificationShowing()
    }
    
    /**
     * Set callback for notification actions
     */
    fun setCallback(callback: TTSNotificationCallback) {
        notificationManager.setNotificationCallback(callback)
    }
    
    /**
     * Set callback with lambda functions
     */
    fun setCallback(
        onPlayPause: () -> Unit = {},
        onStop: () -> Unit = {},
        onNext: () -> Unit = {},
        onPrevious: () -> Unit = {},
        onSeek: (Int) -> Unit = {},
        onSpeedChange: (Float) -> Unit = {}
    ) {
        notificationManager.setNotificationCallback(object : TTSNotificationCallback {
            override fun onPlayPause() = onPlayPause()
            override fun onStop() = onStop()
            override fun onNext() = onNext()
            override fun onPrevious() = onPrevious()
            override fun onSeek(position: Int) = onSeek(position)
            override fun onSpeedChange(speed: Float) = onSpeedChange(speed)
        })
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        notificationManager.cleanup()
    }
    
    // Helper to track current state (simplified - in real implementation would query the manager)
    private var cachedState: TTSNotificationState? = null
    
    private fun getCurrentState(): TTSNotificationState? {
        return cachedState
    }
    
    // Update cached state when showing/updating
    private fun cacheState(state: TTSNotificationState) {
        cachedState = state
    }
}

/**
 * Builder for TTSNotificationState
 */
class TTSNotificationStateBuilder {
    private var isPlaying: Boolean = false
    private var isPaused: Boolean = false
    private var currentParagraph: Int = 0
    private var totalParagraphs: Int = 0
    private var progress: Float = 0f
    private var bookTitle: String = ""
    private var chapterTitle: String = ""
    private var bookCoverUrl: String? = null
    private var speed: Float = 1.0f
    private var ttsProvider: String = "Native TTS"
    
    fun playing(isPlaying: Boolean) = apply { this.isPlaying = isPlaying }
    fun paused(isPaused: Boolean) = apply { this.isPaused = isPaused }
    fun paragraph(current: Int, total: Int) = apply {
        this.currentParagraph = current
        this.totalParagraphs = total
        this.progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
    }
    fun bookTitle(title: String) = apply { this.bookTitle = title }
    fun chapterTitle(title: String) = apply { this.chapterTitle = title }
    fun coverUrl(url: String?) = apply { this.bookCoverUrl = url }
    fun speed(speed: Float) = apply { this.speed = speed }
    fun provider(provider: String) = apply { this.ttsProvider = provider }
    
    fun build() = TTSNotificationState(
        isPlaying = isPlaying,
        isPaused = isPaused,
        currentParagraph = currentParagraph,
        totalParagraphs = totalParagraphs,
        progress = progress,
        bookTitle = bookTitle,
        chapterTitle = chapterTitle,
        bookCoverUrl = bookCoverUrl,
        speed = speed,
        ttsProvider = ttsProvider
    )
}

/**
 * Extension function for easy state building
 */
fun ttsNotificationState(block: TTSNotificationStateBuilder.() -> Unit): TTSNotificationState {
    return TTSNotificationStateBuilder().apply(block).build()
}
