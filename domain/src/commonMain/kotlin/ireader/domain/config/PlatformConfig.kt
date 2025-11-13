package ireader.domain.config

/**
 * Platform-specific configuration
 * Provides Supabase credentials from platform-specific sources
 */
expect object PlatformConfig {
    // Primary endpoint (Users)
    fun getSupabaseUrl(): String
    fun getSupabaseAnonKey(): String
    
    // Books endpoint
    fun getSupabaseBooksUrl(): String
    fun getSupabaseBooksKey(): String
    
    // Progress endpoint
    fun getSupabaseProgressUrl(): String
    fun getSupabaseProgressKey(): String
    
    // Reviews endpoint
    fun getSupabaseReviewsUrl(): String
    fun getSupabaseReviewsKey(): String
    
    // Community endpoint
    fun getSupabaseCommunityUrl(): String
    fun getSupabaseCommunityKey(): String
}
