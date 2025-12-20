package ireader.domain.plugins

import ireader.core.io.FileSystem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central security manager coordinating plugin sandboxing, permissions, and resource monitoring
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 11.1, 11.2, 11.3, 11.4, 11.5
 */
class PluginSecurityManager(
    private val permissionManager: PluginPermissionManager,
    private val fileSystem: FileSystem
) {
    private val mutex = Mutex()
    private val sandboxes = mutableMapOf<String, PluginSandbox>()
    private val contextFactory = PluginContextFactory(permissionManager, fileSystem)
    
    /**
     * Pending permission requests from plugins
     */
    val pendingPermissionRequests: StateFlow<List<PermissionRequest>> = 
        permissionManager.pendingRequests
    
    /**
     * Initialize security manager
     * Requirements: 10.1
     */
    suspend fun initialize() {
        permissionManager.initialize()
    }
    
    /**
     * Create a sandboxed context for a plugin
     * Requirements: 10.1, 10.2, 10.3, 10.4
     */
    suspend fun createPluginContext(
        pluginId: String,
        manifest: PluginManifest,
        preferencesStore: PluginPreferencesStore
    ): SandboxedPluginContext {
        mutex.withLock {
            // Create sandbox if it doesn't exist
            if (!sandboxes.containsKey(pluginId)) {
                val sandbox = PluginSandbox(
                    pluginId = pluginId,
                    manifest = manifest,
                    permissionManager = permissionManager,
                    fileSystem = fileSystem
                )
                sandboxes[pluginId] = sandbox
            }
        }
        
        // Auto-grant permissions declared in the manifest
        // This ensures plugins can use their declared permissions without requiring
        // additional user approval (the user already approved by installing the plugin)
        manifest.permissions.forEach { permission ->
            if (!permissionManager.isPermissionGranted(pluginId, permission)) {
                println("[PluginSecurityManager] Auto-granting permission $permission to plugin $pluginId")
                permissionManager.grantPermission(pluginId, permission)
            }
        }
        
        return contextFactory.createContext(pluginId, manifest, preferencesStore)
    }
    
    /**
     * Request a permission for a plugin
     * Requirements: 10.5
     */
    suspend fun requestPermission(
        pluginId: String,
        permission: PluginPermission,
        manifest: PluginManifest
    ): PermissionRequestResult {
        return permissionManager.requestPermission(pluginId, permission, manifest)
    }
    
    /**
     * Grant a permission to a plugin
     * Requirements: 10.2, 10.5
     */
    suspend fun grantPermission(
        pluginId: String,
        permission: PluginPermission
    ): PermissionRequestResult {
        return permissionManager.grantPermission(pluginId, permission)
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
        return permissionManager.denyPermission(pluginId, permission, reason)
    }
    
    /**
     * Revoke a permission from a plugin
     * Requirements: 10.2
     */
    suspend fun revokePermission(pluginId: String, permission: PluginPermission) {
        permissionManager.revokePermission(pluginId, permission)
    }
    
    /**
     * Check if a plugin has a permission
     * Requirements: 10.1, 10.2
     */
    fun hasPermission(pluginId: String, permission: PluginPermission): Boolean {
        return permissionManager.isPermissionGranted(pluginId, permission)
    }
    
    /**
     * Get all granted permissions for a plugin
     * Requirements: 10.1
     */
    fun getGrantedPermissions(pluginId: String): Set<PluginPermission> {
        return permissionManager.getGrantedPermissions(pluginId)
    }
    
    /**
     * Validate file access for a plugin
     * Requirements: 10.3, 10.4
     */
    suspend fun validateFileAccess(pluginId: String, path: String): Result<Unit> {
        val sandbox = sandboxes[pluginId]
            ?: return Result.failure(IllegalStateException("Plugin sandbox not found"))
        
        return sandbox.validateFileOperation(path, FileOperation.READ)
    }
    
    /**
     * Validate network access for a plugin
     * Requirements: 10.3
     */
    fun validateNetworkAccess(pluginId: String, url: String): Result<Unit> {
        val sandbox = sandboxes[pluginId]
            ?: return Result.failure(IllegalStateException("Plugin sandbox not found"))
        
        return sandbox.validateNetworkOperation(url)
    }
    
    /**
     * Record resource usage for a plugin
     * Requirements: 11.1, 11.2
     */
    suspend fun recordResourceUsage(
        pluginId: String,
        cpuUsage: Double,
        memoryUsage: Long,
        networkUsage: Long
    ) {
        val sandbox = sandboxes[pluginId] ?: return
        sandbox.recordResourceUsage(cpuUsage, memoryUsage, networkUsage)
    }
    
    /**
     * Get resource usage for a plugin
     * Requirements: 11.1, 11.2
     */
    fun getResourceUsage(pluginId: String): PluginResourceUsage? {
        return sandboxes[pluginId]?.getResourceUsage()
    }
    
    /**
     * Check if a plugin has exceeded resource limits
     * Requirements: 11.3, 11.4
     */
    fun hasExceededResourceLimits(pluginId: String): Boolean {
        return sandboxes[pluginId]?.hasExceededResourceLimits() ?: false
    }
    
    /**
     * Get all plugins that have exceeded resource limits
     * Requirements: 11.3, 11.4, 11.5
     */
    fun getPluginsExceedingLimits(): List<String> {
        return sandboxes
            .filter { (_, sandbox) -> sandbox.hasExceededResourceLimits() }
            .keys
            .toList()
    }
    
    /**
     * Terminate a plugin due to resource limit violation
     * Requirements: 11.5
     */
    suspend fun terminatePlugin(pluginId: String, reason: String): Result<Unit> {
        mutex.withLock {
            val sandbox = sandboxes[pluginId]
                ?: return Result.failure(Exception("Plugin sandbox not found"))
            
            // Reset resource usage
            sandbox.resetResourceUsage()
            
            // Remove sandbox
            sandboxes.remove(pluginId)
            
            return Result.success(Unit)
        }
    }
    
    /**
     * Clean up security resources for a plugin
     * Requirements: 10.4
     */
    suspend fun cleanupPlugin(pluginId: String) {
        mutex.withLock {
            sandboxes.remove(pluginId)
        }
    }
    
    /**
     * Get permission description for UI display
     */
    fun getPermissionDescription(permission: PluginPermission): String {
        return permissionManager.getPermissionDescription(permission)
    }
    
    /**
     * Get permission risk level
     */
    fun getPermissionRiskLevel(permission: PluginPermission): PermissionRiskLevel {
        return permissionManager.getPermissionRiskLevel(permission)
    }
}
