package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common extension/plugin management service
 */
interface ExtensionService : PlatformService {
    /**
     * Current service state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Available extensions
     */
    val availableExtensions: StateFlow<List<ExtensionInfo>>
    
    /**
     * Installed extensions
     */
    val installedExtensions: StateFlow<List<ExtensionInfo>>
    
    /**
     * Installation progress
     */
    val installProgress: StateFlow<Map<String, InstallProgress>>
    
    /**
     * Fetch available extensions from repository
     */
    suspend fun fetchAvailableExtensions(
        repositoryUrl: String? = null
    ): ServiceResult<List<ExtensionInfo>>
    
    /**
     * Install extension
     */
    suspend fun installExtension(
        extensionId: String,
        showNotification: Boolean = true
    ): ServiceResult<Unit>
    
    /**
     * Uninstall extension
     */
    suspend fun uninstallExtension(
        extensionId: String
    ): ServiceResult<Unit>
    
    /**
     * Update extension
     */
    suspend fun updateExtension(
        extensionId: String,
        showNotification: Boolean = true
    ): ServiceResult<Unit>
    
    /**
     * Update all extensions
     */
    suspend fun updateAllExtensions(
        showNotification: Boolean = true
    ): ServiceResult<UpdateResult>
    
    /**
     * Check for extension updates
     */
    suspend fun checkForUpdates(): ServiceResult<List<ExtensionInfo>>
    
    /**
     * Enable/disable extension
     */
    suspend fun setExtensionEnabled(
        extensionId: String,
        enabled: Boolean
    ): ServiceResult<Unit>
}

/**
 * Extension information
 */
data class ExtensionInfo(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val iconUrl: String? = null,
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = true,
    val hasUpdate: Boolean = false,
    val availableVersion: String? = null,
    val sourceCount: Int = 0
)

/**
 * Installation progress
 */
data class InstallProgress(
    val extensionId: String,
    val status: InstallStatus,
    val progress: Float = 0f,
    val errorMessage: String? = null
)

/**
 * Installation status
 */
enum class InstallStatus {
    DOWNLOADING,
    INSTALLING,
    COMPLETED,
    FAILED
}
