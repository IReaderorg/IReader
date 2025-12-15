package ireader.domain.config

/**
 * iOS implementation of PlatformConfig
 * 
 * TODO: Read from Info.plist or environment configuration
 */
actual object PlatformConfig {
    actual fun getSupabaseAuthUrl(): String = ""
    actual fun getSupabaseAuthKey(): String = ""
    actual fun getSupabaseReadingUrl(): String = ""
    actual fun getSupabaseReadingKey(): String = ""
    actual fun getSupabaseLibraryUrl(): String = ""
    actual fun getSupabaseLibraryKey(): String = ""
    actual fun getSupabaseBookReviewsUrl(): String = ""
    actual fun getSupabaseBookReviewsKey(): String = ""
    actual fun getSupabaseChapterReviewsUrl(): String = ""
    actual fun getSupabaseChapterReviewsKey(): String = ""
    actual fun getSupabaseBadgesUrl(): String = ""
    actual fun getSupabaseBadgesKey(): String = ""
    actual fun getSupabaseAnalyticsUrl(): String = ""
    actual fun getSupabaseAnalyticsKey(): String = ""
    
    // Cloudflare R2 Storage (Character Art)
    actual fun getR2AccountId(): String = ""
    actual fun getR2AccessKeyId(): String = ""
    actual fun getR2SecretAccessKey(): String = ""
    actual fun getR2BucketName(): String = "ireader-character-art"
    actual fun getR2PublicUrl(): String = ""
    
    // Cloudflare D1 + R2 (Community Translations)
    // TODO: Read from Info.plist or environment configuration
    actual fun getCommunityCloudflareAccountId(): String = ""
    actual fun getCommunityCloudflareApiToken(): String = ""
    actual fun getCommunityD1DatabaseId(): String = ""
    actual fun getCommunityR2BucketName(): String = "ireader-community-translations"
    actual fun getCommunityR2PublicUrl(): String = ""
    
    // Device identification for license binding
    actual fun getDeviceId(): String {
        // iOS: Use identifierForVendor or generate UUID
        return platform.UIKit.UIDevice.currentDevice.identifierForVendor?.UUIDString 
            ?: platform.Foundation.NSUUID().UUIDString
    }
}
