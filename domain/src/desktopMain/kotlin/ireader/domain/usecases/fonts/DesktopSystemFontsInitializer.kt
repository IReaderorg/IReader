package ireader.domain.usecases.fonts

import ireader.domain.data.repository.FontRepository
import ireader.domain.models.fonts.CustomFont
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * Desktop-specific implementation for system fonts initialization
 * Loads fonts from the operating system
 */
class DesktopSystemFontsInitializer(
    private val fontRepository: FontRepository
) {
    
    /**
     * Initialize system fonts if not already present
     */
    suspend fun initializeSystemFonts() {
        val existingSystemFonts = fontRepository.getSystemFonts()
        
        if (existingSystemFonts.isEmpty()) {
            // Add common system fonts
            val systemFonts = getSystemFontsList()
            
            systemFonts.forEach { font ->
                try {
                    fontRepository.importFont(font.filePath, font.name)
                } catch (e: Exception) {
                    // Ignore errors for system fonts that might not be available
                }
            }
        }
    }
    
    private fun getSystemFontsList(): List<CustomFont> {
        val systemFonts = mutableListOf<CustomFont>()
        
        // Add basic Compose fonts
        systemFonts.addAll(
            listOf(
                CustomFont(
                    id = "system_default",
                    name = "Default",
                    filePath = "",
                    isSystemFont = true
                ),
                CustomFont(
                    id = "system_serif",
                    name = "Serif",
                    filePath = "",
                    isSystemFont = true
                ),
                CustomFont(
                    id = "system_sans_serif",
                    name = "Sans Serif",
                    filePath = "",
                    isSystemFont = true
                ),
                CustomFont(
                    id = "system_monospace",
                    name = "Monospace",
                    filePath = "",
                    isSystemFont = true
                ),
                CustomFont(
                    id = "system_cursive",
                    name = "Cursive",
                    filePath = "",
                    isSystemFont = true
                )
            )
        )
        
        // Try to load system fonts from OS
        try {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val fontFamilies = ge.availableFontFamilyNames
            
            // Get common font directories based on OS
            val fontDirs = getSystemFontDirectories()
            
            // Add popular system fonts that are commonly available
            val popularFonts = listOf(
                "Arial", "Times New Roman", "Courier New", "Verdana", "Georgia",
                "Comic Sans MS", "Trebuchet MS", "Impact", "Palatino", "Garamond",
                "Bookman", "Tahoma", "Lucida", "Helvetica", "Calibri", "Cambria"
            )
            
            popularFonts.forEach { fontName ->
                if (fontFamilies.contains(fontName)) {
                    // Try to find the font file
                    val fontFile = findFontFile(fontName, fontDirs)
                    systemFonts.add(
                        CustomFont(
                            id = "system_${fontName.lowercase().replace(" ", "_")}",
                            name = fontName,
                            filePath = fontFile?.absolutePath ?: "",
                            isSystemFont = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // If system font loading fails, just use basic fonts
            ireader.core.log.Log.warn("Failed to load system fonts: ${e.message}")
        }
        
        return systemFonts
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
    
    /**
     * Find font file for a given font name
     */
    private fun findFontFile(fontName: String, fontDirs: List<File>): File? {
        val possibleNames = listOf(
            "${fontName}.ttf",
            "${fontName}.otf",
            "${fontName.replace(" ", "")}.ttf",
            "${fontName.replace(" ", "")}.otf",
            "${fontName.replace(" ", "-")}.ttf",
            "${fontName.replace(" ", "-")}.otf"
        )
        
        for (dir in fontDirs) {
            for (name in possibleNames) {
                val file = File(dir, name)
                if (file.exists()) return file
                
                // Also search in subdirectories
                dir.listFiles()?.forEach { subDir ->
                    if (subDir.isDirectory) {
                        val subFile = File(subDir, name)
                        if (subFile.exists()) return subFile
                    }
                }
            }
        }
        
        return null
    }
}
