package ireader.domain.plugins

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manages runtime permissions for plugins
 * Requirements: 10.1, 10.2, 10.3, 10.5
 */
class PluginPermissionManager(
    private val database: PluginDatabase
) {
    private val mutex = Mutex()
    
    // Cache of granted permissions per plugin
    private val grantedPermissions = mutableMapOf<String, MutableSet<PluginPermission>>()
    
    // Pending permission requests
    private val _pendingRequests = MutableStateFlow<List<PermissionRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PermissionRequest>> = _pendingRequests.asStateFlow()
    
    /**
     * Initialize permission manager and load granted permissions from database
     */
    suspend fun initialize() {
        mutex.withLock {
            val permissions = database.getAllGrantedPermissions()
            grantedPermissions.clear()
            permissions.forEach { (pluginId, permissionList) ->
                grantedPermissions[pluginId] = permissionList.toMutableSet()
            }
        }
    }
    
    /**
     * Check if a permission is granted for a plugin
     * Requirements: 10.1, 10.2
     */
    fun isPermissionGranted(pluginId: String, permission: PluginPermission): Boolean {
        return grantedPermissions[pluginId]?.contains(permission) ?: false
    }
    
    /**
     * Request a permission for a plugin
     * This will trigger a user dialog for sensitive permissions
     * Requirements: 10.5
     */
    suspend fun requestPermission(
        pluginId: String,
        permission: PluginPermission,
        manifest: PluginManifest
    ): PermissionRequestResult {
        // Check if permission is in manifest
        if (!manifest.permissions.contains(permission)) {
            return PermissionRequestResult.Denied(
                "Permission $permission not declared in plugin manifest"
            )
        }
        
        // Check if already granted
        if (isPermissionGranted(pluginId, permission)) {
            return PermissionRequestResult.Granted
        }
        
        // For sensitive permissions, create a pending request for user approval
        if (isSensitivePermission(permission)) {
            val request = PermissionRequest(
                pluginId = pluginId,
                pluginName = manifest.name,
                permission = permission,
                timestamp = currentTimeToLong()
            )
            
            mutex.withLock {
                _pendingRequests.value = _pendingRequests.value + request
            }
            
            return PermissionRequestResult.Pending
        }
        
        // Auto-grant non-sensitive permissions
        return grantPermission(pluginId, permission)
    }
    
    /**
     * Grant a permission to a plugin
     * Requirements: 10.2, 10.5
     */
    suspend fun grantPermission(
        pluginId: String,
        permission: PluginPermission
    ): PermissionRequestResult {
        mutex.withLock {
            val permissions = grantedPermissions.getOrPut(pluginId) { mutableSetOf() }
            permissions.add(permission)
            
            // Save to database
            database.saveGrantedPermission(pluginId, permission)
            
            // Remove from pending requests
            _pendingRequests.value = _pendingRequests.value.filter {
                !(it.pluginId == pluginId && it.permission == permission)
            }
        }
        
        return PermissionRequestResult.Granted
    }
    
    /**
     * Deny a permission request
     * Requirements: 10.5
     */
    suspend fun denyPermission(
        pluginId: String,
        permission: PluginPermission,
        reason: String = "User denied permission"
    ): PermissionRequestResult {
        mutex.withLock {
            // Remove from pending requests
            _pendingRequests.value = _pendingRequests.value.filter {
                !(it.pluginId == pluginId && it.permission == permission)
            }
        }
        
        return PermissionRequestResult.Denied(reason)
    }
    
    /**
     * Revoke a permission from a plugin
     * Requirements: 10.2
     */
    suspend fun revokePermission(pluginId: String, permission: PluginPermission) {
        mutex.withLock {
            grantedPermissions[pluginId]?.remove(permission)
            database.revokeGrantedPermission(pluginId, permission)
        }
    }
    
    /**
     * Revoke all permissions for a plugin
     * Requirements: 10.2
     */
    suspend fun revokeAllPermissions(pluginId: String) {
        mutex.withLock {
            grantedPermissions.remove(pluginId)
            database.revokeAllGrantedPermissions(pluginId)
        }
    }
    
    /**
     * Get all granted permissions for a plugin
     * Requirements: 10.1
     */
    fun getGrantedPermissions(pluginId: String): Set<PluginPermission> {
        return grantedPermissions[pluginId]?.toSet() ?: emptySet()
    }
    
    /**
     * Get all plugins with a specific permission
     */
    fun getPluginsWithPermission(permission: PluginPermission): Set<String> {
        return grantedPermissions
            .filter { (_, permissions) -> permissions.contains(permission) }
            .keys
            .toSet()
    }
    
    /**
     * Check if a permission is sensitive and requires user approval
     * Requirements: 10.5
     */
    private fun isSensitivePermission(permission: PluginPermission): Boolean {
        return when (permission) {
            PluginPermission.NETWORK -> true
            PluginPermission.STORAGE -> true
            PluginPermission.LIBRARY_ACCESS -> true
            PluginPermission.PREFERENCES -> true
            PluginPermission.READER_CONTEXT -> false
            PluginPermission.NOTIFICATIONS -> false
            PluginPermission.CATALOG_WRITE -> true
            PluginPermission.SYNC_DATA -> true
            PluginPermission.BACKGROUND_SERVICE -> true
            PluginPermission.LOCAL_SERVER -> true
            PluginPermission.IMAGE_PROCESSING -> false
            PluginPermission.UI_INJECTION -> true
            PluginPermission.GLOSSARY_ACCESS -> false
            PluginPermission.CHARACTER_DATABASE -> false
            PluginPermission.AUDIO_PLAYBACK -> false
            PluginPermission.GRADIO_ACCESS -> true
        }
    }
    
    /**
     * Get permission description for user display
     */
    fun getPermissionDescription(permission: PluginPermission): String {
        return when (permission) {
            PluginPermission.NETWORK -> 
                "Access the internet to fetch data or communicate with external services"
            PluginPermission.STORAGE -> 
                "Read and write files to local storage"
            PluginPermission.READER_CONTEXT -> 
                "Access information about the current book and reading position"
            PluginPermission.LIBRARY_ACCESS -> 
                "Access your library and book collection"
            PluginPermission.PREFERENCES -> 
                "Access and modify app preferences"
            PluginPermission.NOTIFICATIONS -> 
                "Show notifications"
            PluginPermission.CATALOG_WRITE ->
                "Add or modify content catalog sources"
            PluginPermission.SYNC_DATA ->
                "Synchronize your reading data across devices"
            PluginPermission.BACKGROUND_SERVICE ->
                "Run background services for sync or downloads"
            PluginPermission.LOCAL_SERVER ->
                "Connect to local servers on your network"
            PluginPermission.IMAGE_PROCESSING ->
                "Process and enhance images"
            PluginPermission.UI_INJECTION ->
                "Add custom screens and UI elements"
            PluginPermission.GLOSSARY_ACCESS ->
                "Access and modify glossaries and dictionaries"
            PluginPermission.CHARACTER_DATABASE ->
                "Access the character database"
            PluginPermission.AUDIO_PLAYBACK ->
                "Play audio for text-to-speech"
            PluginPermission.GRADIO_ACCESS ->
                "Connect to Gradio AI endpoints"
        }
    }
    
    /**
     * Get permission risk level
     */
    fun getPermissionRiskLevel(permission: PluginPermission): PermissionRiskLevel {
        return when (permission) {
            PluginPermission.NETWORK -> PermissionRiskLevel.HIGH
            PluginPermission.STORAGE -> PermissionRiskLevel.HIGH
            PluginPermission.LIBRARY_ACCESS -> PermissionRiskLevel.MEDIUM
            PluginPermission.PREFERENCES -> PermissionRiskLevel.MEDIUM
            PluginPermission.READER_CONTEXT -> PermissionRiskLevel.LOW
            PluginPermission.NOTIFICATIONS -> PermissionRiskLevel.LOW
            PluginPermission.CATALOG_WRITE -> PermissionRiskLevel.HIGH
            PluginPermission.SYNC_DATA -> PermissionRiskLevel.HIGH
            PluginPermission.BACKGROUND_SERVICE -> PermissionRiskLevel.MEDIUM
            PluginPermission.LOCAL_SERVER -> PermissionRiskLevel.MEDIUM
            PluginPermission.IMAGE_PROCESSING -> PermissionRiskLevel.LOW
            PluginPermission.UI_INJECTION -> PermissionRiskLevel.HIGH
            PluginPermission.GLOSSARY_ACCESS -> PermissionRiskLevel.LOW
            PluginPermission.CHARACTER_DATABASE -> PermissionRiskLevel.LOW
            PluginPermission.AUDIO_PLAYBACK -> PermissionRiskLevel.LOW
            PluginPermission.GRADIO_ACCESS -> PermissionRiskLevel.MEDIUM
        }
    }
}

/**
 * Permission request pending user approval
 * Requirements: 10.5
 */
data class PermissionRequest(
    val pluginId: String,
    val pluginName: String,
    val permission: PluginPermission,
    val timestamp: Long
)

/**
 * Result of a permission request
 */
sealed class PermissionRequestResult {
    object Granted : PermissionRequestResult()
    object Pending : PermissionRequestResult()
    data class Denied(val reason: String) : PermissionRequestResult()
}

/**
 * Risk level for permissions
 */
enum class PermissionRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}
