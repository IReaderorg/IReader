package ireader.domain.plugins

import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import ireader.core.util.createICoroutineScope
import ireader.plugin.api.PluginMonetization as ApiPluginMonetization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

/**
 * Progress callback for plugin installation
 */
typealias PluginInstallProgressCallback = (PluginInstallProgress) -> Unit

/**
 * Plugin installation progress data
 */
data class PluginInstallProgress(
    val pluginId: String,
    val pluginName: String,
    val stage: InstallStage,
    val progress: Float = 0f,  // 0.0 to 1.0
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null
)

/**
 * Installation stage enum
 */
enum class InstallStage {
    PENDING,
    DOWNLOADING,
    VALIDATING,
    INSTALLING,
    COMPLETED,
    FAILED
}

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
        println("[PluginManager] loadPlugins() called")
        try {
            // Initialize security manager
            securityManager.initialize()
            
            val plugins = loader.loadAll()
            println("[PluginManager] Loaded ${plugins.size} plugins: ${plugins.map { "${it.manifest.id} (${it.manifest.type})" }}")
            registry.registerAll(plugins)
            
            // Initialize enabled plugins and auto-enable engine plugins
            val enabledIds = preferences.enabledPlugins().get().toMutableSet()
            println("[PluginManager] Currently enabled plugins: $enabledIds")
            var enabledIdsChanged = false
            
            plugins.forEach { plugin ->
                // Auto-enable engine plugins (JS_ENGINE, TTS, GRADIO_TTS) if not already enabled
                // These are infrastructure plugins that should be enabled automatically
                val isEnginePlugin = plugin.manifest.type == ireader.plugin.api.PluginType.JS_ENGINE ||
                                     plugin.manifest.type == ireader.plugin.api.PluginType.TTS ||
                                     plugin.manifest.type == ireader.plugin.api.PluginType.GRADIO_TTS
                
                println("[PluginManager] Plugin ${plugin.manifest.id}: type=${plugin.manifest.type}, isEnginePlugin=$isEnginePlugin, alreadyEnabled=${enabledIds.contains(plugin.manifest.id)}")
                
                if (isEnginePlugin && !enabledIds.contains(plugin.manifest.id)) {
                    enabledIds.add(plugin.manifest.id)
                    enabledIdsChanged = true
                    println("[PluginManager] Auto-enabling engine plugin: ${plugin.manifest.id}")
                }
                
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
            
            // Save updated enabled plugins if changed
            if (enabledIdsChanged) {
                println("[PluginManager] Saving updated enabled plugins: $enabledIds")
                preferences.enabledPlugins().set(enabledIds)
            }
            
            _pluginsFlow.value = registry.getAll()
            println("[PluginManager] loadPlugins() completed successfully")
        } catch (e: Exception) {
            // Log error but don't crash
            println("[PluginManager] loadPlugins() failed: ${e.message}")
            e.printStackTrace()
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
            val monetizationModel = plugin.manifest.monetization
            if (monetizationModel is ApiPluginMonetization.Premium) {
                val isPurchased = monetization.isPurchased(plugin.manifest.id)
                if (!isPurchased) {
                    return Result.failure(Exception("Plugin requires purchase"))
                }
            }
            
            registry.register(plugin)
            
            // Auto-enable engine plugins (JS_ENGINE, TTS, GRADIO_TTS) since they're required dependencies
            val shouldAutoEnable = plugin.manifest.type == ireader.plugin.api.PluginType.JS_ENGINE ||
                                   plugin.manifest.type == ireader.plugin.api.PluginType.TTS ||
                                   plugin.manifest.type == ireader.plugin.api.PluginType.GRADIO_TTS
            
            val initialStatus = if (shouldAutoEnable) PluginStatus.ENABLED else PluginStatus.DISABLED
            database.insertOrUpdate(plugin.manifest, initialStatus)
            
            if (shouldAutoEnable) {
                // Add to enabled plugins preference
                val enabledIds = preferences.enabledPlugins().get().toMutableSet()
                enabledIds.add(plugin.manifest.id)
                preferences.enabledPlugins().set(enabledIds)
                
                // Initialize the plugin
                try {
                    val context = securityManager.createPluginContext(
                        pluginId = plugin.manifest.id,
                        manifest = plugin.manifest,
                        preferencesStore = createPluginPreferencesStore(plugin.manifest.id)
                    )
                    plugin.initialize(context)
                } catch (e: Exception) {
                    // Log but don't fail installation
                    println("Failed to auto-initialize plugin ${plugin.manifest.id}: ${e.message}")
                }
            }
            
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
            
            println("[PluginManager] Uninstalling plugin: $pluginId")
            
            // Cleanup plugin resources
            try {
                plugin.cleanup()
            } catch (e: Exception) {
                // Log but continue with uninstall
                println("[PluginManager] Cleanup error (continuing): ${e.message}")
            }
            
            // Remove from enabled plugins
            val enabledIds = preferences.enabledPlugins().get().toMutableSet()
            enabledIds.remove(pluginId)
            preferences.enabledPlugins().set(enabledIds)
            
            // Remove from registry and database
            registry.remove(pluginId)
            
            // Delete the plugin file from disk
            try {
                val pluginsDir = getPluginsDirectory()
                val pluginFiles = fileSystem.getFile(pluginsDir.toString()).listFiles()
                    .filter { it.name.startsWith(pluginId) && it.extension == "iplugin" }
                
                pluginFiles.forEach { file ->
                    println("[PluginManager] Deleting plugin file: ${file.name}")
                    file.delete()
                }
                
                // Also delete native library directory if exists
                val nativeDir = fileSystem.getFile("${pluginsDir}/${pluginId}-native")
                if (nativeDir.exists()) {
                    println("[PluginManager] Deleting native library directory: ${nativeDir.path}")
                    nativeDir.delete()
                }
            } catch (e: Exception) {
                println("[PluginManager] Failed to delete plugin files: ${e.message}")
                // Continue even if file deletion fails
            }
            
            _pluginsFlow.value = registry.getAll()
            println("[PluginManager] Plugin uninstalled: $pluginId")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("[PluginManager] Uninstall failed: ${e.message}")
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
    
    /**
     * Get the plugins directory path
     */
    suspend fun getPluginsDirectory(): okio.Path {
        val dataDir = fileSystem.getDataDirectory()
        val pluginsDir = dataDir.resolve("plugins")
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
        }
        return pluginsDir.path.toPath()
    }
    
    /**
     * Download a plugin from URL to destination path
     */
    suspend fun downloadPlugin(url: String, destination: okio.Path): Result<Unit> {
        return try {
            // Use the loader's HTTP client to download
            loader.downloadToFile(url, destination)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate a plugin package structure
     */
    suspend fun validatePluginPackage(packagePath: okio.Path): Result<Unit> {
        return try {
            // Extract and validate manifest
            // packagePath is already an absolute path, use it directly
            val file = fileSystem.getFile(packagePath.toString())
            loader.extractManifest(file)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid plugin package: ${e.message}"))
        }
    }
    
    /**
     * Install plugin from Okio Path
     */
    suspend fun installPlugin(packagePath: okio.Path): Result<PluginInfo> {
        // packagePath is already an absolute path, use it directly
        val file = fileSystem.getFile(packagePath.toString())
        return installPlugin(file)
    }
    
    /**
     * Install a plugin from PluginInfo (downloads from remote URL and installs)
     * This is used by the Feature Store to install plugins from repositories
     */
    suspend fun installPlugin(pluginInfo: PluginInfo): Result<PluginInfo> {
        val downloadUrl = pluginInfo.downloadUrl
            ?: return Result.failure(Exception("Plugin has no download URL"))
        
        println("[PluginManager] Installing plugin: ${pluginInfo.manifest.name}")
        println("[PluginManager] Download URL: $downloadUrl")
        
        return try {
            // Get plugins directory
            val pluginsDir = getPluginsDirectory()
            val fileName = "${pluginInfo.id}-${pluginInfo.manifest.version}.iplugin"
            val destination = pluginsDir.resolve(fileName)
            
            println("[PluginManager] Destination: $destination")
            
            // Download the plugin
            downloadPlugin(downloadUrl, destination)
                .onFailure { 
                    println("[PluginManager] Download failed: ${it.message}")
                    return Result.failure(Exception("Download failed: ${it.message}")) 
                }
            
            // Check file size after download
            // destination is already an absolute path from getPluginsDirectory()
            println("[PluginManager] Checking file at destination: $destination")
            val downloadedFile = fileSystem.getFile(destination.toString())
            println("[PluginManager] VirtualFile path: ${downloadedFile.path}")
            val fileExists = downloadedFile.exists()
            println("[PluginManager] File exists: $fileExists")
            val fileSize = if (fileExists) downloadedFile.size() else 0L
            println("[PluginManager] Downloaded file size: $fileSize bytes")
            
            if (fileSize == 0L) {
                return Result.failure(Exception("Downloaded file is empty. URL may be invalid: $downloadUrl"))
            }
            
            // Check if it's a valid ZIP by reading first bytes
            val firstBytes = try {
                downloadedFile.readBytes().take(4).map { it.toInt() and 0xFF }
            } catch (e: Exception) {
                emptyList()
            }
            val isZip = firstBytes.size >= 4 && firstBytes[0] == 0x50 && firstBytes[1] == 0x4B
            println("[PluginManager] Is valid ZIP: $isZip (first bytes: $firstBytes)")
            
            if (!isZip) {
                // Read first 200 chars to see what we got
                val content = try {
                    downloadedFile.readBytes().take(200).toByteArray().decodeToString()
                } catch (e: Exception) {
                    "Unable to read content"
                }
                println("[PluginManager] File content preview: $content")
                downloadedFile.delete()
                return Result.failure(Exception("Downloaded file is not a valid ZIP. Got: ${content.take(100)}..."))
            }
            
            // Validate the package
            validatePluginPackage(destination)
                .onFailure { 
                    println("[PluginManager] Validation failed: ${it.message}")
                    // Clean up invalid package
                    try {
                        downloadedFile.delete()
                    } catch (_: Exception) {}
                    return Result.failure(it) 
                }
            
            println("[PluginManager] Validation passed, installing...")
            
            // Install the plugin
            installPlugin(destination)
        } catch (e: Exception) {
            println("[PluginManager] Installation error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Install a plugin from PluginInfo with progress callback
     * This is used by the PluginDownloadService to track download progress
     */
    suspend fun installPluginWithProgress(
        pluginInfo: PluginInfo,
        onProgress: (PluginInstallProgress) -> Unit
    ): Result<PluginInfo> {
        val downloadUrl = pluginInfo.downloadUrl
            ?: return Result.failure(Exception("Plugin has no download URL"))
        
        val pluginId = pluginInfo.id
        val pluginName = pluginInfo.manifest.name
        
        onProgress(PluginInstallProgress(
            pluginId = pluginId,
            pluginName = pluginName,
            stage = InstallStage.PENDING
        ))
        
        return try {
            // Get plugins directory
            val pluginsDir = getPluginsDirectory()
            val fileName = "${pluginInfo.id}-${pluginInfo.manifest.version}.iplugin"
            val destination = pluginsDir.resolve(fileName)
            
            // Update to downloading stage
            onProgress(PluginInstallProgress(
                pluginId = pluginId,
                pluginName = pluginName,
                stage = InstallStage.DOWNLOADING,
                totalBytes = pluginInfo.fileSize ?: 0
            ))
            
            // Download the plugin with progress
            downloadPluginWithProgress(downloadUrl, destination) { bytesDownloaded, totalBytes ->
                val progress = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
                onProgress(PluginInstallProgress(
                    pluginId = pluginId,
                    pluginName = pluginName,
                    stage = InstallStage.DOWNLOADING,
                    progress = progress,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes
                ))
            }.onFailure { 
                onProgress(PluginInstallProgress(
                    pluginId = pluginId,
                    pluginName = pluginName,
                    stage = InstallStage.FAILED,
                    error = "Download failed: ${it.message}"
                ))
                return Result.failure(Exception("Download failed: ${it.message}")) 
            }
            
            // Validating stage
            onProgress(PluginInstallProgress(
                pluginId = pluginId,
                pluginName = pluginName,
                stage = InstallStage.VALIDATING,
                progress = 0.9f
            ))
            
            // Validate the package
            validatePluginPackage(destination).onFailure { 
                onProgress(PluginInstallProgress(
                    pluginId = pluginId,
                    pluginName = pluginName,
                    stage = InstallStage.FAILED,
                    error = "Validation failed: ${it.message}"
                ))
                return Result.failure(it) 
            }
            
            // Installing stage
            onProgress(PluginInstallProgress(
                pluginId = pluginId,
                pluginName = pluginName,
                stage = InstallStage.INSTALLING,
                progress = 0.95f
            ))
            
            // Install the plugin
            val result = installPlugin(destination)
            
            result.onSuccess {
                onProgress(PluginInstallProgress(
                    pluginId = pluginId,
                    pluginName = pluginName,
                    stage = InstallStage.COMPLETED,
                    progress = 1f
                ))
            }.onFailure {
                onProgress(PluginInstallProgress(
                    pluginId = pluginId,
                    pluginName = pluginName,
                    stage = InstallStage.FAILED,
                    error = "Installation failed: ${it.message}"
                ))
            }
            
            result
        } catch (e: Exception) {
            onProgress(PluginInstallProgress(
                pluginId = pluginId,
                pluginName = pluginName,
                stage = InstallStage.FAILED,
                error = e.message
            ))
            Result.failure(e)
        }
    }
    
    /**
     * Download a plugin with progress callback
     */
    private suspend fun downloadPluginWithProgress(
        url: String,
        destination: okio.Path,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): Result<Unit> {
        return try {
            loader.downloadToFileWithProgress(url, destination, onProgress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return data[key] as? Long ?: defaultValue
    }
    
    override fun putLong(key: String, value: Long) {
        data[key] = value
    }
    
    override fun getFloat(key: String, defaultValue: Float): Float {
        return data[key] as? Float ?: defaultValue
    }
    
    override fun putFloat(key: String, value: Float) {
        data[key] = value
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        return data[key] as? Set<String> ?: defaultValue
    }
    
    override fun putStringSet(key: String, value: Set<String>) {
        data[key] = value
    }
    
    override fun remove(key: String) {
        data.remove(key)
    }
    
    override fun clear() {
        data.clear()
    }
    
    override fun contains(key: String): Boolean {
        return data.containsKey(key)
    }
    
    override fun getAllKeys(): Set<String> {
        return data.keys.toSet()
    }
}
