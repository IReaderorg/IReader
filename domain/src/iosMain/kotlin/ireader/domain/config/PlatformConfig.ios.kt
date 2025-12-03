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
}
