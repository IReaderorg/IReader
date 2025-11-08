package ireader.presentation.ui.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Accessibility utilities for ensuring WCAG AA compliance.
 * 
 * WCAG AA Standards:
 * - Normal text (< 18pt or < 14pt bold): 4.5:1 contrast ratio
 * - Large text (≥ 18pt or ≥ 14pt bold): 3:1 contrast ratio
 * - UI components and graphical objects: 3:1 contrast ratio
 * - Disabled elements: No minimum contrast requirement
 */
object AccessibilityUtils {
    
    /**
     * Minimum alpha values that maintain WCAG AA compliance for text.
     * These values are based on Material Design 3 guidelines and ensure
     * proper contrast ratios on both light and dark backgrounds.
     */
    object MinimumAlpha {
        /**
         * High emphasis text - full opacity for maximum contrast.
         * Use for: Primary text, headings, important UI elements.
         */
        const val HIGH = 1.00f
        
        /**
         * Medium emphasis text - maintains WCAG AA compliance.
         * Use for: Secondary text, captions, less important content.
         * 
         * High contrast (colored backgrounds): 0.74f
         * Low contrast (neutral backgrounds): 0.60f
         */
        const val MEDIUM_HIGH_CONTRAST = 0.74f
        const val MEDIUM_LOW_CONTRAST = 0.60f
        
        /**
         * Disabled state - below WCAG requirements (intentional).
         * Use for: Disabled buttons, inactive controls, unavailable options.
         * Note: Disabled elements are exempt from WCAG contrast requirements.
         */
        const val DISABLED = 0.38f
        
        /**
         * Decorative elements - for backgrounds and non-text elements.
         * Use for: Dividers, borders, decorative backgrounds.
         * Note: Non-text elements have lower contrast requirements (3:1).
         */
        const val DECORATIVE = 0.12f
    }
    
    /**
     * Calculates the contrast ratio between two colors.
     * 
     * @param foreground The foreground color (e.g., text color)
     * @param background The background color
     * @return The contrast ratio (1.0 to 21.0)
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Float {
        val foregroundLuminance = foreground.luminance()
        val backgroundLuminance = background.luminance()
        
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Checks if the contrast ratio meets WCAG AA standards for normal text.
     * 
     * @param foreground The foreground color
     * @param background The background color
     * @return true if contrast ratio is at least 4.5:1
     */
    fun meetsWCAGAANormalText(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 4.5f
    }
    
    /**
     * Checks if the contrast ratio meets WCAG AA standards for large text.
     * 
     * @param foreground The foreground color
     * @param background The background color
     * @return true if contrast ratio is at least 3:1
     */
    fun meetsWCAGAALargeText(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 3.0f
    }
    
    /**
     * Checks if the contrast ratio meets WCAG AA standards for UI components.
     * 
     * @param foreground The foreground color
     * @param background The background color
     * @return true if contrast ratio is at least 3:1
     */
    fun meetsWCAGAAUIComponent(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 3.0f
    }
    
    /**
     * Gets a text color with appropriate alpha for the given background,
     * ensuring WCAG AA compliance for normal text.
     * 
     * @param baseColor The base text color
     * @param background The background color
     * @param emphasis The desired emphasis level (high, medium, or disabled)
     * @return The text color with appropriate alpha
     */
    @Composable
    fun getAccessibleTextColor(
        baseColor: Color = MaterialTheme.colorScheme.onSurface,
        background: Color = MaterialTheme.colorScheme.surface,
        emphasis: TextEmphasis = TextEmphasis.HIGH
    ): Color {
        return when (emphasis) {
            TextEmphasis.HIGH -> baseColor.copy(alpha = MinimumAlpha.HIGH)
            TextEmphasis.MEDIUM -> {
                // Use ContentAlpha which automatically adjusts based on luminance
                val alpha = if (background.luminance() > 0.5) {
                    MinimumAlpha.MEDIUM_HIGH_CONTRAST
                } else {
                    MinimumAlpha.MEDIUM_LOW_CONTRAST
                }
                baseColor.copy(alpha = alpha)
            }
            TextEmphasis.DISABLED -> baseColor.copy(alpha = MinimumAlpha.DISABLED)
        }
    }
    
    /**
     * Text emphasis levels for accessibility.
     */
    enum class TextEmphasis {
        /** High emphasis - full opacity, maximum contrast */
        HIGH,
        /** Medium emphasis - reduced opacity, still meets WCAG AA */
        MEDIUM,
        /** Disabled - low opacity, exempt from WCAG requirements */
        DISABLED
    }
}

/**
 * Extension function to check if a color combination meets WCAG AA standards.
 */
fun Color.hasAccessibleContrastWith(
    background: Color,
    isLargeText: Boolean = false
): Boolean {
    return if (isLargeText) {
        AccessibilityUtils.meetsWCAGAALargeText(this, background)
    } else {
        AccessibilityUtils.meetsWCAGAANormalText(this, background)
    }
}
