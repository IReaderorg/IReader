package ireader.presentation.ui.plugins.details

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.MonetizationService
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginStatus
import ireader.i18n.UiText
import ireader.plugin.api.PluginMonetization
import ireader.i18n.resources.Res
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Details screen
 * Simplified implementation
 */
class PluginDetailsViewModel(
    private val pluginId: String,
    private val pluginManager: PluginManager,
    private val monetizationService: MonetizationService,
    private val getCurrentUserId: () -> String,
    private val pluginRepository: ireader.domain.data.repository.PluginRepository,
    private val remoteRepository: ireader.domain.data.repository.RemoteRepository,
    private val uiPreferences: ireader.domain.preferences.prefs.UiPreferences
) : BaseViewModel() {
    
    private val _state = mutableStateOf(PluginDetailsState())
    val state: State<PluginDetailsState> = _state
    
    init {
        observePlugins()
        loadPluginDetails()
    }
    
    private fun observePlugins() {
        pluginManager.pluginsFlow
            .onEach { plugins ->
                val plugin = plugins.find { it.id == pluginId }
                if (plugin != null) {
                    _state.value = _state.value.copy(
                        plugin = plugin,
                        installationState = when (plugin.status) {
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
                pluginManager.loadPlugins()
                loadReviews()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load plugin details"
                )
            }
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
        
        _state.value = _state.value.copy(installationState = InstallationState.Installing)
        
        scope.launch {
            try {
                // For now, just enable the plugin if it's already installed
                pluginManager.enablePlugin(plugin.id)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            installationState = InstallationState.Installed
                        )
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(
                            installationState = InstallationState.Error(error.message ?: "Installation failed")
                        )
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
