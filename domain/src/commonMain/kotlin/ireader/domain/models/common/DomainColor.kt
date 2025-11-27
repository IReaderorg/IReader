package ireader.domain.models.common

import kotlin.math.pow

/**
 * Domain-layer color representation that doesn't depend on Compose UI
 * 
 * This replaces androidx.compose.ui.graphics.Color in the domain layer
 * to maintain clean architecture principles.
 * 
 * @property red Red component (0.0 - 1.0)
 * @property green Green component (0.0 - 1.0)
 * @property blue Blue component (0.0 - 1.0)
 * @property alpha Alpha component (0.0 - 1.0)
 */
data class DomainColor(
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float = 1f
) {
    /**
     * Convert to ARGB integer representation
     */
    fun toArgb(): Int {
        val a = (alpha * 255.0f + 0.5f).toInt().coerceIn(0, 255)
        val r = (red * 255.0f + 0.5f).toInt().coerceIn(0, 255)
        val g = (green * 255.0f + 0.5f).toInt().coerceIn(0, 255)
        val b = (blue * 255.0f + 0.5f).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    
    /**
     * Calculate luminance (brightness) of the color
     * Used to determine if text should be light or dark
     */
    fun luminance(): Float {
        // Convert to linear RGB
        fun linearize(component: Float): Float {
            return if (component <= 0.04045f) {
                component / 12.92f
            } else {
                ((component + 0.055f) / 1.055f).pow(2.4f)
            }
        }
        
        val r = linearize(red)
        val g = linearize(green)
        val b = linearize(blue)
        
        // Calculate relative luminance
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
    
    /**
     * Check if this is a light color (luminance > 0.5)
     */
    fun isLight(): Boolean = luminance() > 0.5f
    
    /**
     * Check if this is a dark color (luminance <= 0.5)
     */
    fun isDark(): Boolean = !isLight()
    

    
    companion object {
        /**
         * Create from ARGB integer
         */
        fun fromArgb(color: Int): DomainColor {
            val a = ((color shr 24) and 0xFF) / 255.0f
            val r = ((color shr 16) and 0xFF) / 255.0f
            val g = ((color shr 8) and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f
            return DomainColor(r, g, b, a)
        }
        
        /**
         * Create from RGB bytes (0-255)
         */
        fun fromRgb(red: Int, green: Int, blue: Int, alpha: Int = 255): DomainColor {
            return DomainColor(
                red = red / 255.0f,
                green = green / 255.0f,
                blue = blue / 255.0f,
                alpha = alpha / 255.0f
            )
        }
        
        /**
         * Create from hex string (#RRGGBB or #AARRGGBB)
         */
        fun fromHex(hex: String): DomainColor {
            val cleanHex = hex.removePrefix("#")
            val argb = when (cleanHex.length) {
                6 -> "FF$cleanHex" // Add full alpha
                8 -> cleanHex
                else -> throw IllegalArgumentException("Invalid hex color: $hex")
            }
            return fromArgb(argb.toLong(16).toInt())
        }
        
        // Common colors
        val Transparent = DomainColor(0f, 0f, 0f, 0f)
        val Black = DomainColor(0f, 0f, 0f, 1f)
        val White = DomainColor(1f, 1f, 1f, 1f)
        val Red = DomainColor(1f, 0f, 0f, 1f)
        val Green = DomainColor(0f, 1f, 0f, 1f)
        val Blue = DomainColor(0f, 0f, 1f, 1f)
        val Gray = DomainColor(0.5f, 0.5f, 0.5f, 1f)
        val Unspecified = DomainColor(0f, 0f, 0f, 0f)
    }
}

/**
 * Extension to convert Int (ARGB) to DomainColor
 */
fun Int.toDomainColor(): DomainColor = DomainColor.fromArgb(this)

/**
 * Extension to convert hex string to DomainColor
 */
fun String.toDomainColor(): DomainColor = DomainColor.fromHex(this)
