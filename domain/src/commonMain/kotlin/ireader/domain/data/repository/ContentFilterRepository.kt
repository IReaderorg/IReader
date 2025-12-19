package ireader.domain.data.repository

import ireader.domain.models.entities.ContentFilter
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing content filter patterns.
 */
interface ContentFilterRepository {
    
    /**
     * Get all global patterns (not book-specific)
     */
    fun getGlobalPatterns(): Flow<List<ContentFilter>>
    
    /**
     * Get all enabled global patterns
     */
    suspend fun getEnabledGlobalPatterns(): List<ContentFilter>
    
    /**
     * Get patterns for a specific book (includes global patterns)
     */
    fun getPatternsForBook(bookId: Long): Flow<List<ContentFilter>>
    
    /**
     * Get enabled patterns for a specific book (includes global patterns)
     */
    suspend fun getEnabledPatternsForBook(bookId: Long): List<ContentFilter>
    
    /**
     * Get book-specific patterns only
     */
    fun getBookSpecificPatterns(bookId: Long): Flow<List<ContentFilter>>
    
    /**
     * Get a pattern by ID
     */
    suspend fun getPatternById(id: Long): ContentFilter?
    
    /**
     * Get preset patterns
     */
    suspend fun getPresetPatterns(): List<ContentFilter>
    
    /**
     * Insert a new pattern
     * @return The ID of the inserted pattern
     */
    suspend fun insert(filter: ContentFilter): Long
    
    /**
     * Update an existing pattern
     */
    suspend fun update(filter: ContentFilter)
    
    /**
     * Toggle the enabled state of a pattern
     */
    suspend fun toggleEnabled(id: Long)
    
    /**
     * Delete a pattern
     */
    suspend fun delete(id: Long)
    
    /**
     * Delete all patterns for a book
     */
    suspend fun deleteBookPatterns(bookId: Long)
    
    /**
     * Initialize preset patterns if they don't exist
     */
    suspend fun initializePresets()
    
    /**
     * Count total patterns
     */
    suspend fun countPatterns(): Long
    
    /**
     * Count enabled patterns
     */
    suspend fun countEnabledPatterns(): Long
}
