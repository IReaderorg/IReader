package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate
import platform.UserNotifications.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of JSPluginUpdateNotifier
 * 
 * Uses UserNotifications framework for local notifications
 */
@OptIn(ExperimentalForeignApi::class)
actual class JSPluginUpdateNotifier {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    companion object {
        const val UPDATE_NOTIFICATION_ID = "plugin_updates"
        const val UPDATE_CATEGORY_ID = "PLUGIN_UPDATE"
    }
    
    init {
        registerNotificationCategory()
    }
    
    /**
     * Register notification category with actions
     */
    private fun registerNotificationCategory() {
        val updateAction = UNNotificationAction.actionWithIdentifier(
            identifier = "UPDATE_ACTION",
            title = "Update All",
            options = UNNotificationActionOptionForeground
        )
        
        val dismissAction = UNNotificationAction.actionWithIdentifier(
            identifier = "DISMISS_ACTION",
            title = "Later",
            options = UNNotificationActionOptionNone
        )
        
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = UPDATE_CATEGORY_ID,
            actions = listOf(updateAction, dismissAction),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptionNone
        )
        
        notificationCenter.setNotificationCategories(setOf(category))
    }
    
    /**
     * Show notification about available plugin updates
     */
    actual fun showUpdateNotification(updates: List<PluginUpdate>) {
        if (updates.isEmpty()) return
        
        val content = UNMutableNotificationContent().apply {
            setTitle("Plugin Updates Available")
            
            val firstUpdate = updates.first()
            val body = if (updates.size == 1) {
                "Plugin ${firstUpdate.pluginId} has an update available"
            } else {
                "${updates.size} plugins have updates available"
            }
            setBody(body)
            
            setSound(UNNotificationSound.defaultSound)
            setCategoryIdentifier(UPDATE_CATEGORY_ID)
            
            // Add badge
            setBadge(NSNumber(int = updates.size))
            
            // Add user info with update details
            val userInfo = mutableMapOf<Any?, Any?>()
            userInfo["updateCount"] = updates.size
            userInfo["pluginIds"] = updates.map { it.pluginId }.joinToString(",")
            setUserInfo(userInfo)
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = UPDATE_NOTIFICATION_ID,
            content = content,
            trigger = null // Immediate
        )
        
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("[JSPluginUpdateNotifier] Error: ${error.localizedDescription}")
            }
        }
    }
    
    /**
     * Cancel update notification
     */
    actual fun cancelUpdateNotification() {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(
            listOf(UPDATE_NOTIFICATION_ID)
        )
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(
            listOf(UPDATE_NOTIFICATION_ID)
        )
        
        // Clear badge
        val content = UNMutableNotificationContent().apply {
            setBadge(NSNumber(int = 0))
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "clear_badge",
            content = content,
            trigger = null
        )
        notificationCenter.addNotificationRequest(request, null)
    }
    
    /**
     * Show notification for a single plugin update
     */
    fun showSingleUpdateNotification(update: PluginUpdate) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Plugin Update")
            setBody("${update.pluginId} ${update.currentVersion} → ${update.newVersion}")
            setSound(UNNotificationSound.defaultSound)
            setCategoryIdentifier(UPDATE_CATEGORY_ID)
            
            val userInfo = mutableMapOf<Any?, Any?>()
            userInfo["pluginId"] = update.pluginId
            userInfo["newVersion"] = update.newVersion
            setUserInfo(userInfo)
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "plugin_update_${update.pluginId}",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }
    
    /**
     * Show notification for update completion
     */
    fun showUpdateCompletedNotification(updatedCount: Int, failedCount: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Plugin Updates Complete")
            
            val body = when {
                failedCount == 0 -> "$updatedCount plugins updated successfully"
                updatedCount == 0 -> "$failedCount plugins failed to update"
                else -> "$updatedCount updated, $failedCount failed"
            }
            setBody(body)
            
            setSound(UNNotificationSound.defaultSound)
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "plugin_update_complete",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }
}
