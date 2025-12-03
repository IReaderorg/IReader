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
    override fun show(notification: NotificationData) {
        // TODO: Implement using UNUserNotificationCenter
    }
    
    override fun showPlatformNotification(id: Int, platformNotification: Any) {
        // TODO: Implement using UNUserNotificationCenter
    }
    
    override fun cancel(id: Int) {
        // TODO: Implement
    }
    
    override fun cancelAll() {
        // TODO: Implement
    }
    
    override fun areNotificationsEnabled(): Boolean {
        // TODO: Check UNUserNotificationCenter authorization status
        return false
    }
}
