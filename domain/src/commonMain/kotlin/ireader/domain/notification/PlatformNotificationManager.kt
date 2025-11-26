package ireader.domain.notification

/**
 * Platform-agnostic notification manager interface.
 * Provides type-safe notification management across all platforms.
 * 
 * This replaces the old NotificationManager with proper type safety.
 */
interface PlatformNotificationManager {
    
    /**
     * Show a notification using platform-agnostic data
     */
    fun show(notification: NotificationData)
    
    /**
     * Show a notification using platform-specific notification object
     * This allows direct platform notification usage when needed
     */
    fun showPlatformNotification(id: Int, platformNotification: Any)
    
    /**
     * Cancel a notification by ID
     */
    fun cancel(id: Int)
    
    /**
     * Cancel all notifications
     */
    fun cancelAll()
    
    /**
     * Check if notifications are enabled/permitted
     */
    fun areNotificationsEnabled(): Boolean
}

/**
 * Factory function to create platform-specific notification manager
 */
expect fun createPlatformNotificationManager(): PlatformNotificationManager
