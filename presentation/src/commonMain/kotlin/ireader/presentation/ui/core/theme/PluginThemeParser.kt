package ireader.presentation.ui.core.theme

import ireader.domain.plugins.ThemeColorScheme
import ireader.domain.plugins.ThemeExtraColors
import ireader.domain.plugins.ThemeTypography
import ireader.domain.plugins.ThemeBackgrounds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Parser for plugin theme definitions
 * Requirements: 9.1, 9.2, 9.3
 */
object PluginThemeParser {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Parse theme JSON from plugin metadata
     * @param jsonString The JSON string containing theme definition
     * @return Result containing ThemeDefinition or error
     */
    fun parseThemeJson(jsonString: String): Result<ThemeDefinition> {
        return try {
            val definition = json.decodeFromString<ThemeDefinition>(jsonString)
            validateThemeDefinition(definition)
        } catch (e: Exception) {
            Result.failure(ThemeParseException("Failed to parse theme JSON: ${e.message}", e))
        }
    }
    
    /**
     * Validate theme definition structure
     * Requirements: 9.2
     */
    private fun validateThemeDefinition(definition: ThemeDefinition): Result<ThemeDefinition> {
        return try {
            // Validate color scheme
            validateColorScheme(definition.lightColorScheme, "light")
            validateColorScheme(definition.darkColorScheme, "dark")
            
            // Validate extra colors
            validateExtraColors(definition.lightExtraColors, "light")
            validateExtraColors(definition.darkExtraColors, "dark")
            
            // Typography is optional, but validate if present
            definition.typography?.let { validateTypography(it) }
            
            Result.success(definition)
        } catch (e: Exception) {
            Result.failure(ThemeValidationException("Theme validation failed: ${e.message}", e))
        }
    }
    
    /**
     * Validate color scheme has all required colors
     */
    private fun validateColorScheme(colorScheme: ThemeColorScheme, mode: String) {
        // All colors are required in the data class, so just verify they're valid
        // Color values should be valid ARGB integers
        val colors = listOf(
            colorScheme.primary, colorScheme.onPrimary,
            colorScheme.primaryContainer, colorScheme.onPrimaryContainer,
            colorScheme.secondary, colorScheme.onSecondary,
            colorScheme.secondaryContainer, colorScheme.onSecondaryContainer,
            colorScheme.tertiary, colorScheme.onTertiary,
            colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer,
            colorScheme.error, colorScheme.onError,
            colorScheme.errorContainer, colorScheme.onErrorContainer,
            colorScheme.background, colorScheme.onBackground,
            colorScheme.surface, colorScheme.onSurface,
            colorScheme.surfaceVariant, colorScheme.onSurfaceVariant,
            colorScheme.outline, colorScheme.outlineVariant,
            colorScheme.scrim, colorScheme.inverseSurface,
            colorScheme.inverseOnSurface, colorScheme.inversePrimary
        )
        
        // Verify all colors are within valid range
        colors.forEach { color ->
            if (color < 0) {
                throw IllegalArgumentException("Invalid color value in $mode color scheme: $color")
            }
        }
    }
    
    /**
     * Validate extra colors
     */
    private fun validateExtraColors(extraColors: ThemeExtraColors, mode: String) {
        if (extraColors.bars < 0 || extraColors.onBars < 0) {
            throw IllegalArgumentException("Invalid extra color value in $mode extra colors")
        }
    }
    
    /**
     * Validate typography settings
     */
    private fun validateTypography(typography: ThemeTypography) {
        // Validate font sizes if present
        val fontSizes = listOf(
            typography.displayLargeFontSize,
            typography.displayMediumFontSize,
            typography.displaySmallFontSize,
            typography.headlineLargeFontSize,
            typography.headlineMediumFontSize,
            typography.headlineSmallFontSize,
            typography.titleLargeFontSize,
            typography.titleMediumFontSize,
            typography.titleSmallFontSize,
            typography.bodyLargeFontSize,
            typography.bodyMediumFontSize,
            typography.bodySmallFontSize,
            typography.labelLargeFontSize,
            typography.labelMediumFontSize,
            typography.labelSmallFontSize
        )
        
        fontSizes.filterNotNull().forEach { size ->
            if (size <= 0 || size > 200) {
                throw IllegalArgumentException("Invalid font size: $size (must be between 0 and 200)")
            }
        }
    }
}

/**
 * Complete theme definition from plugin
 * Requirements: 9.1, 9.2, 9.3
 */
@Serializable
data class ThemeDefinition(
    val lightColorScheme: ThemeColorScheme,
    val darkColorScheme: ThemeColorScheme,
    val lightExtraColors: ThemeExtraColors,
    val darkExtraColors: ThemeExtraColors,
    val typography: ThemeTypography? = null,
    val backgrounds: ThemeBackgrounds? = null
)

/**
 * Exception thrown when theme parsing fails
 */
class ThemeParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when theme validation fails
 */
class ThemeValidationException(message: String, cause: Throwable? = null) : Exception(message, cause)
