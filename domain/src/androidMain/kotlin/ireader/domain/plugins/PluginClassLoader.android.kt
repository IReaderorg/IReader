package ireader.domain.plugins

import android.os.Build
import dalvik.system.DexClassLoader
import dalvik.system.InMemoryDexClassLoader
import ireader.core.io.VirtualFile
import ireader.core.log.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * Android implementation of PluginClassLoader using DexClassLoader/InMemoryDexClassLoader
 * Loads plugin classes from .iplugin packages (APK/DEX format)
 * 
 * Android version compatibility:
 * - Android 15+ (API 35+): Uses InMemoryDexClassLoader for enhanced security
 * - Android 14+ (API 34+): Uses DexClassLoader with secure codeCacheDir
 * - Older versions: Uses DexClassLoader with standard approach
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
actual class PluginClassLoader(
    private val cacheDir: File
) {
    // Secure directories for DEX loading (Android 14+ requirement)
    private val securePluginDir = File(cacheDir, "secure_plugins").apply { mkdirs() }
    private val secureDexCacheDir = File(cacheDir, "plugin_dex").apply { mkdirs() }
    
    /**
     * Load a plugin class from a package file
     * On Android, .iplugin files are ZIP archives containing android/classes.dex
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        try {
            // Convert VirtualFile to java.io.File
            val javaFile = File(file.path)
            
            // Extract DEX file from the ZIP package
            val dexFile = extractDexFromPackage(javaFile, manifest.id)
            
            // Create appropriate ClassLoader based on Android version
            val classLoader = createClassLoader(dexFile, manifest.id)
            
            // Determine the class name to load
            val className = manifest.mainClass ?: deriveClassName(manifest.id)
            
            Log.info("Loading plugin class: $className from ${dexFile.absolutePath}")
            
            @Suppress("UNCHECKED_CAST")
            return classLoader.loadClass(className) as Class<out Plugin>
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("Plugin class not found for ${manifest.id}: ${e.message}", e)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load plugin class for ${manifest.id}: ${e.message}", e)
        }
    }
    
    /**
     * Derive the class name from the plugin ID.
     * Converts plugin ID like "io.github.ireaderorg.plugins.ocean-theme" to
     * a valid Java class name like "io.github.ireaderorg.plugins.oceantheme.OceanTheme"
     */
    private fun deriveClassName(pluginId: String): String {
        // Split the ID into package parts
        val parts = pluginId.split(".")
        if (parts.isEmpty()) return pluginId
        
        // The last part is typically the plugin name (e.g., "ocean-theme")
        val lastPart = parts.last()
        
        // Convert hyphenated name to package name (remove hyphens) and class name (PascalCase)
        val packageName = lastPart.replace("-", "")
        val className = lastPart.split("-")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
        
        // Build the full class name: package.subpackage.ClassName
        val basePackage = parts.dropLast(1).joinToString(".")
        return "$basePackage.$packageName.$className"
    }
    
    /**
     * Extract the DEX file from the .iplugin ZIP package
     */
    private fun extractDexFromPackage(packageFile: File, pluginId: String): File {
        val dexOutputFile = File(securePluginDir, "${pluginId}.dex")
        
        java.util.zip.ZipFile(packageFile).use { zip ->
            // Try android/classes.dex first (standard location)
            var dexEntry = zip.getEntry("android/classes.dex")
            
            // Fallback to classes.dex at root
            if (dexEntry == null) {
                dexEntry = zip.getEntry("classes.dex")
            }
            
            if (dexEntry == null) {
                throw IllegalStateException("No DEX file found in plugin package. Expected android/classes.dex or classes.dex")
            }
            
            Log.info("Extracting DEX from ${dexEntry.name} (${dexEntry.size} bytes)")
            
            zip.getInputStream(dexEntry).use { input ->
                dexOutputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        
        Log.info("Extracted DEX to ${dexOutputFile.absolutePath} (${dexOutputFile.length()} bytes)")
        return dexOutputFile
    }
    
    /**
     * Creates the appropriate ClassLoader based on Android version.
     */
    private fun createClassLoader(file: File, pluginId: String): ClassLoader {
        // Android 15+ (API 35): Use InMemoryDexClassLoader for enhanced security
        if (Build.VERSION.SDK_INT >= 35) {
            return try {
                createInMemoryClassLoader(file)
            } catch (e: Exception) {
                Log.warn("InMemoryDexClassLoader failed for $pluginId, falling back to DexClassLoader", e)
                createSecureDexClassLoader(file, pluginId)
            }
        }
        
        // Android 14+ (API 34): Use secure DexClassLoader
        return createSecureDexClassLoader(file, pluginId)
    }
    
    /**
     * Creates an InMemoryDexClassLoader for Android 15+.
     */
    @Suppress("NewApi")
    private fun createInMemoryClassLoader(file: File): ClassLoader {
        val dexBytes = file.readBytes()
        val buffer = ByteBuffer.wrap(dexBytes)
        return InMemoryDexClassLoader(buffer, this::class.java.classLoader)
    }
    
    /**
     * Creates a secure DexClassLoader for Android 14+.
     * The file parameter is already the extracted DEX file.
     */
    private fun createSecureDexClassLoader(file: File, pluginId: String): ClassLoader {
        // The DEX file is already extracted to securePluginDir
        // Just ensure it's read-only for Android 14+ security requirements
        file.setReadOnly()
        
        // Create unique dex output directory per plugin
        val dexOutputDir = File(secureDexCacheDir, pluginId).apply { mkdirs() }
        
        Log.info("Creating DexClassLoader for $pluginId from ${file.absolutePath}")
        
        return DexClassLoader(
            file.absolutePath,
            dexOutputDir.absolutePath,
            null,
            this::class.java.classLoader
        )
    }
}
