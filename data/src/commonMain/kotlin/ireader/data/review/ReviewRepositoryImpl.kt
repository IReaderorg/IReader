package ireader.data.review

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

class ReviewRepositoryImpl(
    private val handler: DatabaseHandler,
    private val supabaseClient: SupabaseClient
) : ReviewRepository {
    
    @Serializable
    private data class UserInfo(
        val username: String? = null
    )
    
    @Serializable
    private data class BookReviewDto(
        val id: String? = null,
        val user_id: String,
        val book_title: String,
        val rating: Int,
        val review_text: String,
        val created_at: String? = null,
        val updated_at: String? = null,
        val users: UserInfo? = null  // Nested user data from JOIN
    )
    
    @Serializable
    private data class ChapterReviewDto(
        val id: String? = null,
        val user_id: String,
        val book_title: String,
        val chapter_name: String,
        val rating: Int,
        val review_text: String,
        val created_at: String? = null,
        val updated_at: String? = null,
        val users: UserInfo? = null  // Nested user data from JOIN
    )
    
    @Serializable
    private data class RatingStats(
        val avg_rating: Double?
    )
    
    private fun normalizeTitle(title: String): String {
        return title.trim().lowercase()
    }
    
    @OptIn(ExperimentalTime::class)
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        return try {
            // Try to parse ISO 8601 timestamp to milliseconds
            // Format: 2024-01-15T10:30:00Z or 2024-01-15T10:30:00.123Z
            kotlinx.datetime.Instant.parse(timestamp).toEpochMilliseconds()
        } catch (e: Exception) {
            // Fallback: try to parse as long
            timestamp.toLongOrNull() ?: System.currentTimeMillis()
        }
    }
    
    // Book Reviews
    override suspend fun getBookReview(bookTitle: String): Result<BookReview?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val result = supabaseClient.postgrest["book_reviews"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                    }
                }
                .decodeSingleOrNull<BookReviewDto>()
            
            result?.let {
                BookReview(
                    id = it.id ?: "",
                    userId = it.user_id,
                    bookTitle = it.book_title,
                    rating = it.rating,
                    reviewText = it.review_text,
                    createdAt = parseTimestamp(it.created_at),
                    updatedAt = parseTimestamp(it.updated_at)
                )
            }
        }
    
    override suspend fun getBookReviews(bookTitle: String): Result<List<BookReview>> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val results: List<BookReviewDto> = supabaseClient.postgrest["book_reviews"]
                .select(columns = Columns.raw("*, users!inner(username)")) {
                    filter {
                        eq("book_title", normalized)
                    }
                }
                .decodeList()
            
            results.map {
                BookReview(
                    id = it.id ?: "",
                    userId = it.user_id,
                    bookTitle = it.book_title,
                    rating = it.rating,
                    reviewText = it.review_text,
                    createdAt = parseTimestamp(it.created_at),
                    updatedAt = parseTimestamp(it.updated_at),
                    username = it.users?.username
                )
            }
        }
    
    override suspend fun submitBookReview(bookTitle: String, rating: Int, reviewText: String): Result<BookReview> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val dto = BookReviewDto(
                user_id = userId,
                book_title = normalized,
                rating = rating,
                review_text = reviewText
            )
            
            // Simple insert - users can make multiple reviews
            val result = supabaseClient.postgrest["book_reviews"]
                .insert(dto) {
                    select()
                }
                .decodeSingle<BookReviewDto>()
            
            BookReview(
                id = result.id ?: "",
                userId = result.user_id,
                bookTitle = result.book_title,
                rating = result.rating,
                reviewText = result.review_text,
                createdAt = parseTimestamp(result.created_at),
                updatedAt = parseTimestamp(result.updated_at)
            )
        }
    
    override suspend fun updateBookReview(bookTitle: String, rating: Int, reviewText: String): Result<BookReview> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val dto = BookReviewDto(
                user_id = userId,
                book_title = normalized,
                rating = rating,
                review_text = reviewText
            )
            
            val result = supabaseClient.postgrest["book_reviews"]
                .update(dto) {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                    }
                }
                .decodeSingle<BookReviewDto>()
            
            BookReview(
                id = result.id ?: "",
                userId = result.user_id,
                bookTitle = result.book_title,
                rating = result.rating,
                reviewText = result.review_text,
                createdAt = parseTimestamp(result.created_at),
                updatedAt = parseTimestamp(result.updated_at)
            )
        }
    
    override suspend fun deleteBookReview(bookTitle: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            supabaseClient.postgrest["book_reviews"]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                    }
                }
        }
    
    override fun observeBookReview(bookTitle: String): Flow<BookReview?> = flow {
        getBookReview(bookTitle).onSuccess { review ->
            emit(review)
        }
    }
    
    // Chapter Reviews
    override suspend fun getChapterReview(bookTitle: String, chapterName: String): Result<ChapterReview?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val result = supabaseClient.postgrest["chapter_reviews"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                        eq("chapter_name", chapterName)
                    }
                }
                .decodeSingleOrNull<ChapterReviewDto>()
            
            result?.let {
                ChapterReview(
                    id = it.id ?: "",
                    userId = it.user_id,
                    bookTitle = it.book_title,
                    chapterName = it.chapter_name,
                    rating = it.rating,
                    reviewText = it.review_text,
                    createdAt = parseTimestamp(it.created_at),
                    updatedAt = parseTimestamp(it.updated_at)
                )
            }
        }
    
    override suspend fun getChapterReviews(bookTitle: String): Result<List<ChapterReview>> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val results: List<ChapterReviewDto> = supabaseClient.postgrest["chapter_reviews"]
                .select(columns = Columns.raw("*, users!inner(username)")) {
                    filter {
                        eq("book_title", normalized)
                    }
                }
                .decodeList()
            
            results.map {
                ChapterReview(
                    id = it.id ?: "",
                    userId = it.user_id,
                    bookTitle = it.book_title,
                    chapterName = it.chapter_name,
                    rating = it.rating,
                    reviewText = it.review_text,
                    createdAt = parseTimestamp(it.created_at),
                    updatedAt = parseTimestamp(it.updated_at),
                    username = it.users?.username
                )
            }
        }
    
    override suspend fun submitChapterReview(bookTitle: String, chapterName: String, rating: Int, reviewText: String): Result<ChapterReview> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val dto = ChapterReviewDto(
                user_id = userId,
                book_title = normalized,
                chapter_name = chapterName,
                rating = rating,
                review_text = reviewText
            )
            
            // Simple insert - users can make multiple reviews
            val result = supabaseClient.postgrest["chapter_reviews"]
                .insert(dto) {
                    select()
                }
                .decodeSingle<ChapterReviewDto>()
            
            ChapterReview(
                id = result.id ?: "",
                userId = result.user_id,
                bookTitle = result.book_title,
                chapterName = result.chapter_name,
                rating = result.rating,
                reviewText = result.review_text,
                createdAt = parseTimestamp(result.created_at),
                updatedAt = parseTimestamp(result.updated_at)
            )
        }
    
    override suspend fun updateChapterReview(bookTitle: String, chapterName: String, rating: Int, reviewText: String): Result<ChapterReview> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            val dto = ChapterReviewDto(
                user_id = userId,
                book_title = normalized,
                chapter_name = chapterName,
                rating = rating,
                review_text = reviewText
            )
            
            val result = supabaseClient.postgrest["chapter_reviews"]
                .update(dto) {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                        eq("chapter_name", chapterName)
                    }
                }
                .decodeSingle<ChapterReviewDto>()
            
            ChapterReview(
                id = result.id ?: "",
                userId = result.user_id,
                bookTitle = result.book_title,
                chapterName = result.chapter_name,
                rating = result.rating,
                reviewText = result.review_text,
                createdAt = parseTimestamp(result.created_at),
                updatedAt = parseTimestamp(result.updated_at)
            )
        }
    
    override suspend fun deleteChapterReview(bookTitle: String, chapterName: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            val normalized = normalizeTitle(bookTitle)
            supabaseClient.postgrest["chapter_reviews"]
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("book_title", normalized)
                        eq("chapter_name", chapterName)
                    }
                }
        }
    
    override fun observeChapterReview(bookTitle: String, chapterName: String): Flow<ChapterReview?> = flow {
        getChapterReview(bookTitle, chapterName).onSuccess { review ->
            emit(review)
        }
    }
    
    // Aggregate stats
    override suspend fun getBookAverageRating(bookTitle: String): Result<Float> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val result: List<BookReviewDto> = supabaseClient.postgrest["book_reviews"]
                .select(columns = Columns.list("rating")) {
                    filter {
                        eq("book_title", normalized)
                    }
                }
                .decodeList()
            
            if (result.isEmpty()) 0f
            else result.map { it.rating }.average().toFloat()
        }
    
    override suspend fun getChapterAverageRating(bookTitle: String, chapterName: String): Result<Float> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val result: List<ChapterReviewDto> = supabaseClient.postgrest["chapter_reviews"]
                .select(columns = Columns.list("rating")) {
                    filter {
                        eq("book_title", normalized)
                        eq("chapter_name", chapterName)
                    }
                }
                .decodeList()
            
            if (result.isEmpty()) 0f
            else result.map { it.rating }.average().toFloat()
        }
}

