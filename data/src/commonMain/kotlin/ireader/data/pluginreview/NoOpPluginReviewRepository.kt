package ireader.data.pluginreview

import ireader.domain.data.repository.PluginReviewRepository
import ireader.domain.models.remote.PluginRatingStats
import ireader.domain.models.remote.PluginReview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of PluginReviewRepository for when Supabase is not configured
 */
object NoOpPluginReviewRepository : PluginReviewRepository {
    
    override suspend fun getPluginReviews(
        pluginId: String,
        limit: Int,
        offset: Int,
        orderBy: String
    ): Result<List<PluginReview>> = Result.success(emptyList())
    
    override suspend fun getUserReview(pluginId: String): Result<PluginReview?> = 
        Result.success(null)
    
    override suspend fun submitReview(
        pluginId: String,
        rating: Int,
        reviewText: String?
    ): Result<PluginReview> = Result.failure(
        Exception("Reviews are not available - Supabase not configured")
    )
    
    override suspend fun deleteReview(pluginId: String): Result<Unit> = 
        Result.failure(Exception("Reviews are not available - Supabase not configured"))
    
    override suspend fun markReviewHelpful(reviewId: String): Result<Boolean> = 
        Result.failure(Exception("Reviews are not available - Supabase not configured"))
    
    override suspend fun unmarkReviewHelpful(reviewId: String): Result<Boolean> = 
        Result.failure(Exception("Reviews are not available - Supabase not configured"))
    
    override suspend fun getRatingStats(pluginId: String): Result<PluginRatingStats?> = 
        Result.success(null)
    
    override fun observePluginReviews(pluginId: String): Flow<List<PluginReview>> = 
        flowOf(emptyList())
}
