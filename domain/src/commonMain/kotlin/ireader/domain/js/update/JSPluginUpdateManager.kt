package ireader.domain.js.update

import ireader.domain.js.models.PluginUpdate
import ireader.domain.js.util.JSPluginLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manages plugin updates including checking, downloading, and installing.
 * Coordinates between update checker, scheduler, and notifier.
 */
class JSPluginUpdateManager(
    private val updateChecker: JSPluginUpdateChecker,
    private val scheduler: JSPluginUpdateScheduler,
    private val notifier: JSPluginUpdateNotifier,
    private val scope: CoroutineScope
) {
    
    private var lastCheckTime: Long = 0
    private var availableUpdates: List<PluginUpdate> = emptyList()
    
    /**
     * Enables automatic update checking.
     * 
     * @param intervalHours Interval between checks in hours (default: 24)
     */
    fun enableAutoUpdate(intervalHours: Int = 24) {
        scheduler.schedulePeriodicCheck(intervalHours)
        JSPluginLogger.logInfo("update-manager", "Auto-update enabled with $intervalHours hour interval")
    }
    
    /**
     * Disables automatic update checking.
     */
    fun disableAutoUpdate() {
        scheduler.cancelPeriodicCheck()
        notifier.cancelUpdateNotification()
        JSPluginLogger.logInfo("update-manager", "Auto-update disabled")
    }
    
    /**
     * Checks if auto-update is currently enabled.
     */
    fun isAutoUpdateEnabled(): Boolean {
        return scheduler.isScheduled()
    }
    
    /**
     * Manually checks for updates.
     * 
     * @param showNotification Whether to show a notification if updates are found
     * @return List of available updates
     */
    suspend fun checkForUpdates(showNotification: Boolean = true): List<PluginUpdate> {
        JSPluginLogger.logInfo("update-manager", "Checking for plugin updates")
        
        val updates = updateChecker.checkForUpdates()
        lastCheckTime = currentTimeToLong()
        availableUpdates = updates
        
        if (updates.isNotEmpty()) {
            JSPluginLogger.logInfo("update-manager", "Found ${updates.size} plugin updates")
            if (showNotification) {
                notifier.showUpdateNotification(updates)
            }
        } else {
            JSPluginLogger.logInfo("update-manager", "No plugin updates available")
        }
        
        return updates
    }
    
    /**
     * Downloads and installs a plugin update.
     * 
     * @param update The update to install
     * @return True if installation succeeded, false otherwise
     */
    suspend fun installUpdate(update: PluginUpdate): Boolean {
        JSPluginLogger.logInfo("update-manager", "Installing update for ${update.pluginId}")
        
        val file = updateChecker.downloadUpdate(update)
        if (file == null) {
            JSPluginLogger.logError(
                "update-manager",
                ireader.domain.js.models.JSPluginError.NetworkError(
                    update.pluginId,
                    update.downloadUrl,
                    Exception("Failed to download update")
                )
            )
            return false
        }
        
        val success = updateChecker.installUpdate(update, file)
        
        // Clean up temp file
        okio.FileSystem.SYSTEM.delete(file)
        
        if (success) {
            // Remove from available updates
            availableUpdates = availableUpdates.filter { it.pluginId != update.pluginId }
            
            // Update notification if there are still updates available
            if (availableUpdates.isNotEmpty()) {
                notifier.showUpdateNotification(availableUpdates)
            } else {
                notifier.cancelUpdateNotification()
            }
        }
        
        return success
    }
    
    /**
     * Downloads and installs all available updates.
     * 
     * @return Map of plugin ID to installation success
     */
    suspend fun installAllUpdates(): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        for (update in availableUpdates) {
            val success = installUpdate(update)
            results[update.pluginId] = success
        }
        
        return results
    }
    
    /**
     * Rolls back a plugin to its previous version.
     * 
     * @param pluginId The plugin identifier
     * @return True if rollback succeeded, false otherwise
     */
    suspend fun rollbackUpdate(pluginId: String): Boolean {
        return updateChecker.rollbackUpdate(pluginId)
    }
    
    /**
     * Gets the list of currently available updates.
     */
    fun getAvailableUpdates(): List<PluginUpdate> {
        return availableUpdates
    }
    
    /**
     * Gets the timestamp of the last update check.
     */
    fun getLastCheckTime(): Long {
        return lastCheckTime
    }
    
    /**
     * Performs a background update check (used by scheduler).
     */
    fun performBackgroundCheck() {
        scope.launch(Dispatchers.IO) {
            try {
                checkForUpdates(showNotification = true)
            } catch (e: Exception) {
                JSPluginLogger.logError(
                    "update-manager",
                    ireader.domain.js.models.JSPluginError.ExecutionError(
                        "update-manager",
                        "performBackgroundCheck",
                        e
                    )
                )
            }
        }
    }
}
