package ireader.presentation.ui.featurestore

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
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.common.PluginDownloadProgress
import ireader.domain.services.common.PluginDownloadService
import ireader.domain.services.common.PluginDownloadStatus
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.plugin.api.PluginAuthor
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginMonetization
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.SortOrder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Feature Store screen
 * Manages plugin discovery, filtering, and monetization display
 * 
 * Caching behavior:
 * - Plugin index is cached in memory and only refreshed when:
 *   1. Cache duration has expired (default: 24 hours)
 *   2. User explicitly taps refresh button
 * - Cache timestamp is persisted in preferences
 */
class FeatureStoreViewModel(
    private val pluginManager: PluginManager,
    private val repositoryRepository: PluginRepositoryRepository,
    private val indexFetcher: PluginRepositoryIndexFetcher,
    private val downloadService: PluginDownloadService,
    private val uiPreferences: UiPreferences
) : BaseViewModel() {
    
    private val _state = mutableStateOf(FeatureStoreState())
    val state: State<FeatureStoreState> = _state
    
    // Cache of installed plugin IDs for quick lookup
    private var installedPluginIds: Set<String> = emptySet()
    
    // In-memory cache of plugins (survives configuration changes)
    private var cachedPlugins: List<PluginInfo>? = null
    
    init {
        observeInstalledPlugins()
        observeRepositories()
        observeDownloads()
        loadPlugins()
    }
    
    /**
     * Check if the cache is still valid based on last fetch time and cache duration
     */
    private fun isCacheValid(): Boolean {
        val lastFetchTime = uiPreferences.featureStoreLastFetchTime().get()
        if (lastFetchTime == 0L) return false
        
        val cacheDurationHours = uiPreferences.featureStoreCacheDurationHours().get()
        val cacheDurationMillis = cacheDurationHours * 60 * 60 * 1000L
        val currentTime = currentTimeToLong()
        
        return (currentTime - lastFetchTime) < cacheDurationMillis
    }
    
    /**
     * Update the cache timestamp to current time
     */
    private fun updateCacheTimestamp() {
        uiPreferences.featureStoreLastFetchTime().set(currentTimeToLong())
    }
    
    /**
     * Observe download progress from PluginDownloadService
     */
    private fun observeDownloads() {
        downloadService.downloads
            .onEach { downloads ->
                // Convert PluginDownloadProgress to DownloadProgress for UI
                val uiDownloads = downloads.mapValues { (_, progress) ->
                    DownloadProgress(
                        pluginId = progress.pluginId,
                        pluginName = progress.pluginName,
                        progress = progress.progress,
                        bytesDownloaded = progress.bytesDownloaded,
                        totalBytes = progress.totalBytes,
                        status = progress.status.toUiStatus(),
                        error = progress.errorMessage
                    )
                }
                _state.value = _state.value.copy(downloadProgress = uiDownloads)
                
                // Update plugin status based on download status
                downloads.forEach { (pluginId, progress) ->
                    val pluginStatus = when (progress.status) {
                        PluginDownloadStatus.QUEUED,
                        PluginDownloadStatus.DOWNLOADING,
                        PluginDownloadStatus.VALIDATING,
                        PluginDownloadStatus.INSTALLING -> PluginStatus.UPDATING
                        PluginDownloadStatus.COMPLETED -> PluginStatus.ENABLED
                        PluginDownloadStatus.FAILED -> PluginStatus.ERROR
                        PluginDownloadStatus.CANCELLED -> PluginStatus.NOT_INSTALLED
                    }
                    updatePluginStatus(pluginId, pluginStatus)
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Convert service download status to UI download status
     */
    private fun PluginDownloadStatus.toUiStatus(): DownloadStatus {
        return when (this) {
            PluginDownloadStatus.QUEUED -> DownloadStatus.PENDING
            PluginDownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            PluginDownloadStatus.VALIDATING,
            PluginDownloadStatus.INSTALLING -> DownloadStatus.INSTALLING
            PluginDownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            PluginDownloadStatus.FAILED -> DownloadStatus.FAILED
            PluginDownloadStatus.CANCELLED -> DownloadStatus.FAILED
        }
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
                // Repositories changed, might want to reload
            }
            .launchIn(scope)
    }
    
    /**
     * Load plugins from all enabled repositories.
     * Uses cached data if available and not expired.
     * 
     * @param forceRefresh If true, bypasses cache and fetches from remote
     */
    fun loadPlugins(forceRefresh: Boolean = false) {
        // Check if we have valid cached data
        if (!forceRefresh && cachedPlugins != null && isCacheValid()) {
            val lastFetch = uiPreferences.featureStoreLastFetchTime().get()
            println("[FeatureStore] Using cached plugin data (${cachedPlugins?.size} plugins)")
            _state.value = _state.value.copy(
                plugins = cachedPlugins!!,
                featuredPlugins = getFeaturedPlugins(cachedPlugins!!),
                isLoading = false,
                error = null,
                lastFetchTime = lastFetch,
                isFromCache = true
            )
            applyFilters()
            return
        }
        
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
                println("[FeatureStore] Fetching plugins from ${repositories.size} repositories...")
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
                
                // Update cache
                cachedPlugins = uniquePlugins
                updateCacheTimestamp()
                val fetchTime = currentTimeToLong()
                println("[FeatureStore] Cached ${uniquePlugins.size} plugins")

                _state.value = _state.value.copy(
                    plugins = uniquePlugins,
                    featuredPlugins = getFeaturedPlugins(uniquePlugins),
                    isLoading = false,
                    error = if (uniquePlugins.isEmpty() && errors.isNotEmpty()) {
                        "Failed to load features: ${errors.joinToString("; ")}"
                    } else null,
                    lastFetchTime = fetchTime,
                    isFromCache = false
                )
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load features"
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
     * Force refresh plugins from remote repositories.
     * This bypasses the cache and always fetches fresh data.
     */
    fun refreshPlugins() {
        _state.value = _state.value.copy(isRefreshing = true, error = null)
        scope.launch {
            try {
                val repositories = repositoryRepository.getEnabled().first()
                val allPlugins = mutableListOf<PluginInfo>()

                println("[FeatureStore] Force refreshing plugins from ${repositories.size} repositories...")
                val results = repositories.map { repo ->
                    async { fetchPluginsFromRepository(repo) }
                }.awaitAll()

                results.forEach { result ->
                    result.onSuccess { plugins ->
                        allPlugins.addAll(plugins)
                    }
                }

                val uniquePlugins = allPlugins.distinctBy { it.id }
                
                // Update cache with fresh data
                cachedPlugins = uniquePlugins
                updateCacheTimestamp()
                val fetchTime = currentTimeToLong()
                println("[FeatureStore] Refreshed and cached ${uniquePlugins.size} plugins")

                _state.value = _state.value.copy(
                    plugins = uniquePlugins,
                    featuredPlugins = getFeaturedPlugins(uniquePlugins),
                    isRefreshing = false,
                    lastFetchTime = fetchTime,
                    isFromCache = false
                )
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Failed to refresh features"
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

        val monetizationModel = when (monetization?.type?.uppercase()) {
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
                monetization = monetizationModel
            ),
            status = if (id in installedPluginIds) PluginStatus.ENABLED else PluginStatus.NOT_INSTALLED,
            installDate = null,
            rating = null,
            downloadCount = 0,
            repositoryUrl = repository.url,
            downloadUrl = resolvedDownloadUrl,
            fileSize = fileSize,
            checksum = checksum,
            featured = featured,
            tags = tags
        )
    }
    
    /**
     * Resolve download URL - handles both absolute and relative URLs
     */
    private fun resolveDownloadUrl(downloadUrl: String, repositoryUrl: String): String {
        // If already absolute URL, return as-is
        if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
            println("[FeatureStore] Download URL is absolute: $downloadUrl")
            return downloadUrl
        }
        
        // Get base URL from repository URL (remove index.json or similar)
        val baseUrl = repositoryUrl.substringBeforeLast("/")
        
        // Combine base URL with relative path
        val resolved = if (downloadUrl.startsWith("/")) {
            "$baseUrl$downloadUrl"
        } else {
            "$baseUrl/$downloadUrl"
        }
        
        println("[FeatureStore] Resolved download URL: $downloadUrl -> $resolved (base: $baseUrl)")
        return resolved
    }
    
    fun selectCategory(category: PluginType?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateSortOrder(sortOrder: SortOrder) {
        _state.value = _state.value.copy(sortOrder = sortOrder)
        applyFilters()
    }
    
    fun updatePriceFilter(priceFilter: PriceFilter) {
        _state.value = _state.value.copy(priceFilter = priceFilter)
        applyFilters()
    }
    
    fun updateMinRating(rating: Float) {
        _state.value = _state.value.copy(minRating = rating)
        applyFilters()
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    private fun applyFilters() {
        var filtered = _state.value.plugins
        
        // Category filter
        _state.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.manifest.type == category }
        }
        
        // Search filter
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.manifest.name.lowercase().contains(query) ||
                it.manifest.description.lowercase().contains(query) ||
                it.manifest.author.name.lowercase().contains(query)
            }
        }
        
        // Price filter
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
        
        // Rating filter
        if (_state.value.minRating > 0) {
            filtered = filtered.filter { 
                (it.rating ?: 0f) >= _state.value.minRating 
            }
        }
        
        // Sorting
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
    
    private fun getFeaturedPlugins(plugins: List<PluginInfo>): List<PluginInfo> {
        // First, get plugins marked as featured
        val featuredPlugins = plugins.filter { it.featured }
        
        // If we have featured plugins, use them; otherwise fall back to rating/downloads
        return if (featuredPlugins.isNotEmpty()) {
            featuredPlugins.sortedByDescending { it.downloadCount }.take(10)
        } else {
            plugins
                .filter { (it.rating ?: 0f) >= 4.0f || it.downloadCount > 1000 }
                .sortedByDescending { it.downloadCount }
                .take(10)
        }
    }
    
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
     * Install a plugin from the store using the download service
     */
    fun installPlugin(pluginId: String) {
        val plugin = _state.value.plugins.find { it.id == pluginId } ?: return
        
        // Check if already downloading
        if (downloadService.isDownloading(pluginId)) {
            showSnackBar(ireader.i18n.UiText.DynamicString("${plugin.manifest.name} is already downloading"))
            return
        }
        
        // Check if plugin has a download URL
        if (plugin.downloadUrl.isNullOrBlank()) {
            _state.value = _state.value.copy(
                error = "Plugin ${plugin.manifest.name} has no download URL configured"
            )
            return
        }
        
        scope.launch {
            try {
                // Use download service for progress tracking and notifications
                when (val result = downloadService.downloadPlugin(plugin)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("${plugin.manifest.name} download started"))
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        _state.value = _state.value.copy(
                            error = "Failed to start download for ${plugin.manifest.name}: ${result.message}"
                        )
                    }
                    is ireader.domain.services.common.ServiceResult.Loading -> {
                        // Ignore loading state
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to install ${plugin.manifest.name}: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Cancel a plugin download
     */
    fun cancelDownload(pluginId: String) {
        scope.launch {
            downloadService.cancelDownload(pluginId)
        }
    }
    
    /**
     * Retry a failed download
     */
    fun retryDownload(pluginId: String) {
        // Find the plugin and restart download
        val plugin = _state.value.plugins.find { it.id == pluginId } ?: return
        installPlugin(pluginId)
    }
    
    /**
     * Uninstall a plugin
     */
    fun uninstallPlugin(pluginId: String) {
        val plugin = _state.value.plugins.find { it.id == pluginId } ?: return
        
        scope.launch {
            try {
                pluginManager.uninstallPlugin(pluginId)
                    .onSuccess {
                        showSnackBar(ireader.i18n.UiText.DynamicString("${plugin.manifest.name} uninstalled"))
                        updatePluginStatus(pluginId, PluginStatus.NOT_INSTALLED)
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(
                            error = "Failed to uninstall ${plugin.manifest.name}: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to uninstall ${plugin.manifest.name}: ${e.message}"
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
