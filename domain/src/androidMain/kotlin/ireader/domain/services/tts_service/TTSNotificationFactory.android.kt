package ireader.domain.services.tts_service

/**
 * Android implementation of TTS Notification Factory
 * 
 * Note: TTSV2Service handles its own notifications internally using MediaStyle notifications.
 * This factory provides a stub implementation for backward compatibility with the expect/actual pattern.
 */
actual object TTSNotificationFactory {
    actual fun create(callback: TTSNotificationCallback): TTSNotification {
        return AndroidTTSNotificationStub()
    }
}

/**
 * Stub implementation - TTSV2Service handles notifications directly
 */
private class AndroidTTSNotificationStub : TTSNotification {
    override fun show(data: TTSNotificationData) {
        // TTSV2Service handles notifications directly
    }
    
    override fun hide() {
        // TTSV2Service handles notifications directly
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        // TTSV2Service handles notifications directly
    }
    
    override fun updateProgress(current: Int, total: Int) {
        // TTSV2Service handles notifications directly
    }
}
