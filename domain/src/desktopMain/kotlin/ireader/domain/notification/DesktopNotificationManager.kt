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
        // Try Compose Desktop notification first (requires TrayState)
        if (showComposeNotification(notification)) {
            return
        }
        
        // Try AWT system tray notification (requires tray icon)
        if (showAWTNotification(notification)) {
            return
        }
        
        // Fallback to console logging if no notification method available
        Log.info { "NOTIFICATION: ${notification.title} - ${notification.content}" }
        println("═══════════════════════════════════════════════════════")
        println("📢 NOTIFICATION")
        println("Title: ${notification.title}")
        println("Content: ${notification.content}")
        println("Priority: ${notification.priority}")
        println("═══════════════════════════════════════════════════════")
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
     * 
     * @return true if notification was shown successfully, false otherwise
     */
    private fun showAWTNotification(notification: NotificationData): Boolean {
        try {
            if (!java.awt.SystemTray.isSupported()) {
                Log.warn { "System tray not supported on this platform" }
                return false
            }
            
            val tray = java.awt.SystemTray.getSystemTray()
            
            // If no tray icon exists, create a temporary one for notifications
            val trayIcon = if (tray.trayIcons.isEmpty()) {
                createTemporaryTrayIcon()?.also { icon ->
                    tray.add(icon)
                    Log.info { "Created temporary tray icon for notifications" }
                }
            } else {
                tray.trayIcons[0]
            }
            
            if (trayIcon == null) {
                Log.warn { "Could not create tray icon for notification" }
                return false
            }
            
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
            return true
        } catch (e: Exception) {
            Log.error { "Failed to show AWT notification: ${e.message}" }
            return false
        }
    }
    
    /**
     * Create a temporary tray icon for showing notifications
     */
    private fun createTemporaryTrayIcon(): java.awt.TrayIcon? {
        return try {
            // Create a simple 16x16 icon
            val image = java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            
            // Draw a simple notification icon (bell shape)
            g.color = java.awt.Color(33, 150, 243) // Blue color
            g.fillOval(4, 2, 8, 8)
            g.fillRect(6, 10, 4, 2)
            g.fillOval(5, 12, 6, 3)
            g.dispose()
            
            java.awt.TrayIcon(image, "iReader Notifications")
        } catch (e: Exception) {
            Log.error { "Failed to create temporary tray icon: ${e.message}" }
            null
        }
    }
}

actual fun createPlatformNotificationManager(): PlatformNotificationManager {
    return DesktopNotificationManager()
}
