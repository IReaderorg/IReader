package ireader.domain.js.loader

import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStoreFactory
import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.JSBridgeServiceImpl
import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.engine.PluginLoadException
import ireader.domain.js.models.PluginMetadata
import ireader.domain.models.entities.JSPluginCatalog
import java.io.File

/**
 * Common interface for JS engines.
 */
interface JSEngine {
    suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin
    fun close()
    fun isLoaded(): Boolean
}

/**
 * Platform-specific engine creation.
 */
expect fun createEngine(bridgeService: JSBridgeService): JSEngine

/**
 * Cached loaded plugin data.
 */
private data class LoadedPlugin(
    val catalog: JSPluginCatalog,
    val plugin: LNReaderPlugin,
    val engine: JSEngine,
    val lastModified: Long
)

/**
 * Loader for JavaScript plugins using Zipline.
 * Executes JavaScript plugins in a type-safe manner.
 */
class JSPluginLoader(
    private val pluginsDirectory: File,
    private val httpClient: HttpClient,
    private val preferenceStoreFactory: PreferenceStoreFactory
) {
    
    private val pluginCache = mutableMapOf<String, LoadedPlugin>()
    
    /**
     * Loads all JavaScript plugins from the plugins directory.
     * @return List of loaded catalogs
     */
    suspend fun loadAllPlugins(): List<JSPluginCatalog> {
        val startTime = System.currentTimeMillis()
        
        // Ensure plugins directory exists
        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs()
            Log.info("JSPluginLoader: Created plugins directory: ${pluginsDirectory.absolutePath}")
        }
        
        // Scan for plugin files
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        } ?: emptyArray()
        
        Log.info("JSPluginLoader: Found ${pluginFiles.size} plugin files")
        
        // Load each plugin
        val catalogs = pluginFiles.mapNotNull { file ->
            try {
                loadPlugin(file)
            } catch (e: Exception) {
                Log.error("JSPluginLoader: Failed to load plugin ${file.nameWithoutExtension}", e)
                null
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        Log.info("JSPluginLoader: Loaded ${catalogs.size} of ${pluginFiles.size} plugins in ${duration}ms")
        
        return catalogs
    }
    
    /**
     * Loads a single plugin from a file using Zipline.
     * @param file The plugin file
     * @return Loaded catalog or null if loading failed
     */
    suspend fun loadPlugin(file: File): JSPluginCatalog? {
        val pluginId = file.nameWithoutExtension
        val startTime = System.currentTimeMillis()
        
        try {
            Log.info("JSPluginLoader: Loading plugin: $pluginId")
            
            // Check cache
            val lastModified = file.lastModified()
            val cached = pluginCache[pluginId]
            if (cached != null && cached.lastModified == lastModified) {
                Log.debug("JSPluginLoader: Using cached plugin: $pluginId")
                return cached.catalog
            }
            
            // Read plugin code
            val jsCode = file.readText()
            Log.debug("JSPluginLoader: Read ${jsCode.length} chars from $pluginId")
            
            // Create bridge service for this plugin
            val pluginPreferenceStore = preferenceStoreFactory.create("js_plugin_$pluginId")
            val bridgeService = JSBridgeServiceImpl(httpClient, pluginPreferenceStore, pluginId)
            
            // Create engine with bridge (platform-specific)
            val engine = createEngine(bridgeService)
            
            // Load via JS engine
            val plugin = engine.loadPlugin(jsCode, pluginId)
            Log.info("JSPluginLoader: JS engine loaded plugin: $pluginId")
            
            // Get metadata from plugin
            val metadata = PluginMetadata(
                id = plugin.getId(),
                name = plugin.getName(),
                site = plugin.getSite(),
                version = plugin.getVersion(),
                lang = plugin.getLang(),
                icon = plugin.getIcon()
            )
            
            Log.info("JSPluginLoader: Plugin metadata - name: ${metadata.name}, site: ${metadata.site}")
            
            // Create dependencies for the source wrapper
            val httpClientsInterface = object : ireader.core.http.HttpClientsInterface {
                override val default: HttpClient = httpClient
                override val cloudflareClient: HttpClient = httpClient
                override val browser: ireader.core.http.BrowserEngine = ireader.core.http.BrowserEngine()
            }
            val dependencies = ireader.core.source.Dependencies(
                httpClients = httpClientsInterface,
                preferences = pluginPreferenceStore
            )
            
            // Create source wrapper
            val source = JSPluginSource(plugin, metadata, dependencies)
            
            // Wrap in catalog
            val catalog = JSPluginCatalog(
                source = source,
                metadata = metadata,
                pluginFile = file
            )
            
            // Cache the loaded plugin
            pluginCache[pluginId] = LoadedPlugin(catalog, plugin, engine, lastModified)
            
            val duration = System.currentTimeMillis() - startTime
            Log.info("JSPluginLoader: Successfully loaded $pluginId in ${duration}ms")
            
            return catalog
            
        } catch (e: PluginLoadException) {
            Log.error("JSPluginLoader: Plugin load error for $pluginId: ${e.message}", e)
            e.printStackTrace()
            val duration = System.currentTimeMillis() - startTime
            Log.debug("JSPluginLoader: Failed to load $pluginId after ${duration}ms")
            return null
        } catch (e: Exception) {
            Log.error("JSPluginLoader: Unexpected error loading $pluginId", e)
            return null
        }
    }
    
    /**
     * Unloads a plugin and cleans up its resources.
     * @param pluginId The plugin identifier
     */
    suspend fun unloadPlugin(pluginId: String) {
        Log.debug("JSPluginLoader: Unloading plugin: $pluginId")
        
        // Remove from cache
        pluginCache.remove(pluginId)
        
        Log.info("JSPluginLoader: Plugin unloaded: $pluginId")
    }
    
    /**
     * Clears all cached plugins and closes all engines.
     */
    fun clearCache() {
        pluginCache.values.forEach { it.engine.close() }
        pluginCache.clear()
        Log.info("JSPluginLoader: Plugin cache cleared and all engines closed")
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
        return pluginCache.mapValues { (_, loaded) ->
            loaded.catalog.metadata
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
            Log.info("JSPluginLoader: Plugin reloaded: $pluginId")
        } else {
            Log.error("JSPluginLoader: Plugin file not found for reload: $pluginId", null)
        }
    }
}
