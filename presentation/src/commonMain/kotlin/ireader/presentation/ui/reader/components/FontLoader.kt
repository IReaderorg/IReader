package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import ireader.domain.models.fonts.CustomFont

/**
 * Loads a custom font and returns a FontFamily
 * Platform-specific implementations will handle the actual font loading
 */
@Composable
expect fun rememberCustomFontFamily(font: CustomFont?): FontFamily

/**
 * Get system font family by ID
 */
@Composable
fun getSystemFontFamily(fontId: String): FontFamily {
    return remember(fontId) {
        when (fontId) {
            "system_default" -> FontFamily.Default
            "system_serif" -> FontFamily.Serif
            "system_sans_serif" -> FontFamily.SansSerif
            "system_monospace" -> FontFamily.Monospace
            else -> FontFamily.Default
        }
    }
}

/**
 * Load font family from CustomFont
 */
@Composable
fun loadFontFamily(font: CustomFont?): FontFamily {
    return if (font == null) {
        FontFamily.Default
    } else if (font.isSystemFont) {
        getSystemFontFamily(font.id)
    } else {
        rememberCustomFontFamily(font)
    }
}
