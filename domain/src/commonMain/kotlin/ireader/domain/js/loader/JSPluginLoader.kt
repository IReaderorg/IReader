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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use
import ireader.domain.utils.extensions.currentTimeToLong

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
 * Uses Okio for KMP-compatible file operations.
 */
class JSPluginLoader(
    private val pluginsDirectoryProvider: () -> Path,
    private val httpClient: HttpClient,
    private val preferenceStoreFactory: PreferenceStoreFactory,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    
    // Get the current plugins directory (may change if user selects new storage folder)
    private val pluginsDirectory: Path
        get() = pluginsDirectoryProvider()
    
    private val pluginCache = mutableMapOf<String, LoadedPlugin>()
    private val stubManager = JSPluginStubManager(preferenceStoreFactory.create("js_plugin_stubs"))
    
    /**
     * Tracks if JS engine is missing (NoJSEngineException was thrown during loading)
     */
    var jsEngineMissing: Boolean = false
        private set
    
    /**
     * Number of JS plugin files found but not loaded due to missing engine
     */
    var pendingPluginsCount: Int = 0
        private set
    
    /**
     * Loads all JavaScript plugins from the plugins directory.
     * @return List of loaded catalogs
     */
    suspend fun loadAllPlugins(): List<JSPluginCatalog> {
        val startTime = currentTimeToLong()
        jsEngineMissing = false
        pendingPluginsCount = 0
        
        // Debug: Log the directory being scanned
        ireader.core.log.Log.info("JSPluginLoader: Scanning directory: $pluginsDirectory")
        
        // Ensure plugins directory exists
        if (!fileSystem.exists(pluginsDirectory)) {
            fileSystem.createDirectories(pluginsDirectory)
            ireader.core.log.Log.info("JSPluginLoader: Created plugins directory: $pluginsDirectory")
        }
        
        // Scan for plugin files
        val allFiles = fileSystem.list(pluginsDirectory)
        ireader.core.log.Log.info("JSPluginLoader: Found ${allFiles.size} files in directory: ${allFiles.map { it.name }}")
        
        val pluginFiles = allFiles.filter { path ->
            path.name.endsWith(".js")
        }
        ireader.core.log.Log.info("JSPluginLoader: Found ${pluginFiles.size} .js files: ${pluginFiles.map { it.name }}")
        
        // Load each plugin
        val catalogs = pluginFiles.mapNotNull { file ->
            try {
                loadPlugin(file)
            } catch (e: ireader.domain.js.engine.NoJSEngineException) {
                // Track that JS engine is missing
                jsEngineMissing = true
                pendingPluginsCount++
                ireader.core.log.Log.warn("JSPluginLoader: JS engine not available for ${file.name}. Install J2V8/GraalVM plugin from Feature Store.")
                null
            } catch (e: Exception) {
                ireader.core.log.Log.error("JSPluginLoader: Failed to load plugin ${file.name}", e)
                null
            }
        }
        
        if (jsEngineMissing && pendingPluginsCount > 0) {
            ireader.core.log.Log.info("JSPluginLoader: $pendingPluginsCount JS plugins pending - JS engine plugin required")
        }
        
        return catalogs
    }
    
    /**
     * Create metadata files for existing plugins that don't have them.
     * This is useful for migrating plugins installed before the metadata system was added.
     * The metadata will be extracted from the plugin code and language will be converted.
     */
    suspend fun createMissingMetadataFiles() {
        if (!fileSystem.exists(pluginsDirectory)) return
        
        val pluginFiles = fileSystem.list(pluginsDirectory).filter { path ->
            path.name.endsWith(".js")
        }
        
        pluginFiles.forEach { file ->
            val pluginId = file.name.substringBeforeLast(".")
            val metadataFile = file.parent!! / "${pluginId}.meta.json"
            
            if (!fileSystem.exists(metadataFile)) {
                try {
                    // Load the plugin to extract metadata
                    val catalog = loadPlugin(file)
                } catch (e: Exception) {
                    // Ignore errors
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
        val startTime = currentTimeToLong()
        
        // Get saved stubs
        val stubs = stubManager.getPluginStubs()
        
        // Scan for actual plugin files to verify they still exist
        val pluginFiles = if (fileSystem.exists(pluginsDirectory)) {
            fileSystem.list(pluginsDirectory)
                .filter { it.name.endsWith(".js") }
                .associateBy { it.name.substringBeforeLast(".") }
        } else {
            emptyMap()
        }
        
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
    private fun createStubCatalog(metadata: PluginMetadata, file: Path): JSPluginCatalog {
        val pluginPreferenceStore = preferenceStoreFactory.create("js_plugin_${metadata.id}")
        
        val httpClientsInterface = object : ireader.core.http.HttpClientsInterface {
            override val default: HttpClient = httpClient
            override val cloudflareClient: HttpClient = httpClient
            override val browser: ireader.core.http.BrowserEngine = ireader.core.http.BrowserEngine()
            override val config: ireader.core.http.NetworkConfig = ireader.core.http.NetworkConfig()
            override val sslConfig: ireader.core.http.SSLConfiguration = ireader.core.http.SSLConfiguration()
            override val cookieSynchronizer: ireader.core.http.CookieSynchronizer = createCookieSynchronizer()
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
    suspend fun loadPlugin(file: Path): JSPluginCatalog? {
        val pluginId = file.name.substringBeforeLast(".")
        val startTime = currentTimeToLong()
        
        try {
            // Check cache
            val lastModified = fileSystem.metadata(file).lastModifiedAtMillis ?: 0L
            val cached = pluginCache[pluginId]
            if (cached != null && cached.lastModified == lastModified) {
                return cached.catalog
            }
            
            // Try to load saved metadata first (contains language from remote catalog)
            val metadataFile = file.parent!! / "${pluginId}.meta.json"
            val savedMetadata = if (fileSystem.exists(metadataFile)) {
                try {
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val content = fileSystem.source(metadataFile).buffer().use { it.readUtf8() }
                    json.decodeFromString<PluginMetadata>(content)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
            
            // Read plugin code
            val jsCode = fileSystem.source(file).buffer().use { it.readUtf8() }
            
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
                override val config: ireader.core.http.NetworkConfig = ireader.core.http.NetworkConfig()
                override val sslConfig: ireader.core.http.SSLConfiguration = ireader.core.http.SSLConfiguration()
                override val cookieSynchronizer: ireader.core.http.CookieSynchronizer = createCookieSynchronizer()
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
            stubManager.savePluginStub(metadata, file.name.substringBeforeLast("."))
            
            // Save metadata file if it doesn't exist (for plugins without saved metadata)
            if (savedMetadata == null) {
                try {
                    val metadataFilePath = file.parent!! / "${pluginId}.meta.json"
                    val json = kotlinx.serialization.json.Json { 
                        prettyPrint = true
                        ignoreUnknownKeys = true 
                    }
                    val metadataJson = json.encodeToString(PluginMetadata.serializer(), metadata)
                    fileSystem.sink(metadataFilePath).buffer().use { it.writeUtf8(metadataJson) }
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
            
            return catalog
            
        } catch (e: ireader.domain.js.engine.NoJSEngineException) {
            // Re-throw NoJSEngineException so caller can track missing engine
            Log.warn { "JSPluginLoader: No JS engine available for plugin ${file.name}" }
            throw e
        } catch (e: PluginLoadException) {
            Log.warn { "JSPluginLoader: Plugin load error for ${file.name}: ${e.message}" }
            return null
        } catch (e: Exception) {
            Log.warn { "JSPluginLoader: Unexpected error loading ${file.name}: ${e.message}" }
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
    fun getPluginFile(pluginId: String): Path? {
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
        }
    }
    
    /**
     * Load plugins asynchronously with priority support and parallel execution.
     * Priority plugins load first (sequentially for immediate availability),
     * then remaining plugins load in parallel for maximum throughput.
     * 
     * @param onPluginLoaded Callback when each plugin is loaded
     * @param maxConcurrency Maximum number of plugins to load in parallel (default: 4)
     * @return List of all loaded catalogs
     */
    suspend fun loadPluginsAsync(
        onPluginLoaded: (JSPluginCatalog) -> Unit = {},
        maxConcurrency: Int = 4
    ): List<JSPluginCatalog> {
        val startTime = currentTimeToLong()
        jsEngineMissing = false
        pendingPluginsCount = 0
        
        // Debug: Log the directory being scanned
        Log.info { "JSPluginLoader.loadPluginsAsync: Scanning directory: $pluginsDirectory" }
        
        // Ensure plugins directory exists
        if (!fileSystem.exists(pluginsDirectory)) {
            fileSystem.createDirectories(pluginsDirectory)
            Log.info { "JSPluginLoader.loadPluginsAsync: Created plugins directory" }
        }
        
        // Scan for plugin files
        val allFiles = fileSystem.list(pluginsDirectory)
        Log.info { "JSPluginLoader.loadPluginsAsync: Found ${allFiles.size} files: ${allFiles.map { it.name }}" }
        
        val pluginFiles = allFiles.filter { path ->
            path.name.endsWith(".js")
        }
        Log.info { "JSPluginLoader.loadPluginsAsync: Found ${pluginFiles.size} .js files" }
        
        if (pluginFiles.isEmpty()) {
            Log.debug { "JSPluginLoader: No plugin files found" }
            return emptyList()
        }
        
        // Get priority plugins
        val priorityPluginIds = stubManager.getPriorityPlugins()
        
        // Separate priority and normal plugins
        val (priorityFiles, normalFiles) = pluginFiles.partition { file ->
            file.name.substringBeforeLast(".") in priorityPluginIds
        }
        
        val allCatalogs = mutableListOf<JSPluginCatalog>()
        val catalogsMutex = Mutex()
        val semaphore = Semaphore(maxConcurrency)
        
        // Load priority plugins first (sequentially for immediate availability)
        priorityFiles.forEach { file ->
            try {
                val catalog = loadPlugin(file)
                if (catalog != null) {
                    allCatalogs.add(catalog)
                    onPluginLoaded(catalog)
                }
            } catch (e: ireader.domain.js.engine.NoJSEngineException) {
                jsEngineMissing = true
                pendingPluginsCount++
                Log.warn { "JSPluginLoader: JS engine not available for ${file.name}" }
            } catch (e: Exception) {
                Log.warn { "JSPluginLoader: Failed to load priority plugin ${file.name}: ${e.message}" }
            }
        }
        
        val priorityLoadTime = currentTimeToLong() - startTime
        if (priorityFiles.isNotEmpty()) {
            Log.debug { "JSPluginLoader: Loaded ${priorityFiles.size} priority plugins in ${priorityLoadTime}ms" }
        }
        
        // Load remaining plugins in parallel with controlled concurrency
        coroutineScope {
            normalFiles.map { file ->
                async(Dispatchers.Default) {
                    semaphore.withPermit {
                        try {
                            val catalog = loadPlugin(file)
                            if (catalog != null) {
                                catalogsMutex.withLock {
                                    allCatalogs.add(catalog)
                                }
                                onPluginLoaded(catalog)
                            }
                        } catch (e: ireader.domain.js.engine.NoJSEngineException) {
                            catalogsMutex.withLock {
                                jsEngineMissing = true
                                pendingPluginsCount++
                            }
                            Log.warn { "JSPluginLoader: JS engine not available for ${file.name}" }
                        } catch (e: Exception) {
                            Log.warn { "JSPluginLoader: Failed to load plugin ${file.name}: ${e.message}" }
                        }
                    }
                }
            }.awaitAll()
        }
        
        val totalLoadTime = currentTimeToLong() - startTime
        Log.info { "JSPluginLoader: Loaded ${allCatalogs.size} plugins in ${totalLoadTime}ms (${priorityFiles.size} priority, ${normalFiles.size} normal)" }
        
        if (jsEngineMissing && pendingPluginsCount > 0) {
            Log.info { "JSPluginLoader: $pendingPluginsCount JS plugins pending - JS engine plugin required" }
        }
        
        return allCatalogs.toList()
    }
    
    /**
     * Load plugins with streaming callback - plugins are delivered as they load.
     * This provides the fastest time-to-first-plugin for UI responsiveness.
     * 
     * @param onPluginLoaded Callback when each plugin is loaded (called from IO dispatcher)
     * @param maxConcurrency Maximum number of plugins to load in parallel
     */
    suspend fun loadPluginsStreaming(
        onPluginLoaded: suspend (JSPluginCatalog) -> Unit,
        maxConcurrency: Int = 4
    ) {
        val startTime = currentTimeToLong()
        
        if (!fileSystem.exists(pluginsDirectory)) {
            fileSystem.createDirectories(pluginsDirectory)
            return
        }
        
        val pluginFiles = fileSystem.list(pluginsDirectory).filter { path ->
            path.name.endsWith(".js")
        }
        
        if (pluginFiles.isEmpty()) return
        
        val priorityPluginIds = stubManager.getPriorityPlugins()
        val (priorityFiles, normalFiles) = pluginFiles.partition { file ->
            file.name.substringBeforeLast(".") in priorityPluginIds
        }
        
        val semaphore = Semaphore(maxConcurrency)
        var loadedCount = 0
        val catalogsMutex = Mutex()
        
        // Reset engine status
        jsEngineMissing = false
        pendingPluginsCount = 0
        
        // Load priority plugins first
        priorityFiles.forEach { file ->
            try {
                val catalog = loadPlugin(file)
                if (catalog != null) {
                    loadedCount++
                    onPluginLoaded(catalog)
                }
            } catch (e: ireader.domain.js.engine.NoJSEngineException) {
                jsEngineMissing = true
                pendingPluginsCount++
                Log.warn { "JSPluginLoader: JS engine not available for ${file.name}" }
            } catch (e: Exception) {
                // Continue with next plugin
            }
        }
        
        // Load remaining plugins in parallel
        coroutineScope {
            normalFiles.map { file ->
                async(Dispatchers.Default) {
                    semaphore.withPermit {
                        try {
                            val catalog = loadPlugin(file)
                            if (catalog != null) {
                                loadedCount++
                                onPluginLoaded(catalog)
                            }
                        } catch (e: ireader.domain.js.engine.NoJSEngineException) {
                            catalogsMutex.withLock {
                                jsEngineMissing = true
                                pendingPluginsCount++
                            }
                            Log.warn { "JSPluginLoader: JS engine not available for ${file.name}" }
                        } catch (e: Exception) {
                            // Continue with next plugin
                        }
                    }
                }
            }.awaitAll()
        }
        
        val totalTime = currentTimeToLong() - startTime
        Log.debug { "JSPluginLoader: Streaming load completed - $loadedCount plugins in ${totalTime}ms" }
        
        if (jsEngineMissing && pendingPluginsCount > 0) {
            Log.info { "JSPluginLoader: $pendingPluginsCount JS plugins pending - JS engine plugin required" }
        }
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
            else -> "en"
        }
    }
    
    /**
     * Platform-specific CookieSynchronizer creation
     * Desktop doesn't need webViewCookieJar, Android does
     */
    private fun createCookieSynchronizer(): ireader.core.http.CookieSynchronizer {
        // This will use the platform-specific constructor
        // Desktop: no-arg constructor
        // Android: requires webViewCookieJar (will be provided by platform-specific code)
        return createPlatformCookieSynchronizer()
    }
}

/**
 * Platform-specific CookieSynchronizer creation
 */
expect fun createPlatformCookieSynchronizer(): ireader.core.http.CookieSynchronizer
