package ireader.presentation.ui.settings.notifications

import ireader.core.log.Log
import java.awt.Desktop
import java.net.URI

/**
 * Desktop-specific implementation of NotificationHelper.
 * Desktop platforms generally don't require notification permissions.
 */
class DesktopNotificationHelper : NotificationHelper {
    
    override fun hasNotificationPermission(): Boolean {
        // Desktop platforms don't require notification permissions
        return true
    }
    
    override fun requestNotificationPermission() {
        // No-op on desktop - permissions not required
        Log.debug { "Desktop: Notification permissions not required" }
    }
    
    override fun openNotificationSettings() {
        // Try to open system notification settings
        try {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> {
                    // Windows: Open notification settings
                    Runtime.getRuntime().exec("cmd /c start ms-settings:notifications")
                }
                os.contains("mac") -> {
                    // macOS: Open notification preferences
                    Runtime.getRuntime().exec(arrayOf("open", "x-apple.systempreferences:com.apple.preference.notifications"))
                }
                os.contains("linux") -> {
                    // Linux: Try to open GNOME settings or KDE settings
                    try {
                        Runtime.getRuntime().exec("gnome-control-center notifications")
                    } catch (e: Exception) {
                        try {
                            Runtime.getRuntime().exec("systemsettings5 kcm_notifications")
                        } catch (e2: Exception) {
                            Log.warn { "Could not open notification settings on Linux" }
                        }
                    }
                }
                else -> {
                    Log.warn { "Unknown OS: $os - cannot open notification settings" }
                }
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to open notification settings")
        }
    }
}
