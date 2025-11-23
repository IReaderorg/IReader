package ireader.data.review

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.time.ExperimentalTime

class ReviewRepositoryImpl(
    private val handler: DatabaseHandler,
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : ReviewRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
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
            val queryResult = backendService.query(
                table = "book_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized
                )
            ).getOrThrow()
            
            val resultJson = queryResult.firstOrNull()
            resultJson?.let {
                val result = json.decodeFromJsonElement(BookReviewDto.serializer(), it)
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
        }
    
    override suspend fun getBookReviews(bookTitle: String): Result<List<BookReview>> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            
            // Query with left join to get usernames
            val queryResult = backendService.query(
                table = "book_reviews",
                filters = mapOf("book_title" to normalized),
                columns = "*, users(username)"
            ).getOrThrow()
            
            val results = queryResult.map { 
                json.decodeFromJsonElement(BookReviewDto.serializer(), it) 
            }
            
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
            
            // Create JSON data
            val data = buildJsonObject {
                put("user_id", userId)
                put("book_title", normalized)
                put("rating", rating)
                put("review_text", reviewText)
            }
            
            // Insert using BackendService
            val insertResult = backendService.insert(
                table = "book_reviews",
                data = data,
                returning = true
            ).getOrThrow()
            
            val resultJson = insertResult ?: throw Exception("No result returned from insert")
            val result = json.decodeFromJsonElement(BookReviewDto.serializer(), resultJson)
            
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
            
            // Create JSON data
            val data = buildJsonObject {
                put("rating", rating)
                put("review_text", reviewText)
            }
            
            // Update using BackendService
            val updateResult = backendService.update(
                table = "book_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized
                ),
                data = data,
                returning = true
            ).getOrThrow()
            
            val resultJson = updateResult ?: throw Exception("No result returned from update")
            val result = json.decodeFromJsonElement(BookReviewDto.serializer(), resultJson)
            
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
            backendService.delete(
                table = "book_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized
                )
            ).getOrThrow()
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
            val queryResult = backendService.query(
                table = "chapter_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized,
                    "chapter_name" to chapterName
                )
            ).getOrThrow()
            
            val resultJson = queryResult.firstOrNull()
            resultJson?.let {
                val result = json.decodeFromJsonElement(ChapterReviewDto.serializer(), it)
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
        }
    
    override suspend fun getChapterReviews(bookTitle: String): Result<List<ChapterReview>> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val queryResult = backendService.query(
                table = "chapter_reviews",
                filters = mapOf("book_title" to normalized),
                columns = "*, users!inner(username)"
            ).getOrThrow()
            
            val results = queryResult.map { json.decodeFromJsonElement(ChapterReviewDto.serializer(), it) }
            
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
            
            // Create JSON data
            val data = buildJsonObject {
                put("user_id", userId)
                put("book_title", normalized)
                put("chapter_name", chapterName)
                put("rating", rating)
                put("review_text", reviewText)
            }
            
            // Insert using BackendService
            val insertResult = backendService.insert(
                table = "chapter_reviews",
                data = data,
                returning = true
            ).getOrThrow()
            
            val resultJson = insertResult ?: throw Exception("No result returned from insert")
            val result = json.decodeFromJsonElement(ChapterReviewDto.serializer(), resultJson)
            
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
            
            // Create JSON data
            val data = buildJsonObject {
                put("rating", rating)
                put("review_text", reviewText)
            }
            
            // Update using BackendService
            val updateResult = backendService.update(
                table = "chapter_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized,
                    "chapter_name" to chapterName
                ),
                data = data,
                returning = true
            ).getOrThrow()
            
            val resultJson = updateResult ?: throw Exception("No result returned from update")
            val result = json.decodeFromJsonElement(ChapterReviewDto.serializer(), resultJson)
            
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
            backendService.delete(
                table = "chapter_reviews",
                filters = mapOf(
                    "user_id" to userId,
                    "book_title" to normalized,
                    "chapter_name" to chapterName
                )
            ).getOrThrow()
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
            val queryResult = backendService.query(
                table = "book_reviews",
                filters = mapOf("book_title" to normalized),
                columns = "rating"
            ).getOrThrow()
            
            if (queryResult.isEmpty()) 0f
            else {
                val ratings = queryResult.map { 
                    json.decodeFromJsonElement(BookReviewDto.serializer(), it).rating 
                }
                ratings.average().toFloat()
            }
        }
    
    override suspend fun getChapterAverageRating(bookTitle: String, chapterName: String): Result<Float> = 
        RemoteErrorMapper.withErrorMapping {
            val normalized = normalizeTitle(bookTitle)
            val queryResult = backendService.query(
                table = "chapter_reviews",
                filters = mapOf(
                    "book_title" to normalized,
                    "chapter_name" to chapterName
                ),
                columns = "rating"
            ).getOrThrow()
            
            if (queryResult.isEmpty()) 0f
            else {
                val ratings = queryResult.map { 
                    json.decodeFromJsonElement(ChapterReviewDto.serializer(), it).rating 
                }
                ratings.average().toFloat()
            }
        }
}

