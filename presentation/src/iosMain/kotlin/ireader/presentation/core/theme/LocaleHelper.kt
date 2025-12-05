package ireader.presentation.core.theme

import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

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
        setAppleLanguage(lang)
        _currentLanguageFlow.value = lang
    }

    actual fun updateLocal() {
        val lang = language.get().ifEmpty { "en" }
        setAppleLanguage(lang)
        _currentLanguageFlow.value = lang
    }

    actual fun resetLocale() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey("AppleLanguages")
        _currentLanguageFlow.value = NSLocale.currentLocale.languageCode
    }

    actual fun getLocales() {
        languages.clear()
        languages.addAll(AppLocales.AVAILABLE_LOCALES)
        languages.sort()
    }

    actual fun getCurrentLanguageCode(): String {
        val lang = language.get()
        return if (lang.isEmpty()) {
            NSLocale.currentLocale.languageCode
        } else {
            lang
        }
    }

    private fun setAppleLanguage(languageCode: String) {
        val iosLocale = when {
            languageCode == "zh-CN" -> "zh-Hans"
            languageCode == "zh-TW" -> "zh-Hant"
            languageCode.contains("-") -> languageCode.replace("-", "_")
            else -> languageCode
        }
        NSUserDefaults.standardUserDefaults.setObject(listOf(iosLocale), "AppleLanguages")
    }
}
