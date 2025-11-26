package ireader.domain.plugins

import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central service for managing plugin lifecycle
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 14.1, 14.2, 14.3, 14.4, 14.5
 */
class PluginManager(
    private val fileSystem: FileSystem,
    private val loader: PluginLoader,
    private val registry: PluginRegistry,
    private val preferences: PluginPreferences,
    private val monetization: MonetizationService,
    private val database: PluginDatabase,
    private val securityManager: PluginSecurityManager,
    private val performanceMetricsManager: ireader.domain.monitoring.PerformanceMetricsManager
) {
    private val scope = createICoroutineScope()
    private val _pluginsFlow = MutableStateFlow<List<PluginInfo>>(emptyList())
    
    /**
     * Observable flow of all plugins
     */
    val pluginsFlow: StateFlow<List<PluginInfo>> = _pluginsFlow.asStateFlow()
    
    /**
     * Load all plugins from the plugins directory
     */
    suspend fun loadPlugins() {
        try {
            // Initialize security manager
            securityManager.initialize()
            
            val plugins = loader.loadAll()
            registry.registerAll(plugins)
            
            // Initialize enabled plugins
            val enabledIds = preferences.enabledPlugins().get()
            plugins.forEach { plugin ->
                if (enabledIds.contains(plugin.manifest.id)) {
                    try {
                        // Track plugin load performance
                        performanceMetricsManager.startOperation(plugin.manifest.id, "load")
                        
                        // Create sandboxed context for plugin
                        val context = securityManager.createPluginContext(
                            pluginId = plugin.manifest.id,
                            manifest = plugin.manifest,
                            preferencesStore = createPluginPreferencesStore(plugin.manifest.id)
                        )
                        
                        plugin.initialize(context)
                        database.updateStatus(plugin.manifest.id, PluginStatus.ENABLED)
                        
                        performanceMetricsManager.endOperation(plugin.manifest.id, "load", success = true)
                    } catch (e: Exception) {
                        performanceMetricsManager.endOperation(plugin.manifest.id, "load", success = false)
                        performanceMetricsManager.recordError(plugin.manifest.id, e)
                        database.updateStatus(plugin.manifest.id, PluginStatus.ERROR)
                    }
                }
            }
            
            _pluginsFlow.value = registry.getAll()
        } catch (e: Exception) {
            // Log error but don't crash
            _pluginsFlow.value = emptyList()
        }
    }
    
    /**
     * Install a plugin from a package file
     */
    suspend fun installPlugin(packageFile: VirtualFile): Result<PluginInfo> {
        return try {
            val plugin = loader.loadPlugin(packageFile)
                ?: return Result.failure(Exception("Failed to load plugin from package"))
            
            // Check if plugin requires purchase
            if (plugin.manifest.monetization is PluginMonetization.Premium) {
                val isPurchased = monetization.isPurchased(plugin.manifest.id)
                if (!isPurchased) {
                    return Result.failure(Exception("Plugin requires purchase"))
                }
            }
            
            registry.register(plugin)
            database.insertOrUpdate(plugin.manifest, PluginStatus.DISABLED)
            
            _pluginsFlow.value = registry.getAll()
            
            val pluginInfo = database.getPluginInfo(plugin.manifest.id)
                ?: return Result.failure(Exception("Failed to retrieve plugin info"))
            
            Result.success(pluginInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Uninstall a plugin
     */
    suspend fun uninstallPlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = registry.get(pluginId)
                ?: return Result.failure(Exception("Plugin not found"))
            
            // Cleanup plugin resources
            try {
                plugin.cleanup()
            } catch (e: Exception) {
                // Log but continue with uninstall
            }
            
            // Remove from enabled plugins
            val enabledIds = preferences.enabledPlugins().get().toMutableSet()
            enabledIds.remove(pluginId)
            preferences.enabledPlugins().set(enabledIds)
            
            // Remove from registry and database
            registry.remove(pluginId)
            
            _pluginsFlow.value = registry.getAll()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Enable a plugin
     */
    suspend fun enablePlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = registry.get(pluginId)
                ?: return Result.failure(Exception("Plugin not found"))
            
            // Track plugin enable performance
            performanceMetricsManager.startOperation(pluginId, "enable")
            
            // Create sandboxed context for plugin
            val context = securityManager.createPluginContext(
                pluginId = plugin.manifest.id,
                manifest = plugin.manifest,
                preferencesStore = createPluginPreferencesStore(plugin.manifest.id)
            )
            
            // Initialize plugin
            plugin.initialize(context)
            
            // Add to enabled plugins
            val enabledIds = preferences.enabledPlugins().get().toMutableSet()
            enabledIds.add(pluginId)
            preferences.enabledPlugins().set(enabledIds)
            
            // Update status
            database.updateStatus(pluginId, PluginStatus.ENABLED)
            
            performanceMetricsManager.endOperation(pluginId, "enable", success = true)
            
            _pluginsFlow.value = registry.getAll()
            
            Result.success(Unit)
        } catch (e: Exception) {
            performanceMetricsManager.endOperation(pluginId, "enable", success = false)
            performanceMetricsManager.recordError(pluginId, e)
            database.updateStatus(pluginId, PluginStatus.ERROR)
            _pluginsFlow.value = registry.getAll()
            Result.failure(e)
        }
    }
    
    /**
     * Disable a plugin
     */
    suspend fun disablePlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = registry.get(pluginId)
                ?: return Result.failure(Exception("Plugin not found"))
            
            // Cleanup plugin resources
            plugin.cleanup()
            
            // Cleanup security resources
            securityManager.cleanupPlugin(pluginId)
            
            // Remove from enabled plugins
            val enabledIds = preferences.enabledPlugins().get().toMutableSet()
            enabledIds.remove(pluginId)
            preferences.enabledPlugins().set(enabledIds)
            
            // Update status
            database.updateStatus(pluginId, PluginStatus.DISABLED)
            
            _pluginsFlow.value = registry.getAll()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a plugin by ID
     */
    fun getPlugin(pluginId: String): Plugin? {
        return registry.get(pluginId)
    }
    
    /**
     * Get plugins by type
     */
    fun getPluginsByType(type: PluginType): List<Plugin> {
        return registry.getByType(type)
    }
    
    /**
     * Get enabled plugins
     */
    fun getEnabledPlugins(): List<Plugin> {
        val enabledIds = preferences.enabledPlugins().get()
        return enabledIds.mapNotNull { registry.get(it) }
    }
    
    /**
     * Refresh the plugins flow
     */
    fun refreshPlugins() {
        scope.launch {
            _pluginsFlow.value = registry.getAll()
        }
    }
    
    /**
     * Request a permission for a plugin
     * Requirements: 10.5
     */
    suspend fun requestPermission(
        pluginId: String,
        permission: PluginPermission
    ): PermissionRequestResult {
        val plugin = registry.get(pluginId)
            ?: return PermissionRequestResult.Denied("Plugin not found")
        
        return securityManager.requestPermission(pluginId, permission, plugin.manifest)
    }
    
    /**
     * Grant a permission to a plugin
     * Requirements: 10.2, 10.5
     */
    suspend fun grantPermission(
        pluginId: String,
        permission: PluginPermission
    ): PermissionRequestResult {
        return securityManager.grantPermission(pluginId, permission)
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
        return securityManager.denyPermission(pluginId, permission, reason)
    }
    
    /**
     * Get resource usage for a plugin
     * Requirements: 11.1, 11.2
     */
    fun getPluginResourceUsage(pluginId: String): PluginResourceUsage? {
        return securityManager.getResourceUsage(pluginId)
    }
    
    /**
     * Get performance metrics for a plugin
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
     */
    fun getPluginPerformanceMetrics(pluginId: String): PluginPerformanceInfo {
        return performanceMetricsManager.getMetrics(pluginId)
    }
    
    /**
     * Get performance metrics for all plugins
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
     */
    fun getAllPerformanceMetrics(): List<PluginPerformanceInfo> {
        return performanceMetricsManager.getAllMetrics()
    }
    
    /**
     * Check plugins exceeding resource limits and terminate if needed
     * Requirements: 11.3, 11.4, 11.5
     */
    suspend fun checkAndTerminateExcessivePlugins() {
        val excessivePlugins = securityManager.getPluginsExceedingLimits()
        
        excessivePlugins.forEach { pluginId ->
            // Disable the plugin
            disablePlugin(pluginId)
            
            // Terminate it
            securityManager.terminatePlugin(
                pluginId,
                "Plugin exceeded resource limits"
            )
        }
    }
    
    /**
     * Execute a plugin operation with performance tracking
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
     */
    suspend fun <T> executePluginOperation(
        pluginId: String,
        operation: String,
        block: suspend () -> T
    ): Result<T> {
        performanceMetricsManager.startOperation(pluginId, operation)

        return try {
            val result = block()
            performanceMetricsManager.endOperation(pluginId, operation, success = true)
            Result.success(result)
        } catch (e: Exception) {
            performanceMetricsManager.endOperation(pluginId, operation, success = false)
            performanceMetricsManager.recordError(pluginId, e)
            Result.failure(e)
        }
    }
    
    /**
     * Create plugin-specific preferences store
     */
    private fun createPluginPreferencesStore(pluginId: String): PluginPreferencesStore {
        // This would be implemented to use the actual preferences system
        // For now, return a simple in-memory implementation
        return InMemoryPluginPreferencesStore()
    }
}

/**
 * Simple in-memory implementation of PluginPreferencesStore
 * In production, this would be backed by actual persistent storage
 */
private class InMemoryPluginPreferencesStore : PluginPreferencesStore {
    private val data = mutableMapOf<String, Any>()
    
    override fun getString(key: String, defaultValue: String): String {
        return data[key] as? String ?: defaultValue
    }
    
    override fun putString(key: String, value: String) {
        data[key] = value
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return data[key] as? Int ?: defaultValue
    }
    
    override fun putInt(key: String, value: Int) {
        data[key] = value
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return data[key] as? Boolean ?: defaultValue
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        data[key] = value
    }
    
    override fun remove(key: String) {
        data.remove(key)
    }
    
    override fun clear() {
        data.clear()
    }
}
