package ireader.presentation.ui.plugins.marketplace

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginMonetization
import ireader.domain.plugins.PluginType
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Marketplace screen
 * Requirements: 2.1, 2.2, 2.3, 16.1, 16.2, 16.3, 16.4, 16.5
 */
class PluginMarketplaceViewModel(
    private val pluginManager: PluginManager
) : BaseViewModel() {
    
    private val _state = mutableStateOf(PluginMarketplaceState())
    val state: State<PluginMarketplaceState> = _state
    
    init {
        observePlugins()
        loadPlugins()
    }
    
    /**
     * Observe plugin changes from PluginManager
     */
    private fun observePlugins() {
        pluginManager.pluginsFlow
            .onEach { plugins ->
                _state.value = _state.value.copy(
                    plugins = plugins,
                    featuredPlugins = getFeaturedPlugins(plugins),
                    isLoading = false
                )
                applyFilters()
            }
            .launchIn(scope)
    }
    
    /**
     * Load plugins from PluginManager
     */
    fun loadPlugins() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                pluginManager.loadPlugins()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load plugins"
                )
            }
        }
    }
    
    /**
     * Refresh plugins (pull-to-refresh)
     */
    fun refreshPlugins() {
        _state.value = _state.value.copy(isRefreshing = true, error = null)
        scope.launch {
            try {
                pluginManager.loadPlugins()
                _state.value = _state.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Failed to refresh plugins"
                )
            }
        }
    }
    
    /**
     * Select a category filter
     */
    fun selectCategory(category: PluginType?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    /**
     * Update sort order
     */
    fun updateSortOrder(sortOrder: SortOrder) {
        _state.value = _state.value.copy(sortOrder = sortOrder)
        applyFilters()
    }
    
    /**
     * Update price filter
     */
    fun updatePriceFilter(priceFilter: PriceFilter) {
        _state.value = _state.value.copy(priceFilter = priceFilter)
        applyFilters()
    }
    
    /**
     * Update minimum rating filter
     */
    fun updateMinRating(rating: Float) {
        _state.value = _state.value.copy(minRating = rating)
        applyFilters()
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * Apply all filters and sorting to plugins list
     */
    private fun applyFilters() {
        var filtered = _state.value.plugins
        
        // Apply category filter
        _state.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.manifest.type == category }
        }
        
        // Apply search filter
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.manifest.name.lowercase().contains(query) ||
                it.manifest.description.lowercase().contains(query) ||
                it.manifest.author.name.lowercase().contains(query)
            }
        }
        
        // Apply price filter
        filtered = when (_state.value.priceFilter) {
            PriceFilter.ALL -> filtered
            PriceFilter.FREE -> filtered.filter { 
                it.manifest.monetization is PluginMonetization.Free 
            }
            PriceFilter.PAID -> filtered.filter { 
                it.manifest.monetization is PluginMonetization.Premium 
            }
            PriceFilter.FREEMIUM -> filtered.filter { 
                it.manifest.monetization is PluginMonetization.Freemium 
            }
        }
        
        // Apply rating filter
        if (_state.value.minRating > 0) {
            filtered = filtered.filter { 
                (it.rating ?: 0f) >= _state.value.minRating 
            }
        }
        
        // Apply sorting
        filtered = when (_state.value.sortOrder) {
            SortOrder.POPULARITY -> filtered.sortedByDescending { it.downloadCount }
            SortOrder.RATING -> filtered.sortedByDescending { it.rating ?: 0f }
            SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.installDate }
            SortOrder.PRICE_LOW_TO_HIGH -> filtered.sortedBy { getPluginPrice(it) }
            SortOrder.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { getPluginPrice(it) }
            SortOrder.NAME -> filtered.sortedBy { it.manifest.name }
        }
        
        _state.value = _state.value.copy(filteredPlugins = filtered)
    }
    
    /**
     * Get featured plugins (top rated and popular)
     */
    private fun getFeaturedPlugins(plugins: List<PluginInfo>): List<PluginInfo> {
        return plugins
            .filter { (it.rating ?: 0f) >= 4.0f || it.downloadCount > 1000 }
            .sortedByDescending { it.downloadCount }
            .take(10)
    }
    
    /**
     * Get plugin price for sorting
     */
    private fun getPluginPrice(plugin: PluginInfo): Double {
        return when (val monetization = plugin.manifest.monetization) {
            is PluginMonetization.Premium -> monetization.price
            is PluginMonetization.Freemium -> {
                monetization.features.minOfOrNull { it.price } ?: 0.0
            }
            is PluginMonetization.Free, null -> 0.0
        }
    }
}
