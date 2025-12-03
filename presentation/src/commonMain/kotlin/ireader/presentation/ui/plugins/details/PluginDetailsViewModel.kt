package ireader.presentation.ui.plugins.details

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.remote.User
import ireader.domain.plugins.MonetizationService
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginMonetization
import ireader.domain.plugins.PluginStatus
import ireader.i18n.UiText
import ireader.i18n.resources.*
import ireader.i18n.resources.error_unknown
import ireader.i18n.resources.failed_to_enable_plugin
import ireader.i18n.resources.failed_to_open_plugin
import ireader.i18n.resources.failed_to_start_trial
import ireader.i18n.resources.failed_to_submit_review
import ireader.i18n.resources.installation_failed_generic
import ireader.i18n.resources.js_plugins_enabled_active
import ireader.i18n.resources.plugin_already_enabled
import ireader.i18n.resources.plugin_enabled
import ireader.i18n.resources.purchase_failed
import ireader.i18n.resources.review_submitted_successfully
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import ireader.i18n.resources.Res
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * ViewModel for Plugin Details screen
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 13.1, 13.2, 13.3
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
        try {
            // Fetch reviews from repository
            val domainReviews = pluginRepository.getReviewsByPlugin(pluginId)
            
            // Get current user to check if they have reviewed
            val currentUser = remoteRepository.getCurrentUser().getOrNull()
            val currentUserId = currentUser?.id ?: getCurrentUserId()
            
            // Convert domain reviews to presentation reviews
            val presentationReviews = domainReviews
                .sortedWith(
                    compareByDescending<ireader.domain.data.repository.PluginReview> { it.helpful }
                        .thenByDescending { it.timestamp }
                )
                .map { domainReview ->
                    PluginReview(
                        id = domainReview.id,
                        pluginId = domainReview.pluginId,
                        userId = domainReview.userId,
                        userName = getUserDisplayName(domainReview.userId, currentUser),
                        rating = domainReview.rating,
                        reviewText = domainReview.reviewText,
                        timestamp = domainReview.timestamp,
                        helpfulCount = domainReview.helpful,
                        isHelpful = false // TODO: Track per-user helpful marks
                    )
                }
            
            // Find user's own review if exists
            val userReview = presentationReviews.find { it.userId == currentUserId }
            
            _state.value = _state.value.copy(
                reviews = presentationReviews,
                userReview = userReview
            )
        } catch (e: Exception) {
            // Log error but don't fail the entire load
            _state.value = _state.value.copy(reviews = emptyList())
        }
    }
    
    /**
     * Get display name for a user
     */
    private suspend fun getUserDisplayName(userId: String, currentUser: User?): String {
        return when {
            currentUser?.id == userId -> currentUser.username ?: currentUser.email
            else -> "User $userId" // In production, fetch from user service
        }
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
                
                // Download plugin from repository URL
                val downloadResult = downloadPlugin(plugin)
                if (downloadResult.isFailure) {
                    throw downloadResult.exceptionOrNull() 
                        ?: Exception("Download failed")
                }
                
                val pluginFile = downloadResult.getOrThrow()
                
                // Verify plugin signature/checksum
                val verifyResult = verifyPlugin(pluginFile)
                if (verifyResult.isFailure) {
                    throw verifyResult.exceptionOrNull() 
                        ?: Exception("Plugin verification failed")
                }
                
                // Install plugin to plugins directory
                val installResult = installPluginFile(pluginFile)
                if (installResult.isFailure) {
                    throw installResult.exceptionOrNull() 
                        ?: Exception("Installation failed")
                }
                
                // Check if JS plugins are enabled before enabling the plugin
                if (!uiPreferences.enableJSPlugins().get()) {
                    _state.value = _state.value.copy(
                        installationState = InstallationState.Installed,
                        showEnablePluginPrompt = true
                    )
                    return@launch
                }
                
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
                        showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.installation_failed_generic))
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    installationState = InstallationState.Error(
                        e.message ?: "Installation failed"
                    )
                )
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.installation_failed_generic))
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
                    showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.purchase_failed))
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(showPurchaseDialog = false)
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.purchase_failed))
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
                        showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_start_trial))
                    }
            } catch (e: Exception) {
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_start_trial))
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
                            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.plugin_enabled))
                        }
                        .onFailure { error ->
                            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_enable_plugin))
                        }
                } else {
                    showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.plugin_already_enabled))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_open_plugin))
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
                // Get current user info
                val currentUser = remoteRepository.getCurrentUser().getOrNull()
                val userId = currentUser?.id ?: getCurrentUserId()
                val userName = currentUser?.username ?: currentUser?.email ?: "Anonymous User"
                
                val reviewId = "${pluginId}_${userId}"
                val timestamp = currentTimeToLong()
                
                // Save review to repository
                val result = pluginRepository.insertReview(
                    id = reviewId,
                    pluginId = pluginId,
                    userId = userId,
                    rating = rating,
                    reviewText = reviewText.ifBlank { null },
                    timestamp = timestamp,
                    helpful = 0
                )
                
                if (result.isFailure) {
                    throw result.exceptionOrNull() 
                        ?: Exception("Failed to save review")
                }
                
                // Create presentation review object
                val review = PluginReview(
                    id = reviewId,
                    pluginId = pluginId,
                    userId = userId,
                    userName = userName,
                    rating = rating,
                    reviewText = reviewText.ifBlank { null },
                    timestamp = timestamp,
                    helpfulCount = 0,
                    isHelpful = false
                )
                
                // Update local state
                _state.value = _state.value.copy(
                    showReviewDialog = false,
                    userReview = review,
                    reviews = listOf(review) + _state.value.reviews.filter { it.userId != userId }
                )
                
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.review_submitted_successfully))
            } catch (e: Exception) {
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_submit_review))
            }
        }
    }
    
    /**
     * Mark a review as helpful
     */
    fun markReviewHelpful(reviewId: String) {
        scope.launch {
            try {
                // Find the review in the current list
                val review = _state.value.reviews.find { it.id == reviewId }
                    ?: throw Exception("Review not found")
                
                // Toggle helpful state
                val newIsHelpful = !review.isHelpful
                val newHelpfulCount = if (newIsHelpful) {
                    review.helpfulCount + 1
                } else {
                    review.helpfulCount - 1
                }
                
                // Update review in repository
                val domainReview = pluginRepository.getReviewsByPlugin(pluginId)
                    .find { it.id == reviewId }
                
                if (domainReview != null) {
                    pluginRepository.updateReview(
                        id = reviewId,
                        rating = domainReview.rating,
                        reviewText = domainReview.reviewText,
                        timestamp = domainReview.timestamp
                    )
                    
                    // Note: In production, helpful count should be stored separately
                    // per user to track who marked what as helpful
                }
                
                // Update local state
                val updatedReviews = _state.value.reviews.map { r ->
                    if (r.id == reviewId) {
                        r.copy(
                            isHelpful = newIsHelpful,
                            helpfulCount = newHelpfulCount
                        )
                    } else {
                        r
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
    
    /**
     * Show prompt to enable JS plugins in settings
     */
    fun showEnablePluginPrompt() {
        _state.value = _state.value.copy(showEnablePluginPrompt = true)
    }
    
    /**
     * Dismiss the enable plugin prompt
     */
    fun dismissEnablePluginPrompt() {
        _state.value = _state.value.copy(showEnablePluginPrompt = false)
    }
    
    /**
     * Enable JS plugins feature in settings and continue with plugin activation
     */
    fun enableJSPluginsFeature() {
        scope.launch {
            uiPreferences.enableJSPlugins().set(true)
            _state.value = _state.value.copy(showEnablePluginPrompt = false)
            
            // Now enable the plugin
            pluginManager.enablePlugin(pluginId)
                .onSuccess {
                    _state.value = _state.value.copy(showSuccessMessage = true)
                    showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.js_plugins_enabled_active))
                }
                .onFailure { error ->
                    showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_enable_plugin))
                }
        }
    }
    
    /**
     * Download plugin from repository URL.
     * Uses Okio Path for KMP compatibility.
     */
    private suspend fun downloadPlugin(plugin: PluginInfo): Result<Path> = runCatching {
        // TODO: Implement actual HTTP download with progress tracking
        // For now, simulate download
        delay(1000)
        
        // In production, this would download from a plugin repository URL
        // and save to a temporary file
        val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "plugin_${plugin.id}.zip"
        tempPath
    }
    
    /**
     * Verify plugin signature/checksum
     */
    private suspend fun verifyPlugin(pluginFile: Path): Result<Unit> = runCatching {
        // TODO: Implement actual signature/checksum verification
        // For now, simulate verification
        delay(200)
        
        // In production, this would:
        // 1. Calculate file checksum (SHA-256)
        // 2. Verify against expected checksum from manifest
        // 3. Verify digital signature if present
        if (!FileSystem.SYSTEM.exists(pluginFile)) {
            throw Exception("Plugin file not found")
        }
    }
    
    /**
     * Install plugin file to plugins directory
     */
    private suspend fun installPluginFile(pluginFile: Path): Result<Unit> = runCatching {
        // TODO: Implement actual plugin installation
        // For now, simulate installation
        delay(300)
        
        // In production, this would:
        // 1. Extract plugin archive to plugins directory
        // 2. Validate plugin structure
        // 3. Register plugin with PluginManager
        // 4. Clean up temporary files
    }
}
