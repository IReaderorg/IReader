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
    
    // Cloudflare D1 + R2 (Community Translations)
    fun getCommunityCloudflareAccountId(): String
    fun getCommunityCloudflareApiToken(): String
    fun getCommunityD1DatabaseId(): String
    fun getCommunityR2BucketName(): String
    fun getCommunityR2PublicUrl(): String
    
    // Discord Webhooks
    fun getDiscordCharacterArtWebhookUrl(): String
    
    // Device identification for license binding
    fun getDeviceId(): String
}
