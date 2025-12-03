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
     * On Android, .iplugin files are APK/DEX files that can be loaded with DexClassLoader
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        try {
            // Convert VirtualFile to java.io.File for DexClassLoader
            val javaFile = File(file.path)
            
            // Create appropriate ClassLoader based on Android version
            val classLoader = createClassLoader(javaFile, manifest.id)
            
            // Load the main plugin class
            // Convention: plugin class name is {manifest.id}.Plugin
            val className = "${manifest.id}.Plugin"
            
            @Suppress("UNCHECKED_CAST")
            return classLoader.loadClass(className) as Class<out Plugin>
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("Plugin class not found for ${manifest.id}: ${e.message}", e)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load plugin class for ${manifest.id}: ${e.message}", e)
        }
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
     */
    private fun createSecureDexClassLoader(file: File, pluginId: String): ClassLoader {
        // Create a fresh copy to avoid "writable dex file" issues
        val secureFile = File(securePluginDir, "${pluginId}.iplugin")
        if (secureFile.exists()) {
            secureFile.delete()
        }
        
        // Copy to secure location
        file.copyTo(secureFile, overwrite = true)
        
        // Set read-only permissions (Android 14+ requirement)
        secureFile.setReadOnly()
        
        // Create unique dex output directory per plugin
        val dexOutputDir = File(secureDexCacheDir, pluginId).apply { mkdirs() }
        
        return DexClassLoader(
            secureFile.absolutePath,
            dexOutputDir.absolutePath,
            null,
            this::class.java.classLoader
        )
    }
}
