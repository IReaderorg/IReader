package ireader.domain.models.remote

/**
 * Domain model for plugin reviews
 */
data class PluginReview(
    val id: String,
    val pluginId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val reviewText: String?,
    val helpfulCount: Int,
    val isHelpful: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Aggregated rating statistics for a plugin
 */
data class PluginRatingStats(
    val pluginId: String,
    val averageRating: Float,
    val totalReviews: Int,
    val rating1Count: Int,
    val rating2Count: Int,
    val rating3Count: Int,
    val rating4Count: Int,
    val rating5Count: Int
)
