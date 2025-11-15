package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate

/**
 * Desktop implementation of plugin update notifier.
 * Uses system tray notifications if available.
 */
actual class JSPluginUpdateNotifier {
    
    actual fun showUpdateNotification(updates: List<PluginUpdate>) {
        if (updates.isEmpty()) return
        
        // TODO: Implement desktop notification using system tray
        // This would require platform-specific notification APIs
        // For now, just log the updates
        println("Plugin updates available: ${updates.size}")
        updates.forEach { update ->
            println("  ${update.pluginId}: ${update.currentVersion} → ${update.newVersion}")
        }
    }
    
    actual fun cancelUpdateNotification() {
        // TODO: Implement notification cancellation
    }
}
