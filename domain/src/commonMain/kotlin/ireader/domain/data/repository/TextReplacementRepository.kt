package ireader.domain.data.repository

import ireader.domain.models.entities.TextReplacement
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing text replacement rules.
 */
interface TextReplacementRepository {
    
    /**
     * Get all global replacements (not book-specific)
     */
    fun getGlobalReplacements(): Flow<List<TextReplacement>>
    
    /**
     * Get all enabled global replacements
     */
    suspend fun getEnabledGlobalReplacements(): List<TextReplacement>
    
    /**
     * Get replacements for a specific book (includes global replacements)
     */
    fun getReplacementsForBook(bookId: Long): Flow<List<TextReplacement>>
    
    /**
     * Get enabled replacements for a specific book (includes global replacements)
     */
    suspend fun getEnabledReplacementsForBook(bookId: Long): List<TextReplacement>
    
    /**
     * Get book-specific replacements only
     */
    fun getBookSpecificReplacements(bookId: Long): Flow<List<TextReplacement>>
    
    /**
     * Get a replacement by ID
     */
    suspend fun getReplacementById(id: Long): TextReplacement?
    
    /**
     * Insert a new replacement
     * @return The ID of the inserted replacement
     */
    suspend fun insert(replacement: TextReplacement): Long
    
    /**
     * Insert a new replacement with a specific ID (for default replacements)
     */
    suspend fun insertWithId(replacement: TextReplacement)
    
    /**
     * Update an existing replacement
     */
    suspend fun update(replacement: TextReplacement)
    
    /**
     * Toggle the enabled state of a replacement
     */
    suspend fun toggleEnabled(id: Long)
    
    /**
     * Delete a replacement
     */
    suspend fun delete(id: Long)
    
    /**
     * Delete all replacements for a book
     */
    suspend fun deleteBookReplacements(bookId: Long)
    
    /**
     * Count total replacements
     */
    suspend fun countReplacements(): Long
    
    /**
     * Count enabled replacements
     */
    suspend fun countEnabledReplacements(): Long
}
