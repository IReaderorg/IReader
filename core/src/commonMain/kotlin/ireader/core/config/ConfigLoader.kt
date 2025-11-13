package ireader.core.config

/**
 * Loads configuration from config.properties file
 */
object ConfigLoader {
    private var config: Map<String, String>? = null
    
    fun load(): Map<String, String> {
        if (config != null) return config!!
        
        val properties = mutableMapOf<String, String>()
        
        try {
            // Try to load from config.properties in resources
            val inputStream = this::class.java.classLoader?.getResourceAsStream("config.properties")
            if (inputStream != null) {
                inputStream.bufferedReader().use { reader ->
                    reader.lineSequence()
                        .filter { it.isNotBlank() && !it.trim().startsWith("#") }
                        .forEach { line ->
                            val parts = line.split("=", limit = 2)
                            if (parts.size == 2) {
                                properties[parts[0].trim()] = parts[1].trim()
                            }
                        }
                }
            }
        } catch (e: Exception) {
            println("Failed to load config.properties: ${e.message}")
        }
        
        config = properties
        return properties
    }
    
    fun get(key: String, default: String = ""): String {
        return load()[key] ?: default
    }
}
