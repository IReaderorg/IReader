package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

class DownloadPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val DOWNLOAD_DELAY_MS = "download_delay_ms"
        const val CONCURRENT_DOWNLOADS_LIMIT = "concurrent_downloads_limit"
    }

    /**
     * Delay between chapter downloads in milliseconds
     * Default: 1000ms (1 second)
     */
    fun downloadDelayMs(): Preference<Long> {
        return preferenceStore.getLong(DOWNLOAD_DELAY_MS, 1000L)
    }
}
