package ireader.domain.data.repository

import ireader.domain.models.fonts.CustomFont

/**
 * Repository interface for managing custom fonts
 */
interface FontRepository {
    /**
     * Import a font file from the given file path
     * @param filePath The path to the font file (.ttf or .otf)
     * @param fontName The display name for the font
     * @return Result containing the imported CustomFont or an error
     */
    suspend fun importFont(filePath: String, fontName: String): Result<CustomFont>
    
    /**
     * Get all custom fonts (both system and user-imported)
     * @return List of all available custom fonts
     */
    suspend fun getAllFonts(): List<CustomFont>
    
    /**
     * Get only user-imported custom fonts
     * @return List of user-imported fonts
     */
    suspend fun getCustomFonts(): List<CustomFont>
    
    /**
     * Get system fonts
     * @return List of system fonts
     */
    suspend fun getSystemFonts(): List<CustomFont>
    
    /**
     * Delete a custom font
     * @param fontId The ID of the font to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteFont(fontId: String): Result<Unit>
    
    /**
     * Get a font by its ID
     * @param fontId The ID of the font
     * @return The CustomFont if found, null otherwise
     */
    suspend fun getFontById(fontId: String): CustomFont?
}
