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
        const val SYNC_DEVICE_NAME = "sync_custom_device_name"
        const val SYNC_DEVICE_TYPE = "sync_custom_device_type"
        const val SYNC_SAVED_DEVICES = "sync_saved_devices_json"
        const val SYNC_TRANSFER_LIBRARY = "sync_transfer_library"
        const val SYNC_TRANSFER_PROGRESS = "sync_transfer_progress"
        const val SYNC_TRANSFER_DOWNLOADED_CHAPTERS = "sync_transfer_downloaded_chapters"
        const val SYNC_TRANSFER_SETTINGS = "sync_transfer_settings"
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

    // ========== Device Identity & Quick Share ==========

    /**
     * Custom name for this device broadcasted during Wi-Fi discovery.
     */
    fun deviceName(): Preference<String> {
        return preferenceStore.getString(SYNC_DEVICE_NAME, "")
    }

    /**
     * Custom device type override (e.g. PHONE, TABLET, DESKTOP, TV).
     */
    fun deviceType(): Preference<String> {
        return preferenceStore.getString(SYNC_DEVICE_TYPE, "")
    }

    /**
     * Serialized list of saved / trusted devices for fast reconnection ("Your Devices").
     */
    fun savedDevicesJson(): Preference<String> {
        return preferenceStore.getString(SYNC_SAVED_DEVICES, "[]")
    }

    /**
     * Whether to transfer library books and categories.
     */
    fun syncLibrary(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_TRANSFER_LIBRARY, true)
    }

    /**
     * Whether to transfer reading history and chapter progress.
     */
    fun syncReadingProgress(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_TRANSFER_PROGRESS, true)
    }

    /**
     * Whether to transfer full offline downloaded chapter content.
     */
    fun syncDownloadedChapters(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_TRANSFER_DOWNLOADED_CHAPTERS, false)
    }

    /**
     * Whether to transfer reader and application settings.
     */
    fun syncSettings(): Preference<Boolean> {
        return preferenceStore.getBoolean(SYNC_TRANSFER_SETTINGS, false)
    }

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun getSavedDevices(): List<ireader.domain.models.sync.SavedDevice> {
        val jsonStr = savedDevicesJson().get()
        if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
        return try {
            json.decodeFromString(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveDevice(device: ireader.domain.models.sync.SavedDevice) {
        val current = getSavedDevices().toMutableList()
        val index = current.indexOfFirst { it.deviceId == device.deviceId }
        if (index >= 0) {
            current[index] = device
        } else {
            current.add(0, device)
        }
        val newJson = json.encodeToString(current)
        savedDevicesJson().set(newJson)
    }

    fun removeSavedDevice(deviceId: String) {
        val current = getSavedDevices().filterNot { it.deviceId == deviceId }
        val newJson = json.encodeToString(current)
        savedDevicesJson().set(newJson)
    }
}

