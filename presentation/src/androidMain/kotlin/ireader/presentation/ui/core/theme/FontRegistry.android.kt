package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ireader.core.io.VirtualFile
import java.io.File

/**
 * Android implementation for creating FontFamily from file
 */
actual suspend fun createFontFamilyFromFile(file: VirtualFile): FontFamily {
    return try {
        // Convert VirtualFile to java.io.File for Android Font API
        val javaFile = File(file.path)
        FontFamily(
            Font(javaFile, FontWeight.Normal)
        )
    } catch (e: Exception) {
        e.printStackTrace()
        FontFamily.Default
    }
}
