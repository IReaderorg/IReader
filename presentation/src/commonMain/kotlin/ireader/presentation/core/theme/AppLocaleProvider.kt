package ireader.presentation.core.theme

import androidx.compose.runtime.Composable

/**
 * Provides the app locale configuration for Compose Resources.
 * 
 * This composable observes the current language setting and triggers
 * recomposition when the language changes. The actual locale change
 * is handled at the platform level.
 * 
 * @param localeHelper The LocaleHelper instance for getting current language
 * @param content The content to wrap with locale provider
 */
@Composable
expect fun AppLocaleProvider(
    localeHelper: LocaleHelper,
    content: @Composable () -> Unit
)
