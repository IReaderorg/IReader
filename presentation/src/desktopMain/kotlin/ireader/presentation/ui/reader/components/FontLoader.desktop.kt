package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import ireader.domain.models.fonts.CustomFont
import java.io.File

/**
 * Desktop implementation of custom font loading
 */
@Composable
actual fun rememberCustomFontFamily(font: CustomFont?): FontFamily {
    return remember(font) {
        if (font == null || font.filePath.isEmpty()) {
            FontFamily.Default
        } else {
            try {
                val fontFile = File(font.filePath)
                if (fontFile.exists()) {
                    // Load font from file
                    FontFamily(
                        Font(fontFile, FontWeight.Normal)
                    )
                } else {
                    FontFamily.Default
                }
            } catch (e: Exception) {
                // If font loading fails, fallback to default
                FontFamily.Default
            }
        }
    }
}
