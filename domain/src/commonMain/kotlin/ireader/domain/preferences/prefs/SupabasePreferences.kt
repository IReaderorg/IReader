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
        // Primary endpoint (Users)
        const val SUPABASE_URL = "supabase_url"
        const val SUPABASE_API_KEY = "supabase_api_key"
        const val USE_CUSTOM_SUPABASE = "use_custom_supabase"
        
        // Multi-endpoint configuration
        const val USE_MULTI_ENDPOINT = "use_multi_endpoint"
        
        // Books endpoint
        const val BOOKS_URL = "supabase_books_url"
        const val BOOKS_API_KEY = "supabase_books_api_key"
        const val BOOKS_ENABLED = "supabase_books_enabled"
        
        // Progress endpoint
        const val PROGRESS_URL = "supabase_progress_url"
        const val PROGRESS_API_KEY = "supabase_progress_api_key"
        const val PROGRESS_ENABLED = "supabase_progress_enabled"
        
        // Reviews endpoint
        const val REVIEWS_URL = "supabase_reviews_url"
        const val REVIEWS_API_KEY = "supabase_reviews_api_key"
        const val REVIEWS_ENABLED = "supabase_reviews_enabled"
        
        // Community endpoint
        const val COMMUNITY_URL = "supabase_community_url"
        const val COMMUNITY_API_KEY = "supabase_community_api_key"
        const val COMMUNITY_ENABLED = "supabase_community_enabled"
        
        // Sync settings
        const val AUTO_SYNC_ENABLED = "auto_sync_enabled"
        const val SYNC_ON_WIFI_ONLY = "sync_on_wifi_only"
        const val LAST_SYNC_TIME = "last_sync_time"
        
        // Default values - loaded from local config files (not committed to git)
        // For open source: Users must configure their own Supabase instance
        const val DEFAULT_SUPABASE_URL = ""
        const val DEFAULT_SUPABASE_API_KEY = ""
    }
    
    /**
     * Custom Supabase URL
     */
    fun supabaseUrl(): Preference<String> {
        return preferenceStore.getString(SUPABASE_URL, DEFAULT_SUPABASE_URL)
    }
    
    /**
     * Custom Supabase API Key
     */
    fun supabaseApiKey(): Preference<String> {
        return preferenceStore.getString(SUPABASE_API_KEY, DEFAULT_SUPABASE_API_KEY)
    }
    
    /**
     * Whether to use custom Supabase configuration
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
    
    // Multi-endpoint preferences
    
    /**
     * Whether to use multi-endpoint configuration
     */
    fun useMultiEndpoint(): Preference<Boolean> {
        return preferenceStore.getBoolean(USE_MULTI_ENDPOINT, false)
    }
    
    // Books endpoint
    fun booksUrl(): Preference<String> {
        return preferenceStore.getString(BOOKS_URL, "")
    }
    
    fun booksApiKey(): Preference<String> {
        return preferenceStore.getString(BOOKS_API_KEY, "")
    }
    
    fun booksEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(BOOKS_ENABLED, false)
    }
    
    // Progress endpoint
    fun progressUrl(): Preference<String> {
        return preferenceStore.getString(PROGRESS_URL, "")
    }
    
    fun progressApiKey(): Preference<String> {
        return preferenceStore.getString(PROGRESS_API_KEY, "")
    }
    
    fun progressEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(PROGRESS_ENABLED, false)
    }
    
    // Reviews endpoint
    fun reviewsUrl(): Preference<String> {
        return preferenceStore.getString(REVIEWS_URL, "")
    }
    
    fun reviewsApiKey(): Preference<String> {
        return preferenceStore.getString(REVIEWS_API_KEY, "")
    }
    
    fun reviewsEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(REVIEWS_ENABLED, false)
    }
    
    // Community endpoint
    fun communityUrl(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_URL, "")
    }
    
    fun communityApiKey(): Preference<String> {
        return preferenceStore.getString(COMMUNITY_API_KEY, "")
    }
    
    fun communityEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(COMMUNITY_ENABLED, false)
    }
}
