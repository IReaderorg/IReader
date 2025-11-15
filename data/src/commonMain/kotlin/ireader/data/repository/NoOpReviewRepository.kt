package ireader.data.repository

import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of ReviewRepository used when Supabase is not configured.
 * Returns empty results and failures with descriptive messages.
 */
class NoOpReviewRepository : ReviewRepository {
    
    private val unavailableMessage = "Reviews require Supabase configuration. " +
            "Please configure Supabase credentials in Settings → Supabase Configuration."
    
    // Book Reviews
    override suspend fun getBookReview(bookTitle: String): Result<BookReview?> {
        return Result.success(null)
    }
    
    override suspend fun getBookReviews(bookTitle: String): Result<List<BookReview>> {
        return Result.success(emptyList())
    }
    
    override suspend fun submitBookReview(
        bookTitle: String,
        rating: Int,
        reviewText: String
    ): Result<BookReview> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun updateBookReview(
        bookTitle: String,
        rating: Int,
        reviewText: String
    ): Result<BookReview> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun deleteBookReview(bookTitle: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override fun observeBookReview(bookTitle: String): Flow<BookReview?> {
        return flowOf(null)
    }
    
    // Chapter Reviews
    override suspend fun getChapterReview(
        bookTitle: String,
        chapterName: String
    ): Result<ChapterReview?> {
        return Result.success(null)
    }
    
    override suspend fun getChapterReviews(bookTitle: String): Result<List<ChapterReview>> {
        return Result.success(emptyList())
    }
    
    override suspend fun submitChapterReview(
        bookTitle: String,
        chapterName: String,
        rating: Int,
        reviewText: String
    ): Result<ChapterReview> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun updateChapterReview(
        bookTitle: String,
        chapterName: String,
        rating: Int,
        reviewText: String
    ): Result<ChapterReview> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override suspend fun deleteChapterReview(
        bookTitle: String,
        chapterName: String
    ): Result<Unit> {
        return Result.failure(UnsupportedOperationException(unavailableMessage))
    }
    
    override fun observeChapterReview(
        bookTitle: String,
        chapterName: String
    ): Flow<ChapterReview?> {
        return flowOf(null)
    }
    
    // Aggregate stats
    override suspend fun getBookAverageRating(bookTitle: String): Result<Float> {
        return Result.success(0f)
    }
    
    override suspend fun getChapterAverageRating(
        bookTitle: String,
        chapterName: String
    ): Result<Float> {
        return Result.success(0f)
    }
}
