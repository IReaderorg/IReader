package ireader.presentation.ui.plugins.details

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.MonetizationService
import ireader.domain.plugins.PluginIndexEntry
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import ireader.domain.plugins.PluginRepositoryRepository
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import ireader.i18n.UiText
import ireader.plugin.api.PluginAuthor
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginMonetization
import ireader.i18n.resources.Res
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.featurestore.PluginUpdateInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Details screen
 * Loads plugin info from both installed plugins and remote repositories
 */
class PluginDetailsViewModel(
    private val pluginId: String,
    private val pluginManager: PluginManager,
    private val monetizationService: MonetizationService,
    private val getCurrentUserId: () -> String,
    private val pluginRepository: ireader.domain.data.repository.PluginRepository,
    private val remoteRepository: ireader.domain.data.repository.RemoteRepository,
    private val uiPreferences: ireader.domain.preferences.prefs.UiPreferences,
    private val repositoryRepository: PluginRepositoryRepository? = null,
    private val indexFetcher: PluginRepositoryIndexFetcher? = null,
    private val downloadService: ireader.domain.services.common.PluginDownloadService? = null,
    private val pluginReviewRepository: ireader.domain.data.repository.PluginReviewRepository? = null
) : BaseViewModel() {
    
    private val _state = mutableStateOf(PluginDetailsState())
    val state: State<PluginDetailsState> = _state
    
    init {
        observePlugins()
        observeDownloads()
        loadPluginDetails()
    }
    
    /**
     * Observe download progress from PluginDownloadService
     */
    private fun observeDownloads() {
        downloadService?.downloads
            ?.onEach { downloads ->
                val progress = downloads[pluginId]
                if (progress != null) {
                    val newState = when (progress.status) {
                        ireader.domain.services.common.PluginDownloadStatus.QUEUED,
                        ireader.domain.services.common.PluginDownloadStatus.DOWNLOADING -> {
                            InstallationState.Downloading(progress.progress)
                        }
                        ireader.domain.services.common.PluginDownloadStatus.VALIDATING,
                        ireader.domain.services.common.PluginDownloadStatus.INSTALLING -> {
                            InstallationState.Installing
                        }
                        ireader.domain.services.common.PluginDownloadStatus.COMPLETED -> {
                            InstallationState.Installed
                        }
                        ireader.domain.services.common.PluginDownloadStatus.FAILED -> {
                            InstallationState.Error(progress.errorMessage ?: "Installation failed")
                        }
                        ireader.domain.services.common.PluginDownloadStatus.CANCELLED -> {
                            InstallationState.NotInstalled
                        }
                    }
                    
                    // Clear update state if download completed successfully
                    val clearUpdate = progress.status == ireader.domain.services.common.PluginDownloadStatus.COMPLETED
                    
                    _state.value = _state.value.copy(
                        installationState = newState,
                        downloadProgress = progress.progress,
                        updateAvailable = if (clearUpdate) false else _state.value.updateAvailable,
                        updateInfo = if (clearUpdate) null else _state.value.updateInfo
                    )
                }
            }
            ?.launchIn(scope)
    }
    
    private fun observePlugins() {
        pluginManager.pluginsFlow
            .onEach { plugins ->
                val plugin = plugins.find { it.id == pluginId }
                if (plugin != null) {
                    // Update with installed plugin info
                    _state.value = _state.value.copy(
                        plugin = plugin,
                        installationState = when (plugin.status) {
                            PluginStatus.NOT_INSTALLED -> InstallationState.NotInstalled
                            PluginStatus.ENABLED, PluginStatus.DISABLED -> InstallationState.Installed
                            PluginStatus.ERROR -> InstallationState.Error("Plugin error")
                            PluginStatus.UPDATING -> InstallationState.Installing
                        }
                    )
                    loadOtherPluginsByDeveloper(plugin)
                    // Check for updates when plugin info changes
                    checkForUpdate(plugin)
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Check if an update is available for the installed plugin
     */
    private fun checkForUpdate(installedPlugin: PluginInfo) {
        if (repositoryRepository == null || indexFetcher == null) return
        
        scope.launch {
            try {
                val remoteInfo = fetchRemotePluginInfo(installedPlugin.id)
                if (remoteInfo != null && remoteInfo.manifest.versionCode > installedPlugin.manifest.versionCode) {
                    val updateInfo = PluginUpdateInfo(
                        pluginId = installedPlugin.id,
                        pluginName = installedPlugin.manifest.name,
                        currentVersion = installedPlugin.manifest.version,
                        currentVersionCode = installedPlugin.manifest.versionCode,
                        newVersion = remoteInfo.manifest.version,
                        newVersionCode = remoteInfo.manifest.versionCode,
                        downloadUrl = remoteInfo.downloadUrl,
                        changeLog = null
                    )
                    _state.value = _state.value.copy(
                        updateAvailable = true,
                        updateInfo = updateInfo
                    )
                }
            } catch (e: Exception) {
                // Silently fail - update check is not critical
            }
        }
    }
    
    fun loadPluginDetails() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                // First try to find in installed plugins
                pluginManager.loadPlugins()
                val installedPlugins = pluginManager.pluginsFlow.value
                val installedPlugin = installedPlugins.find { it.id == pluginId }
                
                if (installedPlugin != null) {
                    // Try to get remote info for iconUrl and other metadata
                    val remoteInfo = fetchRemotePluginInfo(pluginId)
                    
                    // Merge installed plugin with remote info (for iconUrl, downloadUrl, etc.)
                    val mergedPlugin = if (remoteInfo != null) {
                        installedPlugin.copy(
                            manifest = installedPlugin.manifest.copy(
                                iconUrl = remoteInfo.manifest.iconUrl.takeIf { !it.isNullOrEmpty() } 
                                    ?: installedPlugin.manifest.iconUrl
                            ),
                            rating = remoteInfo.rating ?: installedPlugin.rating,
                            downloadCount = remoteInfo.downloadCount.takeIf { it > 0 } ?: installedPlugin.downloadCount,
                            downloadUrl = remoteInfo.downloadUrl ?: installedPlugin.downloadUrl,
                            repositoryUrl = remoteInfo.repositoryUrl ?: installedPlugin.repositoryUrl
                        )
                    } else {
                        installedPlugin
                    }
                    
                    _state.value = _state.value.copy(
                        plugin = mergedPlugin,
                        installationState = when (installedPlugin.status) {
                            PluginStatus.NOT_INSTALLED -> InstallationState.NotInstalled
                            PluginStatus.ENABLED, PluginStatus.DISABLED -> InstallationState.Installed
                            PluginStatus.ERROR -> InstallationState.Error("Plugin error")
                            PluginStatus.UPDATING -> InstallationState.Installing
                        },
                        isLoading = false
                    )
                    loadOtherPluginsByDeveloper(mergedPlugin)
                } else {
                    // Not installed - try to fetch from remote repositories
                    loadFromRemoteRepositories()
                }
                
                loadReviews()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load plugin details"
                )
            }
        }
    }
    
    /**
     * Fetch plugin info from remote repositories (for getting iconUrl, etc.)
     */
    private suspend fun fetchRemotePluginInfo(pluginId: String): PluginInfo? {
        if (repositoryRepository == null || indexFetcher == null) return null
        
        return try {
            val repositories = repositoryRepository.getEnabled().first()
            
            for (repo in repositories) {
                try {
                    val index = indexFetcher.fetchIndex(repo.url).getOrNull()
                    val entry = index?.plugins?.find { it.id == pluginId }
                    if (entry != null) {
                        return entry.toPluginInfo(repo.url)
                    }
                } catch (e: Exception) {
                    // Continue to next repository
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Load plugin info from remote repositories (for plugins not yet installed)
     */
    private suspend fun loadFromRemoteRepositories() {
        if (repositoryRepository == null || indexFetcher == null) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Plugin not found"
            )
            return
        }
        
        try {
            val repositories = repositoryRepository.getEnabled().first()
            
            // Fetch from all repositories in parallel
            val results = repositories.map { repo ->
                scope.async {
                    try {
                        indexFetcher.fetchIndex(repo.url).getOrNull()?.plugins?.map { entry ->
                            entry to repo.url
                        } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
            
            // Find the plugin by ID
            val found = results.find { (entry, _) -> entry.id == pluginId }
            
            if (found != null) {
                val (entry, repoUrl) = found
                val pluginInfo = entry.toPluginInfo(repoUrl)
                _state.value = _state.value.copy(
                    plugin = pluginInfo,
                    installationState = InstallationState.NotInstalled,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Plugin not found in any repository"
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Failed to load plugin: ${e.message}"
            )
        }
    }
    
    /**
     * Convert PluginIndexEntry to PluginInfo
     */
    private fun PluginIndexEntry.toPluginInfo(repositoryUrl: String): PluginInfo {
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

        val platformList = platforms.mapNotNull { platformStr ->
            try {
                ireader.plugin.api.Platform.valueOf(platformStr.uppercase())
            } catch (e: Exception) {
                null
            }
        }.ifEmpty {
            listOf(
                ireader.plugin.api.Platform.ANDROID,
                ireader.plugin.api.Platform.IOS,
                ireader.plugin.api.Platform.DESKTOP
            )
        }

        // Resolve download URL - if relative, combine with repository base URL
        val resolvedDownloadUrl = resolveDownloadUrl(downloadUrl, repositoryUrl)

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
            status = PluginStatus.NOT_INSTALLED,
            installDate = null,
            rating = null,
            downloadCount = 0,
            repositoryUrl = repositoryUrl,
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
    
    private fun loadOtherPluginsByDeveloper(plugin: PluginInfo) {
        scope.launch {
            val allPlugins = pluginManager.pluginsFlow.value
            val otherPlugins = allPlugins.filter { 
                it.manifest.author.name == plugin.manifest.author.name && it.id != plugin.id 
            }
            
            // Try to enrich with remote info for icons
            val enrichedPlugins = otherPlugins.map { installedPlugin ->
                if (installedPlugin.manifest.iconUrl.isNullOrEmpty()) {
                    // Try to get icon from remote
                    val remoteInfo = fetchRemotePluginInfo(installedPlugin.id)
                    if (remoteInfo?.manifest?.iconUrl != null) {
                        installedPlugin.copy(
                            manifest = installedPlugin.manifest.copy(
                                iconUrl = remoteInfo.manifest.iconUrl
                            )
                        )
                    } else {
                        installedPlugin
                    }
                } else {
                    installedPlugin
                }
            }
            
            _state.value = _state.value.copy(otherPluginsByDeveloper = enrichedPlugins)
        }
    }
    
    private fun loadReviews() {
        if (pluginReviewRepository == null) {
            _state.value = _state.value.copy(reviews = emptyList())
            return
        }
        
        _state.value = _state.value.copy(isLoadingReviews = true)
        
        scope.launch {
            // Load reviews
            pluginReviewRepository.getPluginReviews(pluginId)
                .onSuccess { reviews ->
                    _state.value = _state.value.copy(
                        reviews = reviews,
                        isLoadingReviews = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        reviews = emptyList(),
                        isLoadingReviews = false
                    )
                }
            
            // Load rating stats
            pluginReviewRepository.getRatingStats(pluginId)
                .onSuccess { stats ->
                    _state.value = _state.value.copy(ratingStats = stats)
                }
            
            // Load user's own review
            pluginReviewRepository.getUserReview(pluginId)
                .onSuccess { review ->
                    _state.value = _state.value.copy(userReview = review)
                }
        }
    }
    
    /**
     * Refresh reviews from the server
     */
    fun refreshReviews() {
        loadReviews()
    }
    
    fun installPlugin() {
        val plugin = _state.value.plugin ?: return
        
        // Check if premium and not purchased
        val monetization = plugin.manifest.monetization
        if (monetization is PluginMonetization.Premium && !plugin.isPurchased) {
            _state.value = _state.value.copy(showPurchaseDialog = true)
            return
        }
        
        _state.value = _state.value.copy(
            installationState = InstallationState.Downloading(0f),
            downloadProgress = 0f
        )
        
        scope.launch {
            try {
                // Check if plugin is already installed (just needs enabling)
                val installedPlugins = pluginManager.pluginsFlow.value
                val isInstalled = installedPlugins.any { it.id == plugin.id }
                
                if (isInstalled) {
                    // Just enable the existing plugin
                    pluginManager.enablePlugin(plugin.id)
                        .onSuccess {
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Installed
                            )
                        }
                        .onFailure { error ->
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Error(error.message ?: "Failed to enable plugin")
                            )
                        }
                } else {
                    // Check if plugin has download URL, if not try to fetch from remote
                    var pluginToInstall = plugin
                    if (plugin.downloadUrl.isNullOrBlank()) {
                        // Try to fetch download URL from remote repository
                        val remoteInfo = fetchRemotePluginInfo(plugin.id)
                        if (remoteInfo?.downloadUrl.isNullOrBlank()) {
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Error("Plugin has no download URL. Try refreshing the page.")
                            )
                            return@launch
                        }
                        pluginToInstall = plugin.copy(
                            downloadUrl = remoteInfo?.downloadUrl,
                            repositoryUrl = remoteInfo?.repositoryUrl
                        )
                    }
                    
                    // Use download service if available for progress tracking
                    if (downloadService != null) {
                        when (val result = downloadService.downloadPlugin(pluginToInstall)) {
                            is ireader.domain.services.common.ServiceResult.Success -> {
                                // Progress will be tracked via observeDownloads()
                            }
                            is ireader.domain.services.common.ServiceResult.Error -> {
                                _state.value = _state.value.copy(
                                    installationState = InstallationState.Error(result.message)
                                )
                            }
                            is ireader.domain.services.common.ServiceResult.Loading -> {
                                // Still loading
                            }
                        }
                    } else {
                        // Fallback to direct installation without progress
                        _state.value = _state.value.copy(installationState = InstallationState.Installing)
                        pluginManager.installPlugin(pluginToInstall)
                            .onSuccess {
                                _state.value = _state.value.copy(
                                    installationState = InstallationState.Installed,
                                    showSuccessMessage = true
                                )
                            }
                            .onFailure { error ->
                                _state.value = _state.value.copy(
                                    installationState = InstallationState.Error(error.message ?: "Installation failed")
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    installationState = InstallationState.Error(e.message ?: "Installation failed")
                )
            }
        }
    }
    
    fun uninstallPlugin() {
        val plugin = _state.value.plugin ?: return
        
        scope.launch {
            pluginManager.uninstallPlugin(plugin.id)
                .onSuccess {
                    _state.value = _state.value.copy(
                        installationState = InstallationState.NotInstalled
                    )
                }
                .onFailure { error ->
                    showSnackBar(UiText.DynamicString("Failed to uninstall: ${error.message}"))
                }
        }
    }
    
    /**
     * Update the plugin to the latest version
     */
    fun updatePlugin() {
        val updateInfo = _state.value.updateInfo ?: return
        val plugin = _state.value.plugin ?: return
        
        // Create a PluginInfo with the remote download URL for the update
        val remotePlugin = plugin.copy(
            downloadUrl = updateInfo.downloadUrl,
            manifest = plugin.manifest.copy(
                version = updateInfo.newVersion,
                versionCode = updateInfo.newVersionCode
            )
        )
        
        _state.value = _state.value.copy(
            installationState = InstallationState.Downloading(0f),
            downloadProgress = 0f
        )
        
        scope.launch {
            try {
                if (downloadService != null) {
                    when (val result = downloadService.downloadPlugin(remotePlugin)) {
                        is ireader.domain.services.common.ServiceResult.Success -> {
                            showSnackBar(UiText.DynamicString("Updating to v${updateInfo.newVersion}"))
                        }
                        is ireader.domain.services.common.ServiceResult.Error -> {
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Error(result.message)
                            )
                        }
                        is ireader.domain.services.common.ServiceResult.Loading -> {
                            // Progress tracked via observeDownloads()
                        }
                    }
                } else {
                    // Fallback without progress tracking
                    _state.value = _state.value.copy(installationState = InstallationState.Installing)
                    pluginManager.installPlugin(remotePlugin)
                        .onSuccess {
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Installed,
                                updateAvailable = false,
                                updateInfo = null,
                                showSuccessMessage = true
                            )
                        }
                        .onFailure { error ->
                            _state.value = _state.value.copy(
                                installationState = InstallationState.Error(error.message ?: "Update failed")
                            )
                        }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    installationState = InstallationState.Error(e.message ?: "Update failed")
                )
            }
        }
    }
    
    fun enablePlugin() {
        val plugin = _state.value.plugin ?: return
        
        scope.launch {
            pluginManager.enablePlugin(plugin.id)
                .onSuccess {
                    showSnackBar(UiText.DynamicString("Plugin enabled"))
                }
                .onFailure { error ->
                    showSnackBar(UiText.DynamicString("Failed to enable: ${error.message}"))
                }
        }
    }
    
    fun disablePlugin() {
        val plugin = _state.value.plugin ?: return
        
        scope.launch {
            pluginManager.disablePlugin(plugin.id)
                .onSuccess {
                    showSnackBar(UiText.DynamicString("Plugin disabled"))
                }
                .onFailure { error ->
                    showSnackBar(UiText.DynamicString("Failed to disable: ${error.message}"))
                }
        }
    }
    
    fun purchasePlugin() {
        val plugin = _state.value.plugin ?: return
        val monetization = plugin.manifest.monetization as? PluginMonetization.Premium ?: return
        
        scope.launch {
            try {
                monetizationService.purchasePlugin(
                    pluginId = plugin.id,
                    price = monetization.price,
                    currency = monetization.currency
                ).onSuccess {
                    _state.value = _state.value.copy(showPurchaseDialog = false)
                    pluginManager.refreshPlugins()
                    installPlugin()
                }.onFailure {
                    _state.value = _state.value.copy(showPurchaseDialog = false)
                    showSnackBar(UiText.DynamicString("Purchase failed"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseDialog = false)
                showSnackBar(UiText.DynamicString("Purchase failed: ${e.message}"))
            }
        }
    }
    
    fun startTrial() {
        val plugin = _state.value.plugin ?: return
        val monetization = plugin.manifest.monetization as? PluginMonetization.Premium ?: return
        val trialDays = monetization.trialDays ?: return
        
        scope.launch {
            try {
                monetizationService.startTrial(plugin.id, trialDays)
                    .onSuccess {
                        _state.value = _state.value.copy(showPurchaseDialog = false)
                        pluginManager.refreshPlugins()
                        installPlugin()
                    }
                    .onFailure {
                        showSnackBar(UiText.DynamicString("Failed to start trial"))
                    }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to start trial: ${e.message}"))
            }
        }
    }
    
    fun showPurchaseDialog() {
        _state.value = _state.value.copy(showPurchaseDialog = true)
    }
    
    fun dismissPurchaseDialog() {
        _state.value = _state.value.copy(showPurchaseDialog = false)
    }
    
    fun submitReview(rating: Float, reviewText: String) {
        if (pluginReviewRepository == null) {
            showSnackBar(UiText.DynamicString("Reviews are not available"))
            return
        }
        
        _state.value = _state.value.copy(isSubmittingReview = true)
        
        scope.launch {
            pluginReviewRepository.submitReview(
                pluginId = pluginId,
                rating = rating.toInt().coerceIn(1, 5),
                reviewText = reviewText.takeIf { it.isNotBlank() }
            ).onSuccess { review ->
                _state.value = _state.value.copy(
                    userReview = review,
                    showReviewDialog = false,
                    isSubmittingReview = false
                )
                showSnackBar(UiText.DynamicString("Review submitted successfully"))
                // Refresh reviews to show the new one
                loadReviews()
            }.onFailure { error ->
                _state.value = _state.value.copy(isSubmittingReview = false)
                showSnackBar(UiText.DynamicString("Failed to submit review: ${error.message}"))
            }
        }
    }
    
    /**
     * Delete the user's review
     */
    fun deleteReview() {
        if (pluginReviewRepository == null) return
        
        scope.launch {
            pluginReviewRepository.deleteReview(pluginId)
                .onSuccess {
                    _state.value = _state.value.copy(userReview = null)
                    showSnackBar(UiText.DynamicString("Review deleted"))
                    loadReviews()
                }
                .onFailure { error ->
                    showSnackBar(UiText.DynamicString("Failed to delete review: ${error.message}"))
                }
        }
    }
    
    fun showEnablePluginPrompt() {
        _state.value = _state.value.copy(showEnablePluginPrompt = true)
    }
    
    fun dismissEnablePluginPrompt() {
        _state.value = _state.value.copy(showEnablePluginPrompt = false)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun openPlugin() {
        enablePlugin()
    }
    
    fun retryInstallation() {
        installPlugin()
    }
    
    fun showWriteReviewDialog() {
        _state.value = _state.value.copy(showReviewDialog = true)
    }
    
    fun dismissReviewDialog() {
        _state.value = _state.value.copy(showReviewDialog = false)
    }
    
    fun markReviewHelpful(reviewId: String) {
        if (pluginReviewRepository == null) return
        
        // Find the review to check if it's already marked as helpful
        val review = _state.value.reviews.find { it.id == reviewId } ?: return
        
        scope.launch {
            if (review.isHelpful) {
                // Unmark as helpful
                pluginReviewRepository.unmarkReviewHelpful(reviewId)
                    .onSuccess {
                        // Update local state
                        val updatedReviews = _state.value.reviews.map { r ->
                            if (r.id == reviewId) {
                                r.copy(
                                    isHelpful = false,
                                    helpfulCount = (r.helpfulCount - 1).coerceAtLeast(0)
                                )
                            } else r
                        }
                        _state.value = _state.value.copy(reviews = updatedReviews)
                    }
            } else {
                // Mark as helpful
                pluginReviewRepository.markReviewHelpful(reviewId)
                    .onSuccess {
                        // Update local state
                        val updatedReviews = _state.value.reviews.map { r ->
                            if (r.id == reviewId) {
                                r.copy(
                                    isHelpful = true,
                                    helpfulCount = r.helpfulCount + 1
                                )
                            } else r
                        }
                        _state.value = _state.value.copy(reviews = updatedReviews)
                    }
            }
        }
    }
    
    fun dismissSuccessMessage() {
        _state.value = _state.value.copy(showSuccessMessage = false)
    }
    
    fun enableJSPluginsFeature() {
        scope.launch {
            uiPreferences.enableJSPlugins().set(true)
            _state.value = _state.value.copy(showEnablePluginPrompt = false)
            installPlugin()
        }
    }
}
