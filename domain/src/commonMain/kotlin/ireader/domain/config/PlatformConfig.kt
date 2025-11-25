package ireader.domain.config

/**
 * Platform-specific configuration
 * Provides default Supabase credentials from platform-specific sources
 * (local.properties, config.properties, or environment variables)
 * 
 * These are the DEFAULT values that ship with the app.
 * Users can optionally override these in Settings → Sync → Supabase Configuration
 */
expect object PlatformConfig {
    // Project 1 - Auth
    fun getSupabaseAuthUrl(): String
    fun getSupabaseAuthKey(): String
    
    // Project 2 - Reading
    fun getSupabaseReadingUrl(): String
    fun getSupabaseReadingKey(): String
    
    // Project 3 - Library
    fun getSupabaseLibraryUrl(): String
    fun getSupabaseLibraryKey(): String
    
    // Project 4 - Book Reviews
    fun getSupabaseBookReviewsUrl(): String
    fun getSupabaseBookReviewsKey(): String
    
    // Project 5 - Chapter Reviews
    fun getSupabaseChapterReviewsUrl(): String
    fun getSupabaseChapterReviewsKey(): String
    
    // Project 6 - Badges
    fun getSupabaseBadgesUrl(): String
    fun getSupabaseBadgesKey(): String
    
    // Project 7 - Analytics
    fun getSupabaseAnalyticsUrl(): String
    fun getSupabaseAnalyticsKey(): String
}
