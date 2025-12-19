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
    companion object {
        private val pluginClassLoaders = mutableMapOf<String, ClassLoader>()
        
        fun getClassLoader(pluginId: String): ClassLoader? = pluginClassLoaders[pluginId]
        
        fun getRegisteredPluginIds(): Set<String> = pluginClassLoaders.keys.toSet()
        
        fun clearAll() = pluginClassLoaders.clear()
    }
    
    /**
     * Load a plugin class from a package file
     * On Desktop, .iplugin files are JAR files that can be loaded with URLClassLoader
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        try {
            // Convert VirtualFile to java.io.File for URLClassLoader
            val javaFile = File(file.path)
            
            // Extract classes.jar from the .iplugin ZIP if it exists
            val tempDir = File(System.getProperty("java.io.tmpdir"), "ireader_plugins/${manifest.id}")
            tempDir.mkdirs()
            
            val urls = mutableListOf<java.net.URL>()
            
            // Extract JAR files from the plugin package
            java.util.zip.ZipFile(javaFile).use { zip ->
                // Look for classes.jar or libs/*.jar
                zip.entries().asSequence().forEach { entry ->
                    when {
                        entry.name == "classes.jar" -> {
                            val classesJar = File(tempDir, "classes.jar")
                            zip.getInputStream(entry).use { input ->
                                classesJar.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            urls.add(classesJar.toURI().toURL())
                            println("[PluginClassLoader] Extracted classes.jar")
                        }
                        entry.name.startsWith("libs/") && entry.name.endsWith(".jar") -> {
                            val libJar = File(tempDir, entry.name.substringAfterLast("/"))
                            zip.getInputStream(entry).use { input ->
                                libJar.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            urls.add(libJar.toURI().toURL())
                            println("[PluginClassLoader] Extracted lib: ${entry.name}")
                        }
                    }
                }
            }
            
            // If no JARs found inside, treat the .iplugin itself as a JAR
            if (urls.isEmpty()) {
                urls.add(javaFile.toURI().toURL())
                println("[PluginClassLoader] Using .iplugin directly as JAR")
            }
            
            // Create URLClassLoader with all JARs
            val classLoader = URLClassLoader(
                urls.toTypedArray(),
                this::class.java.classLoader
            )
            
            // Store the classloader for later access
            pluginClassLoaders[manifest.id] = classLoader
            
            // Load the main plugin class from manifest.mainClass
            // Fallback to convention: {manifest.id}.Plugin if mainClass is not specified
            val className = manifest.mainClass ?: "${manifest.id}.Plugin"
            println("[PluginClassLoader] Loading class: $className")
            
            @Suppress("UNCHECKED_CAST")
            return classLoader.loadClass(className) as Class<out Plugin>
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("Plugin class not found for ${manifest.id}: ${e.message}", e)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load plugin class for ${manifest.id}: ${e.message}", e)
        }
    }
}
