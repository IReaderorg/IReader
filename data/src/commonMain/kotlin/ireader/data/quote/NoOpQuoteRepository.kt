package ireader.data.quote

import ireader.domain.data.repository.QuoteRepository
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.SubmitQuoteRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of QuoteRepository when Supabase is not configured
 */
class NoOpQuoteRepository : QuoteRepository {
    
    override suspend fun getDailyQuote(): Result<Quote?> = Result.success(null)
    
    override suspend fun getApprovedQuotes(limit: Int, offset: Int): Result<List<Quote>> = 
        Result.success(emptyList())
    
    override suspend fun getQuotesByBook(bookTitle: String): Result<List<Quote>> = 
        Result.success(emptyList())
    
    override suspend fun submitQuote(request: SubmitQuoteRequest): Result<Quote> = 
        Result.failure(Exception("Supabase not configured. Please configure cloud sync to submit quotes."))
    
    override suspend fun getUserQuotes(userId: String): Result<List<Quote>> = 
        Result.success(emptyList())
    
    override suspend fun toggleLike(quoteId: String): Result<Boolean> = 
        Result.failure(Exception("Supabase not configured"))
    
    override suspend fun isLikedByUser(quoteId: String): Result<Boolean> = 
        Result.success(false)
    
    override suspend fun getPendingQuotes(): Result<List<Quote>> = 
        Result.success(emptyList())
    
    override suspend fun approveQuote(quoteId: String, featured: Boolean): Result<Unit> = 
        Result.failure(Exception("Supabase not configured"))
    
    override suspend fun rejectQuote(quoteId: String): Result<Unit> = 
        Result.failure(Exception("Supabase not configured"))
    
    override fun observeApprovedQuotes(): Flow<List<Quote>> = flowOf(emptyList())
}
