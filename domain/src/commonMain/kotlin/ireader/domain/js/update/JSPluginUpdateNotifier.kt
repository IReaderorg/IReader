package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate

/**
 * Interface for showing plugin update notifications.
 * Platform-specific implementations handle the actual notification display.
 */
expect class JSPluginUpdateNotifier {
    
    /**
     * Shows a notification about available plugin updates.
     * 
     * @param updates List of available updates
     */
    fun showUpdateNotification(updates: List<PluginUpdate>)
    
    /**
     * Cancels any existing update notifications.
     */
    fun cancelUpdateNotification()
}
