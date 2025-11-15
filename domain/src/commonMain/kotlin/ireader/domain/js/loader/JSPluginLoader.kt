package ireader.domain.js.loader

import io.ktor.client.HttpClient
import ireader.core.prefs.PreferenceStoreFactory
import ireader.domain.js.bridge.JSPluginBridge
import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.engine.JSEnginePool
import ireader.domain.js.library.JSLibraryProvider
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.PluginMetadata
import ireader.domain.js.util.JSPluginLogger
import ireader.domain.js.util.JSPluginValidator
import ireader.domain.js.util.ValidationResult
import ireader.domain.models.entities.JSPluginCatalog
import java.io.File

/**
 * Cached compiled plugin data.
 */
private data class CompiledPlugin(
    val catalog: JSPluginCatalog,
    val lastModified: Long
)

/**
 * Loader for JavaScript plugins.
 * Discovers, validates, and loads JavaScript plugins from the file system.
 */
class JSPluginLoader(
    private val pluginsDirectory: File,
    private val httpClient: HttpClient,
    private val preferenceStoreFactory: PreferenceStoreFactory,
    private val enginePool: JSEnginePool
) {
    
    private val validator = JSPluginValidator()
    private val pluginCache = mutableMapOf<String, CompiledPlugin>()
    
    /**
     * Loads all JavaScript plugins from the plugins directory.
     * @return List of loaded catalogs
     */
    suspend fun loadAllPlugins(): List<JSPluginCatalog> {
        val startTime = System.currentTimeMillis()
        
        // Ensure plugins directory exists
        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs()
            JSPluginLogger.logInfo("loader", "Created plugins directory: ${pluginsDirectory.absolutePath}")
        }
        
        // Scan for plugin files
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        } ?: emptyArray()
        
        JSPluginLogger.logInfo("loader", "Found ${pluginFiles.size} plugin files")
        
        // Load each plugin
        val catalogs = pluginFiles.mapNotNull { file ->
            try {
                loadPlugin(file)
            } catch (e: Exception) {
                JSPluginLogger.logError(
                    file.nameWithoutExtension,
                    JSPluginError.LoadError(file.nameWithoutExtension, e)
                )
                null
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        JSPluginLogger.logInfo(
            "loader",
            "Loaded ${catalogs.size} of ${pluginFiles.size} plugins in ${duration}ms"
        )
        
        return catalogs
    }
    
    /**
     * Loads a single plugin from a file.
     * @param file The plugin file
     * @return Loaded catalog or null if loading failed
     */
    suspend fun loadPlugin(file: File): JSPluginCatalog? {
        val pluginId = file.nameWithoutExtension
        val startTime = System.currentTimeMillis()
        
        try {
            JSPluginLogger.logDebug(pluginId, "Loading plugin from: ${file.absolutePath}")
            
            // Check cache
            val lastModified = file.lastModified()
            val cached = pluginCache[pluginId]
            if (cached != null && cached.lastModified == lastModified) {
                JSPluginLogger.logDebug(pluginId, "Using cached plugin")
                return cached.catalog
            }
            
            // Read plugin code
            val code = file.readText()
            JSPluginLogger.logDebug(pluginId, "Read ${code.length} characters of code")
            
            // Validate code
            val codeValidation = validator.validateCode(code)
            if (!codeValidation.isValid()) {
                val error = JSPluginError.ValidationError(pluginId, codeValidation.getError() ?: "Unknown validation error")
                JSPluginLogger.logError(pluginId, error)
                val duration = System.currentTimeMillis() - startTime
                JSPluginLogger.logPluginLoad(pluginId, false, duration)
                return null
            }
            
            // Get or create engine from pool
            val engine = enginePool.getOrCreate(pluginId)
            JSPluginLogger.logDebug(pluginId, "Got engine from pool")
            
            // Setup library provider
            val pluginPreferenceStore = preferenceStoreFactory.create("js_plugin_$pluginId")
            val libraryProvider = JSLibraryProvider(engine, pluginId, httpClient, pluginPreferenceStore)
            libraryProvider.setupRequireFunction()
            JSPluginLogger.logDebug(pluginId, "Setup library provider")
            
            // Evaluate plugin code
            val pluginInstance = engine.evaluateScript(code)
            if (pluginInstance == null) {
                val error = JSPluginError.LoadError(pluginId, Exception("Plugin evaluation returned null"))
                JSPluginLogger.logError(pluginId, error)
                val duration = System.currentTimeMillis() - startTime
                JSPluginLogger.logPluginLoad(pluginId, false, duration)
                return null
            }
            JSPluginLogger.logDebug(pluginId, "Evaluated plugin code")
            
            // Create bridge
            val bridge = JSPluginBridge(engine, pluginInstance, httpClient, pluginId)
            
            // Extract metadata
            val metadata = bridge.getPluginMetadata()
            JSPluginLogger.logDebug(pluginId, "Extracted metadata: ${metadata.name} v${metadata.version}")
            
            // Validate metadata
            val metadataValidation = validator.validateMetadata(metadata)
            if (!metadataValidation.isValid()) {
                val error = JSPluginError.ValidationError(
                    pluginId,
                    metadataValidation.getError() ?: "Unknown validation error"
                )
                JSPluginLogger.logError(pluginId, error)
                val duration = System.currentTimeMillis() - startTime
                JSPluginLogger.logPluginLoad(pluginId, false, duration)
                return null
            }
            
            // Create source
            val source = JSPluginSource(
                bridge = bridge,
                metadata = metadata,
                id = metadata.id.hashCode().toLong(),
                name = metadata.name,
                lang = metadata.lang
            )
            
            // Wrap in catalog
            val catalog = JSPluginCatalog(
                source = source,
                metadata = metadata,
                pluginFile = file
            )
            
            // Cache the compiled plugin
            pluginCache[pluginId] = CompiledPlugin(catalog, lastModified)
            
            val duration = System.currentTimeMillis() - startTime
            JSPluginLogger.logPluginLoad(pluginId, true, duration)
            
            return catalog
            
        } catch (e: Exception) {
            val error = JSPluginError.LoadError(pluginId, e)
            JSPluginLogger.logError(pluginId, error)
            val duration = System.currentTimeMillis() - startTime
            JSPluginLogger.logPluginLoad(pluginId, false, duration)
            return null
        }
    }
    
    /**
     * Unloads a plugin and cleans up its resources.
     * @param pluginId The plugin identifier
     */
    suspend fun unloadPlugin(pluginId: String) {
        JSPluginLogger.logDebug(pluginId, "Unloading plugin")
        
        // Remove from cache
        pluginCache.remove(pluginId)
        
        // Return engine to pool (which will dispose it)
        enginePool.remove(pluginId)
        
        // Clear plugin storage
        // Note: Storage clearing would be handled by JSStorage if we had a reference to it
        // For now, we just log the operation
        JSPluginLogger.logInfo(pluginId, "Plugin unloaded")
    }
    
    /**
     * Clears all cached plugins.
     */
    fun clearCache() {
        pluginCache.clear()
        JSPluginLogger.logInfo("loader", "Plugin cache cleared")
    }
    
    /**
     * Gets the number of cached plugins.
     */
    fun getCacheSize(): Int = pluginCache.size
    
    /**
     * Gets a map of installed plugins with their metadata.
     * @return Map of plugin ID to metadata
     */
    fun getInstalledPlugins(): Map<String, PluginMetadata> {
        return pluginCache.mapValues { (_, compiled) ->
            compiled.catalog.metadata
        }
    }
    
    /**
     * Gets the file for a specific plugin.
     * @param pluginId The plugin identifier
     * @return The plugin file or null if not found
     */
    fun getPluginFile(pluginId: String): File? {
        return pluginCache[pluginId]?.catalog?.pluginFile
    }
    
    /**
     * Reloads a specific plugin.
     * @param pluginId The plugin identifier
     */
    suspend fun reloadPlugin(pluginId: String) {
        val pluginFile = getPluginFile(pluginId)
        if (pluginFile != null) {
            // Remove from cache to force reload
            pluginCache.remove(pluginId)
            // Reload the plugin
            loadPlugin(pluginFile)
            JSPluginLogger.logInfo(pluginId, "Plugin reloaded")
        } else {
            JSPluginLogger.logError(
                pluginId,
                JSPluginError.LoadError(pluginId, Exception("Plugin file not found for reload"))
            )
        }
    }
}
