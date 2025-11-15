package ireader.data.plugin

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.PluginRepository
import ireader.domain.plugins.PluginDatabase
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.PluginPermission
import ireader.domain.plugins.PluginStatus

/**
 * Implementation of PluginDatabase interface
 * Wraps PluginRepository and provides high-level database operations
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
class PluginDatabaseImpl(
    private val repository: PluginRepository,
    private val handler: DatabaseHandler
) : PluginDatabase {

    override suspend fun getPluginInfo(pluginId: String): PluginInfo? {
        return repository.getPlugin(pluginId)
    }

    override suspend fun insertOrUpdate(manifest: PluginManifest, status: PluginStatus) {
        val existing = repository.getPlugin(manifest.id)
        
        val pluginInfo = if (existing != null) {
            existing.copy(
                manifest = manifest,
                status = status,
                lastUpdate = System.currentTimeMillis()
            )
        } else {
            PluginInfo(
                id = manifest.id,
                manifest = manifest,
                status = status,
                installDate = System.currentTimeMillis(),
                lastUpdate = null,
                isPurchased = false,
                rating = null,
                downloadCount = 0
            )
        }
        
        if (existing != null) {
            repository.updatePlugin(pluginInfo)
        } else {
            repository.insertPlugin(pluginInfo)
        }
    }

    override suspend fun delete(pluginId: String) {
        repository.deletePlugin(pluginId)
    }

    override suspend fun getAllPlugins(): List<PluginInfo> {
        return repository.getAllPlugins()
    }

    override suspend fun updateStatus(pluginId: String, status: PluginStatus) {
        val plugin = repository.getPlugin(pluginId) ?: return
        val updated = plugin.copy(
            status = status,
            lastUpdate = System.currentTimeMillis()
        )
        repository.updatePlugin(updated)
    }

    // Permission management methods
    // Note: These would ideally be stored in a separate table, but for now we'll use a simple approach
    // In a full implementation, you'd want to create a plugin_permissions table
    
    override suspend fun saveGrantedPermission(pluginId: String, permission: PluginPermission) {
        // TODO: Implement with dedicated permissions table if needed
        // For now, permissions are managed in-memory by PluginPermissionManager
    }

    override suspend fun revokeGrantedPermission(pluginId: String, permission: PluginPermission) {
        // TODO: Implement with dedicated permissions table if needed
    }

    override suspend fun revokeAllGrantedPermissions(pluginId: String) {
        // TODO: Implement with dedicated permissions table if needed
    }

    override suspend fun getAllGrantedPermissions(): Map<String, List<PluginPermission>> {
        // TODO: Implement with dedicated permissions table if needed
        return emptyMap()
    }

    override suspend fun getGrantedPermissions(pluginId: String): List<PluginPermission> {
        // TODO: Implement with dedicated permissions table if needed
        // For now, return permissions from manifest
        val plugin = repository.getPlugin(pluginId)
        return plugin?.manifest?.permissions ?: emptyList()
    }
}
