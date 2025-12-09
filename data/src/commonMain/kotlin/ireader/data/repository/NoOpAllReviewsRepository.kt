package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.AllReviewsRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview

/**
 * No-op implementation of AllReviewsRepository.
 * Used when Supabase is not configured.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpAllReviewsRepository : NoOpRepositoryBase(), AllReviewsRepository {
    
    override suspend fun getAllBookReviews(limit: Int, offset: Int): Result<List<BookReview>> =
        emptyListResult()
    
    override suspend fun getAllChapterReviews(limit: Int, offset: Int): Result<List<ChapterReview>> =
        emptyListResult()
    
    override suspend fun getBookReviewsForBook(bookTitle: String): Result<List<BookReview>> =
        emptyListResult()
    
    override suspend fun getChapterReviewsForBook(bookTitle: String): Result<List<ChapterReview>> =
        emptyListResult()
}
