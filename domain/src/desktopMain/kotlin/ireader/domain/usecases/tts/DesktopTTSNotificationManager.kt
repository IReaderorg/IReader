package ireader.domain.usecases.tts

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Desktop implementation of TTS notification manager
 * 
 * Uses system tray notifications on desktop platforms.
 * Can be extended to support:
 * - Windows: System tray notifications
 * - macOS: Notification Center
 * - Linux: libnotify or similar
 */
class DesktopTTSNotificationManager : TTSNotificationManager {
    
    private var callback: TTSNotificationCallback? = null
    private var currentState: TTSNotificationState? = null
    private var isShowing = false
    
    override fun showNotification(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        this.currentState = state
        this.isShowing = true
        
        // Desktop notification implementation
        // This can be extended to use platform-specific notification APIs
        Log.info { "Desktop TTS notification: ${book.title} - ${chapter.name}" }
        
        // TODO: Implement platform-specific notifications
        // - Windows: Use Windows Toast Notifications
        // - macOS: Use NSUserNotificationCenter
        // - Linux: Use libnotify
    }
    
    override fun updateNotification(state: TTSNotificationState) {
        if (!isShowing) return
        
        this.currentState = state
        
        Log.debug { "Desktop TTS notification updated: ${state.ttsProvider}" }
        
        // TODO: Update platform-specific notification
    }
    
    override fun hideNotification() {
        isShowing = false
        currentState = null
        
        Log.info { "Desktop TTS notification hidden" }
        
        // TODO: Hide platform-specific notification
    }
    
    override fun isNotificationShowing(): Boolean = isShowing
    
    override fun setNotificationCallback(callback: TTSNotificationCallback) {
        this.callback = callback
    }
    
    override fun cleanup() {
        hideNotification()
        callback = null
    }
}
