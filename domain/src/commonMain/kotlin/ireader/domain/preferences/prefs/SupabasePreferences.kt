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
        const val SUPABASE_URL = "supabase_url"
        const val SUPABASE_API_KEY = "supabase_api_key"
        const val USE_CUSTOM_SUPABASE = "use_custom_supabase"
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
}
