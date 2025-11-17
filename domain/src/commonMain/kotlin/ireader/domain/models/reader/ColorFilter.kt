package ireader.domain.models.reader

/**
 * Color filter configuration for reader
 * Requirements: 5.1, 5.2
 */
data class ColorFilter(
    val enabled: Boolean = false,
    val colorValue: Int = 0,
    val blendMode: ColorFilterBlendMode = ColorFilterBlendMode.DEFAULT,
    val customBrightness: Boolean = false,
    val brightnessValue: Int = 0,
    val grayscale: Boolean = false,
    val invertedColors: Boolean = false,
) {
    /**
     * Check if any filter is active
     */
    fun isActive(): Boolean {
        return enabled || customBrightness || grayscale || invertedColors
    }

    /**
     * Get brightness as float (0.0 to 1.0)
     */
    fun getBrightnessFloat(): Float {
        return brightnessValue / 100f
    }

    /**
     * Get color filter alpha value
     */
    fun getColorAlpha(): Int {
        return (colorValue shr 24) and 0xFF
    }

    /**
     * Get color filter RGB value
     */
    fun getColorRGB(): Int {
        return colorValue and 0xFFFFFF
    }
}

/**
 * Color filter blend modes
 */
enum class ColorFilterBlendMode(val value: Int) {
    DEFAULT(0),
    MULTIPLY(1),
    SCREEN(2),
    OVERLAY(3),
    LIGHTEN(4),
    DARKEN(5);

    companion object {
        fun fromValue(value: Int): ColorFilterBlendMode {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}

/**
 * Predefined color filter presets
 */
object ColorFilterPresets {
    val SEPIA = ColorFilter(
        enabled = true,
        colorValue = 0x40704214, // Semi-transparent brown
        blendMode = ColorFilterBlendMode.MULTIPLY
    )

    val BLUE_LIGHT_FILTER = ColorFilter(
        enabled = true,
        colorValue = 0x60FF9500, // Semi-transparent orange
        blendMode = ColorFilterBlendMode.MULTIPLY
    )

    val DARK_MODE = ColorFilter(
        enabled = true,
        invertedColors = true
    )

    val GRAYSCALE = ColorFilter(
        enabled = true,
        grayscale = true
    )

    val NIGHT_MODE = ColorFilter(
        enabled = true,
        colorValue = 0x80000000.toInt(), // Semi-transparent black
        blendMode = ColorFilterBlendMode.MULTIPLY,
        customBrightness = true,
        brightnessValue = 30
    )
}
