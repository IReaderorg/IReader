package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.sync.SyncProviderType

/**
 * Unified preferences for all sync features (Google Drive, Supabase, Local Wi-Fi).
 */
class SyncPreferences(private val preferenceStore: PreferenceStore) {
    
    companion object {
        const val SELECTED_PROVIDER = "unified_sync_selected_provider"
        const val AUTO_SYNC_ON_LAUNCH = "unified_sync_auto_on_launch"
        const val AUTO_SYNC_ON_CHAPTER_FINISH = "unified_sync_auto_on_chapter_finish"
        const val SYNC_ON_WIFI_ONLY = "unified_sync_on_wifi_only"
        const val LAST_SYNC_TIMESTAMP = "unified_sync_last_timestamp"
        const val SYNC_ACT_AS_SERVER = "sync_act_as_server"
    }

    /**
     * Active sync provider
     */
    fun selectedProvider(): Preference<String> {
        return preferenceStore.getString(SELECTED_PROVIDER, SyncProviderType.NONE.name)
    }

    fun getSelectedProviderType(): SyncProviderType {
        return try {
            SyncProviderType.valueOf(selectedProvider().get())
        } catch (_: Exception) {
            SyncProviderType.NONE
        }
    }

    fun setSelectedProviderType(type: SyncProviderType) {
        selectedProvider().set(type.name)
    }

    /**
     * Whether to automatically sync when the app starts
     */
    fun autoSyncOnLaunch(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_SYNC_ON_LAUNCH, true)
    }

    /**
     * Whether to automatically sync reading progress when finishing a chapter
     */
    fun autoSyncOnChapterFinish(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_SYNC_ON_CHAPTER_FINISH, true)
    }

    /**
     * Whether to sync only when connected to Wi-Fi
     */
    fun syncOnWifiOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_ON_WIFI_ONLY, false)
    }

    /**
     * Timestamp of last successful synchronization
     */
    fun lastSyncTimestamp(): Preference<Long> {
        return preferenceStore.getLong(LAST_SYNC_TIMESTAMP, 0L)
    }

    /**
     * Manual server/client role selection for Local Wi-Fi sync.
     */
    fun actAsServer(): Preference<String> {
        return preferenceStore.getString(SYNC_ACT_AS_SERVER, "server")
    }
    
    fun isServer(): Boolean {
        return actAsServer().get() == "server"
    }
}

