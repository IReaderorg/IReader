package ireader.domain.services.tts_service

/**
 * iOS implementation of TTS Notification Factory
 * 
 * TODO: Full implementation using UserNotifications framework
 */
actual object TTSNotificationFactory {
    actual fun create(callback: TTSNotificationCallback): TTSNotification {
        return IosTTSNotification(callback)
    }
}

/**
 * iOS TTS Notification implementation stub
 */
private class IosTTSNotification(
    private val callback: TTSNotificationCallback
) : TTSNotification {
    
    override fun show(data: TTSNotificationData) {
        // TODO: Implement using UserNotifications
    }
    
    override fun hide() {
        // TODO: Implement
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        // TODO: Implement
    }
    
    override fun updateProgress(current: Int, total: Int) {
        // TODO: Implement
    }
}
