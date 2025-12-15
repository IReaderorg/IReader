package ireader.domain.community

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.config.PlatformConfig

/**
 * Preferences for Community Source configuration.
 */
class CommunityPreferences(
    private val preferenceStore: PreferenceStore
) {
    companion object {
        // Community Source enable/disable
        const val COMMUNITY_SOURCE_ENABLED = "community_source_enabled"
        
        // Custom Community Source URL (separate from main Supabase)
        const val COMMUNITY_SOURCE_URL = "community_source_url"
        const val COMMUNITY_SOURCE_API_KEY = "community_source_api_key"
        
        // User contribution settings
        const val AUTO_SHARE_TRANSLATIONS = "auto_share_translations"
        const val AUTO_SHARE_AI_ONLY = "auto_share_ai_only"
        const val CONTRIBUTOR_NAME = "contributor_name"
        const val SHOW_CONTRIBUTOR_BADGE = "show_contributor_badge"
        
        // Content preferences
        const val PREFERRED_LANGUAGE = "community_preferred_language"
        const val SHOW_NSFW_CONTENT = "community_show_nsfw"
        const val MINIMUM_RATING = "community_minimum_rating"
        
        // Cache settings
        const val CACHE_DURATION_HOURS = "community_cache_duration"
        const val LAST_SYNC_TIME = "community_last_sync"
        
        // Cloudflare D1 + R2 settings
        const val CLOUDFLARE_ACCOUNT_ID = "cloudflare_account_id"
        const val CLOUDFLARE_API_TOKEN = "cloudflare_api_token"
        const val CLOUDFLARE_D1_DATABASE_ID = "cloudflare_d1_database_id"
        const val CLOUDFLARE_R2_BUCKET_NAME = "cloudflare_r2_bucket_name"
        const val CLOUDFLARE_R2_PUBLIC_URL = "cloudflare_r2_public_url"
        const val CLOUDFLARE_COMPRESSION_ENABLED = "cloudflare_compression_enabled"
        
        // Check community first before translating
        const val CHECK_COMMUNITY_FIRST = "check_community_first"
    }
    
    /**
     * Whether Community Source is enabled.
     */
    fun communitySourceEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(COMMUNITY_SOURCE_ENABLED, true)
    }
    
    /**
     * Custom Community Source Supabase URL.
     * If empty, falls back to the main Supabase library URL.
     */
    fun communitySourceUrl(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_SOURCE_URL, "")
    }
    
    /**
     * Custom Community Source API key.
     * If empty, falls back to the main Supabase library key.
     */
    fun communitySourceApiKey(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_SOURCE_API_KEY, "")
    }
    
    /**
     * Whether to automatically share translations to the community.
     */
    fun autoShareTranslations(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_SHARE_TRANSLATIONS, false)
    }
    
    /**
     * Only auto-share AI translations (not low-quality machine translations).
     * When true, only translations from AI engines (OpenAI, Gemini, DeepSeek, etc.) are shared.
     */
    fun autoShareAiOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_SHARE_AI_ONLY, true)
    }
    
    /**
     * Display name for contributions.
     */
    fun contributorName(): Preference<String> {
        return preferenceStore.getString(CONTRIBUTOR_NAME, "")
    }
    
    /**
     * Whether to show contributor badge on profile.
     */
    fun showContributorBadge(): Preference<Boolean> {
        return preferenceStore.getBoolean(SHOW_CONTRIBUTOR_BADGE, true)
    }
    
    /**
     * Preferred language for community content.
     */
    fun preferredLanguage(): Preference<String> {
        return preferenceStore.getString(PREFERRED_LANGUAGE, "en")
    }
    
    /**
     * Whether to show NSFW content.
     */
    fun showNsfwContent(): Preference<Boolean> {
        return preferenceStore.getBoolean(SHOW_NSFW_CONTENT, false)
    }
    
    /**
     * Minimum rating filter for translations (0-5).
     */
    fun minimumRating(): Preference<Int> {
        return preferenceStore.getInt(MINIMUM_RATING, 0)
    }
    
    /**
     * Cache duration in hours.
     */
    fun cacheDurationHours(): Preference<Int> {
        return preferenceStore.getInt(CACHE_DURATION_HOURS, 24)
    }
    
    /**
     * Last sync timestamp.
     */
    fun lastSyncTime(): Preference<Long> {
        return preferenceStore.getLong(LAST_SYNC_TIME, 0L)
    }
    
    // ==================== Cloudflare D1 + R2 Settings ====================
    
    /**
     * Cloudflare Account ID.
     */
    fun cloudflareAccountId(): Preference<String> {
        return preferenceStore.getString(CLOUDFLARE_ACCOUNT_ID, "")
    }
    
    /**
     * Cloudflare API Token with D1 and R2 permissions.
     */
    fun cloudflareApiToken(): Preference<String> {
        return preferenceStore.getString(CLOUDFLARE_API_TOKEN, "")
    }
    
    /**
     * Cloudflare D1 Database ID for translation metadata.
     */
    fun cloudflareD1DatabaseId(): Preference<String> {
        return preferenceStore.getString(CLOUDFLARE_D1_DATABASE_ID, "")
    }
    
    /**
     * Cloudflare R2 Bucket name for translation content.
     */
    fun cloudflareR2BucketName(): Preference<String> {
        return preferenceStore.getString(CLOUDFLARE_R2_BUCKET_NAME, "")
    }
    
    /**
     * Cloudflare R2 Public URL for CDN access (optional).
     */
    fun cloudflareR2PublicUrl(): Preference<String> {
        return preferenceStore.getString(CLOUDFLARE_R2_PUBLIC_URL, "")
    }
    
    /**
     * Whether to enable compression for translations.
     */
    fun cloudflareCompressionEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(CLOUDFLARE_COMPRESSION_ENABLED, true)
    }
    
    /**
     * Whether to check community translations before translating.
     * If enabled, will look for existing translations first.
     */
    fun checkCommunityFirst(): Preference<Boolean> {
        return preferenceStore.getBoolean(CHECK_COMMUNITY_FIRST, true)
    }
    
    /**
     * Check if Cloudflare is configured (user override or platform default).
     */
    fun isCloudflareConfigured(): Boolean {
        return getEffectiveCloudflareAccountId().isNotBlank() &&
            getEffectiveCloudflareApiToken().isNotBlank() &&
            getEffectiveD1DatabaseId().isNotBlank() &&
            getEffectiveR2BucketName().isNotBlank()
    }
    
    // ==================== Effective Values (User Override or Platform Default) ====================
    
    /**
     * Get effective Cloudflare Account ID (user override or platform default).
     */
    fun getEffectiveCloudflareAccountId(): String {
        val userValue = cloudflareAccountId().get()
        return userValue.ifBlank { PlatformConfig.getCommunityCloudflareAccountId() }
    }
    
    /**
     * Get effective Cloudflare API Token (user override or platform default).
     */
    fun getEffectiveCloudflareApiToken(): String {
        val userValue = cloudflareApiToken().get()
        return userValue.ifBlank { PlatformConfig.getCommunityCloudflareApiToken() }
    }
    
    /**
     * Get effective D1 Database ID (user override or platform default).
     */
    fun getEffectiveD1DatabaseId(): String {
        val userValue = cloudflareD1DatabaseId().get()
        return userValue.ifBlank { PlatformConfig.getCommunityD1DatabaseId() }
    }
    
    /**
     * Get effective R2 Bucket Name (user override or platform default).
     */
    fun getEffectiveR2BucketName(): String {
        val userValue = cloudflareR2BucketName().get()
        return userValue.ifBlank { PlatformConfig.getCommunityR2BucketName() }
    }
    
    /**
     * Get effective R2 Public URL (user override or platform default).
     */
    fun getEffectiveR2PublicUrl(): String {
        val userValue = cloudflareR2PublicUrl().get()
        return userValue.ifBlank { PlatformConfig.getCommunityR2PublicUrl() }
    }
}
