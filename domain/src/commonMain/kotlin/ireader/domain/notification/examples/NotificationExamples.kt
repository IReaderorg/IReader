package ireader.domain.notification.examples

import ireader.domain.notification.*

/**
 * Example usage of the new type-safe notification system.
 * These examples demonstrate best practices for different notification scenarios.
 */
class NotificationExamples(
    private val notificationManager: PlatformNotificationManager
) {
    
    /**
     * Example 1: Simple notification
     */
    fun showSimpleNotification() {
        notificationManager.showSimple(
            id = 1,
            channelId = "general",
            title = "Hello",
            content = "This is a simple notification",
            autoCancel = true
        )
    }
    
    /**
     * Example 2: Progress notification
     */
    fun showProgressNotification(current: Int, total: Int) {
        notificationManager.showProgress(
            id = 2,
            channelId = "downloads",
            title = "Downloading",
            content = "$current of $total chapters",
            max = total,
            current = current,
            ongoing = true
        )
    }
    
    /**
     * Example 3: Using builder DSL for complex notifications
     */
    fun showComplexNotification() {
        val notification = buildNotification(
            id = 3,
            channelId = "updates"
        ) {
            title = "Library Update"
            content = "Found 5 new chapters"
            priority = NotificationPriority.DEFAULT
            autoCancel = true
            setBigTextStyle(
                "Found new chapters in:\n" +
                "- Book 1: 2 chapters\n" +
                "- Book 2: 3 chapters"
            )
        }
        
        notificationManager.show(notification)
    }
    
    /**
     * Example 4: Notification with actions
     */
    fun showNotificationWithActions(pendingIntent: Any?) {
        val notification = buildNotification(
            id = 4,
            channelId = "tts"
        ) {
            title = "Reading Chapter 5"
            content = "The Adventure Begins"
            ongoing = true
            priority = NotificationPriority.LOW
            
            // Add action buttons (pendingIntent is platform-specific)
            addAction("Pause", pendingIntent)
            addAction("Stop", pendingIntent)
            addAction("Next", pendingIntent)
        }
        
        notificationManager.show(notification)
    }
    
    /**
     * Example 5: Indeterminate progress
     */
    fun showIndeterminateProgress() {
        val notification = buildNotification(
            id = 5,
            channelId = "sync"
        ) {
            title = "Syncing"
            content = "Please wait..."
            setProgress(max = 0, current = 0, indeterminate = true)
            ongoing = true
            priority = NotificationPriority.LOW
        }
        
        notificationManager.show(notification)
    }
    
    /**
     * Example 6: Update existing notification
     */
    fun updateNotification(progress: Int) {
        // Just call show() again with the same ID
        notificationManager.showProgress(
            id = 2,
            channelId = "downloads",
            title = "Downloading",
            content = "$progress% complete",
            max = 100,
            current = progress
        )
    }
    
    /**
     * Example 7: Cancel notification
     */
    fun cancelNotification() {
        notificationManager.cancel(2)
    }
    
    /**
     * Example 8: Check if notifications are enabled
     */
    fun checkNotificationPermission(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    /**
     * Example 9: Platform-specific notification (when you need Android-specific features)
     */
    fun showPlatformSpecificNotification(platformNotification: Any) {
        // This allows you to use platform-specific notification objects
        // when the abstraction doesn't cover your use case
        notificationManager.showPlatformNotification(
            id = 10,
            platformNotification = platformNotification
        )
    }
}

/**
 * Example: Download service using type-safe notifications
 */
class DownloadNotificationHelper(
    private val notificationManager: PlatformNotificationManager
) {
    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "downloads"
    }
    
    fun showStarting(bookName: String) {
        notificationManager.showProgress(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "Starting download",
            content = bookName,
            max = 0,
            current = 0,
            indeterminate = true,
            ongoing = true
        )
    }
    
    fun updateProgress(bookName: String, current: Int, total: Int) {
        notificationManager.showProgress(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "Downloading $bookName",
            content = "$current of $total chapters",
            max = total,
            current = current,
            ongoing = true
        )
    }
    
    fun showComplete(bookName: String, chaptersDownloaded: Int) {
        notificationManager.showSimple(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "Download complete",
            content = "$bookName - $chaptersDownloaded chapters",
            autoCancel = true
        )
    }
    
    fun showError(bookName: String, error: String) {
        val notification = buildNotification(NOTIFICATION_ID, CHANNEL_ID) {
            title = "Download failed"
            content = bookName
            priority = NotificationPriority.HIGH
            autoCancel = true
            setBigTextStyle("Failed to download $bookName\n\nError: $error")
        }
        notificationManager.show(notification)
    }
    
    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
