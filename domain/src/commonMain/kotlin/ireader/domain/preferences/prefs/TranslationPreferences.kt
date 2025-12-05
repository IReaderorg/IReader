package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Preferences for mass translation feature
 */
class TranslationPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val BYPASS_TRANSLATION_WARNING = "bypass_translation_warning"
        const val TRANSLATION_RATE_LIMIT_DELAY_MS = "translation_rate_limit_delay_ms"
        const val TRANSLATION_WARNING_THRESHOLD = "translation_warning_threshold"
        const val AUTO_DOWNLOAD_BEFORE_TRANSLATE = "auto_download_before_translate"
    }

    /**
     * Bypass the rate limit warning for mass translation.
     * When enabled, allows translating large numbers of chapters without confirmation.
     * WARNING: This may result in IP blocks or API credit exhaustion.
     * Default: false
     */
    fun bypassTranslationWarning(): Preference<Boolean> {
        return preferenceStore.getBoolean(BYPASS_TRANSLATION_WARNING, false)
    }

    /**
     * Delay between translation requests in milliseconds.
     * Used to prevent rate limiting from web-based AI services.
     * Default: 3000ms (3 seconds)
     */
    fun translationRateLimitDelayMs(): Preference<Long> {
        return preferenceStore.getLong(TRANSLATION_RATE_LIMIT_DELAY_MS, 3000L)
    }

    /**
     * Number of chapters that triggers the rate limit warning.
     * Default: 10 chapters
     */
    fun translationWarningThreshold(): Preference<Int> {
        return preferenceStore.getInt(TRANSLATION_WARNING_THRESHOLD, 10)
    }

    /**
     * Automatically download chapter content before translating if not already downloaded.
     * Default: true
     */
    fun autoDownloadBeforeTranslate(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_DOWNLOAD_BEFORE_TRANSLATE, true)
    }
}
