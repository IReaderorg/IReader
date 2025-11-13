package ireader.domain.data.repository

import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing book and chapter reviews
 * Reviews are based on normalized book titles - shared across all sources
 */
interface ReviewRepository {
    
    // Book Reviews
    suspend fun getBookReview(bookTitle: String): Result<BookReview?>
    suspend fun getBookReviews(bookTitle: String): Result<List<BookReview>>
    suspend fun submitBookReview(bookTitle: String, rating: Int, reviewText: String): Result<BookReview>
    suspend fun updateBookReview(bookTitle: String, rating: Int, reviewText: String): Result<BookReview>
    suspend fun deleteBookReview(bookTitle: String): Result<Unit>
    fun observeBookReview(bookTitle: String): Flow<BookReview?>
    
    // Chapter Reviews
    suspend fun getChapterReview(bookTitle: String, chapterName: String): Result<ChapterReview?>
    suspend fun getChapterReviews(bookTitle: String): Result<List<ChapterReview>>
    suspend fun submitChapterReview(bookTitle: String, chapterName: String, rating: Int, reviewText: String): Result<ChapterReview>
    suspend fun updateChapterReview(bookTitle: String, chapterName: String, rating: Int, reviewText: String): Result<ChapterReview>
    suspend fun deleteChapterReview(bookTitle: String, chapterName: String): Result<Unit>
    fun observeChapterReview(bookTitle: String, chapterName: String): Flow<ChapterReview?>
    
    // Aggregate stats
    suspend fun getBookAverageRating(bookTitle: String): Result<Float>
    suspend fun getChapterAverageRating(bookTitle: String, chapterName: String): Result<Float>
}
