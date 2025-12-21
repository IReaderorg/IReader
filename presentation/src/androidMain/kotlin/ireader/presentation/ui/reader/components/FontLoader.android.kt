package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ireader.domain.models.fonts.CustomFont
import java.io.File

/**
 * Android implementation of custom font loading
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
            } catch (@Suppress("SwallowedException") e: Exception) {
                // If font loading fails, fallback to default - this is expected behavior
                FontFamily.Default
            }
        }
    }
}
