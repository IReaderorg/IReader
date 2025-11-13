package ireader.domain.config

import ireader.core.config.ConfigLoader

/**
 * Desktop platform configuration
 * Loads credentials from config.properties or environment variables
 */
actual object PlatformConfig {
    actual fun getSupabaseUrl(): String {
        return System.getenv("SUPABASE_URL") 
            ?: ConfigLoader.get("supabase.url", "")
    }
    
    actual fun getSupabaseAnonKey(): String {
        return System.getenv("SUPABASE_ANON_KEY") 
            ?: ConfigLoader.get("supabase.anon.key", "")
    }
}
