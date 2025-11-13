package ireader.domain.config

import ireader.core.config.ConfigLoader

/**
 * Desktop platform configuration
 * Loads credentials from config.properties or environment variables
 */
actual object PlatformConfig {
    // Primary endpoint (Users)
    actual fun getSupabaseUrl(): String {
        return System.getProperty("supabase.url") 
            ?: System.getenv("SUPABASE_URL") 
            ?: ConfigLoader.get("supabase.url", "")
    }
    
    actual fun getSupabaseAnonKey(): String {
        return System.getProperty("supabase.anon.key")
            ?: System.getenv("SUPABASE_ANON_KEY") 
            ?: ConfigLoader.get("supabase.anon.key", "")
    }
    
    // Books endpoint
    actual fun getSupabaseBooksUrl(): String {
        return System.getProperty("supabase.books.url")
            ?: System.getenv("SUPABASE_BOOKS_URL")
            ?: ConfigLoader.get("supabase.books.url", "")
    }
    
    actual fun getSupabaseBooksKey(): String {
        return System.getProperty("supabase.books.key")
            ?: System.getenv("SUPABASE_BOOKS_KEY")
            ?: ConfigLoader.get("supabase.books.key", "")
    }
    
    // Progress endpoint
    actual fun getSupabaseProgressUrl(): String {
        return System.getProperty("supabase.progress.url")
            ?: System.getenv("SUPABASE_PROGRESS_URL")
            ?: ConfigLoader.get("supabase.progress.url", "")
    }
    
    actual fun getSupabaseProgressKey(): String {
        return System.getProperty("supabase.progress.key")
            ?: System.getenv("SUPABASE_PROGRESS_KEY")
            ?: ConfigLoader.get("supabase.progress.key", "")
    }
    
    // Reviews endpoint
    actual fun getSupabaseReviewsUrl(): String {
        return System.getProperty("supabase.reviews.url")
            ?: System.getenv("SUPABASE_REVIEWS_URL")
            ?: ConfigLoader.get("supabase.reviews.url", "")
    }
    
    actual fun getSupabaseReviewsKey(): String {
        return System.getProperty("supabase.reviews.key")
            ?: System.getenv("SUPABASE_REVIEWS_KEY")
            ?: ConfigLoader.get("supabase.reviews.key", "")
    }
    
    // Community endpoint
    actual fun getSupabaseCommunityUrl(): String {
        return System.getProperty("supabase.community.url")
            ?: System.getenv("SUPABASE_COMMUNITY_URL")
            ?: ConfigLoader.get("supabase.community.url", "")
    }
    
    actual fun getSupabaseCommunityKey(): String {
        return System.getProperty("supabase.community.key")
            ?: System.getenv("SUPABASE_COMMUNITY_KEY")
            ?: ConfigLoader.get("supabase.community.key", "")
    }
}
