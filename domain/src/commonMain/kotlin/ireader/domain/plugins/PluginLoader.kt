package ireader.domain.plugins

import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.collections.emptyList

/**
 * Plugin loader - loads and validates plugins from packages
 * .iplugin file format: ZIP archive containing:
 * - plugin.json: Plugin manifest
 * - Compiled plugin classes (DEX for Android, JAR classes for Desktop)
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 17.1, 17.2, 17.3, 17.4, 17.5
 */
class PluginLoader(
    private val fileSystem: FileSystem,
    private val validator: PluginValidator,
    private val classLoader: PluginClassLoader
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Additional plugin directories (e.g., SAF folder for testing)
    private val additionalPluginDirs = mutableListOf<String>()
    
    /**
     * Add an additional directory to scan for plugins.
     * Useful for testing via ADB push to a SAF-accessible folder.
     */
    fun addPluginDirectory(path: String) {
        if (path.isNotBlank() && !additionalPluginDirs.contains(path)) {
            additionalPluginDirs.add(path)
            println("[PluginLoader] Added plugin directory: $path")
        }
    }
    
    /**
     * Remove an additional plugin directory.
     */
    fun removePluginDirectory(path: String) {
        additionalPluginDirs.remove(path)
    }
    
    /**
     * Get all plugin directories being scanned.
     */
    fun getPluginDirectories(): List<String> {
        val dirs = mutableListOf<String>()
        dirs.add(fileSystem.getDataDirectory().resolve("plugins").path)
        dirs.addAll(additionalPluginDirs)
        return dirs
    }
    
    /**
     * Load all plugins from the plugins directory and additional directories
     * Scans for .iplugin files and loads each valid plugin
     */
    suspend fun loadAll(): List<Plugin> {
        return withContext(Dispatchers.Default) {
            val allPlugins = mutableListOf<Plugin>()
            val loadedIds = mutableSetOf<String>()
            
            // Load from main plugins directory
            val mainPluginsDir = fileSystem.getDataDirectory().resolve("plugins")
            println("[PluginLoader] Loading plugins from main dir: ${mainPluginsDir.path}")
            
            if (!mainPluginsDir.exists()) {
                println("[PluginLoader] Main plugins directory does not exist, creating it")
                mainPluginsDir.mkdirs()
            } else {
                val plugins = loadFromDirectory(mainPluginsDir, loadedIds)
                allPlugins.addAll(plugins)
            }
            
            // Load from additional directories (e.g., SAF folder for testing)
            for (additionalDir in additionalPluginDirs) {
                try {
                    val dir = fileSystem.getFile(additionalDir)
                    if (dir.exists() && dir.isDirectory()) {
                        println("[PluginLoader] Loading plugins from additional dir: $additionalDir")
                        val plugins = loadFromDirectory(dir, loadedIds)
                        allPlugins.addAll(plugins)
                    } else {
                        println("[PluginLoader] Additional dir not found or not a directory: $additionalDir")
                    }
                } catch (e: Exception) {
                    println("[PluginLoader] Error loading from additional dir $additionalDir: ${e.message}")
                }
            }
            
            // Also check common test locations
            val testLocations = listOf(
                "/sdcard/Download/plugins",
                "/sdcard/IReader/plugins",
                "/storage/emulated/0/Download/plugins",
                "/storage/emulated/0/IReader/plugins"
            )
            
            for (testPath in testLocations) {
                if (!additionalPluginDirs.contains(testPath)) {
                    try {
                        val testDir = fileSystem.getFile(testPath)
                        if (testDir.exists() && testDir.isDirectory()) {
                            println("[PluginLoader] Found test plugins dir: $testPath")
                            val plugins = loadFromDirectory(testDir, loadedIds)
                            allPlugins.addAll(plugins)
                        }
                    } catch (_: Exception) {
                        // Ignore - test location not accessible
                    }
                }
            }
            
            println("[PluginLoader] Total plugins loaded: ${allPlugins.size}")
            allPlugins
        }
    }
    
    /**
     * Load plugins from a specific directory
     */
    private suspend fun loadFromDirectory(
        dir: VirtualFile,
        loadedIds: MutableSet<String>
    ): List<Plugin> {
        val plugins = mutableListOf<Plugin>()
        
        val allFiles = dir.listFiles()
        println("[PluginLoader] Found ${allFiles.size} files in ${dir.path}")
        
        val ipluginFiles = allFiles.filter { it.isFile() && it.extension == "iplugin" }
        println("[PluginLoader] Found ${ipluginFiles.size} .iplugin files: ${ipluginFiles.map { it.name }}")
        
        for (file in ipluginFiles) {
            try {
                println("[PluginLoader] Loading plugin: ${file.name}")
                val plugin = loadPlugin(file)
                if (plugin != null) {
                    // Skip if already loaded (from another directory)
                    if (loadedIds.contains(plugin.manifest.id)) {
                        println("[PluginLoader] Skipping duplicate plugin: ${plugin.manifest.id}")
                        continue
                    }
                    loadedIds.add(plugin.manifest.id)
                    plugins.add(plugin)
                    println("[PluginLoader] Loaded plugin: ${plugin.manifest.id} (type: ${plugin.manifest.type})")
                }
            } catch (e: Exception) {
                println("[PluginLoader] Failed to load plugin from ${file.name}: ${e.message}")
                e.printStackTrace()
            }
        }
        
        return plugins
    }
    
    /**
     * Load a single plugin from a package file
     * Steps:
     * 1. Extract and parse manifest from plugin.json
     * 2. Validate manifest
     * 3. Register plugin package path for native library extraction
     * 4. Load plugin class using platform-specific class loader
     * 5. Instantiate plugin
     */
    suspend fun loadPlugin(file: VirtualFile): Plugin? {
        return withContext(Dispatchers.Default) {
            try {
                // Step 1: Extract manifest
                val manifest = extractManifest(file)
                
                // Step 2: Validate manifest
                validator.validate(manifest).getOrElse { error ->
                    throw IllegalStateException("Plugin validation failed: ${error.message}", error)
                }
                
                // Step 3: Register plugin package path for native library extraction
                registerPluginPackage(manifest.id, file.path)
                
                // Step 4: Load plugin class
                val pluginClass = classLoader.loadPluginClass(file, manifest)
                
                // Step 5: Instantiate plugin
                val plugin = instantiatePlugin(pluginClass)
                
                plugin
            } catch (e: Exception) {
                // Wrap in PluginError for consistent error handling
                throw IllegalStateException("Failed to load plugin from ${file.name}", e)
            }
        }
    }
    
    /**
     * Extract and parse plugin manifest from .iplugin package
     * The manifest is stored as plugin.json in the root of the ZIP archive
     * Also checks alternative locations for compatibility
     */
    suspend fun extractManifest(file: VirtualFile): PluginManifest {
        try {
            println("[PluginLoader] Extracting manifest from: ${file.name}")
            println("[PluginLoader] File exists: ${file.exists()}, size: ${file.size()} bytes")
            
            // List all entries first for debugging
            val allEntries = listZipEntries(file)
            println("[PluginLoader] ZIP entries (${allEntries.size}): ${allEntries.take(20).joinToString(", ")}")
            
            // Try standard location first
            var manifestContent = ireader.domain.plugins.extractZipEntry(file, "plugin.json")
            println("[PluginLoader] plugin.json at root: ${manifestContent != null}")
            
            // Try alternative locations if not found at root
            if (manifestContent == null) {
                manifestContent = ireader.domain.plugins.extractZipEntry(file, "assets/plugin.json")
                println("[PluginLoader] plugin.json at assets/: ${manifestContent != null}")
            }
            if (manifestContent == null) {
                manifestContent = ireader.domain.plugins.extractZipEntry(file, "META-INF/plugin.json")
                println("[PluginLoader] plugin.json at META-INF/: ${manifestContent != null}")
            }
            
            if (manifestContent == null) {
                val entriesPreview = allEntries.take(10).joinToString(", ")
                val errorMsg = "plugin.json not found in package. " +
                    "Expected at root, assets/, or META-INF/. " +
                    "Package contains ${allEntries.size} entries: $entriesPreview${if (allEntries.size > 10) "..." else ""}"
                println("[PluginLoader] ERROR: $errorMsg")
                throw IllegalArgumentException(errorMsg)
            }
            
            println("[PluginLoader] Manifest content length: ${manifestContent.length}")
            println("[PluginLoader] Manifest content: $manifestContent")
            val parsedManifest = json.decodeFromString(PluginManifest.serializer(), manifestContent)
            println("[PluginLoader] Parsed manifest mainClass: ${parsedManifest.mainClass}")
            return parsedManifest
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            println("[PluginLoader] Exception: ${e.message}")
            e.printStackTrace()
            throw IllegalArgumentException("Failed to extract manifest from ${file.name}: ${e.message}", e)
        }
    }
    
    /**
     * List entries in a ZIP file for debugging
     */
    private suspend fun listZipEntries(file: VirtualFile): List<String> {
        return try {
            ireader.domain.plugins.listZipEntries(file)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Download a plugin file from URL to destination
     */
    suspend fun downloadToFile(url: String, destination: okio.Path): Result<Unit> {
        return withContext(Dispatchers.Default) {
            try {
                // Platform-specific download implementation
                downloadFile(url, destination)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Download a plugin file from URL to destination with progress callback
     */
    suspend fun downloadToFileWithProgress(
        url: String,
        destination: okio.Path,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            downloadFileWithProgress(url, destination, onProgress)
        }
    }
}

/**
 * Platform-specific ZIP extraction
 */
expect suspend fun extractZipEntry(file: VirtualFile, entryName: String): String?

/**
 * Platform-specific ZIP entry listing for debugging
 */
expect suspend fun listZipEntries(file: VirtualFile): List<String>

/**
 * Platform-specific plugin instantiation
 */
expect fun instantiatePlugin(pluginClass: Any): Plugin

/**
 * Platform-specific file download
 */
expect suspend fun downloadFile(url: String, destination: okio.Path)

/**
 * Platform-specific file download with progress callback
 */
expect suspend fun downloadFileWithProgress(
    url: String,
    destination: okio.Path,
    onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
)

/**
 * Register plugin package path for native library extraction.
 * Called when loading a plugin to enable native library extraction later.
 */
expect fun registerPluginPackage(pluginId: String, packagePath: String)
