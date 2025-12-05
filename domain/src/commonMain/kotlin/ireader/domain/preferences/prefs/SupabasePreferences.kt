package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Preferences for Supabase configuration
 */
class SupabasePreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object {
        // Global Supabase enable/disable toggle
        const val SUPABASE_ENABLED = "supabase_enabled"
        
        // Custom configuration toggle
        const val USE_CUSTOM_SUPABASE = "use_custom_supabase"
        
        // 7-Project configuration
        
        // Project 1 - Auth
        const val AUTH_URL = "supabase_auth_url"
        const val AUTH_API_KEY = "supabase_auth_api_key"
        
        // Project 2 - Reading
        const val READING_URL = "supabase_reading_url"
        const val READING_API_KEY = "supabase_reading_api_key"
        
        // Project 3 - Library
        const val LIBRARY_URL = "supabase_library_url"
        const val LIBRARY_API_KEY = "supabase_library_api_key"
        
        // Project 4 - Book Reviews
        const val BOOK_REVIEWS_URL = "supabase_book_reviews_url"
        const val BOOK_REVIEWS_API_KEY = "supabase_book_reviews_api_key"
        
        // Project 5 - Chapter Reviews
        const val CHAPTER_REVIEWS_URL = "supabase_chapter_reviews_url"
        const val CHAPTER_REVIEWS_API_KEY = "supabase_chapter_reviews_api_key"
        
        // Project 6 - Badges
        const val BADGES_URL = "supabase_badges_url"
        const val BADGES_API_KEY = "supabase_badges_api_key"
        
        // Project 7 - Analytics
        const val ANALYTICS_URL = "supabase_analytics_url"
        const val ANALYTICS_API_KEY = "supabase_analytics_api_key"
        
        // Project 8 - Community Source
        const val COMMUNITY_URL = "supabase_community_url"
        const val COMMUNITY_API_KEY = "supabase_community_api_key"
        
        // Sync settings
        const val AUTO_SYNC_ENABLED = "auto_sync_enabled"
        const val SYNC_ON_WIFI_ONLY = "sync_on_wifi_only"
        const val LAST_SYNC_TIME = "last_sync_time"
    }
    
    /**
     * Whether Supabase features are enabled globally.
     * 
     * When TRUE (default):
     * - All Supabase features (sync, reviews, badges, leaderboard, etc.) are enabled
     * - App makes remote requests to Supabase backend
     * 
     * When FALSE:
     * - All Supabase features are disabled
     * - No remote requests are made to Supabase
     * - App works in fully offline mode
     * - Community features (reviews, badges, leaderboard) are hidden
     */
    fun supabaseEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(SUPABASE_ENABLED, true)
    }
    
    /**
     * Whether to use custom Supabase configuration
     * 
     * When FALSE (default):
     * - App uses default Supabase config from local.properties/config.properties
     * - Perfect for GitHub releases with pre-configured backend
     * 
     * When TRUE:
     * - App uses user-provided configuration from settings
     * - Allows users to connect to their own Supabase instance
     */
    fun useCustomSupabase(): Preference<Boolean> {
        return preferenceStore.getBoolean(USE_CUSTOM_SUPABASE, false)
    }
    
    /**
     * Whether auto-sync is enabled
     */
    fun autoSyncEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_SYNC_ENABLED, true)
    }
    
    /**
     * Whether to sync only on WiFi
     */
    fun syncOnWifiOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_ON_WIFI_ONLY, true)
    }
    
    /**
     * Last sync timestamp
     */
    fun lastSyncTime(): Preference<Long> {
        return preferenceStore.getLong(LAST_SYNC_TIME, 0L)
    }
    
    // 7-Project configuration preferences
    
    // Project 1 - Auth
    fun supabaseAuthUrl(): Preference<String> {
        return preferenceStore.getString(AUTH_URL, "")
    }
    
    fun supabaseAuthKey(): Preference<String> {
        return preferenceStore.getString(AUTH_API_KEY, "")
    }
    
    // Project 2 - Reading
    fun supabaseReadingUrl(): Preference<String> {
        return preferenceStore.getString(READING_URL, "")
    }
    
    fun supabaseReadingKey(): Preference<String> {
        return preferenceStore.getString(READING_API_KEY, "")
    }
    
    // Project 3 - Library
    fun supabaseLibraryUrl(): Preference<String> {
        return preferenceStore.getString(LIBRARY_URL, "")
    }
    
    fun supabaseLibraryKey(): Preference<String> {
        return preferenceStore.getString(LIBRARY_API_KEY, "")
    }
    
    // Project 4 - Book Reviews
    fun supabaseBookReviewsUrl(): Preference<String> {
        return preferenceStore.getString(BOOK_REVIEWS_URL, "")
    }
    
    fun supabaseBookReviewsKey(): Preference<String> {
        return preferenceStore.getString(BOOK_REVIEWS_API_KEY, "")
    }
    
    // Project 5 - Chapter Reviews
    fun supabaseChapterReviewsUrl(): Preference<String> {
        return preferenceStore.getString(CHAPTER_REVIEWS_URL, "")
    }
    
    fun supabaseChapterReviewsKey(): Preference<String> {
        return preferenceStore.getString(CHAPTER_REVIEWS_API_KEY, "")
    }
    
    // Project 6 - Badges
    fun supabaseBadgesUrl(): Preference<String> {
        return preferenceStore.getString(BADGES_URL, "")
    }
    
    fun supabaseBadgesKey(): Preference<String> {
        return preferenceStore.getString(BADGES_API_KEY, "")
    }
    
    // Project 7 - Analytics
    fun supabaseAnalyticsUrl(): Preference<String> {
        return preferenceStore.getString(ANALYTICS_URL, "")
    }
    
    fun supabaseAnalyticsKey(): Preference<String> {
        return preferenceStore.getString(ANALYTICS_API_KEY, "")
    }
    
    // Project 8 - Community Source
    fun supabaseCommunityUrl(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_URL, "")
    }
    
    fun supabaseCommunityKey(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_API_KEY, "")
    }
}
