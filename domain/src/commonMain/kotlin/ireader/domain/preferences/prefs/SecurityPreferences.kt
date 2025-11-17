package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.getEnum

/**
 * Security and privacy preferences for IReader
 * Based on Mihon's SecurityPreferences pattern
 * Requirements: 11.2, 11.4, 11.5
 */
class SecurityPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val USE_AUTHENTICATOR = "use_biometric_lock"
        const val LOCK_APP_AFTER = "lock_app_after"
        const val SECURE_SCREEN = "secure_screen_v2"
        const val HIDE_NOTIFICATION_CONTENT = "hide_notification_content"
        const val LAST_APP_CLOSED = "last_app_closed"
        const val INCOGNITO_MODE = "incognito_mode"
        const val SECURE_SCREEN_ALWAYS = "secure_screen_always"
    }

    /**
     * Enable biometric authentication for app lock
     */
    fun useAuthenticator(): Preference<Boolean> {
        return preferenceStore.getBoolean(USE_AUTHENTICATOR, false)
    }

    /**
     * Time in minutes after which the app should lock
     * 0 = immediately, -1 = never
     */
    fun lockAppAfter(): Preference<Int> {
        return preferenceStore.getInt(LOCK_APP_AFTER, 0)
    }

    /**
     * Secure screen mode to prevent screenshots and screen recording
     */
    fun secureScreen(): Preference<SecureScreenMode> {
        return preferenceStore.getEnum(SECURE_SCREEN, SecureScreenMode.INCOGNITO)
    }

    /**
     * Hide notification content for privacy
     */
    fun hideNotificationContent(): Preference<Boolean> {
        return preferenceStore.getBoolean(HIDE_NOTIFICATION_CONTENT, false)
    }

    /**
     * Timestamp when the app was last closed (for timed lock)
     */
    fun lastAppClosed(): Preference<Long> {
        return preferenceStore.getLong(LAST_APP_CLOSED, 0L)
    }

    /**
     * Enable incognito mode (no history tracking)
     */
    fun incognitoMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(INCOGNITO_MODE, false)
    }

    /**
     * Always enable secure screen regardless of mode
     */
    fun secureScreenAlways(): Preference<Boolean> {
        return preferenceStore.getBoolean(SECURE_SCREEN_ALWAYS, false)
    }
}

/**
 * Secure screen mode options
 */
enum class SecureScreenMode {
    /**
     * Always enable secure screen
     */
    ALWAYS,

    /**
     * Enable secure screen only in incognito mode
     */
    INCOGNITO,

    /**
     * Never enable secure screen
     */
    NEVER
}
