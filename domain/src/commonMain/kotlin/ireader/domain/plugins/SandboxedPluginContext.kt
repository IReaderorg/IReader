package ireader.domain.plugins

import kotlinx.coroutines.runBlocking

/**
 * Sandboxed implementation of PluginContext
 * Provides secure, restricted access to app resources based on permissions
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
class SandboxedPluginContext(
    override val pluginId: String,
    override val permissions: List<PluginPermission>,
    private val sandbox: PluginSandbox,
    private val preferencesStore: PluginPreferencesStore
) : PluginContext {
    
    /**
     * Get the plugin's data directory for storing files
     * Requirements: 10.4
     * 
     * Note: This is cached on first access to avoid blocking calls.
     * The sandbox should initialize the data directory eagerly.
     */
    private val cachedDataDir: String by lazy {
        runBlocking {
            sandbox.getPluginDataDir(pluginId).path
        }
    }
    
    override fun getDataDir(): String {
        return cachedDataDir
    }
    
    /**
     * Check if plugin has a specific permission
     * Requirements: 10.1, 10.2
     */
    override fun hasPermission(permission: PluginPermission): Boolean {
        return sandbox.checkPermission(permission)
    }
    
    /**
     * Get plugin-specific preferences storage
     * Requirements: 10.2
     */
    override fun getPreferences(): PluginPreferencesStore {
        // Verify preferences permission
        if (!hasPermission(PluginPermission.PREFERENCES)) {
            return RestrictedPluginPreferencesStore()
        }
        
        return preferencesStore
    }
    
    /**
     * Validate file access before operation
     * Requirements: 10.3, 10.4
     */
    suspend fun validateFileAccess(path: String): Result<Unit> {
        return sandbox.validateFileOperation(path, FileOperation.READ)
    }
    
    /**
     * Validate network access before operation
     * Requirements: 10.3
     */
    fun validateNetworkAccess(url: String): Result<Unit> {
        return sandbox.validateNetworkOperation(url)
    }
    
    /**
     * Get current resource usage
     * Requirements: 11.1, 11.2
     */
    fun getResourceUsage(): PluginResourceUsage {
        return sandbox.getResourceUsage()
    }
    
    /**
     * Check if plugin has exceeded resource limits
     * Requirements: 11.3, 11.4
     */
    fun hasExceededResourceLimits(): Boolean {
        return sandbox.hasExceededResourceLimits()
    }
}

/**
 * Restricted preferences store that denies all operations
 * Used when plugin doesn't have preferences permission
 */
class RestrictedPluginPreferencesStore : PluginPreferencesStore {
    private fun throwPermissionError(): Nothing {
        throw SecurityException("Plugin does not have PREFERENCES permission")
    }
    
    override fun getString(key: String, defaultValue: String): String = defaultValue
    override fun putString(key: String, value: String) = throwPermissionError()
    override fun getInt(key: String, defaultValue: Int): Int = defaultValue
    override fun putInt(key: String, value: Int) = throwPermissionError()
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun putBoolean(key: String, value: Boolean) = throwPermissionError()
    override fun remove(key: String) = throwPermissionError()
    override fun clear() = throwPermissionError()
}

/**
 * Factory for creating sandboxed plugin contexts
 */
class PluginContextFactory(
    private val permissionManager: PluginPermissionManager,
    private val fileSystem: ireader.core.io.FileSystem
) {
    /**
     * Create a sandboxed context for a plugin
     * Requirements: 10.1, 10.2, 10.3, 10.4
     */
    fun createContext(
        pluginId: String,
        manifest: PluginManifest,
        preferencesStore: PluginPreferencesStore
    ): SandboxedPluginContext {
        val sandbox = PluginSandbox(
            pluginId = pluginId,
            manifest = manifest,
            permissionManager = permissionManager,
            fileSystem = fileSystem
        )
        
        return SandboxedPluginContext(
            pluginId = pluginId,
            permissions = manifest.permissions,
            sandbox = sandbox,
            preferencesStore = preferencesStore
        )
    }
}
