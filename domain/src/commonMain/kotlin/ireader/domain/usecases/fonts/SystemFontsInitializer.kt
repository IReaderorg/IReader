package ireader.domain.usecases.fonts

import ireader.domain.data.repository.FontRepository
import ireader.domain.models.fonts.CustomFont

/**
 * Initializes system fonts in the database
 */
class SystemFontsInitializer(
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
    
    /**
     * Get list of system fonts to initialize
     * This is platform-specific and should be overridden
     */
    protected open fun getSystemFontsList(): List<CustomFont> {
        return listOf(
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
            )
        )
    }
}
