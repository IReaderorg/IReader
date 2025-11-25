package ireader.data.repository

import ireader.domain.data.repository.AllReviewsRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview

/**
 * No-op implementation of AllReviewsRepository
 * Used when Supabase is not configured
 */
class NoOpAllReviewsRepository : AllReviewsRepository {
    override suspend fun getAllBookReviews(limit: Int, offset: Int): Result<List<BookReview>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getAllChapterReviews(limit: Int, offset: Int): Result<List<ChapterReview>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getBookReviewsForBook(bookTitle: String): Result<List<BookReview>> {
        return Result.success(emptyList())
    }
    
    override suspend fun getChapterReviewsForBook(bookTitle: String): Result<List<ChapterReview>> {
        return Result.success(emptyList())
    }
}
