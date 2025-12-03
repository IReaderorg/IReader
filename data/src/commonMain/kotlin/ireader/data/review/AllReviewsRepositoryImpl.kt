package ireader.data.review

import io.github.jan.supabase.SupabaseClient
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.AllReviewsRepository
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.ChapterReview
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import ireader.domain.utils.extensions.currentTimeToLong

class AllReviewsRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : AllReviewsRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class UserInfo(
        @SerialName("username") val username: String? = null
    )
    
    @Serializable
    private data class BadgeInfo(
        @SerialName("badge_id") val badgeId: String? = null,
        @SerialName("badge_name") val badgeName: String? = null,
        @SerialName("badge_icon") val badgeIcon: String? = null,
        @SerialName("badge_image_url") val badgeImageUrl: String? = null
    )
    
    @Serializable
    private data class BookReviewDto(
        @SerialName("id") val id: String? = null,
        @SerialName("user_id") val userId: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("rating") val rating: Int,
        @SerialName("review_text") val reviewText: String,
        @SerialName("created_at") val createdAt: String? = null,
        // Nested format (from simple query with join)
        @SerialName("users") val users: UserInfo? = null,
        @SerialName("primary_badge") val primaryBadge: BadgeInfo? = null,
        // Flat format (from SQL function)
        @SerialName("username") val username: String? = null,
        @SerialName("badge_id") val badgeId: String? = null,
        @SerialName("badge_name") val badgeName: String? = null,
        @SerialName("badge_icon") val badgeIcon: String? = null,
        @SerialName("badge_image_url") val badgeImageUrl: String? = null
    )
    
    @Serializable
    private data class ChapterReviewDto(
        @SerialName("id") val id: String? = null,
        @SerialName("user_id") val userId: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("chapter_name") val chapterName: String,
        @SerialName("rating") val rating: Int,
        @SerialName("review_text") val reviewText: String,
        @SerialName("created_at") val createdAt: String? = null,
        // Nested format (from simple query with join)
        @SerialName("users") val users: UserInfo? = null,
        @SerialName("primary_badge") val primaryBadge: BadgeInfo? = null,
        // Flat format (from SQL function)
        @SerialName("username") val username: String? = null,
        @SerialName("badge_id") val badgeId: String? = null,
        @SerialName("badge_name") val badgeName: String? = null,
        @SerialName("badge_icon") val badgeIcon: String? = null,
        @SerialName("badge_image_url") val badgeImageUrl: String? = null
    )
    
    @OptIn(ExperimentalTime::class)
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return currentTimeToLong()
        return try {
            kotlinx.datetime.Instant.parse(timestamp).toEpochMilliseconds()
        } catch (e: Exception) {
            timestamp.toLongOrNull() ?: currentTimeToLong()
        }
    }
    
    override suspend fun getAllBookReviews(limit: Int, offset: Int): Result<List<BookReview>> =
        RemoteErrorMapper.withErrorMapping {
            // Try using the SQL function first
            try {
                val rpcResult = backendService.rpc(
                    function = "get_book_reviews_with_badges",
                    parameters = mapOf(
                        "p_limit" to limit,
                        "p_offset" to offset
                    )
                ).getOrThrow()
                
                val results = if (rpcResult is kotlinx.serialization.json.JsonArray) {
                    rpcResult.map { json.decodeFromJsonElement(BookReviewDto.serializer(), it) }
                } else {
                    listOf(json.decodeFromJsonElement(BookReviewDto.serializer(), rpcResult))
                }
                
                results.map {
                    BookReview(
                        id = it.id ?: "",
                        userId = it.userId,
                        bookTitle = it.bookTitle,
                        rating = it.rating,
                        reviewText = it.reviewText,
                        createdAt = parseTimestamp(it.createdAt),
                        updatedAt = parseTimestamp(it.createdAt),
                        // Try flat format first (from SQL function), then nested format
                        username = it.username ?: it.users?.username ?: "User${it.userId.take(4)}",
                        userBadge = if (it.badgeId != null && it.badgeName != null && it.badgeIcon != null) {
                            // Flat format (from SQL function)
                            ireader.domain.models.remote.UserBadgeInfo(
                                badgeId = it.badgeId,
                                badgeName = it.badgeName,
                                badgeIcon = it.badgeIcon,
                                badgeImageUrl = it.badgeImageUrl
                            )
                        } else {
                            // Nested format (from simple query)
                            it.primaryBadge?.let { badge ->
                                ireader.domain.models.remote.UserBadgeInfo(
                                    badgeId = badge.badgeId ?: "",
                                    badgeName = badge.badgeName ?: "",
                                    badgeIcon = badge.badgeIcon ?: "",
                                    badgeImageUrl = badge.badgeImageUrl
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                // Fallback to simple query without badges
                val queryResult = backendService.query(
                    table = "book_reviews",
                    filters = emptyMap(),
                    columns = "*, users(username)",
                    orderBy = "created_at",
                    ascending = false,
                    limit = limit,
                    offset = offset
                ).getOrThrow()
                
                queryResult.map {
                    val dto = json.decodeFromJsonElement(BookReviewDto.serializer(), it)
                    BookReview(
                        id = dto.id ?: "",
                        userId = dto.userId,
                        bookTitle = dto.bookTitle,
                        rating = dto.rating,
                        reviewText = dto.reviewText,
                        createdAt = parseTimestamp(dto.createdAt),
                        updatedAt = parseTimestamp(dto.createdAt),
                        username = dto.users?.username ?: "User${dto.userId.take(4)}",
                        userBadge = null
                    )
                }
            }
        }
    
    override suspend fun getAllChapterReviews(limit: Int, offset: Int): Result<List<ChapterReview>> =
        RemoteErrorMapper.withErrorMapping {
            // Try using the SQL function first
            try {
                val rpcResult = backendService.rpc(
                    function = "get_chapter_reviews_with_badges",
                    parameters = mapOf(
                        "p_limit" to limit,
                        "p_offset" to offset
                    )
                ).getOrThrow()
                
                val results = if (rpcResult is kotlinx.serialization.json.JsonArray) {
                    rpcResult.map { json.decodeFromJsonElement(ChapterReviewDto.serializer(), it) }
                } else {
                    listOf(json.decodeFromJsonElement(ChapterReviewDto.serializer(), rpcResult))
                }
                
                results.map {
                    ChapterReview(
                        id = it.id ?: "",
                        userId = it.userId,
                        bookTitle = it.bookTitle,
                        chapterName = it.chapterName,
                        rating = it.rating,
                        reviewText = it.reviewText,
                        createdAt = parseTimestamp(it.createdAt),
                        updatedAt = parseTimestamp(it.createdAt),
                        // Try flat format first (from SQL function), then nested format
                        username = it.username ?: it.users?.username ?: "User${it.userId.take(4)}",
                        userBadge = if (it.badgeId != null && it.badgeName != null && it.badgeIcon != null) {
                            // Flat format (from SQL function)
                            ireader.domain.models.remote.UserBadgeInfo(
                                badgeId = it.badgeId,
                                badgeName = it.badgeName,
                                badgeIcon = it.badgeIcon,
                                badgeImageUrl = it.badgeImageUrl
                            )
                        } else {
                            // Nested format (from simple query)
                            it.primaryBadge?.let { badge ->
                                ireader.domain.models.remote.UserBadgeInfo(
                                    badgeId = badge.badgeId ?: "",
                                    badgeName = badge.badgeName ?: "",
                                    badgeIcon = badge.badgeIcon ?: "",
                                    badgeImageUrl = badge.badgeImageUrl
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                // Fallback to simple query without badges
                val queryResult = backendService.query(
                    table = "chapter_reviews",
                    filters = emptyMap(),
                    columns = "*, users(username)",
                    orderBy = "created_at",
                    ascending = false,
                    limit = limit,
                    offset = offset
                ).getOrThrow()
                
                queryResult.map {
                    val dto = json.decodeFromJsonElement(ChapterReviewDto.serializer(), it)
                    ChapterReview(
                        id = dto.id ?: "",
                        userId = dto.userId,
                        bookTitle = dto.bookTitle,
                        chapterName = dto.chapterName,
                        rating = dto.rating,
                        reviewText = dto.reviewText,
                        createdAt = parseTimestamp(dto.createdAt),
                        updatedAt = parseTimestamp(dto.createdAt),
                        username = dto.users?.username ?: "User${dto.userId.take(4)}",
                        userBadge = null
                    )
                }
            }
        }
    
    override suspend fun getBookReviewsForBook(bookTitle: String): Result<List<BookReview>> =
        RemoteErrorMapper.withErrorMapping {
            val normalized = bookTitle.trim().lowercase()
            val queryResult = backendService.query(
                table = "book_reviews",
                filters = mapOf("book_title" to normalized),
                columns = "*, users(username)",
                orderBy = "created_at",
                ascending = false
            ).getOrThrow()
            
            queryResult.map {
                val dto = json.decodeFromJsonElement(BookReviewDto.serializer(), it)
                BookReview(
                    id = dto.id ?: "",
                    userId = dto.userId,
                    bookTitle = dto.bookTitle,
                    rating = dto.rating,
                    reviewText = dto.reviewText,
                    createdAt = parseTimestamp(dto.createdAt),
                    updatedAt = parseTimestamp(dto.createdAt),
                    username = dto.users?.username
                )
            }
        }
    
    override suspend fun getChapterReviewsForBook(bookTitle: String): Result<List<ChapterReview>> =
        RemoteErrorMapper.withErrorMapping {
            val normalized = bookTitle.trim().lowercase()
            val queryResult = backendService.query(
                table = "chapter_reviews",
                filters = mapOf("book_title" to normalized),
                columns = "*, users(username)",
                orderBy = "created_at",
                ascending = false
            ).getOrThrow()
            
            queryResult.map {
                val dto = json.decodeFromJsonElement(ChapterReviewDto.serializer(), it)
                ChapterReview(
                    id = dto.id ?: "",
                    userId = dto.userId,
                    bookTitle = dto.bookTitle,
                    chapterName = dto.chapterName,
                    rating = dto.rating,
                    reviewText = dto.reviewText,
                    createdAt = parseTimestamp(dto.createdAt),
                    updatedAt = parseTimestamp(dto.createdAt),
                    username = dto.users?.username
                )
            }
        }
}
