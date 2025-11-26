package ireader.presentation.ui.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginType
import ireader.domain.plugins.ThemePlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manager for integrating theme plugins with the app theme system
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
class PluginThemeManager(
    private val pluginManager: PluginManager,
    private val builtInThemes: List<Theme>
) {
    /**
     * Get all available themes (built-in + plugin themes)
     * Requirements: 3.1
     */
    fun getAvailableThemes(): List<ThemeOption> {
        val builtIn = builtInThemes.map { ThemeOption.BuiltIn(it) }
        val pluginThemes = getPluginThemes()
        return builtIn + pluginThemes
    }
    
    /**
     * Get available themes as a Flow that updates when plugins change
     * Requirements: 3.1, 3.5
     */
    fun getAvailableThemesFlow(): Flow<List<ThemeOption>> {
        return pluginManager.pluginsFlow.map { _ ->
            getAvailableThemes()
        }
    }
    
    /**
     * Get only plugin-provided themes
     */
    private fun getPluginThemes(): List<ThemeOption.Plugin> {
        val themePlugins = pluginManager.getEnabledPlugins()
            .filterIsInstance<ThemePlugin>()
        
        return themePlugins.flatMap { plugin ->
            listOf(
                ThemeOption.Plugin(plugin, isDark = false),
                ThemeOption.Plugin(plugin, isDark = true)
            )
        }
    }
    
    /**
     * Apply a theme option to get a Theme object
     * Requirements: 3.2, 3.3, 9.6, 9.7
     */
    fun applyTheme(themeOption: ThemeOption): kotlin.Result<Theme> {
        return try {
            when (themeOption) {
                is ThemeOption.BuiltIn -> kotlin.Result.success(themeOption.theme)
                is ThemeOption.Plugin -> applyPluginTheme(themeOption.plugin, themeOption.isDark)
            }
        } catch (e: Exception) {
            // Fallback to default theme on error
            // Requirements: 9.7
            kotlin.Result.failure(e)
        }
    }
    
    /**
     * Apply a plugin theme and convert it to a Theme object
     * Requirements: 3.2, 3.3, 3.4, 9.2, 9.6
     */
    private fun applyPluginTheme(plugin: ThemePlugin, isDark: Boolean): kotlin.Result<Theme> {
        return try {
            // Get color scheme and extra colors from plugin
            // Requirements: 9.2
            val colorScheme = plugin.getColorScheme(isDark).toColorScheme(isDark)
            val extraColors = plugin.getExtraColors(isDark).toExtraColors()
            
            // Create a unique ID for this plugin theme
            val themeId = generatePluginThemeId(plugin.manifest.id, isDark)
            
            val theme = Theme(
                id = themeId,
                materialColors = colorScheme,
                extraColors = extraColors,
                isDark = isDark
            )
            
            kotlin.Result.success(theme)
        } catch (e: Exception) {
            // Requirements: 9.7
            kotlin.Result.failure(e)
        }
    }
    
    /**
     * Get typography from a plugin theme
     * Requirements: 3.3
     */
    fun getPluginTypography(plugin: ThemePlugin): Typography? {
        return try {
            plugin.getTypography()?.toTypography()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get background assets from a plugin theme
     * Requirements: 3.4
     */
    fun getPluginBackgrounds(plugin: ThemePlugin): Pair<String?, String?>? {
        return try {
            val backgrounds = plugin.getBackgroundAssets()
            backgrounds?.let {
                Pair(it.readerBackground, it.appBackground)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Find a theme option by ID
     */
    fun findThemeById(themeId: String): ThemeOption? {
        return getAvailableThemes().find { it.id == themeId }
    }
    
    /**
     * Get the default theme (fallback)
     */
    fun getDefaultTheme(): Theme {
        return builtInThemes.firstOrNull { !it.isDark } 
            ?: builtInThemes.first()
    }
    
    /**
     * Generate a unique ID for a plugin theme
     */
    private fun generatePluginThemeId(pluginId: String, isDark: Boolean): Long {
        // Use hash code to generate a unique long ID
        // Ensure it's positive and doesn't conflict with built-in themes (which are negative)
        val hash = "$pluginId-${if (isDark) "dark" else "light"}".hashCode()
        return hash.toLong().let { if (it < 0) -it else it } + 1000000L
    }
    
    /**
     * Pre-load custom fonts for a theme asynchronously
     * This should be called before applying a theme with custom fonts
     * Requirements: 9.6
     * 
     * @param fontPath Path to the font file
     * @param fontId Unique identifier for the font (typically the file path)
     * @param fileSystem FileSystem instance for loading the font
     */
    suspend fun preloadThemeFont(fontPath: String, fontId: String, fileSystem: ireader.core.io.FileSystem) {
        try {
            val fontFile = fileSystem.getFile(fontPath)
            if (fontFile.exists() && FontRegistry.validateFontFile(fontFile)) {
                // Create a CustomFont object for loading
                val customFont = ireader.domain.models.fonts.CustomFont(
                    id = fontId,
                    name = fontFile.name,
                    filePath = fontPath
                )
                FontRegistry.loadFontFamily(customFont, fontFile)
            }
        } catch (e: Exception) {
            // Font loading failed, will use default font
            // Requirements: 9.7
        }
    }
}

/**
 * Convert plugin ThemeColorScheme to Compose ColorScheme
 */
private fun ireader.domain.plugins.ThemeColorScheme.toColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = Color(this.primary),
            onPrimary = Color(this.onPrimary),
            primaryContainer = Color(this.primaryContainer),
            onPrimaryContainer = Color(this.onPrimaryContainer),
            secondary = Color(this.secondary),
            onSecondary = Color(this.onSecondary),
            secondaryContainer = Color(this.secondaryContainer),
            onSecondaryContainer = Color(this.onSecondaryContainer),
            tertiary = Color(this.tertiary),
            onTertiary = Color(this.onTertiary),
            tertiaryContainer = Color(this.tertiaryContainer),
            onTertiaryContainer = Color(this.onTertiaryContainer),
            error = Color(this.error),
            onError = Color(this.onError),
            errorContainer = Color(this.errorContainer),
            onErrorContainer = Color(this.onErrorContainer),
            background = Color(this.background),
            onBackground = Color(this.onBackground),
            surface = Color(this.surface),
            onSurface = Color(this.onSurface),
            surfaceVariant = Color(this.surfaceVariant),
            onSurfaceVariant = Color(this.onSurfaceVariant),
            outline = Color(this.outline),
            outlineVariant = Color(this.outlineVariant),
            scrim = Color(this.scrim),
            inverseSurface = Color(this.inverseSurface),
            inverseOnSurface = Color(this.inverseOnSurface),
            inversePrimary = Color(this.inversePrimary)
        )
    } else {
        lightColorScheme(
            primary = Color(this.primary),
            onPrimary = Color(this.onPrimary),
            primaryContainer = Color(this.primaryContainer),
            onPrimaryContainer = Color(this.onPrimaryContainer),
            secondary = Color(this.secondary),
            onSecondary = Color(this.onSecondary),
            secondaryContainer = Color(this.secondaryContainer),
            onSecondaryContainer = Color(this.onSecondaryContainer),
            tertiary = Color(this.tertiary),
            onTertiary = Color(this.onTertiary),
            tertiaryContainer = Color(this.tertiaryContainer),
            onTertiaryContainer = Color(this.onTertiaryContainer),
            error = Color(this.error),
            onError = Color(this.onError),
            errorContainer = Color(this.errorContainer),
            onErrorContainer = Color(this.onErrorContainer),
            background = Color(this.background),
            onBackground = Color(this.onBackground),
            surface = Color(this.surface),
            onSurface = Color(this.onSurface),
            surfaceVariant = Color(this.surfaceVariant),
            onSurfaceVariant = Color(this.onSurfaceVariant),
            outline = Color(this.outline),
            outlineVariant = Color(this.outlineVariant),
            scrim = Color(this.scrim),
            inverseSurface = Color(this.inverseSurface),
            inverseOnSurface = Color(this.inverseOnSurface),
            inversePrimary = Color(this.inversePrimary)
        )
    }
}

/**
 * Convert plugin ThemeExtraColors to app ExtraColors
 */
private fun ireader.domain.plugins.ThemeExtraColors.toExtraColors(): ExtraColors {
    return ExtraColors(
        bars = Color(this.bars),
        onBars = Color(this.onBars),
    )
}

/**
 * Convert plugin ThemeTypography to Compose Typography
 * Requirements: 9.3, 9.6
 * 
 * Note: Font loading is now async. This function returns Typography with default font,
 * and font loading should be handled separately via FontRegistry.loadFontFamily()
 */
private fun ireader.domain.plugins.ThemeTypography.toTypography(): Typography? {
    // Only create custom typography if at least one value is specified
    if (fontFamily == null && 
        displayLargeFontSize == null && 
        displayMediumFontSize == null &&
        displaySmallFontSize == null &&
        headlineLargeFontSize == null &&
        headlineMediumFontSize == null &&
        headlineSmallFontSize == null &&
        titleLargeFontSize == null &&
        titleMediumFontSize == null &&
        titleSmallFontSize == null &&
        bodyLargeFontSize == null &&
        bodyMediumFontSize == null &&
        bodySmallFontSize == null &&
        labelLargeFontSize == null &&
        labelMediumFontSize == null &&
        labelSmallFontSize == null) {
        return null
    }
    
    val defaultTypography = Typography()
    
    // Font loading is now async and should be handled separately
    // For now, use default font family or check if already loaded in cache
    val family = fontFamily?.let { fontFamilyPath ->
        try {
            // Try to get from cache if already loaded
            // The actual loading should be done asynchronously before calling this function
            FontRegistry.getFontFamily(fontFamilyPath) ?: FontFamily.Default
        } catch (e: Exception) {
            // Fallback to default on any error
            // Requirements: 9.7
            FontFamily.Default
        }
    }
    
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(
            fontFamily = family,
            fontSize = displayLargeFontSize?.sp ?: defaultTypography.displayLarge.fontSize
        ),
        displayMedium = defaultTypography.displayMedium.copy(
            fontFamily = family,
            fontSize = displayMediumFontSize?.sp ?: defaultTypography.displayMedium.fontSize
        ),
        displaySmall = defaultTypography.displaySmall.copy(
            fontFamily = family,
            fontSize = displaySmallFontSize?.sp ?: defaultTypography.displaySmall.fontSize
        ),
        headlineLarge = defaultTypography.headlineLarge.copy(
            fontFamily = family,
            fontSize = headlineLargeFontSize?.sp ?: defaultTypography.headlineLarge.fontSize
        ),
        headlineMedium = defaultTypography.headlineMedium.copy(
            fontFamily = family,
            fontSize = headlineMediumFontSize?.sp ?: defaultTypography.headlineMedium.fontSize
        ),
        headlineSmall = defaultTypography.headlineSmall.copy(
            fontFamily = family,
            fontSize = headlineSmallFontSize?.sp ?: defaultTypography.headlineSmall.fontSize
        ),
        titleLarge = defaultTypography.titleLarge.copy(
            fontFamily = family,
            fontSize = titleLargeFontSize?.sp ?: defaultTypography.titleLarge.fontSize
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            fontFamily = family,
            fontSize = titleMediumFontSize?.sp ?: defaultTypography.titleMedium.fontSize
        ),
        titleSmall = defaultTypography.titleSmall.copy(
            fontFamily = family,
            fontSize = titleSmallFontSize?.sp ?: defaultTypography.titleSmall.fontSize
        ),
        bodyLarge = defaultTypography.bodyLarge.copy(
            fontFamily = family,
            fontSize = bodyLargeFontSize?.sp ?: defaultTypography.bodyLarge.fontSize
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(
            fontFamily = family,
            fontSize = bodyMediumFontSize?.sp ?: defaultTypography.bodyMedium.fontSize
        ),
        bodySmall = defaultTypography.bodySmall.copy(
            fontFamily = family,
            fontSize = bodySmallFontSize?.sp ?: defaultTypography.bodySmall.fontSize
        ),
        labelLarge = defaultTypography.labelLarge.copy(
            fontFamily = family,
            fontSize = labelLargeFontSize?.sp ?: defaultTypography.labelLarge.fontSize
        ),
        labelMedium = defaultTypography.labelMedium.copy(
            fontFamily = family,
            fontSize = labelMediumFontSize?.sp ?: defaultTypography.labelMedium.fontSize
        ),
        labelSmall = defaultTypography.labelSmall.copy(
            fontFamily = family,
            fontSize = labelSmallFontSize?.sp ?: defaultTypography.labelSmall.fontSize
        )
    )
}
