package ireader.domain.services.common

import ireader.core.log.Log
import java.awt.SystemTray
import java.awt.TrayIcon

/**
 * Desktop implementation of NotificationService
 */
class DesktopNotificationService : NotificationService {
    
    private val activeNotifications = mutableMapOf<Int, NotificationData>()
    private var trayIcon: TrayIcon? = null
    
    override suspend fun initialize() {
        if (SystemTray.isSupported()) {
            try {
                val tray = SystemTray.getSystemTray()
                if (tray.trayIcons.isNotEmpty()) {
                    trayIcon = tray.trayIcons[0]
                }
            } catch (e: Exception) {
                Log.error { "Failed to initialize tray icon: ${e.message}" }
            }
        }
    }
    
    override suspend fun start() {}
    override suspend fun stop() {}
    override fun isRunning(): Boolean = true
    
    override suspend fun cleanup() {
        activeNotifications.clear()
    }
    
    override fun showNotification(
        id: Int,
        title: String,
        message: String,
        priority: NotificationPriority
    ) {
        val notification = NotificationData(id, title, message, priority)
        activeNotifications[id] = notification
        
        trayIcon?.let { icon ->
            val messageType = when (priority) {
                NotificationPriority.HIGH, NotificationPriority.MAX -> TrayIcon.MessageType.WARNING
                NotificationPriority.LOW, NotificationPriority.MIN -> TrayIcon.MessageType.INFO
                else -> TrayIcon.MessageType.INFO
            }
            
            icon.displayMessage(title, message, messageType)
        } ?: run {
            Log.warn { "No tray icon available for notification: $title" }
        }
    }
    
    override fun showProgressNotification(
        id: Int,
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean
    ) {
        val progressText = if (indeterminate) {
            message
        } else {
            "$message ($progress/$maxProgress)"
        }
        showNotification(id, title, progressText, NotificationPriority.DEFAULT)
    }
    
    override fun updateNotification(
        id: Int,
        title: String?,
        message: String?,
        progress: Int?,
        maxProgress: Int?
    ) {
        val existing = activeNotifications[id] ?: return
        
        val updatedTitle = title ?: existing.title
        val updatedMessage = message ?: existing.message
        
        val finalMessage = if (progress != null && maxProgress != null) {
            "$updatedMessage ($progress/$maxProgress)"
        } else {
            updatedMessage
        }
        
        showNotification(id, updatedTitle, finalMessage, existing.priority)
    }
    
    override fun cancelNotification(id: Int) {
        activeNotifications.remove(id)
    }
    
    override fun cancelAllNotifications() {
        activeNotifications.clear()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return SystemTray.isSupported()
    }
    
    private data class NotificationData(
        val id: Int,
        val title: String,
        val message: String,
        val priority: NotificationPriority
    )
}
