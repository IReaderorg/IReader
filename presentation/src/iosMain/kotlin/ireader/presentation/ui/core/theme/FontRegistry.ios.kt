package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.io.File

/**
 * iOS implementation for creating FontFamily from file
 */
actual fun createFontFamilyFromFile(file: File): FontFamily {
    return try {
        FontFamily(
            Font(file, FontWeight.Normal)
        )
    } catch (e: Exception) {
        e.printStackTrace()
        FontFamily.Default
    }
}
