package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

class DownloadPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val DOWNLOAD_DELAY_MS = "download_delay_ms"
        const val CONCURRENT_DOWNLOADS_LIMIT = "concurrent_downloads_limit"
        const val DOWNLOAD_ONLY_ON_WIFI = "download_only_on_wifi"
        const val PARALLEL_DOWNLOADS_PER_SOURCE = "parallel_downloads_per_source"
        const val MINIMUM_DISK_SPACE_MB = "minimum_disk_space_mb"
        const val SAVE_CHAPTERS_AS_CBZ = "save_chapters_as_cbz"
        const val AUTO_RETRY_FAILED = "auto_retry_failed"
        const val MAX_RETRY_COUNT = "max_retry_count"
        
        // Default values
        const val DEFAULT_DOWNLOAD_DELAY_MS = 1000L
        const val DEFAULT_PARALLEL_DOWNLOADS = 3
        const val DEFAULT_MINIMUM_DISK_SPACE_MB = 200L // 200 MB
        const val DEFAULT_MAX_RETRY_COUNT = 3
    }

    /**
     * Delay between chapter downloads in milliseconds.
     * Default: 1000ms (1 second)
     */
    fun downloadDelayMs(): Preference<Long> {
        return preferenceStore.getLong(DOWNLOAD_DELAY_MS, DEFAULT_DOWNLOAD_DELAY_MS)
    }
    
    /**
     * Only download when connected to WiFi.
     * When enabled, downloads will pause on mobile data.
     * Default: false
     */
    fun downloadOnlyOnWifi(): Preference<Boolean> {
        return preferenceStore.getBoolean(DOWNLOAD_ONLY_ON_WIFI, false)
    }
    
    /**
     * Number of parallel downloads allowed per source.
     * Higher values may trigger rate limiting on some sources.
     * Default: 3
     */
    fun parallelDownloadsPerSource(): Preference<Int> {
        return preferenceStore.getInt(PARALLEL_DOWNLOADS_PER_SOURCE, DEFAULT_PARALLEL_DOWNLOADS)
    }
    
    /**
     * Minimum free disk space required to continue downloads (in MB).
     * Downloads will pause if available space falls below this threshold.
     * Default: 200 MB
     */
    fun minimumDiskSpaceMb(): Preference<Long> {
        return preferenceStore.getLong(MINIMUM_DISK_SPACE_MB, DEFAULT_MINIMUM_DISK_SPACE_MB)
    }
    
    /**
     * Save downloaded chapters as CBZ archives.
     * When disabled, chapters are saved as plain text files.
     * Default: false (plain text)
     */
    fun saveChaptersAsCbz(): Preference<Boolean> {
        return preferenceStore.getBoolean(SAVE_CHAPTERS_AS_CBZ, false)
    }
    
    /**
     * Automatically retry failed downloads.
     * When enabled, failed downloads will be retried with exponential backoff.
     * Default: true
     */
    fun autoRetryFailed(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_RETRY_FAILED, true)
    }
    
    /**
     * Maximum number of retry attempts for failed downloads.
     * Uses exponential backoff: 2s, 4s, 8s delays.
     * Default: 3
     */
    fun maxRetryCount(): Preference<Int> {
        return preferenceStore.getInt(MAX_RETRY_COUNT, DEFAULT_MAX_RETRY_COUNT)
    }
}
