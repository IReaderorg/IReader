package ireader.domain.plugins

import dalvik.system.DexClassLoader
import ireader.core.io.VirtualFile
import java.io.File

/**
 * Android implementation of PluginClassLoader using DexClassLoader
 * Loads plugin classes from .iplugin packages (APK/DEX format)
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
actual class PluginClassLoader(
    private val cacheDir: File
) {
    /**
     * Load a plugin class from a package file
     * On Android, .iplugin files are APK/DEX files that can be loaded with DexClassLoader
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Class<out Plugin> {
        try {
            // Convert VirtualFile to java.io.File for DexClassLoader
            val javaFile = File(file.path)
            
            // Create optimized dex output directory
            val optimizedDir = File(cacheDir, "plugin_dex").apply { mkdirs() }
            
            // Create DexClassLoader to load the plugin
            val classLoader = DexClassLoader(
                javaFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                this::class.java.classLoader
            )
            
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
}
