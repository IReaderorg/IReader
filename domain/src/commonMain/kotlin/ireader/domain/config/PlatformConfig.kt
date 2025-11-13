package ireader.domain.config

/**
 * Platform-specific configuration
 * Provides Supabase credentials from platform-specific sources
 */
expect object PlatformConfig {
    /**
     * Get Supabase URL from platform-specific source
     * Android: BuildConfig, Desktop: config.properties or env vars
     */
    fun getSupabaseUrl(): String
    
    /**
     * Get Supabase anon key from platform-specific source
     * Android: BuildConfig, Desktop: config.properties or env vars
     */
    fun getSupabaseAnonKey(): String
}
