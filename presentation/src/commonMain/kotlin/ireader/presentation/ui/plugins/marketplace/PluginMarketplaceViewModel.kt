package ireader.presentation.ui.plugins.marketplace

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.PluginIndexEntry
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginRepositoryEntity
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import ireader.domain.plugins.PluginRepositoryRepository
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.plugin.api.PluginAuthor
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginMonetization
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Marketplace screen
 * Requirements: 2.1, 2.2, 2.3, 16.1, 16.2, 16.3, 16.4, 16.5
 */
class PluginMarketplaceViewModel(
    private val pluginManager: PluginManager,
    private val repositoryRepository: PluginRepositoryRepository,
    private val indexFetcher: PluginRepositoryIndexFetcher
) : BaseViewModel() {

    private val _state = mutableStateOf(PluginMarketplaceState())
    val state: State<PluginMarketplaceState> = _state

    // Cache of installed plugin IDs for quick lookup
    private var installedPluginIds: Set<String> = emptySet()

    init {
        observeInstalledPlugins()
        observeRepositories()
        loadPlugins()
    }

    /**
     * Observe installed plugins from PluginManager
     */
    private fun observeInstalledPlugins() {
        pluginManager.pluginsFlow
            .onEach { plugins ->
                installedPluginIds = plugins.map { it.id }.toSet()
                // Update installed status in current plugins
                updateInstalledStatus()
            }
            .launchIn(scope)
    }

    /**
     * Observe repository changes
     */
    private fun observeRepositories() {
        repositoryRepository.getEnabled()
            .onEach { repositories ->
                _state.value = _state.value.copy(repositories = repositories)
            }
            .launchIn(scope)
    }

    /**
     * Load plugins from all enabled repositories
     */
    fun loadPlugins() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                val repositories = repositoryRepository.getEnabled().first()
                
                if (repositories.isEmpty()) {
                    _state.value = _state.value.copy(
                        plugins = emptyList(),
                        filteredPlugins = emptyList(),
                        featuredPlugins = emptyList(),
                        isLoading = false,
                        error = "No plugin repositories configured"
                    )
                    return@launch
                }

                val allPlugins = mutableListOf<PluginInfo>()
                val errors = mutableListOf<String>()

                // Fetch plugins from all enabled repositories in parallel
                val results = repositories.map { repo ->
                    async {
                        repo to fetchPluginsFromRepository(repo)
                    }
                }.awaitAll()

                // Combine results and collect errors
                results.forEach { (repo, result) ->
                    result.onSuccess { plugins ->
                        allPlugins.addAll(plugins)
                    }.onFailure { error ->
                        errors.add("${repo.name}: ${error.message}")
                    }
                }

                // Remove duplicates (prefer first occurrence)
                val uniquePlugins = allPlugins.distinctBy { it.id }

                _state.value = _state.value.copy(
                    plugins = uniquePlugins,
                    featuredPlugins = getFeaturedPlugins(uniquePlugins),
                    isLoading = false,
                    error = if (uniquePlugins.isEmpty() && errors.isNotEmpty()) {
                        "Failed to load plugins: ${errors.joinToString("; ")}"
                    } else null
                )
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load plugins"
                )
            }
        }
    }

    /**
     * Fetch plugins from a single repository
     */
    private suspend fun fetchPluginsFromRepository(
        repository: PluginRepositoryEntity
    ): Result<List<PluginInfo>> {
        return try {
            val indexResult = indexFetcher.fetchIndex(repository.url)
            indexResult.map { index ->
                // Update repository plugin count
                repositoryRepository.updatePluginCount(
                    repository.id,
                    index.plugins.size,
                    currentTimeToLong()
                )

                // Convert index entries to PluginInfo
                index.plugins.map { entry ->
                    entry.toPluginInfo(repository)
                }
            }
        } catch (e: Exception) {
            repositoryRepository.updateError(
                repository.id,
                e.message,
                currentTimeToLong()
            )
            Result.failure(e)
        }
    }

    /**
     * Refresh plugins (pull-to-refresh)
     */
    fun refreshPlugins() {
        _state.value = _state.value.copy(isRefreshing = true, error = null)
        scope.launch {
            try {
                val repositories = repositoryRepository.getEnabled().first()
                val allPlugins = mutableListOf<PluginInfo>()

                val results = repositories.map { repo ->
                    async { fetchPluginsFromRepository(repo) }
                }.awaitAll()

                results.forEach { result ->
                    result.onSuccess { plugins ->
                        allPlugins.addAll(plugins)
                    }
                }

                val uniquePlugins = allPlugins.distinctBy { it.id }

                _state.value = _state.value.copy(
                    plugins = uniquePlugins,
                    featuredPlugins = getFeaturedPlugins(uniquePlugins),
                    isRefreshing = false
                )
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Failed to refresh plugins"
                )
            }
        }
    }

    /**
     * Update installed status for all plugins
     */
    private fun updateInstalledStatus() {
        val updatedPlugins = _state.value.plugins.map { plugin ->
            if (plugin.id in installedPluginIds) {
                plugin.copy(status = PluginStatus.ENABLED)
            } else {
                plugin.copy(status = PluginStatus.NOT_INSTALLED)
            }
        }
        _state.value = _state.value.copy(plugins = updatedPlugins)
        applyFilters()
    }

    /**
     * Convert PluginIndexEntry to PluginInfo
     */
    private fun PluginIndexEntry.toPluginInfo(repository: PluginRepositoryEntity): PluginInfo {
        val pluginType = try {
            PluginType.valueOf(type.uppercase())
        } catch (e: Exception) {
            PluginType.FEATURE
        }

        val monetization = when (monetization?.type?.uppercase()) {
            "PREMIUM" -> PluginMonetization.Premium(
                price = monetization?.price ?: 0.0,
                currency = monetization?.currency ?: "USD"
            )
            "FREEMIUM" -> PluginMonetization.Freemium(
                features = emptyList()
            )
            else -> PluginMonetization.Free
        }

        // Convert string platforms to Platform enum
        val platformList = platforms.mapNotNull { platformStr ->
            try {
                ireader.plugin.api.Platform.valueOf(platformStr.uppercase())
            } catch (e: Exception) {
                null
            }
        }.ifEmpty {
            // Default to all platforms if none specified
            listOf(
                ireader.plugin.api.Platform.ANDROID,
                ireader.plugin.api.Platform.IOS,
                ireader.plugin.api.Platform.DESKTOP
            )
        }

        // Resolve download URL - if relative, combine with repository base URL
        val resolvedDownloadUrl = resolveDownloadUrl(downloadUrl, repository.url)

        return PluginInfo(
            id = id,
            manifest = PluginManifest(
                id = id,
                name = name,
                version = version,
                versionCode = versionCode,
                description = description,
                author = PluginAuthor(
                    name = author.name,
                    email = author.email,
                    website = author.website
                ),
                type = pluginType,
                permissions = emptyList(),
                minIReaderVersion = minIReaderVersion,
                platforms = platformList,
                iconUrl = iconUrl,
                monetization = monetization
            ),
            status = if (id in installedPluginIds) PluginStatus.ENABLED else PluginStatus.NOT_INSTALLED,
            installDate = null,
            rating = null,
            downloadCount = 0,
            repositoryUrl = repository.url,
            downloadUrl = resolvedDownloadUrl,
            fileSize = fileSize,
            checksum = checksum
        )
    }
    
    /**
     * Resolve download URL - handles both absolute and relative URLs
     */
    private fun resolveDownloadUrl(downloadUrl: String, repositoryUrl: String): String {
        // If already absolute URL, return as-is
        if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
            return downloadUrl
        }
        
        // Get base URL from repository URL (remove index.json or similar)
        val baseUrl = repositoryUrl.substringBeforeLast("/")
        
        // Combine base URL with relative path
        return if (downloadUrl.startsWith("/")) {
            "$baseUrl$downloadUrl"
        } else {
            "$baseUrl/$downloadUrl"
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
    
    /**
     * Install a plugin from the marketplace
     */
    fun installPlugin(pluginId: String) {
        val plugin = _state.value.plugins.find { it.id == pluginId } ?: return
        
        // Check if plugin has a download URL
        if (plugin.downloadUrl.isNullOrBlank()) {
            _state.value = _state.value.copy(
                error = "Plugin ${plugin.manifest.name} has no download URL configured"
            )
            return
        }
        
        // Update state to show installing
        updatePluginStatus(pluginId, PluginStatus.UPDATING)
        
        scope.launch {
            try {
                pluginManager.installPlugin(plugin)
                    .onSuccess {
                        updatePluginStatus(pluginId, PluginStatus.ENABLED)
                        showSnackBar(ireader.i18n.UiText.DynamicString("${plugin.manifest.name} installed successfully"))
                    }
                    .onFailure { error ->
                        updatePluginStatus(pluginId, PluginStatus.ERROR)
                        _state.value = _state.value.copy(
                            error = "Failed to install ${plugin.manifest.name}: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                updatePluginStatus(pluginId, PluginStatus.ERROR)
                _state.value = _state.value.copy(
                    error = "Failed to install ${plugin.manifest.name}: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update a single plugin's status in the state
     */
    private fun updatePluginStatus(pluginId: String, status: PluginStatus) {
        val updatedPlugins = _state.value.plugins.map { plugin ->
            if (plugin.id == pluginId) {
                plugin.copy(status = status)
            } else {
                plugin
            }
        }
        val updatedFiltered = _state.value.filteredPlugins.map { plugin ->
            if (plugin.id == pluginId) {
                plugin.copy(status = status)
            } else {
                plugin
            }
        }
        val updatedFeatured = _state.value.featuredPlugins.map { plugin ->
            if (plugin.id == pluginId) {
                plugin.copy(status = status)
            } else {
                plugin
            }
        }
        _state.value = _state.value.copy(
            plugins = updatedPlugins,
            filteredPlugins = updatedFiltered,
            featuredPlugins = updatedFeatured
        )
    }
}
