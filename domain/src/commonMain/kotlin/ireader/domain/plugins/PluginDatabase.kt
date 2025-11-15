package ireader.domain.plugins

/**
 * Database interface for plugin persistence
 * This is a placeholder interface - actual implementation will be in the data layer
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
interface PluginDatabase {
    /**
     * Get plugin information from database
     */
    suspend fun getPluginInfo(pluginId: String): PluginInfo?
    
    /**
     * Insert or update plugin information
     */
    suspend fun insertOrUpdate(manifest: PluginManifest, status: PluginStatus = PluginStatus.DISABLED)
    
    /**
     * Delete plugin from database
     */
    suspend fun delete(pluginId: String)
    
    /**
     * Get all plugins from database
     */
    suspend fun getAllPlugins(): List<PluginInfo>
    
    /**
     * Update plugin status
     */
    suspend fun updateStatus(pluginId: String, status: PluginStatus)
    
    /**
     * Save granted permission for a plugin
     * Requirements: 10.2, 10.5
     */
    suspend fun saveGrantedPermission(pluginId: String, permission: PluginPermission)
    
    /**
     * Revoke granted permission from a plugin
     * Requirements: 10.2
     */
    suspend fun revokeGrantedPermission(pluginId: String, permission: PluginPermission)
    
    /**
     * Revoke all granted permissions for a plugin
     * Requirements: 10.2
     */
    suspend fun revokeAllGrantedPermissions(pluginId: String)
    
    /**
     * Get all granted permissions for all plugins
     * Requirements: 10.1, 10.2
     */
    suspend fun getAllGrantedPermissions(): Map<String, List<PluginPermission>>
    
    /**
     * Get granted permissions for a specific plugin
     * Requirements: 10.1
     */
    suspend fun getGrantedPermissions(pluginId: String): List<PluginPermission>
}
