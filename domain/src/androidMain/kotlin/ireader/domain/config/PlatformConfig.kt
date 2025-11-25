package ireader.domain.config

import ireader.domain.BuildConfig

/**
 * Android platform configuration
 * Loads default credentials from BuildConfig (which comes from local.properties or environment variables)
 * These are the defaults that ship with the app - users can override in settings
 */
actual object PlatformConfig {
    // Project 1 - Auth
    actual fun getSupabaseAuthUrl(): String = BuildConfig.SUPABASE_AUTH_URL
    actual fun getSupabaseAuthKey(): String = BuildConfig.SUPABASE_AUTH_KEY
    
    // Project 2 - Reading
    actual fun getSupabaseReadingUrl(): String = BuildConfig.SUPABASE_READING_URL
    actual fun getSupabaseReadingKey(): String = BuildConfig.SUPABASE_READING_KEY
    
    // Project 3 - Library
    actual fun getSupabaseLibraryUrl(): String = BuildConfig.SUPABASE_LIBRARY_URL
    actual fun getSupabaseLibraryKey(): String = BuildConfig.SUPABASE_LIBRARY_KEY
    
    // Project 4 - Book Reviews
    actual fun getSupabaseBookReviewsUrl(): String = BuildConfig.SUPABASE_BOOK_REVIEWS_URL
    actual fun getSupabaseBookReviewsKey(): String = BuildConfig.SUPABASE_BOOK_REVIEWS_KEY
    
    // Project 5 - Chapter Reviews
    actual fun getSupabaseChapterReviewsUrl(): String = BuildConfig.SUPABASE_CHAPTER_REVIEWS_URL
    actual fun getSupabaseChapterReviewsKey(): String = BuildConfig.SUPABASE_CHAPTER_REVIEWS_KEY
    
    // Project 6 - Badges
    actual fun getSupabaseBadgesUrl(): String = BuildConfig.SUPABASE_BADGES_URL
    actual fun getSupabaseBadgesKey(): String = BuildConfig.SUPABASE_BADGES_KEY
    
    // Project 7 - Analytics
    actual fun getSupabaseAnalyticsUrl(): String = BuildConfig.SUPABASE_ANALYTICS_URL
    actual fun getSupabaseAnalyticsKey(): String = BuildConfig.SUPABASE_ANALYTICS_KEY
}
