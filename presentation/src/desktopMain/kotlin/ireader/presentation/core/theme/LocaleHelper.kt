package ireader.presentation.core.theme

import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

actual class LocaleHelper(
    private val uiPreferences: UiPreferences
) {
    private val language = uiPreferences.language()

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
        _currentLanguageFlow.value = lang
    }

    actual fun updateLocal() {
        val lang = language.get().ifEmpty { "en" }
        val locale = parseLocale(lang)
        Locale.setDefault(locale)
        _currentLanguageFlow.value = lang
    }

    actual fun resetLocale() {
        Locale.setDefault(Locale.getDefault())
        _currentLanguageFlow.value = Locale.getDefault().language
    }

    actual fun getLocales() {
        languages.clear()
        languages.addAll(AppLocales.AVAILABLE_LOCALES)
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
     */
    private fun parseLocale(languageCode: String): Locale {
        return when {
            languageCode.contains("-") -> {
                val parts = languageCode.split("-")
                Locale(parts[0], parts.getOrElse(1) { "" })
            }
            languageCode.contains("_") -> {
                val parts = languageCode.split("_")
                Locale(parts[0], parts.getOrElse(1) { "" })
            }
            else -> {
                Locale(languageCode)
            }
        }
    }
}
