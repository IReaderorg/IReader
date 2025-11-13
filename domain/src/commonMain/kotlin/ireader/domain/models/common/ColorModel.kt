package ireader.domain.models.common

/**
 * Domain representation of a color value.
 * This is independent of any UI framework.
 */
data class ColorModel(
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float = 1.0f
) {
    companion object {
        fun fromArgb(argb: Int): ColorModel {
            val alpha = ((argb shr 24) and 0xFF) / 255f
            val red = ((argb shr 16) and 0xFF) / 255f
            val green = ((argb shr 8) and 0xFF) / 255f
            val blue = (argb and 0xFF) / 255f
            return ColorModel(red, green, blue, alpha)
        }
        
        fun fromRgb(red: Int, green: Int, blue: Int): ColorModel {
            return ColorModel(red / 255f, green / 255f, blue / 255f, 1.0f)
        }
    }
    
    fun toArgb(): Int {
        val a = (alpha * 255).toInt() and 0xFF
        val r = (red * 255).toInt() and 0xFF
        val g = (green * 255).toInt() and 0xFF
        val b = (blue * 255).toInt() and 0xFF
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
