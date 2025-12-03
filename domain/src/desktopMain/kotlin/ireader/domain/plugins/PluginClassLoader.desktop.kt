package ireader.domain.plugins

import ireader.core.io.VirtualFile
import java.io.File
import java.net.URLClassLoader

/**
 * Desktop implementation of PluginClassLoader using URLClassLoader
 * Loads plugin classes from .iplugin packages (JAR format)
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
actual class PluginClassLoader {
    /**
     * Load a plugin class from a package file
     * On Desktop, .iplugin files are JAR files that can be loaded with URLClassLoader
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        try {
            // Convert VirtualFile to java.io.File for URLClassLoader
            // This is a temporary bridge until we have a full VirtualFile implementation
            val javaFile = File(file.path)
            
            // Create URLClassLoader to load the plugin JAR
            val classLoader = URLClassLoader(
                arrayOf(javaFile.toURI().toURL()),
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
