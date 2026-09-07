package ireader.domain.config

import ireader.core.config.ConfigLoader

/**
 * Desktop platform configuration
 * Loads default credentials from config.properties or environment variables
 * These are the defaults that ship with the app - users can override in settings
 */
actual object PlatformConfig {
    private val fallbackUrlKeys = listOf("supabase.url", "supabase.community.url")
    private val fallbackUrlEnvs = listOf("SUPABASE_URL", "SUPABASE_COMMUNITY_URL")
    private val fallbackKeyKeys = listOf("supabase.key", "supabase.anon.key", "supabase.community.key")
    private val fallbackKeyEnvs = listOf("SUPABASE_KEY", "SUPABASE_ANON_KEY", "SUPABASE_COMMUNITY_KEY")

    private fun resolveDesktopConfig(
        propertyKey: String,
        envVar: String,
        fallbackKeys: List<String>,
        fallbackEnvs: List<String>
    ): String {
        System.getProperty(propertyKey)?.takeIf { it.isNotBlank() }?.let { return it }
        System.getenv(envVar)?.takeIf { it.isNotBlank() }?.let { return it }
        val loaded = ConfigLoader.get(propertyKey, "").trim()
        if (loaded.isNotBlank()) return loaded
        for (fProp in fallbackKeys) {
            System.getProperty(fProp)?.takeIf { it.isNotBlank() }?.let { return it }
        }
        for (fEnv in fallbackEnvs) {
            System.getenv(fEnv)?.takeIf { it.isNotBlank() }?.let { return it }
        }
        for (fKey in fallbackKeys) {
            val fLoaded = ConfigLoader.get(fKey, "").trim()
            if (fLoaded.isNotBlank()) return fLoaded
        }
        return ""
    }

    // Project 1 - Auth
    actual fun getSupabaseAuthUrl(): String =
        resolveDesktopConfig("supabase.auth.url", "SUPABASE_AUTH_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseAuthKey(): String =
        resolveDesktopConfig("supabase.auth.key", "SUPABASE_AUTH_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 2 - Reading
    actual fun getSupabaseReadingUrl(): String =
        resolveDesktopConfig("supabase.reading.url", "SUPABASE_READING_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseReadingKey(): String =
        resolveDesktopConfig("supabase.reading.key", "SUPABASE_READING_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 3 - Library
    actual fun getSupabaseLibraryUrl(): String =
        resolveDesktopConfig("supabase.library.url", "SUPABASE_LIBRARY_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseLibraryKey(): String =
        resolveDesktopConfig("supabase.library.key", "SUPABASE_LIBRARY_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 4 - Book Reviews
    actual fun getSupabaseBookReviewsUrl(): String =
        resolveDesktopConfig("supabase.book_reviews.url", "SUPABASE_BOOK_REVIEWS_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseBookReviewsKey(): String =
        resolveDesktopConfig("supabase.book_reviews.key", "SUPABASE_BOOK_REVIEWS_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 5 - Chapter Reviews
    actual fun getSupabaseChapterReviewsUrl(): String =
        resolveDesktopConfig("supabase.chapter_reviews.url", "SUPABASE_CHAPTER_REVIEWS_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseChapterReviewsKey(): String =
        resolveDesktopConfig("supabase.chapter_reviews.key", "SUPABASE_CHAPTER_REVIEWS_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 6 - Badges
    actual fun getSupabaseBadgesUrl(): String =
        resolveDesktopConfig("supabase.badges.url", "SUPABASE_BADGES_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseBadgesKey(): String =
        resolveDesktopConfig("supabase.badges.key", "SUPABASE_BADGES_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
    // Project 7 - Analytics
    actual fun getSupabaseAnalyticsUrl(): String =
        resolveDesktopConfig("supabase.analytics.url", "SUPABASE_ANALYTICS_URL", fallbackUrlKeys, fallbackUrlEnvs)
    
    actual fun getSupabaseAnalyticsKey(): String =
        resolveDesktopConfig("supabase.analytics.key", "SUPABASE_ANALYTICS_KEY", fallbackKeyKeys, fallbackKeyEnvs)
    
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
    
    // Discord Webhooks
    actual fun getDiscordCharacterArtWebhookUrl(): String {
        return System.getProperty("discord.characterArt.webhookUrl")
            ?: System.getenv("DISCORD_CHARACTER_ART_WEBHOOK_URL")
            ?: ConfigLoader.get("discord.characterArt.webhookUrl", "")
    }
    
    actual fun getDiscordQuoteWebhookUrl(): String {
        return System.getProperty("discord.quote.webhookUrl")
            ?: System.getenv("DISCORD_QUOTE_WEBHOOK_URL")
            ?: ConfigLoader.get("discord.quote.webhookUrl", "")
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
