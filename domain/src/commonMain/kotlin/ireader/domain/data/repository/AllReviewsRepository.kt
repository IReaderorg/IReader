package ireader.domain.data.repository

import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview

/**
 * Repository for fetching all reviews from all users
 */
interface AllReviewsRepository {
    /**
     * Get all book reviews from all users
     * @param limit Maximum number of reviews to return
     * @param offset Offset for pagination
     */
    suspend fun getAllBookReviews(limit: Int = 50, offset: Int = 0): Result<List<BookReview>>
    
    /**
     * Get all chapter reviews from all users
     * @param limit Maximum number of reviews to return
     * @param offset Offset for pagination
     */
    suspend fun getAllChapterReviews(limit: Int = 50, offset: Int = 0): Result<List<ChapterReview>>
    
    /**
     * Get all book reviews for a specific book
     * @param bookTitle Normalized book title
     */
    suspend fun getBookReviewsForBook(bookTitle: String): Result<List<BookReview>>
    
    /**
     * Get all chapter reviews for a specific book
     * @param bookTitle Normalized book title
     */
    suspend fun getChapterReviewsForBook(bookTitle: String): Result<List<ChapterReview>>
}
