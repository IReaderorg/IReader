package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Privacy preferences for IReader
 * Based on Mihon's PrivacyPreferences pattern
 * Requirements: 11.4, 11.5
 */
class PrivacyPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val CRASH_REPORT = "acra.enable"
        const val ANALYTICS_ENABLED = "analytics_enabled"
        const val TELEMETRY_ENABLED = "telemetry_enabled"
        const val HIDE_NOTIFICATION_CONTENT = "hide_notification_content"
        const val CLEAR_HISTORY_ON_EXIT = "clear_history_on_exit"
        const val ANONYMOUS_USAGE_DATA = "anonymous_usage_data"
    }

    /**
     * Enable crash reporting
     */
    fun crashReport(): Preference<Boolean> {
        return preferenceStore.getBoolean(CRASH_REPORT, true)
    }

    /**
     * Enable analytics collection
     */
    fun analyticsEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(ANALYTICS_ENABLED, false)
    }

    /**
     * Enable telemetry data collection
     */
    fun telemetryEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(TELEMETRY_ENABLED, false)
    }

    /**
     * Hide notification content for privacy
     */
    fun hideNotificationContent(): Preference<Boolean> {
        return preferenceStore.getBoolean(HIDE_NOTIFICATION_CONTENT, false)
    }

    /**
     * Clear reading history when exiting the app
     */
    fun clearHistoryOnExit(): Preference<Boolean> {
        return preferenceStore.getBoolean(CLEAR_HISTORY_ON_EXIT, false)
    }

    /**
     * Allow anonymous usage data collection
     */
    fun anonymousUsageData(): Preference<Boolean> {
        return preferenceStore.getBoolean(ANONYMOUS_USAGE_DATA, false)
    }
}
