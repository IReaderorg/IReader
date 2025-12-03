package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import ireader.domain.models.fonts.CustomFont

/**
 * iOS implementation of custom font loading
 */
@Composable
actual fun rememberCustomFontFamily(font: CustomFont?): FontFamily {
    return remember(font) {
        if (font == null || font.filePath.isEmpty()) {
            FontFamily.Default
        } else {
            // iOS custom fonts need to be bundled with the app
            // or loaded through CoreText
            FontFamily.Default
        }
    }
}
