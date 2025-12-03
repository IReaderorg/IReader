package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import ireader.core.io.VirtualFile

/**
 * iOS implementation for creating FontFamily from file
 */
actual suspend fun createFontFamilyFromFile(file: VirtualFile): FontFamily {
    return try {
        // iOS custom fonts need to be bundled with the app
        // or loaded through CoreText APIs
        // For now, return default font family
        FontFamily.Default
    } catch (e: Exception) {
        e.printStackTrace()
        FontFamily.Default
    }
}
