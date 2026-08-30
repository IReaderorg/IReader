package ireader.domain.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Thread-safe registry for managing loaded plugins
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 14.1, 14.2, 14.3, 14.4, 14.5
 */
class PluginRegistry(
    private val database: PluginDatabase
) {
    @kotlin.concurrent.Volatile
    private var plugins = mapOf<String, Plugin>()
    private val lock = Mutex()
    
    /**
     * Register multiple plugins
     */
    suspend fun registerAll(pluginList: List<Plugin>) {
        lock.withLock {
            val updated = plugins.toMutableMap()
            pluginList.forEach { plugin ->
                updated[plugin.manifest.id] = plugin
                database.insertOrUpdate(plugin.manifest)
            }
            plugins = updated
        }
    }
    
    /**
     * Get a plugin by ID
     */
    fun get(pluginId: String): Plugin? = plugins[pluginId]
    
    /**
     * Get all registered plugins with their info
     */
    suspend fun getAll(): List<PluginInfo> {
        val currentPlugins = plugins.values
        return currentPlugins.map { plugin ->
            val dbInfo = database.getPluginInfo(plugin.manifest.id)
            dbInfo ?: PluginInfo(
                id = plugin.manifest.id,
                manifest = plugin.manifest,
                status = PluginStatus.DISABLED,
                installDate = currentTimeToLong(),
                lastUpdate = null,
                isPurchased = false,
                rating = null,
                downloadCount = 0
            )
        }
    }
    
    /**
     * Get plugins by type
     */
    fun getByType(type: PluginType): List<Plugin> {
        return plugins.values.filter { it.manifest.type == type }
    }
    
    /**
     * Remove a plugin from the registry
     */
    suspend fun remove(pluginId: String) {
        lock.withLock {
            val updated = plugins.toMutableMap()
            updated.remove(pluginId)
            plugins = updated
            database.delete(pluginId)
        }
    }
    
    /**
     * Register a single plugin
     */
    suspend fun register(plugin: Plugin) {
        lock.withLock {
            val updated = plugins.toMutableMap()
            updated[plugin.manifest.id] = plugin
            plugins = updated
            database.insertOrUpdate(plugin.manifest)
        }
    }
    
    /**
     * Check if a plugin is registered
     */
    fun contains(pluginId: String): Boolean = plugins.containsKey(pluginId)
    
    /**
     * Get count of registered plugins
     */
    fun size(): Int = plugins.size
}
