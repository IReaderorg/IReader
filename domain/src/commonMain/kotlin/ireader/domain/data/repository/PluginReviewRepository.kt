package ireader.domain.data.repository

import ireader.domain.models.remote.PluginRatingStats
import ireader.domain.models.remote.PluginReview
import kotlinx.coroutines.flow.Flow

/**
 * Repository for plugin reviews
 */
interface PluginReviewRepository {
    
    /**
     * Get reviews for a plugin
     * @param pluginId The plugin ID
     * @param limit Maximum number of reviews to return
     * @param offset Number of reviews to skip
     * @param orderBy Sort order: "created_at", "helpful", or "rating"
     */
    suspend fun getPluginReviews(
        pluginId: String,
        limit: Int = 20,
        offset: Int = 0,
        orderBy: String = "created_at"
    ): Result<List<PluginReview>>
    
    /**
     * Get the current user's review for a plugin
     */
    suspend fun getUserReview(pluginId: String): Result<PluginReview?>
    
    /**
     * Submit or update a review for a plugin
     */
    suspend fun submitReview(
        pluginId: String,
        rating: Int,
        reviewText: String?
    ): Result<PluginReview>
    
    /**
     * Delete the current user's review for a plugin
     */
    suspend fun deleteReview(pluginId: String): Result<Unit>
    
    /**
     * Mark a review as helpful
     */
    suspend fun markReviewHelpful(reviewId: String): Result<Boolean>
    
    /**
     * Unmark a review as helpful
     */
    suspend fun unmarkReviewHelpful(reviewId: String): Result<Boolean>
    
    /**
     * Get rating statistics for a plugin
     */
    suspend fun getRatingStats(pluginId: String): Result<PluginRatingStats?>
    
    /**
     * Observe reviews for a plugin
     */
    fun observePluginReviews(pluginId: String): Flow<List<PluginReview>>
}
