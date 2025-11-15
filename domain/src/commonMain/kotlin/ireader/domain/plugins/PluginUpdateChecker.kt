package ireader.domain.plugins

import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Service for checking and managing plugin updates
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5
 */
class PluginUpdateChecker(
    private val pluginManager: PluginManager,
    private val pluginRegistry: PluginRegistry,
    private val pluginLoader: PluginLoader,
    private val pluginDatabase: PluginDatabase,
    private val preferences: PluginPreferences,
    private val marketplaceClient: PluginMarketplaceClient,
    private val updateHistoryRepository: PluginUpdateHistoryRepository
) {
    private val scope = createICoroutineScope()
    private var updateCheckJob: Job? = null
    
    private val _availableUpdates = MutableStateFlow<List<PluginUpdate>>(emptyList())
    val availableUpdates: StateFlow<List<PluginUpdate>> = _availableUpdates.asStateFlow()
    
    private val _updateStatus = MutableStateFlow<Map<String, UpdateStatus>>(emptyMap())
    val updateStatus: StateFlow<Map<String, UpdateStatus>> = _updateStatus.asStateFlow()
    
    /**
     * Start periodic update checking based on preferences
     * Requirements: 12.1
     */
    fun startPeriodicUpdateChecking() {
        updateCheckJob?.cancel()
        
        updateCheckJob = scope.launch {
            while (true) {
                try {
                    checkForUpdates()
                } catch (e: Exception) {
                    // Log error but continue checking
                    println("Update check failed: ${e.message}")
                }
                
                // Wait for the configured interval
                val interval = preferences.pluginUpdateCheckInterval().get()
                delay(interval)
            }
        }
    }
    
    /**
     * Stop periodic update checking
     */
    fun stopPeriodicUpdateChecking() {
        updateCheckJob?.cancel()
        updateCheckJob = null
    }
    
    /**
     * Check for updates for all installed plugins
     * Compares installed plugin versions with marketplace versions
     * Requirements: 12.1, 12.2
     */
    suspend fun checkForUpdates(): Result<List<PluginUpdate>> {
        return try {
            val installedPlugins = pluginDatabase.getAllPlugins()
            val updates = mutableListOf<PluginUpdate>()
            
            for (pluginInfo in installedPlugins) {
                try {
                    // Fetch latest version from marketplace
                    val latestVersion = marketplaceClient.getLatestVersion(pluginInfo.id)
                    
                    // Compare versions
                    if (latestVersion.versionCode > pluginInfo.manifest.versionCode) {
                        updates.add(
                            PluginUpdate(
                                pluginId = pluginInfo.id,
                                currentVersion = pluginInfo.manifest.version,
                                currentVersionCode = pluginInfo.manifest.versionCode,
                                latestVersion = latestVersion.version,
                                latestVersionCode = latestVersion.versionCode,
                                changelog = latestVersion.changelog,
                                downloadUrl = latestVersion.downloadUrl,
                                releaseDate = latestVersion.releaseDate
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Log error for this plugin but continue checking others
                    println("Failed to check updates for ${pluginInfo.id}: ${e.message}")
                }
            }
            
            _availableUpdates.value = updates
            
            // Auto-update if enabled
            if (preferences.autoUpdatePlugins().get() && updates.isNotEmpty()) {
                autoUpdatePlugins(updates)
            }
            
            Result.success(updates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download an update for a specific plugin
     * Requirements: 12.2
     */
    suspend fun downloadUpdate(pluginId: String): Result<File> {
        return try {
            // Update status to downloading
            updateStatusForPlugin(pluginId, UpdateStatus.Downloading(0))
            
            val update = _availableUpdates.value.find { it.pluginId == pluginId }
                ?: return Result.failure(Exception("No update available for plugin $pluginId"))
            
            // Download the plugin package with progress tracking
            val packageFile = marketplaceClient.downloadPlugin(
                url = update.downloadUrl,
                onProgress = { progress ->
                    updateStatusForPlugin(pluginId, UpdateStatus.Downloading(progress))
                }
            )
            
            updateStatusForPlugin(pluginId, UpdateStatus.Downloaded)
            
            Result.success(packageFile)
        } catch (e: Exception) {
            updateStatusForPlugin(pluginId, UpdateStatus.Failed(e.message ?: "Download failed"))
            Result.failure(e)
        }
    }
    
    /**
     * Install an update for a specific plugin
     * Replaces the old plugin with the new version
     * Requirements: 12.2, 12.3
     */
    suspend fun installUpdate(pluginId: String, packageFile: File): Result<Unit> {
        return try {
            // Update status to installing
            updateStatusForPlugin(pluginId, UpdateStatus.Installing)
            
            // Get current plugin info for backup
            val currentPlugin = pluginDatabase.getPluginInfo(pluginId)
                ?: return Result.failure(Exception("Plugin $pluginId not found"))
            
            // Save current version to update history before updating
            updateHistoryRepository.saveUpdateHistory(
                PluginUpdateHistory(
                    pluginId = pluginId,
                    fromVersion = currentPlugin.manifest.version,
                    fromVersionCode = currentPlugin.manifest.versionCode,
                    toVersion = "", // Will be filled after loading new manifest
                    toVersionCode = 0,
                    updateDate = System.currentTimeMillis(),
                    success = false // Will be updated after successful install
                )
            )
            
            // Check if plugin was enabled
            val wasEnabled = currentPlugin.status == PluginStatus.ENABLED
            
            // Disable the plugin before updating
            if (wasEnabled) {
                pluginManager.disablePlugin(pluginId)
            }
            
            // Load the new plugin to validate it
            val newPlugin = pluginLoader.loadPlugin(packageFile)
                ?: return Result.failure(Exception("Failed to load updated plugin"))
            
            // Verify it's the same plugin
            if (newPlugin.manifest.id != pluginId) {
                return Result.failure(Exception("Plugin ID mismatch"))
            }
            
            // Update the plugin in registry
            pluginRegistry.register(newPlugin)
            
            // Update database with new manifest
            pluginDatabase.insertOrUpdate(
                manifest = newPlugin.manifest,
                status = if (wasEnabled) PluginStatus.ENABLED else PluginStatus.DISABLED
            )
            
            // Re-enable if it was enabled before
            if (wasEnabled) {
                pluginManager.enablePlugin(pluginId)
            }
            
            // Update the history record with success
            updateHistoryRepository.updateLastHistorySuccess(
                pluginId = pluginId,
                toVersion = newPlugin.manifest.version,
                toVersionCode = newPlugin.manifest.versionCode,
                success = true
            )
            
            // Remove from available updates
            _availableUpdates.value = _availableUpdates.value.filter { it.pluginId != pluginId }
            
            updateStatusForPlugin(pluginId, UpdateStatus.Completed)
            
            // Refresh plugin manager
            pluginManager.refreshPlugins()
            
            Result.success(Unit)
        } catch (e: Exception) {
            updateStatusForPlugin(pluginId, UpdateStatus.Failed(e.message ?: "Installation failed"))
            
            // Record failed update in history
            updateHistoryRepository.updateLastHistorySuccess(
                pluginId = pluginId,
                toVersion = "",
                toVersionCode = 0,
                success = false
            )
            
            Result.failure(e)
        }
    }
    
    /**
     * Download and install an update for a specific plugin
     * Requirements: 12.2, 12.3
     */
    suspend fun updatePlugin(pluginId: String): Result<Unit> {
        return try {
            // Download the update
            val packageFile = downloadUpdate(pluginId).getOrElse { error ->
                return Result.failure(error)
            }
            
            // Install the update
            installUpdate(pluginId, packageFile).getOrElse { error ->
                // Clean up downloaded file
                packageFile.delete()
                return Result.failure(error)
            }
            
            // Clean up downloaded file after successful installation
            packageFile.delete()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Auto-update plugins when enabled in preferences
     * Requirements: 12.3
     */
    private suspend fun autoUpdatePlugins(updates: List<PluginUpdate>) {
        for (update in updates) {
            try {
                updatePlugin(update.pluginId)
            } catch (e: Exception) {
                // Log error but continue with other updates
                println("Auto-update failed for ${update.pluginId}: ${e.message}")
            }
        }
    }
    
    /**
     * Rollback a plugin to a previous version
     * Requirements: 12.4
     */
    suspend fun rollbackPlugin(pluginId: String, targetVersionCode: Int): Result<Unit> {
        return try {
            updateStatusForPlugin(pluginId, UpdateStatus.RollingBack)
            
            // Get the update history for this version
            val history = updateHistoryRepository.getUpdateHistory(pluginId)
                .find { it.toVersionCode == targetVersionCode }
                ?: return Result.failure(Exception("Version $targetVersionCode not found in history"))
            
            // Download the old version from marketplace
            val oldVersionUrl = marketplaceClient.getVersionDownloadUrl(pluginId, targetVersionCode)
            val packageFile = marketplaceClient.downloadPlugin(
                url = oldVersionUrl,
                onProgress = { progress ->
                    updateStatusForPlugin(pluginId, UpdateStatus.RollingBack)
                }
            )
            
            // Install the old version
            installUpdate(pluginId, packageFile).getOrElse { error ->
                packageFile.delete()
                return Result.failure(error)
            }
            
            packageFile.delete()
            
            updateStatusForPlugin(pluginId, UpdateStatus.Completed)
            
            Result.success(Unit)
        } catch (e: Exception) {
            updateStatusForPlugin(pluginId, UpdateStatus.Failed(e.message ?: "Rollback failed"))
            Result.failure(e)
        }
    }
    
    /**
     * Get update history for a plugin
     * Requirements: 12.5
     */
    suspend fun getUpdateHistory(pluginId: String): List<PluginUpdateHistory> {
        return updateHistoryRepository.getUpdateHistory(pluginId)
    }
    
    /**
     * Get all update history
     * Requirements: 12.5
     */
    suspend fun getAllUpdateHistory(): List<PluginUpdateHistory> {
        return updateHistoryRepository.getAllUpdateHistory()
    }
    
    /**
     * Retry a failed update
     * Requirements: 12.5
     */
    suspend fun retryUpdate(pluginId: String): Result<Unit> {
        // Clear the failed status
        updateStatusForPlugin(pluginId, UpdateStatus.Idle)
        
        // Attempt the update again
        return updatePlugin(pluginId)
    }
    
    /**
     * Get the number of available updates
     * Used for notification badges
     * Requirements: 12.2
     */
    fun getAvailableUpdatesCount(): Int {
        return _availableUpdates.value.size
    }
    
    /**
     * Create a notification for available updates
     * Requirements: 12.2
     */
    fun createUpdateNotification(): PluginUpdateNotification {
        return PluginUpdateNotification(
            availableUpdatesCount = _availableUpdates.value.size,
            updates = _availableUpdates.value
        )
    }
    
    /**
     * Update the status for a specific plugin
     */
    private fun updateStatusForPlugin(pluginId: String, status: UpdateStatus) {
        _updateStatus.value = _updateStatus.value.toMutableMap().apply {
            put(pluginId, status)
        }
    }
}

/**
 * Information about an available plugin update
 * Requirements: 12.1, 12.2
 */
data class PluginUpdate(
    val pluginId: String,
    val currentVersion: String,
    val currentVersionCode: Int,
    val latestVersion: String,
    val latestVersionCode: Int,
    val changelog: String,
    val downloadUrl: String,
    val releaseDate: Long
)

/**
 * Status of a plugin update operation
 * Requirements: 12.2, 12.3, 12.4, 12.5
 */
sealed class UpdateStatus {
    object Idle : UpdateStatus()
    data class Downloading(val progress: Int) : UpdateStatus()
    object Downloaded : UpdateStatus()
    object Installing : UpdateStatus()
    object RollingBack : UpdateStatus()
    object Completed : UpdateStatus()
    data class Failed(val message: String) : UpdateStatus()
}
