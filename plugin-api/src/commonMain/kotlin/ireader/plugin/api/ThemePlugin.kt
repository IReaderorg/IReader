package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for custom themes.
 * Theme plugins provide custom color schemes, typography, and backgrounds.
 * 
 * Example:
 * ```kotlin
 * class OceanThemePlugin : ThemePlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.ocean-theme",
 *         name = "Ocean Theme",
 *         type = PluginType.THEME,
 *         // ... other manifest fields
 *     )
 *     
 *     override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
 *         return if (isDark) darkOceanColors else lightOceanColors
 *     }
 *     
 *     // ... other implementations
 * }
 * ```
 */
interface ThemePlugin : Plugin {
    /**
     * Get the color scheme for the theme.
     * 
     * @param isDark Whether to return dark or light theme colors
     * @return Color scheme for the theme
     */
    fun getColorScheme(isDark: Boolean): ThemeColorScheme
    
    /**
     * Get extra colors not included in standard color scheme.
     * 
     * @param isDark Whether to return dark or light theme colors
     * @return Extra colors for the theme
     */
    fun getExtraColors(isDark: Boolean): ThemeExtraColors
    
    /**
     * Get custom typography settings (optional).
     * 
     * @return Typography settings or null to use default
     */
    fun getTypography(): ThemeTypography? = null
    
    /**
     * Get background assets for the theme (optional).
     * 
     * @return Background assets or null for no custom backgrounds
     */
    fun getBackgroundAssets(): ThemeBackgrounds? = null
}

/**
 * Color scheme for theme plugins.
 * Based on Material Design 3 color system.
 * Colors are represented as ARGB Long values (e.g., 0xFF6200EE).
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
 * Extra colors for theme plugins.
 * Used for app bars and other custom UI elements.
 */
@Serializable
data class ThemeExtraColors(
    /** Color for app bars */
    val bars: Long,
    /** Color for content on app bars */
    val onBars: Long,
    /** Whether the bar uses light content (for status bar icons) */
    val isBarLight: Boolean
)

/**
 * Typography settings for theme plugins.
 * All values are optional - null values use system defaults.
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
 * Background assets for theme plugins.
 * URLs to background images for reader and app.
 */
@Serializable
data class ThemeBackgrounds(
    /** URL to reader background image */
    val readerBackground: String? = null,
    /** URL to app background image */
    val appBackground: String? = null
)
