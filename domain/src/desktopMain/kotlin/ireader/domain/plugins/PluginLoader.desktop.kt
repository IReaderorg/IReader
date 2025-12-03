package ireader.domain.plugins

import ireader.core.io.VirtualFile
import java.util.zip.ZipFile

/**
 * Desktop implementation of ZIP entry extraction
 * Uses java.util.zip.ZipFile for efficient ZIP handling
 */
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    return try {
        // Convert VirtualFile to java.io.File for ZipFile
        val javaFile = java.io.File(file.path)
        
        ZipFile(javaFile).use { zip ->
            val entry = zip.getEntry(entryName) ?: return null
            
            zip.getInputStream(entry).use { stream ->
                stream.bufferedReader().readText()
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Desktop implementation of plugin instantiation
 * Uses Java reflection to instantiate the plugin class
 */
@Suppress("UNCHECKED_CAST")
actual fun instantiatePlugin(pluginClass: Any): Plugin {
    val clazz = pluginClass as Class<out Plugin>
    return clazz.getDeclaredConstructor().newInstance()
}
