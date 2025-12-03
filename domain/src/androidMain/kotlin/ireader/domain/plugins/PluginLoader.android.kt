package ireader.domain.plugins

import ireader.core.io.VirtualFile
import java.util.zip.ZipInputStream

/**
 * Android implementation of ZIP entry extraction
 * Uses ZipInputStream for compatibility with Android file system
 */
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    return try {
        // Read the file as bytes and create ZipInputStream
        val bytes = file.readBytes()
        
        ZipInputStream(bytes.inputStream()).use { zipStream ->
            var entry = zipStream.nextEntry
            
            while (entry != null) {
                if (entry.name == entryName) {
                    return zipStream.bufferedReader().readText()
                }
                entry = zipStream.nextEntry
            }
            
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Android implementation of plugin instantiation
 * Uses Java reflection to instantiate the plugin class
 */
@Suppress("UNCHECKED_CAST")
actual fun instantiatePlugin(pluginClass: Any): Plugin {
    val clazz = pluginClass as Class<out Plugin>
    return clazz.getDeclaredConstructor().newInstance()
}
