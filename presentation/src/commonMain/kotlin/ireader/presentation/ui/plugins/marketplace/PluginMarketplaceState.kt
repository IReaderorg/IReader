package ireader.presentation.ui.plugins.marketplace

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginRepositoryEntity
import ireader.domain.plugins.PluginType

/**
 * State for the Plugin Marketplace screen
 * Requirements: 2.1, 2.2, 2.3, 16.1, 16.2, 16.3, 16.4, 16.5
 */
data class PluginMarketplaceState(
    val plugins: List<PluginInfo> = emptyList(),
    val filteredPlugins: List<PluginInfo> = emptyList(),
    val featuredPlugins: List<PluginInfo> = emptyList(),
    val repositories: List<PluginRepositoryEntity> = emptyList(),
    val selectedCategory: PluginType? = null,
    val selectedRepository: PluginRepositoryEntity? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.POPULARITY,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val minRating: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * Sort order options for plugins
 */
enum class SortOrder {
    POPULARITY,
    RATING,
    DATE_ADDED,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    NAME
}

/**
 * Price filter options
 */
enum class PriceFilter {
    ALL,
    FREE,
    PAID,
    FREEMIUM
}
