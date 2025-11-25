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
    private val stubManager = JSPluginStubManager(preferenceStoreFactory.create("js_plugin_stubs"))
    
    /**
     * Loads all JavaScript plugins from the plugins directory.
     * @return List of loaded catalogs
     */
    suspend fun loadAllPlugins(): List<JSPluginCatalog> {
        val startTime = System.currentTimeMillis()
        
        // Ensure plugins directory exists
        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs()
        }
        
        // Scan for plugin files
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        } ?: emptyArray()
        
        // Load each plugin
        val catalogs = pluginFiles.mapNotNull { file ->
            try {
                loadPlugin(file)
            } catch (e: Exception) {
                Log.error("JSPluginLoader: Failed to load plugin ${file.nameWithoutExtension}", e)
                null
            }
        }
        
        return catalogs
    }
    
    /**
     * Create metadata files for existing plugins that don't have them.
     * This is useful for migrating plugins installed before the metadata system was added.
     * The metadata will be extracted from the plugin code and language will be converted.
     */
    suspend fun createMissingMetadataFiles() {
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        } ?: return
        
        pluginFiles.forEach { file ->
            val pluginId = file.nameWithoutExtension
            val metadataFile = File(file.parentFile, "${pluginId}.meta.json")
            
            if (!metadataFile.exists()) {
                try {
                    Log.info("JSPluginLoader: Creating missing metadata file for $pluginId")
                    
                    // Load the plugin to extract metadata
                    val catalog = loadPlugin(file)
                    if (catalog != null) {
                        // Metadata was already saved by loadPlugin
                        Log.info("JSPluginLoader: Created metadata file for $pluginId with language: ${catalog.metadata.lang}")
                    }
                } catch (e: Exception) {
                    Log.error("JSPluginLoader: Failed to create metadata for $pluginId", e)
                }
            }
        }
    }
    
    /**
     * Loads stub sources instantly for fast startup.
     * Returns lightweight placeholder sources based on previously loaded plugins.
     * @return List of stub catalogs
     */
    fun loadStubPlugins(): List<JSPluginCatalog> {
        val startTime = System.currentTimeMillis()
        
        // Get saved stubs
        val stubs = stubManager.getPluginStubs()
        
        // Scan for actual plugin files to verify they still exist
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        }?.associateBy { it.nameWithoutExtension } ?: emptyMap()
        
        // Create stub catalogs for plugins that still exist
        val stubCatalogs = stubs.mapNotNull { (pluginId, stubData) ->
            val file = pluginFiles[stubData.fileName]
            if (file != null) {
                createStubCatalog(stubData.metadata, file)
            } else {
                // Plugin file no longer exists, remove stub
                stubManager.removePluginStub(pluginId)
                null
            }
        }
        
        return stubCatalogs
    }
    
    /**
     * Creates a stub catalog from metadata.
     */
    private fun createStubCatalog(metadata: PluginMetadata, file: File): JSPluginCatalog {
        val pluginPreferenceStore = preferenceStoreFactory.create("js_plugin_${metadata.id}")
        
        val httpClientsInterface = object : ireader.core.http.HttpClientsInterface {
            override val default: HttpClient = httpClient
            override val cloudflareClient: HttpClient = httpClient
            override val browser: ireader.core.http.BrowserEngine = ireader.core.http.BrowserEngine()
        }
        val dependencies = ireader.core.source.Dependencies(
            httpClients = httpClientsInterface,
            preferences = pluginPreferenceStore
        )
        
        val stubSource = JSPluginStubSource(metadata, dependencies)
        
        return JSPluginCatalog(
            source = stubSource,
            metadata = metadata,
            pluginFile = file
        )
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
            // Check cache
            val lastModified = file.lastModified()
            val cached = pluginCache[pluginId]
            if (cached != null && cached.lastModified == lastModified) {
                return cached.catalog
            }
            
            // Try to load saved metadata first (contains language from remote catalog)
            val metadataFile = File(file.parentFile, "${pluginId}.meta.json")
            val savedMetadata = if (metadataFile.exists()) {
                try {
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    json.decodeFromString<PluginMetadata>(metadataFile.readText())
                } catch (e: Exception) {
                    Log.warn("JSPluginLoader: Failed to load saved metadata for $pluginId: ${e.message}")
                    null
                }
            } else {
                null
            }
            
            // Read plugin code
            val jsCode = file.readText()
            
            // Validate that the file contains JavaScript, not an error page
            if (jsCode.contains("404") && jsCode.contains("Not Found") && jsCode.length < 1000) {
                throw PluginLoadException("Plugin file appears to contain an HTTP error response (404 Not Found) instead of JavaScript code. Please re-download the plugin.")
            }
            if (jsCode.trim().startsWith("<!DOCTYPE") || jsCode.trim().startsWith("<html")) {
                throw PluginLoadException("Plugin file contains HTML instead of JavaScript code. Please ensure you downloaded the correct file.")
            }
            if (jsCode.isBlank()) {
                throw PluginLoadException("Plugin file is empty")
            }
            
            // Create bridge service for this plugin
            val pluginPreferenceStore = preferenceStoreFactory.create("js_plugin_$pluginId")
            val bridgeService = JSBridgeServiceImpl(httpClient, pluginPreferenceStore, pluginId)
            
            // Create engine with bridge (platform-specific)
            val engine = createEngine(bridgeService)
            
            // Load via JS engine
            val plugin = engine.loadPlugin(jsCode, pluginId)
            
            // Use saved metadata if available (contains correct language from remote catalog)
            // Otherwise extract from plugin and convert language name to code
            val metadata = if (savedMetadata != null) {
                // Use saved metadata but update version from plugin in case it changed
                savedMetadata.copy(version = plugin.getVersion())
            } else {
                // Extract from plugin code and convert language
                val rawLang = plugin.getLang()
                val langCode = convertLanguageNameToCode(rawLang)
                val site = plugin.getSite()
                
                // Log the site value for debugging
                Log.info("JSPluginLoader: Plugin $pluginId site value: '$site'")
                
                // Validate site URL - if it's not a valid URL, log a warning
                if (site.isBlank() || (!site.startsWith("http://") && !site.startsWith("https://"))) {
                    Log.warn("JSPluginLoader: Plugin $pluginId has invalid site URL: '$site'. This will cause URL construction issues.")
                }
                
                PluginMetadata(
                    id = plugin.getId(),
                    name = plugin.getName(),
                    site = site,
                    version = plugin.getVersion(),
                    lang = langCode,
                    icon = plugin.getIcon()
                )
            }
            
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
            
            // Save stub for future fast loading
            stubManager.savePluginStub(metadata, file.nameWithoutExtension)
            
            // Save metadata file if it doesn't exist (for plugins without saved metadata)
            if (savedMetadata == null) {
                try {
                    val metadataFile = File(file.parentFile, "${pluginId}.meta.json")
                    val json = kotlinx.serialization.json.Json { 
                        prettyPrint = true
                        ignoreUnknownKeys = true 
                    }
                    val metadataJson = json.encodeToString(PluginMetadata.serializer(), metadata)
                    metadataFile.writeText(metadataJson)
                } catch (e: Exception) {
                    Log.warn("JSPluginLoader: Failed to save metadata file for $pluginId: ${e.message}")
                }
            }
            
            return catalog
            
        } catch (e: PluginLoadException) {
            Log.error("JSPluginLoader: Plugin load error for $pluginId: ${e.message}", e)
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
        // Remove from cache
        pluginCache.remove(pluginId)
        
        // Remove stub
        stubManager.removePluginStub(pluginId)
    }
    
    /**
     * Clears all cached plugins and closes all engines.
     */
    fun clearCache() {
        pluginCache.values.forEach { it.engine.close() }
        pluginCache.clear()
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
        } else {
            Log.error("JSPluginLoader: Plugin file not found for reload: $pluginId", null)
        }
    }
    
    /**
     * Load plugins asynchronously with priority support.
     * Priority plugins load first, then remaining plugins.
     * @param onPluginLoaded Callback when each plugin is loaded
     * @return List of all loaded catalogs
     */
    suspend fun loadPluginsAsync(
        onPluginLoaded: (JSPluginCatalog) -> Unit = {}
    ): List<JSPluginCatalog> {
        val startTime = System.currentTimeMillis()
        
        // Ensure plugins directory exists
        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs()
        }
        
        // Scan for plugin files
        val pluginFiles = pluginsDirectory.listFiles { file ->
            file.extension == "js" && file.canRead()
        } ?: emptyArray()
        
        // Get priority plugins
        val priorityPluginIds = stubManager.getPriorityPlugins()
        
        // Separate priority and normal plugins
        val (priorityFiles, normalFiles) = pluginFiles.partition { file ->
            file.nameWithoutExtension in priorityPluginIds
        }
        
        val allCatalogs = mutableListOf<JSPluginCatalog>()
        
        // Load priority plugins first
        priorityFiles.forEach { file ->
            try {
                val catalog = loadPlugin(file)
                if (catalog != null) {
                    allCatalogs.add(catalog)
                    onPluginLoaded(catalog)
                }
            } catch (e: Exception) {
                Log.error("JSPluginLoader: Failed to load priority plugin ${file.nameWithoutExtension}", e)
            }
        }
        
        // Load remaining plugins
        normalFiles.forEach { file ->
            try {
                val catalog = loadPlugin(file)
                if (catalog != null) {
                    allCatalogs.add(catalog)
                    onPluginLoaded(catalog)
                }
            } catch (e: Exception) {
                Log.error("JSPluginLoader: Failed to load plugin ${file.nameWithoutExtension}", e)
            }
        }
        
        return allCatalogs
    }
    
    /**
     * Set a plugin as high priority for faster loading.
     */
    fun setPriorityPlugin(pluginId: String, isPriority: Boolean) {
        stubManager.setPriorityPlugin(pluginId, isPriority)
    }
    
    /**
     * Get priority plugin IDs.
     */
    fun getPriorityPlugins(): Set<String> {
        return stubManager.getPriorityPlugins()
    }
    
    /**
     * Get stub manager for advanced operations.
     */
    fun getStubManager(): JSPluginStubManager {
        return stubManager
    }
    
    /**
     * Converts LNReader language names to ISO 639-1 language codes.
     * LNReader plugins use full language names (e.g., "English", "العربية", "日本語") 
     * while IReader uses language codes (e.g., "en", "ar", "ja").
     */
    private fun convertLanguageNameToCode(languageName: String?): String {
        if (languageName.isNullOrBlank()) return "en"
        
        // Trim and normalize the input
        val normalized = languageName.trim()
        
        // If it's already a 2-letter code, return it lowercase
        if (normalized.length == 2 && normalized.all { it.isLetter() || it.isDigit() }) {
            return normalized.lowercase()
        }
        
        // Map of language names to ISO 639-1 codes (case-insensitive)
        return when (normalized.lowercase()) {
            "english" -> "en"
            "العربية", "arabic" -> "ar"
            "中文", "chinese", "中文 (简体)", "中文 (繁體)" -> "zh"
            "español", "spanish" -> "es"
            "français", "french" -> "fr"
            "bahasa indonesia", "indonesian" -> "id"
            "日本語", "japanese" -> "ja"
            "한국어", "korean" -> "ko"
            "português", "portuguese" -> "pt"
            "русский", "russian" -> "ru"
            "ไทย", "thai" -> "th"
            "türkçe", "turkish" -> "tr"
            "tiếng việt", "vietnamese" -> "vi"
            "deutsch", "german" -> "de"
            "italiano", "italian" -> "it"
            "polski", "polish" -> "pl"
            "українська", "ukrainian" -> "uk"
            "filipino", "tagalog" -> "tl"
            "magyar", "hungarian" -> "hu"
            "čeština", "czech" -> "cs"
            "română", "romanian" -> "ro"
            "nederlands", "dutch" -> "nl"
            "svenska", "swedish" -> "sv"
            "norsk", "norwegian" -> "no"
            "dansk", "danish" -> "da"
            "suomi", "finnish" -> "fi"
            "ελληνικά", "greek" -> "el"
            "עברית", "hebrew" -> "he"
            "हिन्दी", "hindi" -> "hi"
            "বাংলা", "bengali" -> "bn"
            "မြန်မာဘာသာ", "burmese" -> "my"
            "català", "catalan" -> "ca"
            "galego", "galician" -> "gl"
            "euskara", "basque" -> "eu"
            "lietuvių", "lithuanian" -> "lt"
            "latviešu", "latvian" -> "lv"
            "eesti", "estonian" -> "et"
            "slovenčina", "slovak" -> "sk"
            "slovenščina", "slovene" -> "sl"
            "hrvatski", "croatian" -> "hr"
            "srpski", "serbian" -> "sr"
            "български", "bulgarian" -> "bg"
            "македонски", "macedonian" -> "mk"
            else -> {
                // If we can't map it, log a warning and default to English
                Log.warn("JSPluginLoader: Unknown language name: '$normalized', defaulting to 'en'")
                "en"
            }
        }
    }
}
