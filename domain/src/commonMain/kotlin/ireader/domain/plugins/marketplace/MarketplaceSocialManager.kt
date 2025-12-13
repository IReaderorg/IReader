package ireader.domain.plugins.marketplace

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for marketplace social features.
 */
class MarketplaceSocialManager(
    private val socialRepository: MarketplaceSocialRepository,
    private val recommendationEngine: RecommendationEngine
) {
    private val _collections = MutableStateFlow<List<PluginCollection>>(emptyList())
    val collections: StateFlow<List<PluginCollection>> = _collections.asStateFlow()
    
    private val _followedDevelopers = MutableStateFlow<List<DeveloperProfile>>(emptyList())
    val followedDevelopers: StateFlow<List<DeveloperProfile>> = _followedDevelopers.asStateFlow()
    
    private val _activityFeed = MutableStateFlow<List<MarketplaceActivity>>(emptyList())
    val activityFeed: StateFlow<List<MarketplaceActivity>> = _activityFeed.asStateFlow()
    
    private val _recommendations = MutableStateFlow<List<PluginRecommendation>>(emptyList())
    val recommendations: StateFlow<List<PluginRecommendation>> = _recommendations.asStateFlow()
    
    // Collections
    
    suspend fun loadCollections() {
        _collections.value = socialRepository.getPublicCollections()
    }
    
    suspend fun loadUserCollections(userId: String) {
        _collections.value = socialRepository.getUserCollections(userId)
    }
    
    suspend fun createCollection(
        name: String,
        description: String,
        pluginIds: List<String>,
        isPublic: Boolean = true,
        tags: List<String> = emptyList()
    ): Result<PluginCollection> {
        return socialRepository.createCollection(name, description, pluginIds, isPublic, tags)
    }

    suspend fun updateCollection(collection: PluginCollection): Result<PluginCollection> {
        return socialRepository.updateCollection(collection)
    }
    
    suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return socialRepository.deleteCollection(collectionId)
    }
    
    suspend fun addPluginToCollection(collectionId: String, pluginId: String): Result<Unit> {
        return socialRepository.addPluginToCollection(collectionId, pluginId)
    }
    
    suspend fun removePluginFromCollection(collectionId: String, pluginId: String): Result<Unit> {
        return socialRepository.removePluginFromCollection(collectionId, pluginId)
    }
    
    suspend fun likeCollection(collectionId: String): Result<Unit> {
        return socialRepository.likeCollection(collectionId)
    }
    
    suspend fun saveCollection(collectionId: String): Result<Unit> {
        return socialRepository.saveCollection(collectionId)
    }
    
    // Developer Following
    
    suspend fun loadFollowedDevelopers() {
        _followedDevelopers.value = socialRepository.getFollowedDevelopers()
    }
    
    suspend fun followDeveloper(developerId: String): Result<Unit> {
        val result = socialRepository.followDeveloper(developerId)
        if (result.isSuccess) {
            loadFollowedDevelopers()
        }
        return result
    }
    
    suspend fun unfollowDeveloper(developerId: String): Result<Unit> {
        val result = socialRepository.unfollowDeveloper(developerId)
        if (result.isSuccess) {
            loadFollowedDevelopers()
        }
        return result
    }
    
    suspend fun isFollowing(developerId: String): Boolean {
        return socialRepository.isFollowing(developerId)
    }
    
    suspend fun getDeveloperProfile(developerId: String): DeveloperProfile? {
        return socialRepository.getDeveloperProfile(developerId)
    }
    
    suspend fun getDeveloperPlugins(developerId: String): List<String> {
        return socialRepository.getDeveloperPlugins(developerId)
    }
    
    // Activity Feed
    
    suspend fun loadActivityFeed() {
        _activityFeed.value = socialRepository.getActivityFeed()
    }
    
    suspend fun loadFollowingActivityFeed() {
        _activityFeed.value = socialRepository.getFollowingActivityFeed()
    }
    
    // Recommendations
    
    suspend fun loadRecommendations(installedPluginIds: List<String>) {
        _recommendations.value = recommendationEngine.getRecommendations(installedPluginIds)
    }
    
    suspend fun getTrendingPlugins(limit: Int = 20): List<TrendingPlugin> {
        return socialRepository.getTrendingPlugins(limit)
    }
    
    suspend fun getFeaturedCollections(): List<PluginCollection> {
        return socialRepository.getFeaturedCollections()
    }
    
    // Reviews
    
    suspend fun getPluginReviews(pluginId: String, page: Int = 0): List<PluginReview> {
        return socialRepository.getPluginReviews(pluginId, page)
    }
    
    suspend fun submitReview(
        pluginId: String,
        rating: Float,
        title: String?,
        content: String
    ): Result<PluginReview> {
        return socialRepository.submitReview(pluginId, rating, title, content)
    }
    
    suspend fun markReviewHelpful(reviewId: String): Result<Unit> {
        return socialRepository.markReviewHelpful(reviewId)
    }
    
    suspend fun respondToReview(reviewId: String, response: String): Result<Unit> {
        return socialRepository.respondToReview(reviewId, response)
    }
    
    // Search
    
    suspend fun search(filters: MarketplaceSearchFilters): MarketplaceSearchResult {
        return socialRepository.search(filters)
    }
}

/**
 * Search result from marketplace.
 */
data class MarketplaceSearchResult(
    val plugins: List<MarketplacePluginInfo>,
    val collections: List<PluginCollection>,
    val developers: List<DeveloperProfile>,
    val totalResults: Int,
    val page: Int,
    val hasMore: Boolean
)

/**
 * Plugin info for marketplace display.
 */
data class MarketplacePluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val developerName: String,
    val developerId: String,
    val rating: Float,
    val reviewCount: Int,
    val downloadCount: Long,
    val price: Float?,
    val isVerified: Boolean,
    val tags: List<String>,
    val lastUpdated: Long
)

/**
 * Repository interface for marketplace social features.
 */
interface MarketplaceSocialRepository {
    // Collections
    suspend fun getPublicCollections(): List<PluginCollection>
    suspend fun getUserCollections(userId: String): List<PluginCollection>
    suspend fun getFeaturedCollections(): List<PluginCollection>
    suspend fun createCollection(
        name: String,
        description: String,
        pluginIds: List<String>,
        isPublic: Boolean,
        tags: List<String>
    ): Result<PluginCollection>
    suspend fun updateCollection(collection: PluginCollection): Result<PluginCollection>
    suspend fun deleteCollection(collectionId: String): Result<Unit>
    suspend fun addPluginToCollection(collectionId: String, pluginId: String): Result<Unit>
    suspend fun removePluginFromCollection(collectionId: String, pluginId: String): Result<Unit>
    suspend fun likeCollection(collectionId: String): Result<Unit>
    suspend fun saveCollection(collectionId: String): Result<Unit>
    
    // Developers
    suspend fun getFollowedDevelopers(): List<DeveloperProfile>
    suspend fun followDeveloper(developerId: String): Result<Unit>
    suspend fun unfollowDeveloper(developerId: String): Result<Unit>
    suspend fun isFollowing(developerId: String): Boolean
    suspend fun getDeveloperProfile(developerId: String): DeveloperProfile?
    suspend fun getDeveloperPlugins(developerId: String): List<String>
    
    // Activity
    suspend fun getActivityFeed(): List<MarketplaceActivity>
    suspend fun getFollowingActivityFeed(): List<MarketplaceActivity>
    
    // Trending
    suspend fun getTrendingPlugins(limit: Int): List<TrendingPlugin>
    
    // Reviews
    suspend fun getPluginReviews(pluginId: String, page: Int): List<PluginReview>
    suspend fun submitReview(
        pluginId: String,
        rating: Float,
        title: String?,
        content: String
    ): Result<PluginReview>
    suspend fun markReviewHelpful(reviewId: String): Result<Unit>
    suspend fun respondToReview(reviewId: String, response: String): Result<Unit>
    
    // Search
    suspend fun search(filters: MarketplaceSearchFilters): MarketplaceSearchResult
}

/**
 * Engine for generating plugin recommendations.
 */
interface RecommendationEngine {
    suspend fun getRecommendations(installedPluginIds: List<String>): List<PluginRecommendation>
    suspend fun getSimilarPlugins(pluginId: String): List<PluginRecommendation>
    suspend fun getFrequentlyUsedTogether(pluginId: String): List<PluginRecommendation>
}
