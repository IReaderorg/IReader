package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Platform-specific Material You dynamic color scheme.
 * Returns null on platforms that don't support dynamic colors.
 */
@Composable
expect fun getMonetColorScheme(isDark: Boolean): ColorScheme?
