package ireader.domain.plugins

import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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
        return withContext(Dispatchers.IO) {
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
        return withContext(Dispatchers.IO) {
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
                val plugin = pluginClass.getDeclaredConstructor().newInstance()
                
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
     */
    suspend fun extractManifest(file: VirtualFile): PluginManifest {
        try {
            // Platform-specific ZIP extraction would be implemented here
            // For now, we'll use a simplified approach
            val manifestContent = extractZipEntry(file, "plugin.json")
                ?: throw IllegalArgumentException("plugin.json not found in package")
            
            return json.decodeFromString(PluginManifest.serializer(), manifestContent)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to extract manifest from ${file.name}: ${e.message}", e)
        }
    }
    
    /**
     * Platform-specific ZIP entry extraction
     */
    private suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
        // This would be implemented platform-specifically
        // For Android: use ZipInputStream
        // For Desktop: use java.util.zip.ZipFile
        throw NotImplementedError("ZIP extraction must be implemented platform-specifically")
    }
}

/**
 * Platform-specific ZIP extraction
 */
expect suspend fun extractZipEntry(file: VirtualFile, entryName: String): String?
