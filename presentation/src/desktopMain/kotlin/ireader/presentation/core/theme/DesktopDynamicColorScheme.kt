package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme

/**
 * Desktop implementation of dynamic color scheme
 * Dynamic colors are not supported on desktop
 */
class DesktopDynamicColorScheme : DynamicColorScheme {
    
    override fun isSupported(): Boolean = false

    override fun lightColorScheme(): ColorScheme? = null

    override fun darkColorScheme(): ColorScheme? = null
}
