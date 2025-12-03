package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme

/**
 * iOS implementation of dynamic color scheme
 * Dynamic colors (Material You) are not supported on iOS
 */
class IosDynamicColorScheme : DynamicColorScheme {
    
    override fun isSupported(): Boolean = false

    override fun lightColorScheme(): ColorScheme? = null

    override fun darkColorScheme(): ColorScheme? = null
}
