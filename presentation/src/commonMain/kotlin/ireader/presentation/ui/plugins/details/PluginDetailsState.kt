package ireader.presentation.ui.plugins.details

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginResourceUsage
import ireader.domain.plugins.ResourceUsagePercentages

/**
 * State for the Plugin Details screen
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 13.1, 13.2, 13.3, 4.8, 4.9, 4.10
 */
data class PluginDetailsState(
    val plugin: PluginInfo? = null,
    val reviews: List<PluginReview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val installationState: InstallationState = InstallationState.NotInstalled,
    val installProgress: Float = 0f,
    val showPurchaseDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val userReview: PluginReview? = null,
    val otherPluginsByDeveloper: List<PluginInfo> = emptyList(),
    val resourceUsage: PluginResourceUsage? = null,
    val resourcePercentages: ResourceUsagePercentages? = null,
    val resourceHistory: List<PluginResourceUsage> = emptyList()
)

/**
 * Installation state for a plugin
 */
sealed class InstallationState {
    object NotInstalled : InstallationState()
    object Downloading : InstallationState()
    object Installing : InstallationState()
    object Installed : InstallationState()
    data class Error(val message: String) : InstallationState()
}

/**
 * Plugin review data
 */
data class PluginReview(
    val id: String,
    val pluginId: String,
    val userId: String,
    val userName: String,
    val rating: Float,
    val reviewText: String?,
    val timestamp: Long,
    val helpfulCount: Int,
    val isHelpful: Boolean = false
)
