package ireader.presentation.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Utility functions for theme color calculations.
 * Ensures consistent "on" color generation across the app.
 */
object ThemeColorUtils {
    
    /**
     * Determines the appropriate "on" color (text/icon color) for a given background color.
     * Uses WCAG contrast guidelines for accessibility.
     */
    fun getOnColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5) {
            Color.Black.copy(alpha = 0.87f) // High emphasis on light backgrounds
        } else {
            Color.White.copy(alpha = 0.87f) // High emphasis on dark backgrounds
        }
    }
    
    /**
     * Determines if a color should be considered "light" based on its luminance.
     */
    fun isLight(color: Color): Boolean = color.luminance() > 0.5
    
    /**
     * Applies true black (AMOLED) mode to a color scheme.
     */
    fun applyTrueBlack(colorScheme: ColorScheme): ColorScheme {
        return colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF0A0A0A),
            surfaceContainer = Color(0xFF0A0A0A),
            surfaceContainerHigh = Color(0xFF121212),
            surfaceContainerHighest = Color(0xFF1A1A1A),
            surfaceContainerLow = Color(0xFF050505),
            surfaceContainerLowest = Color.Black,
        )
    }
    
    /**
     * Ensures all "on" colors in a ColorScheme have proper contrast with their backgrounds.
     */
    fun ensureProperOnColors(colorScheme: ColorScheme): ColorScheme {
        return colorScheme.copy(
            onPrimary = getOnColor(colorScheme.primary),
            onPrimaryContainer = getOnColor(colorScheme.primaryContainer),
            onSecondary = getOnColor(colorScheme.secondary),
            onSecondaryContainer = getOnColor(colorScheme.secondaryContainer),
            onTertiary = getOnColor(colorScheme.tertiary),
            onTertiaryContainer = getOnColor(colorScheme.tertiaryContainer),
            onBackground = getOnColor(colorScheme.background),
            onSurface = getOnColor(colorScheme.surface),
            onSurfaceVariant = getOnColor(colorScheme.surfaceVariant),
            onError = getOnColor(colorScheme.error),
            onErrorContainer = getOnColor(colorScheme.errorContainer),
        )
    }
    
    /**
     * Applies custom primary and secondary colors while maintaining proper "on" colors.
     */
    fun applyCustomColors(
        baseScheme: ColorScheme,
        customPrimary: Color?,
        customSecondary: Color?,
    ): ColorScheme {
        val primary = customPrimary ?: baseScheme.primary
        val secondary = customSecondary ?: baseScheme.secondary
        
        return baseScheme.copy(
            primary = primary,
            onPrimary = getOnColor(primary),
            primaryContainer = primary,
            onPrimaryContainer = getOnColor(primary),
            secondary = secondary,
            onSecondary = getOnColor(secondary),
            secondaryContainer = secondary,
            onSecondaryContainer = getOnColor(secondary),
        )
    }
}
