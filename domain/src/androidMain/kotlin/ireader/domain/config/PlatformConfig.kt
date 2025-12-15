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
    
    // Cloudflare R2 Storage (Character Art)
    actual fun getR2AccountId(): String = BuildConfig.R2_ACCOUNT_ID
    actual fun getR2AccessKeyId(): String = BuildConfig.R2_ACCESS_KEY_ID
    actual fun getR2SecretAccessKey(): String = BuildConfig.R2_SECRET_ACCESS_KEY
    actual fun getR2BucketName(): String = BuildConfig.R2_BUCKET_NAME
    actual fun getR2PublicUrl(): String = BuildConfig.R2_PUBLIC_URL
    
    // Cloudflare D1 + R2 (Community Translations)
    actual fun getCommunityCloudflareAccountId(): String = BuildConfig.COMMUNITY_CLOUDFLARE_ACCOUNT_ID
    actual fun getCommunityCloudflareApiToken(): String = BuildConfig.COMMUNITY_CLOUDFLARE_API_TOKEN
    actual fun getCommunityD1DatabaseId(): String = BuildConfig.COMMUNITY_D1_DATABASE_ID
    actual fun getCommunityR2BucketName(): String = BuildConfig.COMMUNITY_R2_BUCKET_NAME
    actual fun getCommunityR2PublicUrl(): String = BuildConfig.COMMUNITY_R2_PUBLIC_URL
    
    // Device identification for license binding
    // Note: This requires context, so we use a fallback approach
    private var cachedDeviceId: String? = null
    
    actual fun getDeviceId(): String {
        cachedDeviceId?.let { return it }
        
        // Generate a stable device ID based on available system properties
        val deviceId = try {
            val serial = android.os.Build.SERIAL.takeIf { it != android.os.Build.UNKNOWN } ?: ""
            val board = android.os.Build.BOARD
            val brand = android.os.Build.BRAND
            val device = android.os.Build.DEVICE
            val model = android.os.Build.MODEL
            val product = android.os.Build.PRODUCT
            
            "$serial:$board:$brand:$device:$model:$product".hashCode().toString(16)
        } catch (e: Exception) {
            java.util.UUID.randomUUID().toString()
        }
        
        cachedDeviceId = deviceId
        return deviceId
    }
    
    /**
     * Set device ID from context (call from Application.onCreate)
     */
    fun setDeviceIdFromContext(context: android.content.Context) {
        cachedDeviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: cachedDeviceId
    }
}
