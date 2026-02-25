package ireader.domain.config

import platform.Foundation.NSBundle
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice

/**
 * iOS implementation of PlatformConfig
 * 
 * Reads configuration from Info.plist or uses environment defaults.
 * To configure, add keys to Info.plist with the corresponding values.
 */
actual object PlatformConfig {
    
    private fun getInfoPlistValue(key: String, default: String = ""): String {
        return try {
            NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String ?: default
        } catch (e: Exception) {
            default
        }
    }
    
    actual fun getSupabaseAuthUrl(): String = getInfoPlistValue("SUPABASE_AUTH_URL")
    actual fun getSupabaseAuthKey(): String = getInfoPlistValue("SUPABASE_AUTH_KEY")
    actual fun getSupabaseReadingUrl(): String = getInfoPlistValue("SUPABASE_READING_URL")
    actual fun getSupabaseReadingKey(): String = getInfoPlistValue("SUPABASE_READING_KEY")
    actual fun getSupabaseLibraryUrl(): String = getInfoPlistValue("SUPABASE_LIBRARY_URL")
    actual fun getSupabaseLibraryKey(): String = getInfoPlistValue("SUPABASE_LIBRARY_KEY")
    actual fun getSupabaseBookReviewsUrl(): String = getInfoPlistValue("SUPABASE_BOOK_REVIEWS_URL")
    actual fun getSupabaseBookReviewsKey(): String = getInfoPlistValue("SUPABASE_BOOK_REVIEWS_KEY")
    actual fun getSupabaseChapterReviewsUrl(): String = getInfoPlistValue("SUPABASE_CHAPTER_REVIEWS_URL")
    actual fun getSupabaseChapterReviewsKey(): String = getInfoPlistValue("SUPABASE_CHAPTER_REVIEWS_KEY")
    actual fun getSupabaseBadgesUrl(): String = getInfoPlistValue("SUPABASE_BADGES_URL")
    actual fun getSupabaseBadgesKey(): String = getInfoPlistValue("SUPABASE_BADGES_KEY")
    actual fun getSupabaseAnalyticsUrl(): String = getInfoPlistValue("SUPABASE_ANALYTICS_URL")
    actual fun getSupabaseAnalyticsKey(): String = getInfoPlistValue("SUPABASE_ANALYTICS_KEY")
    
    // Cloudflare R2 Storage (Character Art)
    actual fun getR2AccountId(): String = getInfoPlistValue("R2_ACCOUNT_ID")
    actual fun getR2AccessKeyId(): String = getInfoPlistValue("R2_ACCESS_KEY_ID")
    actual fun getR2SecretAccessKey(): String = getInfoPlistValue("R2_SECRET_ACCESS_KEY")
    actual fun getR2BucketName(): String = getInfoPlistValue("R2_BUCKET_NAME", "ireader-character-art")
    actual fun getR2PublicUrl(): String = getInfoPlistValue("R2_PUBLIC_URL")
    
    // Cloudflare D1 + R2 (Community Translations)
    actual fun getCommunityCloudflareAccountId(): String = getInfoPlistValue("COMMUNITY_CLOUDFLARE_ACCOUNT_ID")
    actual fun getCommunityCloudflareApiToken(): String = getInfoPlistValue("COMMUNITY_CLOUDFLARE_API_TOKEN")
    actual fun getCommunityD1DatabaseId(): String = getInfoPlistValue("COMMUNITY_D1_DATABASE_ID")
    actual fun getCommunityR2BucketName(): String = getInfoPlistValue("COMMUNITY_R2_BUCKET_NAME", "ireader-community-translations")
    actual fun getCommunityR2PublicUrl(): String = getInfoPlistValue("COMMUNITY_R2_PUBLIC_URL")
    
    // Discord Webhooks
    actual fun getDiscordCharacterArtWebhookUrl(): String = getInfoPlistValue("DISCORD_CHARACTER_ART_WEBHOOK_URL")
    
    // Device identification for license binding
    actual fun getDeviceId(): String {
        // iOS: Use identifierForVendor or generate UUID
        return UIDevice.currentDevice.identifierForVendor?.UUIDString 
            ?: NSUUID().UUIDString
    }
}
