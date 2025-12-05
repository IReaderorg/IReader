package ireader.presentation.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of AppLocaleProvider.
 * 
 * Sets the Apple language preference which affects how Compose Resources
 * loads localized strings. Note that some iOS locale changes may require
 * an app restart to take full effect.
 * 
 * The key() composable ensures recomposition when language changes,
 * which triggers Compose Resources to reload strings in the new locale.
 */
@Composable
actual fun AppLocaleProvider(
    localeHelper: LocaleHelper,
    content: @Composable () -> Unit
) {
    val currentLanguage by localeHelper.currentLanguageFlow.collectAsState()
    
    // Apply locale change when language preference changes
    LaunchedEffect(currentLanguage) {
        if (currentLanguage.isNotEmpty()) {
            setAppleLanguage(currentLanguage)
        }
    }
    
    // Use key to force recomposition when language changes
    // This ensures Compose Resources reload strings in the new locale
    key(currentLanguage) {
        content()
    }
}

/**
 * Sets the Apple language preference.
 * Converts language codes to iOS format (e.g., zh-CN -> zh-Hans).
 */
private fun setAppleLanguage(languageCode: String) {
    val iosLocale = when (languageCode) {
        "zh-CN" -> "zh-Hans"
        "zh-TW" -> "zh-Hant"
        else -> {
            val (language, region) = parseLanguageCode(languageCode)
            if (region.isNotEmpty()) "${language}_$region" else language
        }
    }
    NSUserDefaults.standardUserDefaults.setObject(listOf(iosLocale), "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}
