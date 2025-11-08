package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme

/**
 * Interface for platform-specific dynamic color scheme support
 */
interface DynamicColorScheme {
    /**
     * Check if dynamic colors are supported on this platform
     */
    fun isSupported(): Boolean

    /**
     * Get dynamic light color scheme if available
     */
    fun lightColorScheme(): ColorScheme?

    /**
     * Get dynamic dark color scheme if available
     */
    fun darkColorScheme(): ColorScheme?
}
