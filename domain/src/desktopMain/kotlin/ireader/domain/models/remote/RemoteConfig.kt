package ireader.domain.models.remote

import java.io.File
import java.util.Properties

/**
 * Desktop implementation of loadRemoteConfig
 * Loads configuration from config.properties file in the application directory
 */
actual fun loadRemoteConfig(): RemoteConfig? {
    return try {
        val properties = Properties()
        val configFile = File("config.properties")
        
        if (!configFile.exists()) {
            // Try alternative location in user home directory
            val homeConfigFile = File(System.getProperty("user.home"), ".ireader/config.properties")
            if (!homeConfigFile.exists()) {
                return null
            }
            properties.load(homeConfigFile.inputStream())
        } else {
            properties.load(configFile.inputStream())
        }
        
        val url = properties.getProperty("supabase.url", "")
        val key = properties.getProperty("supabase.anon.key", "")
        
        // Return null if configuration is not set
        if (url.isBlank() || key.isBlank()) {
            return null
        }
        
        RemoteConfig(
            supabaseUrl = url,
            supabaseAnonKey = key,
            enableRealtime = properties.getProperty("supabase.realtime.enabled", "true").toBoolean(),
            syncIntervalMs = properties.getProperty("supabase.sync.interval.ms", "30000").toLong()
        )
    } catch (e: Exception) {
        null
    }
}
