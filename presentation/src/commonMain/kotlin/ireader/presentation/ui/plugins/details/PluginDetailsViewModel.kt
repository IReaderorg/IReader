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
    private val downloadService: ireader.domain.services.common.PluginDownloadService? = null
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
                    _state.value = _state.value.copy(
                        installationState = newState,
                        downloadProgress = progress.progress
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
                }
            }
            .launchIn(scope)
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
                    _state.value = _state.value.copy(
                        plugin = installedPlugin,
                        installationState = when (installedPlugin.status) {
                            PluginStatus.NOT_INSTALLED -> InstallationState.NotInstalled
                            PluginStatus.ENABLED, PluginStatus.DISABLED -> InstallationState.Installed
                            PluginStatus.ERROR -> InstallationState.Error("Plugin error")
                            PluginStatus.UPDATING -> InstallationState.Installing
                        },
                        isLoading = false
                    )
                    loadOtherPluginsByDeveloper(installedPlugin)
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
            _state.value = _state.value.copy(otherPluginsByDeveloper = otherPlugins)
        }
    }
    
    private fun loadReviews() {
        // Simplified - no reviews for now
        _state.value = _state.value.copy(reviews = emptyList())
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
                    // Use download service if available for progress tracking
                    if (downloadService != null) {
                        when (val result = downloadService.downloadPlugin(plugin)) {
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
                        pluginManager.installPlugin(plugin)
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
        // Simplified - no review submission for now
        showSnackBar(UiText.DynamicString("Review submitted"))
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
        // Simplified - no implementation for now
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
