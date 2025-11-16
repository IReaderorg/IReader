package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import java.io.File

/**
 * Desktop implementation for creating FontFamily from file
 */
actual fun createFontFamilyFromFile(file: File): FontFamily {
    return try {
        FontFamily(
            Font(file.absolutePath)
        )
    } catch (e: Exception) {
        e.printStackTrace()
        FontFamily.Default
    }
}
