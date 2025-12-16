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
    
    /**
     * Load all plugins from the plugins directory
     * Scans for .iplugin files and loads each valid plugin
     */
    suspend fun loadAll(): List<Plugin> {
        return withContext(Dispatchers.Default) {
            val pluginsDir = fileSystem.getDataDirectory().resolve("plugins")
            
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs()
                return@withContext emptyList()
            }
            
            pluginsDir.listFiles()
                .filter { it.isFile() && it.extension == "iplugin" }
                .mapNotNull { file ->
                    try {
                        loadPlugin(file)
                    } catch (e: Exception) {
                        // Log error but continue loading other plugins
                        println("Failed to load plugin from ${file.name}: ${e.message}")
                        null
                    }
                }
        }
    }
    
    /**
     * Load a single plugin from a package file
     * Steps:
     * 1. Extract and parse manifest from plugin.json
     * 2. Validate manifest
     * 3. Load plugin class using platform-specific class loader
     * 4. Instantiate plugin
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
                
                // Step 3: Load plugin class
                val pluginClass = classLoader.loadPluginClass(file, manifest)
                
                // Step 4: Instantiate plugin
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
            return json.decodeFromString(PluginManifest.serializer(), manifestContent)
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
