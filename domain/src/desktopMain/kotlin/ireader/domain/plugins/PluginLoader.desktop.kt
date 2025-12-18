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
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "IReader-Plugin-Downloader/1.0")
        
        try {
            connection.connect()
            val responseCode = connection.responseCode
            println("[PluginLoader] Response code: $responseCode for URL: $currentUrl")
            
            // Handle redirects manually for cross-protocol redirects
            if (responseCode in 300..399) {
                val newUrl = connection.getHeaderField("Location")
                if (newUrl.isNullOrBlank()) {
                    throw Exception("Redirect response $responseCode but no Location header")
                }
                println("[PluginLoader] Redirecting to: $newUrl")
                currentUrl = if (newUrl.startsWith("/")) {
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
            
            if (bytesWritten == 0L) {
                throw Exception("Downloaded 0 bytes from $url - server may have returned empty response")
            }
            
            return
        } finally {
            connection.disconnect()
        }
    }
    
    throw Exception("Too many redirects ($maxRedirects) while downloading from $url")
}

/**
 * Desktop implementation of file download with progress callback
 */
actual suspend fun downloadFileWithProgress(
    url: String,
    destination: okio.Path,
    onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
) {
    var currentUrl = url
    var redirectCount = 0
    val maxRedirects = 5
    
    while (redirectCount < maxRedirects) {
        val connection = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 60000
        connection.readTimeout = 300000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "IReader-Plugin-Downloader/1.0")
        
        try {
            connection.connect()
            val responseCode = connection.responseCode
            
            if (responseCode in 300..399) {
                val newUrl = connection.getHeaderField("Location")
                if (newUrl.isNullOrBlank()) {
                    throw Exception("Redirect response $responseCode but no Location header")
                }
                currentUrl = if (newUrl.startsWith("/")) {
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
                throw Exception("HTTP error: $responseCode")
            }
            
            val contentLength = connection.contentLengthLong
            val file = destination.toFile()
            file.parentFile?.mkdirs()
            
            var bytesWritten = 0L
            java.io.BufferedInputStream(connection.inputStream, 65536).use { input ->
                java.io.BufferedOutputStream(java.io.FileOutputStream(file), 65536).use { output ->
                    val buffer = ByteArray(65536)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        onProgress(bytesWritten, contentLength)
                    }
                    output.flush()
                }
            }
            
            if (bytesWritten == 0L) {
                throw Exception("Downloaded 0 bytes")
            }
            
            if (contentLength > 0 && bytesWritten != contentLength) {
                file.delete()
                throw Exception("Download incomplete")
            }
            
            return
        } finally {
            connection.disconnect()
        }
    }
    
    throw Exception("Too many redirects")
}

// Cache for plugin package paths (for native library extraction)
private val pluginPackagePaths = mutableMapOf<String, String>()

/**
 * Register plugin package path for native library extraction.
 */
actual fun registerPluginPackage(pluginId: String, packagePath: String) {
    pluginPackagePaths[pluginId] = packagePath
    println("[PluginLoader] Registered plugin package path: $pluginId -> $packagePath")
}

/**
 * Get registered plugin package path.
 */
fun getPluginPackagePath(pluginId: String): String? = pluginPackagePaths[pluginId]
