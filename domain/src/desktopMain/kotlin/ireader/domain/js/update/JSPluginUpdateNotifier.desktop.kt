package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit
import java.awt.Image
import javax.imageio.ImageIO

/**
 * Desktop implementation of plugin update notifier.
 * Uses system tray notifications if available.
 */
actual class JSPluginUpdateNotifier {
    
    private var trayIcon: TrayIcon? = null
    
    actual fun showUpdateNotification(updates: List<PluginUpdate>) {
        if (updates.isEmpty()) return
        
        try {
            // Check if system tray is supported
            if (!SystemTray.isSupported()) {
                // Fallback to console logging
                logUpdatesToConsole(updates)
                return
            }
            
            val tray = SystemTray.getSystemTray()
            
            // Create or get tray icon
            if (trayIcon == null) {
                val image = createNotificationIcon()
                trayIcon = TrayIcon(image, "IReader Plugin Updates")
                trayIcon?.isImageAutoSize = true
                
                try {
                    tray.add(trayIcon)
                } catch (e: Exception) {
                    // Tray icon already added or error
                    logUpdatesToConsole(updates)
                    return
                }
            }
            
            // Show notification
            val message = buildNotificationMessage(updates)
            trayIcon?.displayMessage(
                "Plugin Updates Available",
                message,
                TrayIcon.MessageType.INFO
            )
            
        } catch (e: Exception) {
            // Fallback to console logging if notification fails
            logUpdatesToConsole(updates)
        }
    }
    
    actual fun cancelUpdateNotification() {
        try {
            if (SystemTray.isSupported() && trayIcon != null) {
                val tray = SystemTray.getSystemTray()
                tray.remove(trayIcon)
                trayIcon = null
            }
        } catch (e: Exception) {
            // Ignore errors during cancellation
        }
    }
    
    /**
     * Builds a notification message from the list of updates.
     */
    private fun buildNotificationMessage(updates: List<PluginUpdate>): String {
        return when {
            updates.size == 1 -> {
                val update = updates.first()
                "${update.pluginId}: ${update.currentVersion} → ${update.newVersion}"
            }
            updates.size <= 3 -> {
                updates.joinToString("\n") { update ->
                    "${update.pluginId}: ${update.currentVersion} → ${update.newVersion}"
                }
            }
            else -> {
                val first = updates.take(2).joinToString("\n") { update ->
                    "${update.pluginId}: ${update.currentVersion} → ${update.newVersion}"
                }
                "$first\n...and ${updates.size - 2} more"
            }
        }
    }
    
    /**
     * Creates a simple notification icon.
     */
    private fun createNotificationIcon(): Image {
        return try {
            // Try to load app icon from resources
            val iconUrl = javaClass.getResource("/icon.png")
            if (iconUrl != null) {
                ImageIO.read(iconUrl)
            } else {
                // Create a simple default icon
                createDefaultIcon()
            }
        } catch (e: Exception) {
            createDefaultIcon()
        }
    }
    
    /**
     * Creates a default notification icon if no icon is available.
     */
    private fun createDefaultIcon(): Image {
        // Create a simple 16x16 image
        val image = java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        
        // Draw a simple notification icon (blue circle with white "i")
        g.color = java.awt.Color(33, 150, 243) // Material Blue
        g.fillOval(0, 0, 16, 16)
        
        g.color = java.awt.Color.WHITE
        g.font = java.awt.Font("Arial", java.awt.Font.BOLD, 12)
        g.drawString("i", 5, 12)
        
        g.dispose()
        return image
    }
    
    /**
     * Logs updates to console as a fallback.
     */
    private fun logUpdatesToConsole(updates: List<PluginUpdate>) {
        // Silently ignore - no console logging in production
    }
}
