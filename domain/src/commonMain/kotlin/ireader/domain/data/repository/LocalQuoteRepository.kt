package ireader.domain.data.repository

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteContext
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing local quotes stored on device
 */
interface LocalQuoteRepository {
    
    /**
     * Insert a new local quote
     * @return the ID of the inserted quote
     */
    suspend fun insert(quote: LocalQuote): Long
    
    /**
     * Update an existing quote
     */
    suspend fun update(quote: LocalQuote)
    
    /**
     * Delete a quote by ID
     */
    suspend fun delete(id: Long)
    
    /**
     * Get a quote by ID
     */
    suspend fun getById(id: Long): LocalQuote?
    
    /**
     * Get all quotes
     */
    suspend fun getAll(): List<LocalQuote>
    
    /**
     * Get quotes for a specific book
     */
    suspend fun getByBookId(bookId: Long): List<LocalQuote>
    
    /**
     * Observe all quotes as a Flow
     */
    fun observeAll(): Flow<List<LocalQuote>>
    
    /**
     * Observe quotes for a specific book
     */
    fun observeByBookId(bookId: Long): Flow<List<LocalQuote>>
    
    /**
     * Search quotes by text content
     */
    suspend fun search(query: String): List<LocalQuote>
    
    /**
     * Get total quote count
     */
    suspend fun getCount(): Long
    
    // Context backup operations
    
    /**
     * Save chapter context for a quote
     */
    suspend fun saveContext(quoteId: Long, contexts: List<QuoteContext>)
    
    /**
     * Get context chapters for a quote
     */
    suspend fun getContext(quoteId: Long): List<QuoteContext>
    
    /**
     * Delete context for a quote
     */
    suspend fun deleteContext(quoteId: Long)
    
    /**
     * Check if quote has context backup
     */
    suspend fun hasContext(quoteId: Long): Boolean
}
