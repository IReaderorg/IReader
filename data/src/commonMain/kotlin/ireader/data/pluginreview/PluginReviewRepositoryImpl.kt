package ireader.data.pluginreview

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.PluginReviewRepository
import ireader.domain.models.remote.PluginRatingStats
import ireader.domain.models.remote.PluginReview
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.ExperimentalTime

class PluginReviewRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : PluginReviewRepository {

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
    private data class PluginReviewDto(
        @SerialName("id") val id: String? = null,
        @SerialName("plugin_id") val pluginId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("rating") val rating: Int,
        @SerialName("review_text") val reviewText: String? = null,
        @SerialName("helpful_count") val helpfulCount: Int = 0,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("updated_at") val updatedAt: String? = null,
        @SerialName("users") val users: UserInfo? = null,
        @SerialName("username") val username: String? = null,  // Direct from RPC function
        @SerialName("is_helpful") val isHelpful: Boolean = false
    )

    @Serializable
    private data class PluginRatingStatsDto(
        @SerialName("plugin_id") val pluginId: String,
        @SerialName("average_rating") val averageRating: Double = 0.0,
        @SerialName("total_reviews") val totalReviews: Int = 0,
        @SerialName("rating_1_count") val rating1Count: Int = 0,
        @SerialName("rating_2_count") val rating2Count: Int = 0,
        @SerialName("rating_3_count") val rating3Count: Int = 0,
        @SerialName("rating_4_count") val rating4Count: Int = 0,
        @SerialName("rating_5_count") val rating5Count: Int = 0
    )

    @Serializable
    private data class SubmitReviewResult(
        @SerialName("success") val success: Boolean,
        @SerialName("review_id") val reviewId: String? = null,
        @SerialName("error") val error: String? = null
    )

    @OptIn(ExperimentalTime::class)
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return currentTimeToLong()
        return try {
            kotlin.time.Instant.parse(timestamp).toEpochMilliseconds()
        } catch (e: Exception) {
            timestamp.toLongOrNull() ?: currentTimeToLong()
        }
    }

    private fun PluginReviewDto.toDomain(): PluginReview {
        return PluginReview(
            id = id ?: "",
            pluginId = pluginId,
            userId = userId,
            username = username ?: users?.username ?: "Anonymous",  // Try direct field first, then nested
            rating = rating,
            reviewText = reviewText,
            helpfulCount = helpfulCount,
            isHelpful = isHelpful,
            createdAt = parseTimestamp(createdAt),
            updatedAt = parseTimestamp(updatedAt)
        )
    }

    override suspend fun getPluginReviews(
        pluginId: String,
        limit: Int,
        offset: Int,
        orderBy: String
    ): Result<List<PluginReview>> = RemoteErrorMapper.withErrorMapping {
        // Try to use the RPC function first - it handles the user join properly
        try {
            val result = backendService.rpc(
                function = "get_plugin_reviews",
                parameters = mapOf(
                    "p_plugin_id" to pluginId,
                    "p_limit" to limit,
                    "p_offset" to offset,
                    "p_order_by" to orderBy
                )
            ).getOrThrow()

            // Parse the result as a list - use explicit serializer to avoid reflection
            when (result) {
                is JsonArray -> result.map { element ->
                    json.decodeFromJsonElement(PluginReviewDto.serializer(), element).toDomain()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            // Fallback to direct query if RPC fails (without user join - no FK relationship)
            val queryResult = backendService.query(
                table = "plugin_reviews",
                filters = mapOf("plugin_id" to pluginId),
                columns = "*",  // No user join - FK is to auth.users, not public.users
                orderBy = when (orderBy) {
                    "helpful" -> "helpful_count"
                    "rating" -> "rating"
                    else -> "created_at"
                },
                ascending = false,
                limit = limit,
                offset = offset
            ).getOrThrow()

            queryResult.map { element ->
                json.decodeFromJsonElement(PluginReviewDto.serializer(), element).toDomain()
            }
        }
    }

    override suspend fun getUserReview(pluginId: String): Result<PluginReview?> =
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")

            val queryResult = backendService.query(
                table = "plugin_reviews",
                filters = mapOf(
                    "plugin_id" to pluginId,
                    "user_id" to userId
                ),
                columns = "*"  // No user join - FK is to auth.users, not public.users
            ).getOrThrow()

            queryResult.firstOrNull()?.let { element ->
                json.decodeFromJsonElement(PluginReviewDto.serializer(), element).toDomain()
            }
        }

    /**
     * Internal function to check if user has an existing review (simpler query, no exceptions)
     */
    private suspend fun hasExistingReview(pluginId: String, userId: String): Boolean {
        return try {
            val queryResult = backendService.query(
                table = "plugin_reviews",
                filters = mapOf(
                    "plugin_id" to pluginId,
                    "user_id" to userId
                ),
                columns = "id",  // Only need to check existence
                limit = 1
            ).getOrNull()
            !queryResult.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun submitReview(
        pluginId: String,
        rating: Int,
        reviewText: String?
    ): Result<PluginReview> = RemoteErrorMapper.withErrorMapping {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("User not authenticated")

        // Try RPC function first (handles upsert properly in SQL)
        try {
            val result = backendService.rpc(
                function = "submit_plugin_review",
                parameters = buildMap {
                    put("p_plugin_id", pluginId)
                    put("p_rating", rating)
                    if (reviewText != null) put("p_review_text", reviewText)
                }
            ).getOrThrow()

            val submitResult = json.decodeFromJsonElement(SubmitReviewResult.serializer(), result)
            if (!submitResult.success) {
                throw Exception(submitResult.error ?: "Failed to submit review")
            }

            // Fetch the created/updated review
            getUserReview(pluginId).getOrThrow()
                ?: throw Exception("Review not found after submission")
        } catch (e: Exception) {
            // Fallback: check if review exists, then update or insert
            val reviewExists = hasExistingReview(pluginId, userId)
            
            if (reviewExists) {
                // Update existing review
                val updateData = buildJsonObject {
                    put("rating", rating)
                    if (reviewText != null) put("review_text", reviewText)
                }
                
                backendService.update(
                    table = "plugin_reviews",
                    filters = mapOf(
                        "plugin_id" to pluginId,
                        "user_id" to userId
                    ),
                    data = updateData,
                    returning = true
                ).getOrThrow()
            } else {
                // Insert new review
                val insertData = buildJsonObject {
                    put("plugin_id", pluginId)
                    put("user_id", userId)
                    put("rating", rating)
                    if (reviewText != null) put("review_text", reviewText)
                }
                
                backendService.insert(
                    table = "plugin_reviews",
                    data = insertData,
                    returning = true
                ).getOrThrow()
            }

            getUserReview(pluginId).getOrThrow()
                ?: throw Exception("Review not found after submission")
        }
    }

    override suspend fun deleteReview(pluginId: String): Result<Unit> =
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")

            backendService.delete(
                table = "plugin_reviews",
                filters = mapOf(
                    "plugin_id" to pluginId,
                    "user_id" to userId
                )
            ).getOrThrow()
        }

    override suspend fun markReviewHelpful(reviewId: String): Result<Boolean> =
        RemoteErrorMapper.withErrorMapping {
            try {
                val result = backendService.rpc(
                    function = "mark_review_helpful",
                    parameters = mapOf("p_review_id" to reviewId)
                ).getOrThrow()

                when (result) {
                    is kotlinx.serialization.json.JsonPrimitive -> result.content.toBoolean()
                    else -> true
                }
            } catch (e: Exception) {
                // Fallback: direct insert
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated")

                val data = buildJsonObject {
                    put("review_id", reviewId)
                    put("user_id", userId)
                }

                backendService.insert(
                    table = "plugin_review_helpful",
                    data = data,
                    returning = false
                ).getOrThrow()

                true
            }
        }

    override suspend fun unmarkReviewHelpful(reviewId: String): Result<Boolean> =
        RemoteErrorMapper.withErrorMapping {
            try {
                val result = backendService.rpc(
                    function = "unmark_review_helpful",
                    parameters = mapOf("p_review_id" to reviewId)
                ).getOrThrow()

                when (result) {
                    is kotlinx.serialization.json.JsonPrimitive -> result.content.toBoolean()
                    else -> true
                }
            } catch (e: Exception) {
                // Fallback: direct delete
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated")

                backendService.delete(
                    table = "plugin_review_helpful",
                    filters = mapOf(
                        "review_id" to reviewId,
                        "user_id" to userId
                    )
                ).getOrThrow()

                true
            }
        }

    override suspend fun getRatingStats(pluginId: String): Result<PluginRatingStats?> =
        RemoteErrorMapper.withErrorMapping {
            val queryResult = backendService.query(
                table = "plugin_rating_stats",
                filters = mapOf("plugin_id" to pluginId)
            ).getOrThrow()

            queryResult.firstOrNull()?.let { element ->
                val dto = json.decodeFromJsonElement(PluginRatingStatsDto.serializer(), element)
                PluginRatingStats(
                    pluginId = dto.pluginId,
                    averageRating = dto.averageRating.toFloat(),
                    totalReviews = dto.totalReviews,
                    rating1Count = dto.rating1Count,
                    rating2Count = dto.rating2Count,
                    rating3Count = dto.rating3Count,
                    rating4Count = dto.rating4Count,
                    rating5Count = dto.rating5Count
                )
            }
        }

    override fun observePluginReviews(pluginId: String): Flow<List<PluginReview>> = flow {
        getPluginReviews(pluginId).onSuccess { reviews ->
            emit(reviews)
        }.onFailure {
            emit(emptyList())
        }
    }
}
