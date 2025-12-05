package ireader.presentation.core.theme

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.core.os.LocaleListCompat
import org.koin.compose.koinInject

/**
 * Android implementation of AppLocaleProvider.
 * 
 * Uses AppCompatDelegate.setApplicationLocales() for per-app language support
 * on Android 13+ and falls back to configuration changes on older versions.
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
            applyLocale(currentLanguage, localeHelper.context)
        }
    }
    
    // Use key to force recomposition when language changes
    // This ensures Compose Resources reload strings in the new locale
    key(currentLanguage) {
        content()
    }
}

/**
 * Applies the locale using the appropriate method for the Android version.
 */
private fun applyLocale(languageCode: String, context: Context) {
    val (language, region) = parseLanguageCode(languageCode)
    val localeTag = if (region.isNotEmpty()) "$language-$region" else language
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ - Use per-app language API
        try {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList.forLanguageTags(localeTag)
        } catch (e: Exception) {
            // Fallback to AppCompat method
            applyLocaleCompat(localeTag)
        }
    } else {
        // Android 12 and below - Use AppCompat
        applyLocaleCompat(localeTag)
    }
}

/**
 * Applies locale using AppCompatDelegate for backward compatibility.
 */
private fun applyLocaleCompat(localeTag: String) {
    val localeList = LocaleListCompat.forLanguageTags(localeTag)
    AppCompatDelegate.setApplicationLocales(localeList)
}
