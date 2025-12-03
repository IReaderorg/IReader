package ireader.domain.js.update

/**
 * iOS implementation of JSPluginUpdateNotifier
 * 
 * TODO: Implement using UserNotifications framework
 */
actual class JSPluginUpdateNotifier {
    actual fun notifyUpdatesAvailable(updates: List<PluginUpdate>) {
        // TODO: Show notification using UNUserNotificationCenter
    }
    
    actual fun notifyUpdateComplete(pluginName: String, success: Boolean) {
        // TODO: Show notification
    }
}

data class PluginUpdate(
    val pluginId: String,
    val pluginName: String,
    val currentVersion: String,
    val newVersion: String
)
