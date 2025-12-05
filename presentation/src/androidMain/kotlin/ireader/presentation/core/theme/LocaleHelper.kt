package ireader.presentation.core.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import ireader.core.prefs.Preference
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

actual class LocaleHelper(
    val context: Context,
    val uiPreferences: UiPreferences
) {
    private val language: Preference<String> = uiPreferences.language()

    actual val languages: MutableList<String> = mutableListOf()

    private val _currentLanguageFlow = MutableStateFlow(language.get().ifEmpty { "en" })
    actual val currentLanguageFlow: StateFlow<String> = _currentLanguageFlow.asStateFlow()

    init {
        getLocales()
    }

    actual fun setLocaleLang() {
        val lang = language.get().ifEmpty { return }
        val locale = parseLocale(lang)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        _currentLanguageFlow.value = lang
    }

    actual fun updateLocal() {
        val lang = language.get().ifEmpty { "en" }
        context.resources.apply {
            val locale = parseLocale(lang)
            val config = Configuration(configuration)

            context.createConfigurationContext(configuration)
            Locale.setDefault(locale)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, displayMetrics)
        }
        _currentLanguageFlow.value = lang
    }

    actual fun resetLocale() {
        context.resources.apply {
            val config = Configuration(configuration)
            @Suppress("DEPRECATION")
            val default = this.configuration.locale
            config.setLocale(default)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, displayMetrics)
        }
        _currentLanguageFlow.value = Locale.getDefault().language
    }

    actual fun getLocales() {
        // Use the predefined list of available locales
        languages.clear()
        languages.addAll(AppLocales.AVAILABLE_LOCALES)
        
        // Also try to get from assets if available (for additional locales)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val assetLocales = context.resources.assets.locales
                for (locale in assetLocales) {
                    if (locale.isNotEmpty() && !languages.contains(locale)) {
                        // Normalize locale code
                        val normalized = normalizeLocaleCode(locale)
                        if (!languages.contains(normalized)) {
                            languages.add(normalized)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore errors, use predefined list
            }
        }
        languages.sort()
    }

    actual fun getCurrentLanguageCode(): String {
        val lang = language.get()
        return if (lang.isEmpty()) {
            Locale.getDefault().language
        } else {
            lang
        }
    }

    /**
     * Parses a language code into a Locale object.
     * Handles formats like "zh-CN", "zh_CN", "zh-rCN", etc.
     */
    private fun parseLocale(languageCode: String): Locale {
        return when {
            languageCode.contains("-r") -> {
                // Format: zh-rCN
                val parts = languageCode.split("-r")
                Locale(parts[0], parts.getOrElse(1) { "" })
            }
            languageCode.contains("-") -> {
                // Format: zh-CN
                val parts = languageCode.split("-")
                Locale(parts[0], parts.getOrElse(1) { "" })
            }
            languageCode.contains("_") -> {
                // Format: zh_CN
                val parts = languageCode.split("_")
                Locale(parts[0], parts.getOrElse(1) { "" })
            }
            else -> {
                Locale(languageCode)
            }
        }
    }

    /**
     * Normalizes locale codes to a consistent format.
     */
    private fun normalizeLocaleCode(code: String): String {
        return code.replace("_", "-").replace("-r", "-")
    }
}
