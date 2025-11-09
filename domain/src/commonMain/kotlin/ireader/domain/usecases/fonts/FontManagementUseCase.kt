package ireader.domain.usecases.fonts

import ireader.domain.data.repository.FontRepository
import ireader.domain.models.fonts.CustomFont

/**
 * Use case for managing custom fonts
 */
class FontManagementUseCase(
    private val fontRepository: FontRepository
) {
    /**
     * Import a font from a file
     */
    suspend fun importFont(filePath: String, fontName: String): Result<CustomFont> {
        val result = fontRepository.importFont(filePath, fontName)
        result.getOrNull()?.let { font ->
            FontCache.put(font.id, font)
        }
        return result
    }
    
    /**
     * Get all available fonts
     */
    suspend fun getAllFonts(): List<CustomFont> {
        return fontRepository.getAllFonts()
    }
    
    /**
     * Get only custom (user-imported) fonts
     */
    suspend fun getCustomFonts(): List<CustomFont> {
        return fontRepository.getCustomFonts()
    }
    
    /**
     * Get system fonts
     */
    suspend fun getSystemFonts(): List<CustomFont> {
        return fontRepository.getSystemFonts()
    }
    
    /**
     * Delete a custom font
     */
    suspend fun deleteFont(fontId: String): Result<Unit> {
        val result = fontRepository.deleteFont(fontId)
        if (result.isSuccess) {
            FontCache.remove(fontId)
        }
        return result
    }
    
    /**
     * Get a font by ID (with caching)
     */
    suspend fun getFontById(fontId: String): CustomFont? {
        // Check cache first
        FontCache.get(fontId)?.let { return it }
        
        // Load from repository
        val font = fontRepository.getFontById(fontId)
        font?.let { FontCache.put(fontId, it) }
        return font
    }
}
