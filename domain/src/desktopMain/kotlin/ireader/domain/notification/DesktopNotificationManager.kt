package ireader.domain.notification

import ireader.core.log.Log

/**
 * Desktop implementation of PlatformNotificationManager
 * 
 * Supports:
 * - Windows: Windows Toast Notifications (via AWT SystemTray)
 * - macOS: NSUserNotificationCenter (via AWT SystemTray)
 * - Linux: libnotify (via AWT SystemTray)
 * - Compose Desktop: Native notification API
 */
class DesktopNotificationManager : PlatformNotificationManager {
    
    private val activeNotifications = mutableMapOf<Int, NotificationData>()
    private var trayState: androidx.compose.ui.window.TrayState? = null
    
    /**
     * Set the TrayState for Compose Desktop notifications
     * This should be called from the main application
     */
    fun setTrayState(state: androidx.compose.ui.window.TrayState) {
        this.trayState = state
    }
    
    override fun show(notification: NotificationData) {
        activeNotifications[notification.id] = notification
        Log.debug { "Desktop notification shown: ${notification.title} - ${notification.content}" }
        
        // Show notification using available methods
        showDesktopNotification(notification)
    }
    
    override fun showPlatformNotification(id: Int, platformNotification: Any) {
        Log.debug { "Desktop platform notification shown: $id" }
        // No-op for desktop as there's no common platform notification type
    }
    
    override fun cancel(id: Int) {
        activeNotifications.remove(id)
        Log.debug { "Desktop notification cancelled: $id" }
    }
    
    override fun cancelAll() {
        activeNotifications.clear()
        Log.debug { "All desktop notifications cancelled" }
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return true // Desktop doesn't require permissions
    }
    
    /**
     * Get currently active notifications (for testing/debugging)
     */
    fun getActiveNotifications(): Map<Int, NotificationData> {
        return activeNotifications.toMap()
    }
    
    /**
     * Show desktop notification using available methods
     */
    private fun showDesktopNotification(notification: NotificationData) {
        // Try Compose Desktop notification first
        if (showComposeNotification(notification)) {
            return
        }
        
        // Fallback to AWT system tray notification
        showAWTNotification(notification)
    }
    
    /**
     * Show notification using Compose Desktop API
     */
    private fun showComposeNotification(notification: NotificationData): Boolean {
        return try {
            val state = trayState ?: return false
            
            state.sendNotification(
                androidx.compose.ui.window.Notification(
                    title = notification.title,
                    message = notification.content,
                    type = when (notification.priority) {
                        NotificationPriority.HIGH -> androidx.compose.ui.window.Notification.Type.Warning
                        NotificationPriority.LOW -> androidx.compose.ui.window.Notification.Type.Info
                        else -> androidx.compose.ui.window.Notification.Type.Info
                    }
                )
            )
            
            Log.info { "Compose Desktop notification shown: ${notification.title}" }
            true
        } catch (e: Exception) {
            Log.error { "Failed to show Compose notification: ${e.message}" }
            false
        }
    }
    
    /**
     * Show notification using AWT SystemTray
     * 
     * This works on:
     * - Windows: Shows Windows Toast Notification
     * - macOS: Shows macOS Notification Center notification
     * - Linux: Uses libnotify (if available)
     */
    private fun showAWTNotification(notification: NotificationData) {
        try {
            if (!java.awt.SystemTray.isSupported()) {
                Log.warn { "System tray not supported on this platform" }
                return
            }
            
            val tray = java.awt.SystemTray.getSystemTray()
            val trayIcons = tray.trayIcons
            
            if (trayIcons.isEmpty()) {
                Log.warn { "No tray icon available for notification" }
                return
            }
            
            val trayIcon = trayIcons[0]
            val messageType = when (notification.priority) {
                NotificationPriority.HIGH -> java.awt.TrayIcon.MessageType.WARNING
                NotificationPriority.LOW -> java.awt.TrayIcon.MessageType.INFO
                else -> java.awt.TrayIcon.MessageType.INFO
            }
            
            trayIcon.displayMessage(
                notification.title,
                notification.content,
                messageType
            )
            
            Log.info { "AWT notification shown: ${notification.title}" }
        } catch (e: Exception) {
            Log.error { "Failed to show AWT notification: ${e.message}" }
        }
    }
}

actual fun createPlatformNotificationManager(): PlatformNotificationManager {
    return DesktopNotificationManager()
}
