package ireader.core.config

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * Loads configuration from config.properties file
 * Checks multiple locations:
 * 1. File system (current directory, parent directory)
 * 2. Classpath resources (platform-specific)
 * 
 * Uses Okio for KMP-compatible file operations.
 */
object ConfigLoader {
    private var config: Map<String, String>? = null
    
    fun load(): Map<String, String> {
        if (config != null) return config!!
        
        val properties = mutableMapOf<String, String>()
        
        // Try to load from file system first (for desktop development)
        val loaded = tryLoadFromFileSystem(properties) || tryLoadFromResources(properties)
        
        if (!loaded) {
            println("Warning: config.properties not found in file system or resources")
        }
        
        config = properties
        return properties
    }
    
    private fun tryLoadFromFileSystem(properties: MutableMap<String, String>): Boolean {
        val fileSystem = FileSystem.SYSTEM
        val locations = listOf(
            "config.properties".toPath(),           // Current directory
            "../config.properties".toPath(),        // Parent directory
        )
        
        for (path in locations) {
            if (fileSystem.exists(path)) {
                try {
                    fileSystem.source(path).buffer().use { source ->
                        val content = source.readUtf8()
                        parseProperties(content.lineSequence(), properties)
                    }
                    println("Loaded config.properties from: $path")
                    return true
                } catch (e: Exception) {
                    println("Failed to load config.properties from $path: ${e.message}")
                }
            }
        }
        return false
    }
    
    private fun tryLoadFromResources(properties: MutableMap<String, String>): Boolean {
        // Resource loading is platform-specific, handled via expect/actual if needed
        // For now, file system loading is the primary method
        return false
    }
    
    private fun parseProperties(lines: Sequence<String>, properties: MutableMap<String, String>) {
        lines
            .filter { it.isNotBlank() && !it.trim().startsWith("#") }
            .forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    properties[parts[0].trim()] = parts[1].trim()
                }
            }
    }
    
    fun get(key: String, default: String = ""): String {
        return load()[key] ?: default
    }
    
    fun reload() {
        config = null
    }
}
