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

/**
 * Android implementation of ZIP entry listing for debugging
 */
actual suspend fun listZipEntries(file: VirtualFile): List<String> {
    return try {
        val bytes = file.readBytes()
        val entries = mutableListOf<String>()
        
        ZipInputStream(bytes.inputStream()).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                entry = zipStream.nextEntry
            }
        }
        
        entries
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Android implementation of file download
 * Uses HttpURLConnection for downloading plugin files with proper redirect handling
 */
actual suspend fun downloadFile(url: String, destination: okio.Path) {
    println("[PluginLoader] Starting download from: $url")
    println("[PluginLoader] Destination: $destination")
    
    var currentUrl = url
    var redirectCount = 0
    val maxRedirects = 5
    
    while (redirectCount < maxRedirects) {
        val connection = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true // Enable automatic redirects
        connection.setRequestProperty("User-Agent", "IReader-Plugin-Downloader/1.0")
        
        try {
            connection.connect()
            val responseCode = connection.responseCode
            println("[PluginLoader] Response code: $responseCode for URL: $currentUrl")
            
            // Handle redirects manually for cross-protocol redirects (HTTP -> HTTPS)
            if (responseCode in 300..399) {
                val newUrl = connection.getHeaderField("Location")
                if (newUrl.isNullOrBlank()) {
                    throw Exception("Redirect response $responseCode but no Location header")
                }
                println("[PluginLoader] Redirecting to: $newUrl")
                currentUrl = if (newUrl.startsWith("/")) {
                    // Relative redirect
                    val originalUrl = java.net.URL(currentUrl)
                    "${originalUrl.protocol}://${originalUrl.host}$newUrl"
                } else {
                    newUrl
                }
                redirectCount++
                connection.disconnect()
                continue
            }
            
            if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.readText()?.take(500) ?: "No error body"
                } catch (e: Exception) {
                    "Could not read error body"
                }
                println("[PluginLoader] HTTP error $responseCode: $errorBody")
                throw Exception("HTTP error: $responseCode - $errorBody")
            }
            
            val contentLength = connection.contentLength
            println("[PluginLoader] Content-Length: $contentLength")
            
            val file = destination.toFile()
            println("[PluginLoader] Java File path: ${file.absolutePath}")
            file.parentFile?.mkdirs()
            
            var bytesWritten = 0L
            connection.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                    }
                }
            }
            
            println("[PluginLoader] Download complete. Bytes written: $bytesWritten")
            println("[PluginLoader] File exists after write: ${file.exists()}, size: ${file.length()}")
            
            if (bytesWritten == 0L) {
                throw Exception("Downloaded 0 bytes from $url - server may have returned empty response")
            }
            
            return // Success
        } finally {
            connection.disconnect()
        }
    }
    
    throw Exception("Too many redirects ($maxRedirects) while downloading from $url")
}
