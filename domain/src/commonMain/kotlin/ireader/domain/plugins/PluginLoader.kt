package ireader.domain.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile

/**
 * Plugin loader - loads and validates plugins from packages
 * .iplugin file format: ZIP archive containing:
 * - plugin.json: Plugin manifest
 * - Compiled plugin classes (DEX for Android, JAR classes for Desktop)
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 17.1, 17.2, 17.3, 17.4, 17.5
 */
class PluginLoader(
    private val pluginsDir: File,
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
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs()
                return@withContext emptyList()
            }
            
            pluginsDir.listFiles()
                ?.filter { it.isFile && it.extension == "iplugin" }
                ?.mapNotNull { file ->
                    try {
                        loadPlugin(file)
                    } catch (e: Exception) {
                        // Log error but continue loading other plugins
                        println("Failed to load plugin from ${file.name}: ${e.message}")
                        null
                    }
                }
                ?: emptyList()
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
    suspend fun loadPlugin(file: File): Plugin? {
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
    fun extractManifest(file: File): PluginManifest {
        try {
            ZipFile(file).use { zip ->
                val manifestEntry = zip.getEntry("plugin.json")
                    ?: throw IllegalArgumentException("plugin.json not found in package")
                
                val manifestContent = zip.getInputStream(manifestEntry).use { stream ->
                    stream.bufferedReader().readText()
                }
                
                return json.decodeFromString(PluginManifest.serializer(), manifestContent)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to extract manifest from ${file.name}: ${e.message}", e)
        }
    }
}
