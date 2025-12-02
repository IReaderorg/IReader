package ireader.domain.notification

import ireader.core.log.Log

/**
 * Test notification utility for desktop
 * 
 * Usage: Call testNotification() from anywhere in the app to trigger a test notification
 */
object TestNotification {
    
    private var notificationManager: DesktopNotificationManager? = null
    
    /**
     * Set the notification manager instance
     */
    fun setManager(manager: DesktopNotificationManager) {
        notificationManager = manager
        Log.info { "TestNotification: Manager set" }
    }
    
    /**
     * Show a test notification
     */
    fun showTest() {
        val manager = notificationManager
        
        if (manager == null) {
            Log.error { "TestNotification: Manager not set! Call setManager() first" }
            return
        }
        
        manager.show(
            NotificationData(
                id = 999,
                title = "Test Notification",
                content = "This is a test notification from iReader Desktop. If you see this, notifications are working!",
                channelId = "test",
                priority = NotificationPriority.HIGH
            )
        )
    }
    
    /**
     * Show multiple test notifications
     */
    fun showMultipleTests() {
        val manager = notificationManager ?: run {
            Log.error { "TestNotification: Manager not set!" }
            return
        }
        
        // Test 1: Default priority
        manager.show(
            NotificationData(
                id = 1001,
                title = "Test 1: Default Priority",
                content = "This is a default priority notification",
                channelId = "test",
                priority = NotificationPriority.DEFAULT
            )
        )
        
        Thread.sleep(1000)
        
        // Test 2: High priority
        manager.show(
            NotificationData(
                id = 1002,
                title = "Test 2: High Priority",
                content = "This is a high priority notification",
                channelId = "test",
                priority = NotificationPriority.HIGH
            )
        )
        
        Thread.sleep(1000)
        
        // Test 3: Low priority
        manager.show(
            NotificationData(
                id = 1003,
                title = "Test 3: Low Priority",
                content = "This is a low priority notification",
                channelId = "test",
                priority = NotificationPriority.LOW
            )
        )
        
    }
}
