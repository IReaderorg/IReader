package ireader.domain.config

import ireader.core.config.ConfigLoader

/**
 * Desktop platform configuration
 * Loads default credentials from config.properties or environment variables
 * These are the defaults that ship with the app - users can override in settings
 */
actual object PlatformConfig {
    // Project 1 - Auth
    actual fun getSupabaseAuthUrl(): String {
        return System.getProperty("supabase.auth.url") 
            ?: System.getenv("SUPABASE_AUTH_URL") 
            ?: ConfigLoader.get("supabase.auth.url", "")
    }
    
    actual fun getSupabaseAuthKey(): String {
        return System.getProperty("supabase.auth.key")
            ?: System.getenv("SUPABASE_AUTH_KEY") 
            ?: ConfigLoader.get("supabase.auth.key", "")
    }
    
    // Project 2 - Reading
    actual fun getSupabaseReadingUrl(): String {
        return System.getProperty("supabase.reading.url")
            ?: System.getenv("SUPABASE_READING_URL")
            ?: ConfigLoader.get("supabase.reading.url", "")
    }
    
    actual fun getSupabaseReadingKey(): String {
        return System.getProperty("supabase.reading.key")
            ?: System.getenv("SUPABASE_READING_KEY")
            ?: ConfigLoader.get("supabase.reading.key", "")
    }
    
    // Project 3 - Library
    actual fun getSupabaseLibraryUrl(): String {
        return System.getProperty("supabase.library.url")
            ?: System.getenv("SUPABASE_LIBRARY_URL")
            ?: ConfigLoader.get("supabase.library.url", "")
    }
    
    actual fun getSupabaseLibraryKey(): String {
        return System.getProperty("supabase.library.key")
            ?: System.getenv("SUPABASE_LIBRARY_KEY")
            ?: ConfigLoader.get("supabase.library.key", "")
    }
    
    // Project 4 - Book Reviews
    actual fun getSupabaseBookReviewsUrl(): String {
        return System.getProperty("supabase.book_reviews.url")
            ?: System.getenv("SUPABASE_BOOK_REVIEWS_URL")
            ?: ConfigLoader.get("supabase.book_reviews.url", "")
    }
    
    actual fun getSupabaseBookReviewsKey(): String {
        return System.getProperty("supabase.book_reviews.key")
            ?: System.getenv("SUPABASE_BOOK_REVIEWS_KEY")
            ?: ConfigLoader.get("supabase.book_reviews.key", "")
    }
    
    // Project 5 - Chapter Reviews
    actual fun getSupabaseChapterReviewsUrl(): String {
        return System.getProperty("supabase.chapter_reviews.url")
            ?: System.getenv("SUPABASE_CHAPTER_REVIEWS_URL")
            ?: ConfigLoader.get("supabase.chapter_reviews.url", "")
    }
    
    actual fun getSupabaseChapterReviewsKey(): String {
        return System.getProperty("supabase.chapter_reviews.key")
            ?: System.getenv("SUPABASE_CHAPTER_REVIEWS_KEY")
            ?: ConfigLoader.get("supabase.chapter_reviews.key", "")
    }
    
    // Project 6 - Badges
    actual fun getSupabaseBadgesUrl(): String {
        return System.getProperty("supabase.badges.url")
            ?: System.getenv("SUPABASE_BADGES_URL")
            ?: ConfigLoader.get("supabase.badges.url", "")
    }
    
    actual fun getSupabaseBadgesKey(): String {
        return System.getProperty("supabase.badges.key")
            ?: System.getenv("SUPABASE_BADGES_KEY")
            ?: ConfigLoader.get("supabase.badges.key", "")
    }
    
    // Project 7 - Analytics
    actual fun getSupabaseAnalyticsUrl(): String {
        return System.getProperty("supabase.analytics.url")
            ?: System.getenv("SUPABASE_ANALYTICS_URL")
            ?: ConfigLoader.get("supabase.analytics.url", "")
    }
    
    actual fun getSupabaseAnalyticsKey(): String {
        return System.getProperty("supabase.analytics.key")
            ?: System.getenv("SUPABASE_ANALYTICS_KEY")
            ?: ConfigLoader.get("supabase.analytics.key", "")
    }
    
    // Cloudflare R2 Storage (Character Art)
    actual fun getR2AccountId(): String {
        return System.getProperty("r2.accountId")
            ?: System.getenv("R2_ACCOUNT_ID")
            ?: ConfigLoader.get("r2.accountId", "")
    }
    
    actual fun getR2AccessKeyId(): String {
        return System.getProperty("r2.accessKeyId")
            ?: System.getenv("R2_ACCESS_KEY_ID")
            ?: ConfigLoader.get("r2.accessKeyId", "")
    }
    
    actual fun getR2SecretAccessKey(): String {
        return System.getProperty("r2.secretAccessKey")
            ?: System.getenv("R2_SECRET_ACCESS_KEY")
            ?: ConfigLoader.get("r2.secretAccessKey", "")
    }
    
    actual fun getR2BucketName(): String {
        return System.getProperty("r2.bucketName")
            ?: System.getenv("R2_BUCKET_NAME")
            ?: ConfigLoader.get("r2.bucketName", "ireader-character-art")
    }
    
    actual fun getR2PublicUrl(): String {
        return System.getProperty("r2.publicUrl")
            ?: System.getenv("R2_PUBLIC_URL")
            ?: ConfigLoader.get("r2.publicUrl", "")
    }
    
    // Cloudflare D1 + R2 (Community Translations)
    actual fun getCommunityCloudflareAccountId(): String {
        return System.getProperty("community.cloudflare.accountId")
            ?: System.getenv("COMMUNITY_CLOUDFLARE_ACCOUNT_ID")
            ?: ConfigLoader.get("community.cloudflare.accountId", "")
    }
    
    actual fun getCommunityCloudflareApiToken(): String {
        return System.getProperty("community.cloudflare.apiToken")
            ?: System.getenv("COMMUNITY_CLOUDFLARE_API_TOKEN")
            ?: ConfigLoader.get("community.cloudflare.apiToken", "")
    }
    
    actual fun getCommunityD1DatabaseId(): String {
        return System.getProperty("community.d1.databaseId")
            ?: System.getenv("COMMUNITY_D1_DATABASE_ID")
            ?: ConfigLoader.get("community.d1.databaseId", "")
    }
    
    actual fun getCommunityR2BucketName(): String {
        return System.getProperty("community.r2.bucketName")
            ?: System.getenv("COMMUNITY_R2_BUCKET_NAME")
            ?: ConfigLoader.get("community.r2.bucketName", "ireader-community-translations")
    }
    
    actual fun getCommunityR2PublicUrl(): String {
        return System.getProperty("community.r2.publicUrl")
            ?: System.getenv("COMMUNITY_R2_PUBLIC_URL")
            ?: ConfigLoader.get("community.r2.publicUrl", "")
    }
    
    // Device identification for license binding
    actual fun getDeviceId(): String {
        // Desktop: Use combination of hostname and MAC address hash
        return try {
            val hostname = java.net.InetAddress.getLocalHost().hostName
            val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
            val macAddress = networkInterfaces?.asSequence()
                ?.mapNotNull { it.hardwareAddress }
                ?.firstOrNull()
                ?.joinToString(":") { "%02x".format(it) }
                ?: "unknown"
            "$hostname:$macAddress".hashCode().toString(16)
        } catch (e: Exception) {
            java.util.UUID.randomUUID().toString()
        }
    }
}
