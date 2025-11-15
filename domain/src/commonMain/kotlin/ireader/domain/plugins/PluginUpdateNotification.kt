package ireader.domain.plugins

/**
 * Notification data for plugin updates
 * Requirements: 12.2
 */
data class PluginUpdateNotification(
    val availableUpdatesCount: Int,
    val updates: List<PluginUpdate>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Get a summary message for the notification
     */
    fun getSummaryMessage(): String {
        return when (availableUpdatesCount) {
            0 -> "All plugins are up to date"
            1 -> "1 plugin update available"
            else -> "$availableUpdatesCount plugin updates available"
        }
    }
    
    /**
     * Get detailed message listing plugin names
     */
    fun getDetailedMessage(): String {
        if (availableUpdatesCount == 0) {
            return "All plugins are up to date"
        }
        
        val pluginNames = updates.take(3).joinToString(", ") { it.pluginId }
        return if (availableUpdatesCount > 3) {
            "$pluginNames and ${availableUpdatesCount - 3} more"
        } else {
            pluginNames
        }
    }
}
