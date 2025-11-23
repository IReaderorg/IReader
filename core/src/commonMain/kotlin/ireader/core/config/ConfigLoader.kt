package ireader.core.config

import java.io.File

/**
 * Loads configuration from config.properties file
 * Checks multiple locations:
 * 1. File system (current directory, parent directory)
 * 2. Classpath resources
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
        val locations = listOf(
            File("config.properties"),                    // Current directory
            File("../config.properties"),                 // Parent directory
            File(System.getProperty("user.dir"), "config.properties")  // Working directory
        )
        
        for (file in locations) {
            if (file.exists() && file.isFile) {
                try {
                    file.bufferedReader().use { reader ->
                        parseProperties(reader.lineSequence(), properties)
                    }
                    println("Loaded config.properties from: ${file.absolutePath}")
                    return true
                } catch (e: Exception) {
                    println("Failed to load config.properties from ${file.absolutePath}: ${e.message}")
                }
            }
        }
        return false
    }
    
    private fun tryLoadFromResources(properties: MutableMap<String, String>): Boolean {
        try {
            val inputStream = this::class.java.classLoader?.getResourceAsStream("config.properties")
            if (inputStream != null) {
                inputStream.bufferedReader().use { reader ->
                    parseProperties(reader.lineSequence(), properties)
                }
                println("Loaded config.properties from resources")
                return true
            }
        } catch (e: Exception) {
            println("Failed to load config.properties from resources: ${e.message}")
        }
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
