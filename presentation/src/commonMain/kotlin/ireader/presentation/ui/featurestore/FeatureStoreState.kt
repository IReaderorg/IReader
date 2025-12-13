package ireader.presentation.ui.featurestore

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginType
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.SortOrder

/**
 * State for the Feature Store screen
 */
data class FeatureStoreState(
    val plugins: List<PluginInfo> = emptyList(),
    val filteredPlugins: List<PluginInfo> = emptyList(),
    val featuredPlugins: List<PluginInfo> = emptyList(),
    val selectedCategory: PluginType? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.POPULARITY,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val minRating: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)
