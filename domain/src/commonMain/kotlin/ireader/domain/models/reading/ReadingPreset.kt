package ireader.domain.models.reading

import ireader.domain.models.common.FontFamilyModel

/**
 * A reading preset that bundles common reading settings together.
 * Users can quickly switch between different reading configurations.
 */
data class ReadingPreset(
    val id: String,
    val name: String,
    val description: String,
    val fontSize: Int,
    val lineHeight: Int,
    val paragraphDistance: Int,
    val paragraphIndent: Int,
    val textWeight: Int,
    val letterSpacing: Int,
    val fontName: String,
    val fontFamily: FontFamilyModel
) {
    companion object {
        /**
         * Default presets for different reading scenarios.
         * Font defaults are based on device type (phone vs tablet).
         */
        fun getDefaultPresets(isTablet: Boolean): List<ReadingPreset> {
            val defaultFontName = if (isTablet) "Merriweather" else "Roboto"
            val defaultFontFamily = FontFamilyModel.Custom(defaultFontName)
            
            return listOf(
                ReadingPreset(
                    id = "default",
                    name = "Default",
                    description = "Balanced settings for everyday reading",
                    fontSize = if (isTablet) 20 else 18,
                    lineHeight = 25,
                    paragraphDistance = 2,
                    paragraphIndent = 8,
                    textWeight = 400,
                    letterSpacing = 0,
                    fontName = defaultFontName,
                    fontFamily = defaultFontFamily
                ),
                ReadingPreset(
                    id = "comfortable",
                    name = "Comfortable",
                    description = "Larger text with more spacing for relaxed reading",
                    fontSize = if (isTablet) 24 else 22,
                    lineHeight = 30,
                    paragraphDistance = 4,
                    paragraphIndent = 12,
                    textWeight = 400,
                    letterSpacing = 1,
                    fontName = "Open Sans",
                    fontFamily = FontFamilyModel.Custom("Open Sans")
                ),
                ReadingPreset(
                    id = "compact",
                    name = "Compact",
                    description = "Smaller text to fit more content on screen",
                    fontSize = if (isTablet) 16 else 14,
                    lineHeight = 20,
                    paragraphDistance = 1,
                    paragraphIndent = 4,
                    textWeight = 400,
                    letterSpacing = 0,
                    fontName = "Roboto",
                    fontFamily = FontFamilyModel.Custom("Roboto")
                ),
                ReadingPreset(
                    id = "novel",
                    name = "Novel",
                    description = "Serif font optimized for long-form fiction",
                    fontSize = if (isTablet) 22 else 20,
                    lineHeight = 28,
                    paragraphDistance = 3,
                    paragraphIndent = 16,
                    textWeight = 400,
                    letterSpacing = 0,
                    fontName = "Merriweather",
                    fontFamily = FontFamilyModel.Custom("Merriweather")
                ),
                ReadingPreset(
                    id = "manga",
                    name = "Manga",
                    description = "Bold text for manga and comics with vertical scroll",
                    fontSize = if (isTablet) 18 else 16,
                    lineHeight = 22,
                    paragraphDistance = 1,
                    paragraphIndent = 0,
                    textWeight = 500,
                    letterSpacing = 0,
                    fontName = "Noto Sans",
                    fontFamily = FontFamilyModel.Custom("Noto Sans")
                ),
                ReadingPreset(
                    id = "minimal",
                    name = "Minimal",
                    description = "Clean and simple with maximum readability",
                    fontSize = if (isTablet) 20 else 18,
                    lineHeight = 24,
                    paragraphDistance = 2,
                    paragraphIndent = 0,
                    textWeight = 300,
                    letterSpacing = 1,
                    fontName = "Lato",
                    fontFamily = FontFamilyModel.Custom("Lato")
                )
            )
        }
        
        /**
         * Get the default preset based on device type.
         */
        fun getDefault(isTablet: Boolean): ReadingPreset {
            return getDefaultPresets(isTablet).first { it.id == "default" }
        }
    }
}