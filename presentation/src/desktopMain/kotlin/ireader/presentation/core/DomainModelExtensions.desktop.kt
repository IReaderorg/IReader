package ireader.presentation.core

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import ireader.domain.models.common.FontFamilyModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for loaded fonts to avoid repeated filesystem checks
 * Key: font name, Value: FontFamily or null if not found
 */
private val fontCache = ConcurrentHashMap<String, FontFamily?>()

/**
 * Desktop-specific implementation for converting FontFamilyModel to Compose FontFamily
 * Supports system fonts and downloaded Google Fonts
 */
actual fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // Try to load from cache first, then from filesystem
            loadCustomFont(name)
        }
    }
}

/**
 * Load a custom font from cache or system
 * Uses in-memory cache to avoid repeated filesystem checks
 */
private fun loadCustomFont(fontName: String): FontFamily {
    // Check cache first
    fontCache[fontName]?.let { return it }
    
    try {
        // Check Google Fonts cache first
        val googleFontsDir = File(System.getProperty("user.home"), ".ireader/fonts/google")
        val cachedFont = findFontFile(fontName, googleFontsDir)
        if (cachedFont != null && cachedFont.exists()) {
            try {
                // Use File object directly with multiple weights for better rendering
                val fontFamily = FontFamily(
                    Font(cachedFont, androidx.compose.ui.text.font.FontWeight.Normal),
                    Font(cachedFont, androidx.compose.ui.text.font.FontWeight.Bold),
                    Font(cachedFont, androidx.compose.ui.text.font.FontWeight.Light),
                    Font(cachedFont, androidx.compose.ui.text.font.FontWeight.Medium)
                )
                fontCache[fontName] = fontFamily
                return fontFamily
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load Google Font: $fontName", e)
                return FontFamily.Default
            }
        }
        
        // Try system fonts
        val systemFontDirs = getSystemFontDirectories()
        val systemFont = systemFontDirs.firstNotNullOfOrNull { dir ->
            findFontFile(fontName, dir)
        }
        
        if (systemFont != null && systemFont.exists()) {
            try {
                val fontFamily = FontFamily(
                    Font(systemFont, androidx.compose.ui.text.font.FontWeight.Normal)
                )
                fontCache[fontName] = fontFamily
                return fontFamily
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to load system font: $fontName", e)
                return FontFamily.Default
            }
        }
        
        // Font not found - cache the default result
        fontCache[fontName] = FontFamily.Default
        return FontFamily.Default
    } catch (e: Exception) {
        ireader.core.log.Log.error("Failed to load font: $fontName", e)
        fontCache[fontName] = FontFamily.Default
        return FontFamily.Default
    }
}

/**
 * Clear the font cache (useful when fonts are added/removed)
 */
fun clearFontCache() {
    fontCache.clear()
}

/**
 * Find font file by name in a directory
 */
private fun findFontFile(fontName: String, dir: File): File? {
    if (!dir.exists()) return null
    
    val possibleNames = listOf(
        "${fontName}.ttf",
        "${fontName}.otf",
        "${fontName.replace(" ", "")}.ttf",
        "${fontName.replace(" ", "")}.otf",
        "${fontName.replace(" ", "-")}.ttf",
        "${fontName.replace(" ", "-")}.otf"
    )
    
    for (name in possibleNames) {
        val file = File(dir, name)
        if (file.exists()) return file
        
        // Search in subdirectories
        dir.listFiles()?.forEach { subDir ->
            if (subDir.isDirectory) {
                val subFile = File(subDir, name)
                if (subFile.exists()) return subFile
            }
        }
    }
    
    return null
}

/**
 * Get system font directories based on OS
 */
private fun getSystemFontDirectories(): List<File> {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> listOf(
            File("C:\\Windows\\Fonts"),
            File(System.getenv("WINDIR") ?: "C:\\Windows", "Fonts")
        )
        os.contains("mac") -> listOf(
            File("/System/Library/Fonts"),
            File("/Library/Fonts"),
            File(System.getProperty("user.home"), "Library/Fonts")
        )
        else -> listOf( // Linux
            File("/usr/share/fonts"),
            File("/usr/local/share/fonts"),
            File(System.getProperty("user.home"), ".fonts"),
            File(System.getProperty("user.home"), ".local/share/fonts")
        )
    }.filter { it.exists() }
}
