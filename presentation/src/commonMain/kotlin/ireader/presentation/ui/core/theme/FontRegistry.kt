package ireader.presentation.ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import ireader.domain.models.fonts.CustomFont
import java.io.File

/**
 * Registry for managing custom fonts and their FontFamily objects
 */
object FontRegistry {
    private val fontFamilyCache = mutableMapOf<String, FontFamily>()
    
    /**
     * Load a font from file and create a FontFamily
     * @param font The CustomFont to load
     * @return FontFamily or null if loading fails
     */
    fun loadFontFamily(font: CustomFont): FontFamily? {
        // Check cache first
        fontFamilyCache[font.id]?.let { return it }
        
        return try {
            val fontFile = File(font.filePath)
            if (!fontFile.exists()) {
                return null
            }
            
            // Create FontFamily from file
            val fontFamily = createFontFamilyFromFile(fontFile)
            
            // Cache it
            fontFamilyCache[font.id] = fontFamily
            fontFamily
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get a cached FontFamily by font ID
     */
    fun getFontFamily(fontId: String): FontFamily? {
        return fontFamilyCache[fontId]
    }
    
    /**
     * Remove a font from the cache
     */
    fun removeFontFamily(fontId: String) {
        fontFamilyCache.remove(fontId)
    }
    
    /**
     * Clear all cached fonts
     */
    fun clearCache() {
        fontFamilyCache.clear()
    }
    
    /**
     * Validate if a file is a valid font file by checking magic bytes
     * @param file The file to validate
     * @return true if valid TTF or OTF file
     */
    fun validateFontFile(file: File): Boolean {
        if (!file.exists() || !file.canRead()) {
            return false
        }
        
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                val bytesRead = input.read(header)
                
                if (bytesRead < 4) {
                    return false
                }
                
                // Check for TTF magic bytes: 0x00 0x01 0x00 0x00 or "true" or "typ1"
                // Check for OTF magic bytes: "OTTO"
                when {
                    // TTF: 0x00010000
                    header[0] == 0x00.toByte() && header[1] == 0x01.toByte() && 
                    header[2] == 0x00.toByte() && header[3] == 0x00.toByte() -> true
                    
                    // TTF: "true"
                    header[0] == 't'.code.toByte() && header[1] == 'r'.code.toByte() && 
                    header[2] == 'u'.code.toByte() && header[3] == 'e'.code.toByte() -> true
                    
                    // TTF: "typ1"
                    header[0] == 't'.code.toByte() && header[1] == 'y'.code.toByte() && 
                    header[2] == 'p'.code.toByte() && header[3] == '1'.code.toByte() -> true
                    
                    // OTF: "OTTO"
                    header[0] == 'O'.code.toByte() && header[1] == 'T'.code.toByte() && 
                    header[2] == 'T'.code.toByte() && header[3] == 'O'.code.toByte() -> true
                    
                    else -> false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * Platform-specific font family creation
 */
expect fun createFontFamilyFromFile(file: File): FontFamily
