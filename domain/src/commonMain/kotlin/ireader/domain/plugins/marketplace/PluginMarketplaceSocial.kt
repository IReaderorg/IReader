package ireader.domain.plugins.marketplace

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Plugin Marketplace Social Features
 * 
 * Features:
 * - Plugin collections/bundles curated by users
 * - Follow developers
 * - Plugin recommendations
 * - User reviews and ratings
 * - Activity feed
 */

/**
 * A curated collection of plugins.
 */
@Serializable
data class PluginCollection(
    val id: String,
    val name: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val pluginIds: List<String>,
    val coverImageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val isFeatured: Boolean = false,
    val likesCount: Int = 0,
    val savesCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Developer profile information.
 */
@Serializable
data class DeveloperProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val website: String? = null,
    val pluginCount: Int = 0,
    val totalDownloads: Long = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val joinedAt: Long,
    val badges: List<DeveloperBadge> = emptyList()
)

/**
 * Developer badges for achievements.
 */
@Serializable
data class DeveloperBadge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedAt: Long
)

/**
 * Plugin recommendation.
 */
@Serializable
data class PluginRecommendation(
    val pluginId: String,
    val pluginName: String,
    val pluginIconUrl: String?,
    val reason: RecommendationReason,
    val score: Float,
    val basedOnPluginIds: List<String> = emptyList()
)

@Serializable
enum class RecommendationReason {
    SIMILAR_TO_INSTALLED,
    POPULAR_IN_CATEGORY,
    TRENDING,
    FROM_FOLLOWED_DEVELOPER,
    FREQUENTLY_USED_TOGETHER,
    HIGHLY_RATED,
    NEW_RELEASE,
    PERSONALIZED
}

/**
 * User activity in the marketplace.
 */
@Serializable
sealed class MarketplaceActivity {
    abstract val id: String
    abstract val userId: String
    abstract val timestamp: Long
    
    @Serializable
    data class PluginInstalled(
        override val id: String,
        override val userId: String,
        override val timestamp: Long,
        val pluginId: String,
        val pluginName: String
    ) : MarketplaceActivity()
    
    @Serializable
    data class PluginReviewed(
        override val id: String,
        override val userId: String,
        override val timestamp: Long,
        val pluginId: String,
        val pluginName: String,
        val rating: Float,
        val reviewSnippet: String?
    ) : MarketplaceActivity()
    
    @Serializable
    data class CollectionCreated(
        override val id: String,
        override val userId: String,
        override val timestamp: Long,
        val collectionId: String,
        val collectionName: String
    ) : MarketplaceActivity()
    
    @Serializable
    data class DeveloperFollowed(
        override val id: String,
        override val userId: String,
        override val timestamp: Long,
        val developerId: String,
        val developerName: String
    ) : MarketplaceActivity()
    
    @Serializable
    data class PluginPublished(
        override val id: String,
        override val userId: String,
        override val timestamp: Long,
        val pluginId: String,
        val pluginName: String,
        val version: String
    ) : MarketplaceActivity()
}

/**
 * Follow relationship between users.
 */
@Serializable
data class FollowRelationship(
    val followerId: String,
    val followingId: String,
    val createdAt: Long,
    val notificationsEnabled: Boolean = true
)

/**
 * Plugin review with social features.
 */
@Serializable
data class PluginReview(
    val id: String,
    val pluginId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val rating: Float,
    val title: String?,
    val content: String,
    val helpfulCount: Int = 0,
    val replyCount: Int = 0,
    val isVerifiedPurchase: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val developerResponse: DeveloperResponse? = null
)

@Serializable
data class DeveloperResponse(
    val content: String,
    val respondedAt: Long
)

/**
 * Trending plugin information.
 */
@Serializable
data class TrendingPlugin(
    val pluginId: String,
    val pluginName: String,
    val pluginIconUrl: String?,
    val developerName: String,
    val trendScore: Float,
    val installsThisWeek: Int,
    val ratingChange: Float,
    val rank: Int,
    val previousRank: Int?
)

/**
 * Search filters for marketplace.
 */
@Serializable
data class MarketplaceSearchFilters(
    val query: String? = null,
    val types: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val minRating: Float? = null,
    val priceRange: PriceRange? = null,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val developerId: String? = null,
    val tags: List<String> = emptyList(),
    val onlyVerified: Boolean = false,
    val onlyFree: Boolean = false
)

@Serializable
data class PriceRange(
    val min: Float?,
    val max: Float?
)

@Serializable
enum class SortOption {
    RELEVANCE,
    DOWNLOADS,
    RATING,
    NEWEST,
    RECENTLY_UPDATED,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    TRENDING
}
