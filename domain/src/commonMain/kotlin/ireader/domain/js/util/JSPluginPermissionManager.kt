package ireader.domain.js.util

import ireader.core.prefs.PreferenceStore
import ireader.domain.js.models.JSPluginPermission
import ireader.domain.js.models.PermissionResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages permissions for JavaScript plugins.
 * Tracks granted permissions and handles permission requests.
 */
class JSPluginPermissionManager(
    private val preferenceStore: PreferenceStore
) {
    
    @Serializable
    private data class PluginPermissions(
        val pluginId: String,
        val grantedPermissions: List<String>
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Checks if a plugin has a specific permission.
     * @param pluginId The plugin ID
     * @param permission The permission to check
     * @return true if permission is granted, false otherwise
     */
    fun hasPermission(pluginId: String, permission: JSPluginPermission): Boolean {
        val permissions = getGrantedPermissions(pluginId)
        return permissions.contains(permission)
    }
    
    /**
     * Grants a permission to a plugin.
     * @param pluginId The plugin ID
     * @param permission The permission to grant
     */
    fun grantPermission(pluginId: String, permission: JSPluginPermission) {
        val currentPermissions = getGrantedPermissions(pluginId).toMutableSet()
        currentPermissions.add(permission)
        savePermissions(pluginId, currentPermissions.toList())
    }
    
    /**
     * Revokes a permission from a plugin.
     * @param pluginId The plugin ID
     * @param permission The permission to revoke
     */
    fun revokePermission(pluginId: String, permission: JSPluginPermission) {
        val currentPermissions = getGrantedPermissions(pluginId).toMutableSet()
        currentPermissions.remove(permission)
        savePermissions(pluginId, currentPermissions.toList())
    }
    
    /**
     * Grants all requested permissions to a plugin.
     * @param pluginId The plugin ID
     * @param permissions The permissions to grant
     */
    fun grantAllPermissions(pluginId: String, permissions: List<JSPluginPermission>) {
        savePermissions(pluginId, permissions)
    }
    
    /**
     * Gets all granted permissions for a plugin.
     * @param pluginId The plugin ID
     * @return List of granted permissions
     */
    fun getGrantedPermissions(pluginId: String): List<JSPluginPermission> {
        val key = getPermissionKey(pluginId)
        val permissionsJson = preferenceStore.getString(key).get()
        
        if (permissionsJson.isEmpty()) {
            return emptyList()
        }
        
        return try {
            val pluginPermissions = json.decodeFromString<PluginPermissions>(permissionsJson)
            pluginPermissions.grantedPermissions.mapNotNull { permName ->
                try {
                    JSPluginPermission.valueOf(permName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Checks if permissions have been granted for a plugin (first-time check).
     * @param pluginId The plugin ID
     * @return true if permissions have been set, false if this is first use
     */
    fun hasPermissionsSet(pluginId: String): Boolean {
        val key = getPermissionKey(pluginId)
        return preferenceStore.getString(key).isSet()
    }
    
    /**
     * Requests permissions for a plugin.
     * This method should be called before first use to prompt user.
     * @param pluginId The plugin ID
     * @param permissions The permissions to request
     * @return PermissionResult indicating if permissions were granted
     */
    suspend fun requestPermissions(
        pluginId: String,
        permissions: List<JSPluginPermission>
    ): PermissionResult {
        // Check if permissions are already granted
        if (hasPermissionsSet(pluginId)) {
            val grantedPermissions = getGrantedPermissions(pluginId)
            val allGranted = permissions.all { it in grantedPermissions }
            return if (allGranted) {
                PermissionResult.Granted
            } else {
                PermissionResult.Denied
            }
        }
        
        // For now, auto-grant NETWORK and STORAGE permissions
        // In a full implementation, this would show a dialog to the user
        val autoGrantPermissions = permissions.filter { 
            it == JSPluginPermission.NETWORK || it == JSPluginPermission.STORAGE 
        }
        
        if (autoGrantPermissions.isNotEmpty()) {
            grantAllPermissions(pluginId, autoGrantPermissions)
            return PermissionResult.Granted
        }
        
        return PermissionResult.Denied
    }
    
    /**
     * Clears all permissions for a plugin.
     * @param pluginId The plugin ID
     */
    fun clearPermissions(pluginId: String) {
        val key = getPermissionKey(pluginId)
        preferenceStore.getString(key).delete()
    }
    
    /**
     * Saves permissions for a plugin.
     */
    private fun savePermissions(pluginId: String, permissions: List<JSPluginPermission>) {
        val key = getPermissionKey(pluginId)
        val pluginPermissions = PluginPermissions(
            pluginId = pluginId,
            grantedPermissions = permissions.map { it.name }
        )
        val permissionsJson = json.encodeToString(pluginPermissions)
        preferenceStore.getString(key).set(permissionsJson)
    }
    
    /**
     * Gets the preference key for plugin permissions.
     */
    private fun getPermissionKey(pluginId: String): String {
        return "js_plugin_permissions_$pluginId"
    }
}
