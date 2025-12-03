package ireader.domain.notification

/**
 * iOS implementation of PlatformNotificationManager
 * 
 * TODO: Implement using UserNotifications framework
 */
actual fun createPlatformNotificationManager(): PlatformNotificationManager {
    return IosPlatformNotificationManager()
}

private class IosPlatformNotificationManager : PlatformNotificationManager {
    override fun showNotification(
        id: Int,
        title: String,
        content: String,
        channelId: String
    ) {
        // TODO: Implement using UNUserNotificationCenter
    }
    
    override fun cancelNotification(id: Int) {
        // TODO: Implement
    }
    
    override fun cancelAllNotifications() {
        // TODO: Implement
    }
    
    override fun createNotificationChannel(
        id: String,
        name: String,
        description: String,
        importance: Int
    ) {
        // No-op on iOS - channels are an Android concept
    }
}
