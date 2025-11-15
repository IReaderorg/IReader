package ireader.domain.plugins

/**
 * Context provided to plugins for accessing app resources
 * Provides sandboxed access to system resources based on permissions
 * Requirements: 10.1, 10.2, 10.4
 */
interface PluginContext {
    /**
     * Plugin's unique identifier
     */
    val pluginId: String
    
    /**
     * Granted permissions for this plugin
     */
    val permissions: List<PluginPermission>
    
    /**
     * Get the plugin's data directory for storing files
     */
    fun getDataDir(): String
    
    /**
     * Check if plugin has a specific permission
     */
    fun hasPermission(permission: PluginPermission): Boolean
    
    /**
     * Get plugin-specific preferences storage
     */
    fun getPreferences(): PluginPreferencesStore
}

/**
 * Plugin-specific preferences storage interface
 */
interface PluginPreferencesStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
    fun clear()
}
