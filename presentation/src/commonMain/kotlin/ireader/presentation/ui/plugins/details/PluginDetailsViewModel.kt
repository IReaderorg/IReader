package ireader.presentation.ui.plugins.details

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.MonetizationService
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginMonetization
import ireader.domain.plugins.PluginStatus
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.error_unknown
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Plugin Details screen
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 13.1, 13.2, 13.3
 */
class PluginDetailsViewModel(
    private val pluginId: String,
    private val pluginManager: PluginManager,
    private val monetizationService: MonetizationService,
    private val getCurrentUserId: () -> String
) : BaseViewModel() {
    
    private val _state = mutableStateOf(PluginDetailsState())
    val state: State<PluginDetailsState> = _state
    
    init {
        observePlugins()
        loadPluginDetails()
    }
    
    /**
     * Observe plugin changes
     */
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
                    
                    // Load other plugins by same developer
                    loadOtherPluginsByDeveloper(plugin)
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Load plugin details
     */
    fun loadPluginDetails() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                // Load plugin info
                pluginManager.loadPlugins()
                
                // Load reviews (simulated for now)
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
    
    /**
     * Load reviews for the plugin
     */
    private suspend fun loadReviews() {
        // TODO: Implement actual review loading from repository
        // For now, using empty list
        _state.value = _state.value.copy(reviews = emptyList())
    }
    
    /**
     * Load other plugins by the same developer
     */
    private fun loadOtherPluginsByDeveloper(currentPlugin: PluginInfo) {
        scope.launch {
            val allPlugins = pluginManager.pluginsFlow.value
            val otherPlugins = allPlugins.filter {
                it.manifest.author.name == currentPlugin.manifest.author.name &&
                it.id != currentPlugin.id
            }.take(5)
            
            _state.value = _state.value.copy(otherPluginsByDeveloper = otherPlugins)
        }
    }
    
    /**
     * Install the plugin
     */
    fun installPlugin() {
        val plugin = _state.value.plugin ?: return
        
        scope.launch {
            try {
                // Check if plugin requires purchase
                val monetization = plugin.manifest.monetization
                if (monetization is PluginMonetization.Premium && !plugin.isPurchased) {
                    // Show purchase dialog
                    _state.value = _state.value.copy(showPurchaseDialog = true)
                    return@launch
                }
                
                // Start installation
                _state.value = _state.value.copy(
                    installationState = InstallationState.Downloading,
                    installProgress = 0f
                )
                
                // Simulate download progress
                for (i in 1..10) {
                    delay(100)
                    _state.value = _state.value.copy(installProgress = i / 10f)
                }
                
                _state.value = _state.value.copy(
                    installationState = InstallationState.Installing
                )
                
                // TODO: Implement actual plugin installation
                // For now, simulate installation
                delay(500)
                
                // Enable the plugin after installation
                pluginManager.enablePlugin(pluginId)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            installationState = InstallationState.Installed,
                            showSuccessMessage = true
                        )
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(
                            installationState = InstallationState.Error(
                                error.message ?: "Installation failed"
                            )
                        )
                        showSnackBar(UiText.DynamicString(error.message ?: "Installation failed"))
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    installationState = InstallationState.Error(
                        e.message ?: "Installation failed"
                    )
                )
                showSnackBar(UiText.DynamicString(e.message ?: "Installation failed"))
            }
        }
    }
    
    /**
     * Purchase the plugin
     */
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
                    // Refresh plugin info to update purchase status
                    pluginManager.refreshPlugins()
                    // Start installation after purchase
                    installPlugin()
                }.onFailure { error ->
                    _state.value = _state.value.copy(showPurchaseDialog = false)
                    showSnackBar(UiText.DynamicString(error.message ?: "Purchase failed"))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseDialog = false)
                showSnackBar(UiText.DynamicString(e.message ?: "Purchase failed"))
            }
        }
    }
    
    /**
     * Start trial for premium plugin
     */
    fun startTrial() {
        val plugin = _state.value.plugin ?: return
        val monetization = plugin.manifest.monetization as? PluginMonetization.Premium ?: return
        val trialDays = monetization.trialDays ?: return
        
        scope.launch {
            try {
                monetizationService.startTrial(plugin.id, trialDays)
                    .onSuccess {
                        _state.value = _state.value.copy(showPurchaseDialog = false)
                        // Refresh plugin info
                        pluginManager.refreshPlugins()
                        // Start installation after trial
                        installPlugin()
                    }
                    .onFailure { error ->
                        showSnackBar(UiText.DynamicString(error.message ?: "Failed to start trial"))
                    }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to start trial"))
            }
        }
    }
    
    /**
     * Open the installed plugin
     */
    fun openPlugin() {
        val plugin = _state.value.plugin ?: return
        
        scope.launch {
            try {
                if (plugin.status == PluginStatus.DISABLED) {
                    pluginManager.enablePlugin(pluginId)
                        .onSuccess {
                            showSnackBar(UiText.DynamicString("Plugin enabled"))
                        }
                        .onFailure { error ->
                            showSnackBar(UiText.DynamicString(error.message ?: "Failed to enable plugin"))
                        }
                } else {
                    showSnackBar(UiText.DynamicString("Plugin is already enabled"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to open plugin"))
            }
        }
    }
    
    /**
     * Retry installation after error
     */
    fun retryInstallation() {
        _state.value = _state.value.copy(
            installationState = InstallationState.NotInstalled,
            installProgress = 0f
        )
        installPlugin()
    }
    
    /**
     * Show write review dialog
     */
    fun showWriteReviewDialog() {
        _state.value = _state.value.copy(showReviewDialog = true)
    }
    
    /**
     * Submit a review
     */
    fun submitReview(rating: Float, reviewText: String) {
        scope.launch {
            try {
                // TODO: Implement actual review submission
                val review = PluginReview(
                    id = "${pluginId}_${getCurrentUserId()}",
                    pluginId = pluginId,
                    userId = getCurrentUserId(),
                    userName = "Current User", // TODO: Get actual user name
                    rating = rating,
                    reviewText = reviewText.ifBlank { null },
                    timestamp = System.currentTimeMillis(),
                    helpfulCount = 0
                )
                
                _state.value = _state.value.copy(
                    showReviewDialog = false,
                    userReview = review,
                    reviews = listOf(review) + _state.value.reviews
                )
                
                showSnackBar(UiText.DynamicString("Review submitted successfully"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString(e.message ?: "Failed to submit review"))
            }
        }
    }
    
    /**
     * Mark a review as helpful
     */
    fun markReviewHelpful(reviewId: String) {
        scope.launch {
            try {
                // TODO: Implement actual helpful marking
                val updatedReviews = _state.value.reviews.map { review ->
                    if (review.id == reviewId) {
                        review.copy(
                            isHelpful = !review.isHelpful,
                            helpfulCount = if (review.isHelpful) 
                                review.helpfulCount - 1 
                            else 
                                review.helpfulCount + 1
                        )
                    } else {
                        review
                    }
                }
                
                _state.value = _state.value.copy(reviews = updatedReviews)
            } catch (e: Exception) {
                showSnackBar(UiText.MStringResource(Res.string.error_unknown))
            }
        }
    }
    
    /**
     * Dismiss purchase dialog
     */
    fun dismissPurchaseDialog() {
        _state.value = _state.value.copy(showPurchaseDialog = false)
    }
    
    /**
     * Dismiss review dialog
     */
    fun dismissReviewDialog() {
        _state.value = _state.value.copy(showReviewDialog = false)
    }
    
    /**
     * Dismiss success message
     */
    fun dismissSuccessMessage() {
        _state.value = _state.value.copy(showSuccessMessage = false)
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
