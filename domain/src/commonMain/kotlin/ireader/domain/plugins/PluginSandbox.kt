package ireader.domain.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * Sandbox environment for plugin execution with permission checking and resource restrictions
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 */
class PluginSandbox(
    private val pluginId: String,
    private val manifest: PluginManifest,
    private val permissionManager: PluginPermissionManager,
    private val pluginsBaseDir: File
) {
    private val resourceMonitor = PluginResourceMonitor(pluginId)
    private val mutex = Mutex()
    
    /**
     * Check if plugin has a specific permission
     * Requirements: 10.1, 10.2
     */
    fun checkPermission(permission: PluginPermission): Boolean {
        // Check manifest permissions
        if (!manifest.permissions.contains(permission)) {
            return false
        }
        
        // Check runtime granted permissions
        return permissionManager.isPermissionGranted(pluginId, permission)
    }
    
    /**
     * Restrict file access to plugin's own directory
     * Requirements: 10.3, 10.4
     */
    fun restrictFileAccess(path: String): Boolean {
        val pluginDataDir = getPluginDataDir(pluginId)
        val normalizedPath = File(path).canonicalPath
        val normalizedPluginDir = File(pluginDataDir).canonicalPath
        
        // Only allow access within plugin's directory
        if (!normalizedPath.startsWith(normalizedPluginDir)) {
            return false
        }
        
        // Check storage permission
        return checkPermission(PluginPermission.STORAGE)
    }
    
    /**
     * Restrict network access based on permission
     * Requirements: 10.3, 10.4
     */
    fun restrictNetworkAccess(url: String): Boolean {
        // Check network permission
        if (!checkPermission(PluginPermission.NETWORK)) {
            return false
        }
        
        // Additional URL validation could be added here
        // For example, blocking certain domains or protocols
        
        return true
    }
    
    /**
     * Get plugin-specific data directory path
     * Requirements: 10.4
     */
    fun getPluginDataDir(pluginId: String): String {
        val pluginDir = File(pluginsBaseDir, "data/$pluginId")
        
        // Create directory if it doesn't exist
        if (!pluginDir.exists()) {
            pluginDir.mkdirs()
        }
        
        return pluginDir.absolutePath
    }
    
    /**
     * Check if plugin can access reader context
     * Requirements: 10.2
     */
    fun canAccessReaderContext(): Boolean {
        return checkPermission(PluginPermission.READER_CONTEXT)
    }
    
    /**
     * Check if plugin can access library
     * Requirements: 10.2
     */
    fun canAccessLibrary(): Boolean {
        return checkPermission(PluginPermission.LIBRARY_ACCESS)
    }
    
    /**
     * Check if plugin can access preferences
     * Requirements: 10.2
     */
    fun canAccessPreferences(): Boolean {
        return checkPermission(PluginPermission.PREFERENCES)
    }
    
    /**
     * Check if plugin can show notifications
     * Requirements: 10.2
     */
    fun canShowNotifications(): Boolean {
        return checkPermission(PluginPermission.NOTIFICATIONS)
    }
    
    /**
     * Record resource usage for monitoring
     * Requirements: 11.1, 11.2
     */
    suspend fun recordResourceUsage(
        cpuUsage: Double,
        memoryUsage: Long,
        networkUsage: Long
    ) {
        mutex.withLock {
            resourceMonitor.recordUsage(cpuUsage, memoryUsage, networkUsage)
        }
    }
    
    /**
     * Check if plugin has exceeded resource limits
     * Requirements: 11.3, 11.4
     */
    fun hasExceededResourceLimits(): Boolean {
        return resourceMonitor.hasExceededLimits()
    }
    
    /**
     * Get current resource usage statistics
     * Requirements: 11.1, 11.2
     */
    fun getResourceUsage(): PluginResourceUsage {
        return resourceMonitor.getCurrentUsage()
    }
    
    /**
     * Reset resource usage statistics
     */
    fun resetResourceUsage() {
        resourceMonitor.reset()
    }
    
    /**
     * Validate file operation before allowing it
     */
    fun validateFileOperation(path: String, operation: FileOperation): Result<Unit> {
        if (!restrictFileAccess(path)) {
            return Result.failure(
                SecurityException("Plugin $pluginId does not have access to path: $path")
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Validate network operation before allowing it
     */
    fun validateNetworkOperation(url: String): Result<Unit> {
        if (!restrictNetworkAccess(url)) {
            return Result.failure(
                SecurityException("Plugin $pluginId does not have network permission or URL is blocked: $url")
            )
        }
        
        return Result.success(Unit)
    }
}

/**
 * Types of file operations
 */
enum class FileOperation {
    READ,
    WRITE,
    DELETE,
    CREATE
}


