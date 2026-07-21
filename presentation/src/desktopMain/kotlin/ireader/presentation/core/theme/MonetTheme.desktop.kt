package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Desktop doesn't support Material You dynamic colors.
 * Always returns null to fall back to default theme.
 */
@Composable
actual fun getMonetColorScheme(isDark: Boolean): ColorScheme? {
    return null
}
