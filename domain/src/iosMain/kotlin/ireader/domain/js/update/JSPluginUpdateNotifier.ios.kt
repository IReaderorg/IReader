package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate

/**
 * iOS implementation of JSPluginUpdateNotifier
 * 
 * TODO: Implement using UserNotifications framework
 */
actual class JSPluginUpdateNotifier {
    actual fun showUpdateNotification(updates: List<PluginUpdate>) {
        // TODO: Show notification using UNUserNotificationCenter
    }
    
    actual fun cancelUpdateNotification() {
        // TODO: Cancel notification
    }
}
