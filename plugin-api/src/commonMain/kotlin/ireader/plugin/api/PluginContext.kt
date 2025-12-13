package ireader.plugin.api

/**
 * Context provided to plugins for accessing app resources.
 * Provides sandboxed access to system resources based on granted permissions.
 */
interface PluginContext {
    /**
     * Plugin's unique identifier.
     */
    val pluginId: String
    
    /**
     * Granted permissions for this plugin.
     */
    val permissions: List<PluginPermission>
    
    /**
     * Get the plugin's data directory for storing files.
     * Only available if STORAGE permission is granted.
     * 
     * @return Path to plugin's data directory
     */
    fun getDataDir(): String
    
    /**
     * Check if plugin has a specific permission.
     * 
     * @param permission Permission to check
     * @return true if permission is granted
     */
    fun hasPermission(permission: PluginPermission): Boolean
    
    /**
     * Get plugin-specific preferences storage.
     * Only available if PREFERENCES permission is granted.
     * 
     * @return Preferences store for this plugin
     */
    fun getPreferences(): PluginPreferencesStore
}

/**
 * Plugin-specific preferences storage interface.
 * Provides key-value storage for plugin configuration.
 */
interface PluginPreferencesStore {
    /**
     * Get a string preference.
     */
    fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Set a string preference.
     */
    fun putString(key: String, value: String)
    
    /**
     * Get an integer preference.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Set an integer preference.
     */
    fun putInt(key: String, value: Int)
    
    /**
     * Get a boolean preference.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Set a boolean preference.
     */
    fun putBoolean(key: String, value: Boolean)
    
    /**
     * Remove a preference.
     */
    fun remove(key: String)
    
    /**
     * Clear all preferences for this plugin.
     */
    fun clear()
}
