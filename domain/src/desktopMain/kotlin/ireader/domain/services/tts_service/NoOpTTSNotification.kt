package ireader.domain.services.tts_service

/**
 * No-Op TTS Notification for Desktop
 * 
 * Desktop doesn't use system notifications for TTS playback.
 * The UI handles all playback controls directly.
 */
class NoOpTTSNotification : TTSNotification {
    
    override fun show(data: TTSNotificationData) {
        // Desktop doesn't show system notifications
        // UI handles playback controls directly
    }
    
    override fun hide() {
        // Nothing to hide
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        // Nothing to update
    }
    
    override fun updateProgress(current: Int, total: Int) {
        // Nothing to update
    }
}

/**
 * Factory for creating desktop TTS notifications
 */
object DesktopTTSNotificationFactory {
    fun create(): TTSNotification {
        return NoOpTTSNotification()
    }
}
