package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.usecases.fonts.GoogleFontsDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Desktop-specific implementation for downloading Google Fonts
 * This runs in a background coroutine to avoid blocking the UI
 */
actual suspend fun ReaderScreenViewModel.downloadGoogleFontIfNeeded(fontName: String) {
    withContext(Dispatchers.IO) {
        try {
            val googleFontsDir = File(System.getProperty("user.home"), ".ireader/fonts/google")
            
            // Check if font is already cached
            val cachedFont = findCachedFont(fontName, googleFontsDir)
            if (cachedFont != null && cachedFont.exists()) {
                return@withContext
            }
            
            // Font not cached, download it
            val downloader = DesktopGoogleFontsDownloader()
            val fontFile = downloader.downloadFont(fontName)
            
            if (fontFile != null) {
                // Clear font cache so the new font will be loaded
                ireader.presentation.core.clearFontCache()
            } else {
                throw Exception("Failed to download font")
            }
        } catch (e: Exception) {
            Log.error("Error downloading font: $fontName", e)
            throw e
        }
    }
}

/**
 * Find cached font file
 */
private fun findCachedFont(fontName: String, dir: File): File? {
    if (!dir.exists()) return null
    
    val possibleNames = listOf(
        "${fontName}.ttf",
        "${fontName}.otf",
        "${fontName.replace(" ", "")}.ttf",
        "${fontName.replace(" ", "")}.otf",
        "${fontName.replace(" ", "-")}.ttf",
        "${fontName.replace(" ", "-")}.otf"
    )
    
    return possibleNames.firstNotNullOfOrNull { name ->
        val file = File(dir, name)
        if (file.exists()) file else null
    }
}

/**
 * Helper class to download Google Fonts
 * Uses Koin for dependency injection
 */
private class DesktopGoogleFontsDownloader : KoinComponent {
    private val downloader: GoogleFontsDownloader by inject()
    
    suspend fun downloadFont(fontName: String): File? {
        return downloader.downloadFont(fontName)
    }
}
