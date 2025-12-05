package ireader.domain.community

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

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
        const val CONTRIBUTOR_NAME = "contributor_name"
        const val SHOW_CONTRIBUTOR_BADGE = "show_contributor_badge"
        
        // Content preferences
        const val PREFERRED_LANGUAGE = "community_preferred_language"
        const val SHOW_NSFW_CONTENT = "community_show_nsfw"
        const val MINIMUM_RATING = "community_minimum_rating"
        
        // Cache settings
        const val CACHE_DURATION_HOURS = "community_cache_duration"
        const val LAST_SYNC_TIME = "community_last_sync"
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
}
