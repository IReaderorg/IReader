package ireader.domain.notification

import platform.UserNotifications.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of PlatformNotificationManager
 * 
 * Uses UserNotifications framework (UNUserNotificationCenter) for local notifications
 */
actual fun createPlatformNotificationManager(): PlatformNotificationManager {
    return IosPlatformNotificationManager()
}

@OptIn(ExperimentalForeignApi::class)
private class IosPlatformNotificationManager : PlatformNotificationManager {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private var isAuthorized = false
    
    init {
        requestAuthorization()
    }
    
    private fun requestAuthorization() {
        val options = UNAuthorizationOptionAlert or 
                      UNAuthorizationOptionSound or 
                      UNAuthorizationOptionBadge
        
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            isAuthorized = granted
            if (error != null) {
                println("[Notifications] Authorization error: ${error.localizedDescription}")
            }
        }
    }
    
    override fun show(notification: NotificationData) {
        if (!isAuthorized) {
            println("[Notifications] Not authorized to show notifications")
            return
        }
        
        val content = UNMutableNotificationContent().apply {
            setTitle(notification.title)
            setBody(notification.content)
            setSound(UNNotificationSound.defaultSound)
            
            // Add user info for handling notification taps
            val userInfo = mutableMapOf<Any?, Any?>()
            userInfo["notificationId"] = notification.id
            setUserInfo(userInfo)
        }
        
        // Create trigger (immediate)
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 0.1, // Minimum delay
            repeats = false
        )
        
        // Create request
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notification.id.toString(),
            content = content,
            trigger = trigger
        )
        
        // Schedule notification
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("[Notifications] Error showing notification: ${error.localizedDescription}")
            }
        }
    }
    
    override fun showPlatformNotification(id: Int, platformNotification: Any) {
        when (platformNotification) {
            is UNNotificationRequest -> {
                notificationCenter.addNotificationRequest(platformNotification) { error ->
                    if (error != null) {
                        println("[Notifications] Error: ${error.localizedDescription}")
                    }
                }
            }
            is NotificationData -> show(platformNotification)
            else -> {
                println("[Notifications] Unknown notification type: ${platformNotification::class}")
            }
        }
    }
    
    override fun cancel(id: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf(id.toString()))
    }
    
    override fun cancelAll() {
        notificationCenter.removeAllPendingNotificationRequests()
        notificationCenter.removeAllDeliveredNotifications()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return isAuthorized
    }
}
