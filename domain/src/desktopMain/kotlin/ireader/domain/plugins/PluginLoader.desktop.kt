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

/**
 * Desktop implementation of ZIP entry listing for debugging
 */
actual suspend fun listZipEntries(file: VirtualFile): List<String> {
    return try {
        val javaFile = java.io.File(file.path)
        
        ZipFile(javaFile).use { zip ->
            zip.entries().toList().map { it.name }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Desktop implementation of file download
 * Uses HttpURLConnection for downloading plugin files
 */
actual suspend fun downloadFile(url: String, destination: okio.Path) {
    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 30000
    connection.readTimeout = 30000
    
    try {
        connection.connect()
        
        if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP error: ${connection.responseCode}")
        }
        
        val file = destination.toFile()
        file.parentFile?.mkdirs()
        
        connection.inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } finally {
        connection.disconnect()
    }
}
