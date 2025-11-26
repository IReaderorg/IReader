package ireader.domain.services.common

/**
 * Common notification service interface for both Android and Desktop
 */
interface NotificationService : PlatformService {
    /**
     * Show a simple notification
     */
    fun showNotification(
        id: Int,
        title: String,
        message: String,
        priority: NotificationPriority = NotificationPriority.DEFAULT
    )
    
    /**
     * Show a progress notification
     */
    fun showProgressNotification(
        id: Int,
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean = false
    )
    
    /**
     * Update existing notification
     */
    fun updateNotification(
        id: Int,
        title: String? = null,
        message: String? = null,
        progress: Int? = null,
        maxProgress: Int? = null
    )
    
    /**
     * Cancel notification
     */
    fun cancelNotification(id: Int)
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications()
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean
}

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}
