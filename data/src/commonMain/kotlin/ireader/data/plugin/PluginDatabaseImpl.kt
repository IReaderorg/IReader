package ireader.data.plugin

import ireader.domain.data.repository.PluginRepository
import ireader.domain.plugins.PluginDatabase
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.PluginPermission
import ireader.domain.plugins.PluginStatus
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Implementation of PluginDatabase interface
 * Wraps PluginRepository and provides high-level database operations
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
class PluginDatabaseImpl(
    private val repository: PluginRepository
) : PluginDatabase {

    // In-memory permission storage
    private val grantedPermissions = mutableMapOf<String, MutableSet<PluginPermission>>()

    override suspend fun getPluginInfo(pluginId: String): PluginInfo? {
        return repository.getPlugin(pluginId)
    }

    override suspend fun insertOrUpdate(manifest: PluginManifest, status: PluginStatus) {
        val existing = repository.getPlugin(manifest.id)
        
        val pluginInfo = if (existing != null) {
            existing.copy(
                manifest = manifest,
                status = status,
                lastUpdate = currentTimeToLong()
            )
        } else {
            PluginInfo(
                id = manifest.id,
                manifest = manifest,
                status = status,
                installDate = currentTimeToLong(),
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
        // Also revoke all permissions when deleting plugin
        revokeAllGrantedPermissions(pluginId)
        repository.deletePlugin(pluginId)
    }

    override suspend fun getAllPlugins(): List<PluginInfo> {
        return repository.getAllPlugins()
    }

    override suspend fun updateStatus(pluginId: String, status: PluginStatus) {
        val plugin = repository.getPlugin(pluginId) ?: return
        val updated = plugin.copy(
            status = status,
            lastUpdate = currentTimeToLong()
        )
        repository.updatePlugin(updated)
    }

    // Permission management methods using in-memory storage
    
    override suspend fun saveGrantedPermission(pluginId: String, permission: PluginPermission) {
        val permissions = grantedPermissions.getOrPut(pluginId) { mutableSetOf() }
        permissions.add(permission)
    }

    override suspend fun revokeGrantedPermission(pluginId: String, permission: PluginPermission) {
        grantedPermissions[pluginId]?.remove(permission)
    }

    override suspend fun revokeAllGrantedPermissions(pluginId: String) {
        grantedPermissions.remove(pluginId)
    }

    override suspend fun getAllGrantedPermissions(): Map<String, List<PluginPermission>> {
        return grantedPermissions.mapValues { it.value.toList() }
    }

    override suspend fun getGrantedPermissions(pluginId: String): List<PluginPermission> {
        return grantedPermissions[pluginId]?.toList() ?: emptyList()
    }
}
