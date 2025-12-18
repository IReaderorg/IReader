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
    println("[PluginLoader] Instantiating plugin class: ${clazz.name}")
    
    try {
        // Check for no-arg constructor
        val constructor = clazz.getDeclaredConstructor()
        println("[PluginLoader] Found no-arg constructor: $constructor")
        
        // Make it accessible if private
        constructor.isAccessible = true
        
        val instance = constructor.newInstance()
        println("[PluginLoader] Plugin instantiated successfully: ${instance::class.java.name}")
        println("[PluginLoader] Plugin manifest ID: ${instance.manifest.id}")
        
        return instance
    } catch (e: NoSuchMethodException) {
        println("[PluginLoader] ERROR: No no-arg constructor found for ${clazz.name}")
        println("[PluginLoader] Available constructors: ${clazz.declaredConstructors.map { it.toString() }}")
        throw IllegalStateException("Plugin class ${clazz.name} must have a no-arg constructor", e)
    } catch (e: java.lang.reflect.InvocationTargetException) {
        println("[PluginLoader] ERROR: Constructor threw exception: ${e.targetException?.message}")
        e.targetException?.printStackTrace()
        throw IllegalStateException("Plugin constructor failed: ${e.targetException?.message}", e.targetException ?: e)
    } catch (e: Exception) {
        println("[PluginLoader] ERROR: Failed to instantiate plugin: ${e.message}")
        e.printStackTrace()
        throw e
    }
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
        connection.connectTimeout = 60000  // 60 seconds connect timeout
        connection.readTimeout = 300000    // 5 minutes read timeout for large files
        connection.instanceFollowRedirects = true // Enable automatic redirects
        connection.setRequestProperty("User-Agent", "IReader-Plugin-Downloader/1.0")
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Connection", "keep-alive")
        
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
            
            val contentLength = connection.contentLengthLong  // Use Long for large files
            println("[PluginLoader] Content-Length: $contentLength")
            
            val file = destination.toFile()
            println("[PluginLoader] Java File path: ${file.absolutePath}")
            file.parentFile?.mkdirs()
            
            // Delete existing file if present
            if (file.exists()) {
                file.delete()
            }
            
            var bytesWritten = 0L
            val startTime = System.currentTimeMillis()
            
            java.io.BufferedInputStream(connection.inputStream, 65536).use { input ->
                java.io.BufferedOutputStream(java.io.FileOutputStream(file), 65536).use { output ->
                    val buffer = ByteArray(65536)  // 64KB buffer for better performance
                    var bytesRead: Int
                    var lastLogTime = startTime
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        
                        // Log progress every 5 seconds
                        val now = System.currentTimeMillis()
                        if (now - lastLogTime > 5000) {
                            val progress = if (contentLength > 0) {
                                (bytesWritten * 100 / contentLength)
                            } else {
                                -1
                            }
                            println("[PluginLoader] Download progress: $bytesWritten bytes ($progress%)")
                            lastLogTime = now
                        }
                    }
                    output.flush()
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            println("[PluginLoader] Download complete in ${duration}ms. Bytes written: $bytesWritten")
            println("[PluginLoader] File exists after write: ${file.exists()}, size: ${file.length()}")
            
            if (bytesWritten == 0L) {
                throw Exception("Downloaded 0 bytes from $url - server may have returned empty response")
            }
            
            // Verify file size matches Content-Length if provided
            if (contentLength > 0 && bytesWritten != contentLength) {
                println("[PluginLoader] ERROR: Size mismatch! Expected $contentLength, got $bytesWritten")
                // Delete incomplete file
                file.delete()
                throw Exception("Download incomplete: expected $contentLength bytes, got $bytesWritten bytes. Please try again.")
            }
            
            // Verify the file is a valid ZIP
            try {
                java.util.zip.ZipFile(file).use { zip ->
                    val entryCount = zip.entries().toList().size
                    println("[PluginLoader] ZIP verification passed: $entryCount entries")
                }
            } catch (e: Exception) {
                println("[PluginLoader] ERROR: Downloaded file is not a valid ZIP: ${e.message}")
                file.delete()
                throw Exception("Downloaded file is corrupted (not a valid ZIP). Please try again.")
            }
            
            return // Success
        } finally {
            connection.disconnect()
        }
    }
    
    throw Exception("Too many redirects ($maxRedirects) while downloading from $url")
}

/**
 * Android implementation of file download with progress callback
 */
actual suspend fun downloadFileWithProgress(
    url: String,
    destination: okio.Path,
    onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
) {
    println("[PluginLoader] Starting download with progress from: $url")
    
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
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Connection", "keep-alive")
        
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
            
            if (file.exists()) {
                file.delete()
            }
            
            var bytesWritten = 0L
            
            java.io.BufferedInputStream(connection.inputStream, 65536).use { input ->
                java.io.BufferedOutputStream(java.io.FileOutputStream(file), 65536).use { output ->
                    val buffer = ByteArray(65536)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        
                        // Report progress
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
                throw Exception("Download incomplete: expected $contentLength bytes, got $bytesWritten bytes")
            }
            
            // Verify ZIP
            try {
                java.util.zip.ZipFile(file).use { zip ->
                    zip.entries().toList().size
                }
            } catch (e: Exception) {
                file.delete()
                throw Exception("Downloaded file is corrupted")
            }
            
            return
        } finally {
            connection.disconnect()
        }
    }
    
    throw Exception("Too many redirects")
}

/**
 * Register plugin package path for native library extraction.
 */
actual fun registerPluginPackage(pluginId: String, packagePath: String) {
    registerPluginPackagePath(pluginId, packagePath)
}
