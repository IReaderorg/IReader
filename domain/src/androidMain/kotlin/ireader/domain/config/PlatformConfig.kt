package ireader.domain.config

import org.ireader.app.BuildConfig

/**
 * Android platform configuration
 * Loads credentials from BuildConfig (which comes from local.properties or environment variables)
 */
actual object PlatformConfig {
    // Primary endpoint (Users)
    actual fun getSupabaseUrl(): String = BuildConfig.SUPABASE_URL
    actual fun getSupabaseAnonKey(): String = BuildConfig.SUPABASE_ANON_KEY
    
    // Books endpoint
    actual fun getSupabaseBooksUrl(): String = BuildConfig.SUPABASE_BOOKS_URL
    actual fun getSupabaseBooksKey(): String = BuildConfig.SUPABASE_BOOKS_KEY
    
    // Progress endpoint
    actual fun getSupabaseProgressUrl(): String = BuildConfig.SUPABASE_PROGRESS_URL
    actual fun getSupabaseProgressKey(): String = BuildConfig.SUPABASE_PROGRESS_KEY
    
    // Reviews endpoint
    actual fun getSupabaseReviewsUrl(): String = BuildConfig.SUPABASE_REVIEWS_URL
    actual fun getSupabaseReviewsKey(): String = BuildConfig.SUPABASE_REVIEWS_KEY
    
    // Community endpoint
    actual fun getSupabaseCommunityUrl(): String = BuildConfig.SUPABASE_COMMUNITY_URL
    actual fun getSupabaseCommunityKey(): String = BuildConfig.SUPABASE_COMMUNITY_KEY
}
