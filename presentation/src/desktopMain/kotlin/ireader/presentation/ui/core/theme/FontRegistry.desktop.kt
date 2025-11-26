package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import ireader.core.io.VirtualFile

/**
 * Desktop implementation for creating FontFamily from file
 */
actual suspend fun createFontFamilyFromFile(file: VirtualFile): FontFamily {
    return try {
        // Use the file path to create the font
        // VirtualFile.path gives us the absolute path
        FontFamily(
            Font(file.path)
        )
    } catch (e: Exception) {
        e.printStackTrace()
        FontFamily.Default
    }
}
