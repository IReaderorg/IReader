package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Plugin interface for custom themes
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
interface ThemePlugin : Plugin {
    /**
     * Get the color scheme for the theme
     * @param isDark Whether to return dark or light theme colors
     */
    fun getColorScheme(isDark: Boolean): ThemeColorScheme
    
    /**
     * Get extra colors not included in standard color scheme
     * @param isDark Whether to return dark or light theme colors
     */
    fun getExtraColors(isDark: Boolean): ThemeExtraColors
    
    /**
     * Get custom typography settings (optional)
     * @return Typography settings or null to use default
     */
    fun getTypography(): ThemeTypography?
    
    /**
     * Get background assets for the theme (optional)
     * @return Background assets or null for no custom backgrounds
     */
    fun getBackgroundAssets(): ThemeBackgrounds?
}

/**
 * Color scheme for theme plugins
 * Simplified representation of Material3 ColorScheme
 */
@Serializable
data class ThemeColorScheme(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val error: Long,
    val onError: Long,
    val errorContainer: Long,
    val onErrorContainer: Long,
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val outline: Long,
    val outlineVariant: Long,
    val scrim: Long,
    val inverseSurface: Long,
    val inverseOnSurface: Long,
    val inversePrimary: Long
)

/**
 * Extra colors for theme plugins
 */
@Serializable
data class ThemeExtraColors(
    val bars: Long,
    val onBars: Long,
    val isBarLight: Boolean
)

/**
 * Typography settings for theme plugins
 */
@Serializable
data class ThemeTypography(
    val fontFamily: String? = null,
    val displayLargeFontSize: Float? = null,
    val displayMediumFontSize: Float? = null,
    val displaySmallFontSize: Float? = null,
    val headlineLargeFontSize: Float? = null,
    val headlineMediumFontSize: Float? = null,
    val headlineSmallFontSize: Float? = null,
    val titleLargeFontSize: Float? = null,
    val titleMediumFontSize: Float? = null,
    val titleSmallFontSize: Float? = null,
    val bodyLargeFontSize: Float? = null,
    val bodyMediumFontSize: Float? = null,
    val bodySmallFontSize: Float? = null,
    val labelLargeFontSize: Float? = null,
    val labelMediumFontSize: Float? = null,
    val labelSmallFontSize: Float? = null
)

/**
 * Background assets for theme plugins
 */
@Serializable
data class ThemeBackgrounds(
    val readerBackground: String? = null,
    val appBackground: String? = null
)
