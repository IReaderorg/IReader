package ireader.domain.data.repository

import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteStatus
import ireader.domain.models.quote.SubmitQuoteRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing quotes from Supabase
 */
interface QuoteRepository {
    
    /**
     * Get today's daily quote
     */
    suspend fun getDailyQuote(): Result<Quote?>
    
    /**
     * Get approved quotes with pagination
     */
    suspend fun getApprovedQuotes(limit: Int = 50, offset: Int = 0): Result<List<Quote>>
    
    /**
     * Get quotes by book title
     */
    suspend fun getQuotesByBook(bookTitle: String): Result<List<Quote>>
    
    /**
     * Submit a new quote for approval
     */
    suspend fun submitQuote(request: SubmitQuoteRequest): Result<Quote>
    
    /**
     * Get user's submitted quotes
     */
    suspend fun getUserQuotes(userId: String): Result<List<Quote>>
    
    /**
     * Toggle like on a quote
     */
    suspend fun toggleLike(quoteId: String): Result<Boolean>
    
    /**
     * Check if user has liked a quote
     */
    suspend fun isLikedByUser(quoteId: String): Result<Boolean>
    
    /**
     * Get pending quotes for admin review
     */
    suspend fun getPendingQuotes(): Result<List<Quote>>
    
    /**
     * Approve a quote (admin only)
     */
    suspend fun approveQuote(quoteId: String, featured: Boolean = false): Result<Unit>
    
    /**
     * Reject a quote (admin only)
     */
    suspend fun rejectQuote(quoteId: String): Result<Unit>
    
    /**
     * Observe approved quotes in realtime
     */
    fun observeApprovedQuotes(): Flow<List<Quote>>
}
