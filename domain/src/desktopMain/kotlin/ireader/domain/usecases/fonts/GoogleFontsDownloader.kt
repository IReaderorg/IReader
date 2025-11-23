package ireader.domain.usecases.fonts

import io.ktor.client.call.*
import io.ktor.client.request.*
import ireader.core.http.HttpClients
import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Downloads Google Fonts for desktop use
 * Fonts are cached locally for offline use
 */
class GoogleFontsDownloader(
    private val httpClients: HttpClients
) {
    private val fontsDir = File(System.getProperty("user.home"), ".ireader/fonts/google").apply {
        mkdirs()
    }
    
    /**
     * Download a Google Font by name
     * @param fontName The name of the font (e.g., "Roboto", "Open Sans")
     * @return File path to the downloaded font, or null if download failed
     */
    suspend fun downloadFont(fontName: String): File? = withContext(Dispatchers.IO) {
        try {
            // Check if font is already cached
            val cachedFont = getCachedFont(fontName)
            if (cachedFont != null && cachedFont.exists()) {
                Log.info("Using cached font: $fontName")
                return@withContext cachedFont
            }
            
            // Download font from Google Fonts
            val fontFile = File(fontsDir, "${fontName.replace(" ", "")}.ttf")
            
            // Google Fonts API returns WOFF2 which Compose Desktop doesn't support
            // We need to use a different approach to get TTF format
            // Option 1: Use Google Fonts API with a User-Agent that requests TTF
            val cssUrl = "https://fonts.googleapis.com/css?family=${fontName.replace(" ", "+")}"
            
            // Fetch CSS with a User-Agent that will get TTF format (older browser)
            val css: String = httpClients.default.get(cssUrl) {
                headers.append("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)")
            }.body()
            
            // Extract font URL from CSS - should be TTF with the old User-Agent
            val fontUrlRegex = """url\((https://[^)]+\.(?:ttf|woff))\)""".toRegex()
            val matchResult = fontUrlRegex.find(css)
            var fontUrl = matchResult?.groupValues?.getOrNull(1)
            
            // If still WOFF2, try alternative: use fontsource CDN which provides TTF
            if (fontUrl == null || fontUrl.contains("woff2")) {
                // Try fontsource CDN (provides TTF files)
                val fontNameLower = fontName.lowercase().replace(" ", "-")
                fontUrl = "https://cdn.jsdelivr.net/fontsource/fonts/${fontNameLower}@latest/latin-400-normal.ttf"
            }
            
            if (fontUrl != null) {
                try {
                    // Download the actual font file
                    val fontBytes: ByteArray = httpClients.default.get(fontUrl, block = {}).body()
                    
                    // Verify it's a valid TTF file (starts with specific magic bytes)
                    if (fontBytes.size > 4) {
                        val header = String(fontBytes.take(4).toByteArray())
                        // TTF files start with 0x00 0x01 0x00 0x00 or "OTTO" for OTF
                        // WOFF files start with "wOFF", WOFF2 files start with "wOF2"
                        if (header.startsWith("wOF")) {
                            Log.error("Font $fontName is WOFF/WOFF2 format, not supported by Compose Desktop")
                            return@withContext null
                        }
                    }
                    
                    fontFile.writeBytes(fontBytes)
                    
                    if (!fontFile.canRead()) {
                        Log.error("Font file is not readable: ${fontFile.absolutePath}")
                        return@withContext null
                    }
                    
                    return@withContext fontFile
                } catch (e: Exception) {
                    Log.error("Failed to download font $fontName", e)
                }
            }
            
            Log.warn("Failed to download font: $fontName")
            null
        } catch (e: Exception) {
            Log.error("Error downloading font $fontName: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get cached font file if it exists
     */
    private fun getCachedFont(fontName: String): File? {
        val possibleFiles = listOf(
            File(fontsDir, "${fontName.replace(" ", "")}.ttf"),
            File(fontsDir, "${fontName.replace(" ", "")}.otf"),
            File(fontsDir, "${fontName.replace(" ", "-")}.ttf"),
            File(fontsDir, "${fontName.replace(" ", "-")}.otf")
        )
        
        return possibleFiles.firstOrNull { it.exists() }
    }
    
    /**
     * Check if a font is cached
     */
    fun isFontCached(fontName: String): Boolean {
        return getCachedFont(fontName) != null
    }
    
    /**
     * Clear font cache
     */
    fun clearCache() {
        try {
            fontsDir.listFiles()?.forEach { it.delete() }
            Log.info("Font cache cleared")
        } catch (e: Exception) {
            Log.error("Failed to clear font cache: ${e.message}", e)
        }
    }
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSize(): Long {
        return fontsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
