package ireader.data.repository

import ireader.data.repository.base.NoOpRepositoryBase
import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.coroutines.flow.Flow

/**
 * No-op implementation of ReviewRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 * 
 * Implemented as a singleton object since it is stateless.
 * @see Requirements 2.1, 2.2, 2.3, 2.4
 */
object NoOpReviewRepository : NoOpRepositoryBase(), ReviewRepository {
    
    private const val FEATURE_NAME = "Reviews"
    
    // Book Reviews
    override suspend fun getBookReview(bookTitle: String): Result<BookReview?> =
        emptyResult()
    
    override suspend fun getBookReviews(bookTitle: String): Result<List<BookReview>> =
        emptyListResult()
    
    override suspend fun submitBookReview(
        bookTitle: String,
        rating: Int,
        reviewText: String
    ): Result<BookReview> = unavailableResult(FEATURE_NAME)
    
    override suspend fun updateBookReview(
        bookTitle: String,
        rating: Int,
        reviewText: String
    ): Result<BookReview> = unavailableResult(FEATURE_NAME)
    
    override suspend fun deleteBookReview(bookTitle: String): Result<Unit> =
        unavailableResult(FEATURE_NAME)
    
    override fun observeBookReview(bookTitle: String): Flow<BookReview?> =
        emptyFlow()
    
    // Chapter Reviews
    override suspend fun getChapterReview(
        bookTitle: String,
        chapterName: String
    ): Result<ChapterReview?> = emptyResult()
    
    override suspend fun getChapterReviews(bookTitle: String): Result<List<ChapterReview>> =
        emptyListResult()
    
    override suspend fun submitChapterReview(
        bookTitle: String,
        chapterName: String,
        rating: Int,
        reviewText: String
    ): Result<ChapterReview> = unavailableResult(FEATURE_NAME)
    
    override suspend fun updateChapterReview(
        bookTitle: String,
        chapterName: String,
        rating: Int,
        reviewText: String
    ): Result<ChapterReview> = unavailableResult(FEATURE_NAME)
    
    override suspend fun deleteChapterReview(
        bookTitle: String,
        chapterName: String
    ): Result<Unit> = unavailableResult(FEATURE_NAME)
    
    override fun observeChapterReview(
        bookTitle: String,
        chapterName: String
    ): Flow<ChapterReview?> = emptyFlow()
    
    // Aggregate stats
    override suspend fun getBookAverageRating(bookTitle: String): Result<Float> =
        defaultResult(0f)
    
    override suspend fun getChapterAverageRating(
        bookTitle: String,
        chapterName: String
    ): Result<Float> = defaultResult(0f)
}
