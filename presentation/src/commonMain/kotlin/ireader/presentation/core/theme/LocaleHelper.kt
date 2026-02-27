package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform locale helper for managing app language settings.
 * 
 * This class provides a unified interface for:
 * - Getting available languages in the app
 * - Changing the app's display language at runtime
 * - Providing the current locale for Compose Resources
 * 
 * Each platform implements this differently:
 * - Android: Uses Configuration and Locale
 * - Desktop: Uses JVM Locale
 * - iOS: Uses NSLocale
 */
expect class LocaleHelper {
    /**
     * List of available language codes in the app.
     * These are populated from the available translations.
     */
    val languages: MutableList<String>

    /**
     * Sets the locale on app startup.
     */
    fun setLocaleLang()

    /**
     * Updates the locale when user changes language preference.
     * This triggers a recomposition with the new locale.
     */
    fun updateLocal()

    /**
     * Resets the locale to system default.
     */
    fun resetLocale()

    /**
     * Populates the languages list with available locales.
     */
    fun getLocales()

    /**
     * Returns the currently selected language code.
     */
    fun getCurrentLanguageCode(): String

    /**
     * Returns a StateFlow of the current language code for reactive updates.
     */
    val currentLanguageFlow: StateFlow<String>
}

/**
 * Available app locales - these match the values-* folders in i18n/composeResources
 */
object AppLocales {
    val AVAILABLE_LOCALES = listOf(
        "en",      // English (default)
        "ar",      // Arabic
        "bn",      // Bengali
        "de",      // German
        "es",      // Spanish
        "fa",      // Persian/Farsi
        "fi",      // Finnish
        "fil",     // Filipino
        "fr",      // French
        "hi",      // Hindi
        "hr",      // Croatian
        "hu",      // Hungarian
        "in",      // Indonesian
        "it",      // Italian
        "ja",      // Japanese
        "ko",      // Korean
        "ml",      // Malayalam
        "my",      // Burmese
        "pl",      // Polish
        "pt",      // Portuguese
        "ro",      // Romanian
        "ru",      // Russian
        "si",      // Sinhala
        "sk",      // Slovak
        "th",      // Thai
        "tr",      // Turkish
        "uk",      // Ukrainian
        "uz",      // Uzbek
        "vi",      // Vietnamese
        "zh-CN",   // Chinese Simplified
        "zh-TW",   // Chinese Traditional
    )

    /**
     * Maps language codes to their native display names.
     */
    val LANGUAGE_NAMES = mapOf(
        "en" to "English",
        "ar" to "العربية",
        "bn" to "বাংলা",
        "de" to "Deutsch",
        "es" to "Español",
        "fa" to "فارسی",
        "fi" to "Suomi",
        "fil" to "Filipino",
        "fr" to "Français",
        "hi" to "हिन्दी",
        "hr" to "Hrvatski",
        "hu" to "Magyar",
        "in" to "Indonesia",
        "it" to "Italiano",
        "ja" to "日本語",
        "ko" to "한국어",
        "ml" to "മലയാളം",
        "my" to "မြန်မာဘာသာ",
        "pl" to "Polski",
        "pt" to "Português",
        "ro" to "Română",
        "ru" to "Русский",
        "si" to "සිංහල",
        "sk" to "Slovenčina",
        "th" to "ไทย",
        "tr" to "Türkçe",
        "uk" to "Українська",
        "uz" to "Oʻzbekcha",
        "vi" to "Tiếng Việt",
        "zh-CN" to "简体中文",
        "zh-TW" to "繁體中文",
    )

    fun getDisplayName(code: String): String {
        return LANGUAGE_NAMES[code] ?: code.uppercase()
    }
}
