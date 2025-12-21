package ireader.presentation.ui.plugins.details

import androidx.compose.runtime.Stable
import ireader.domain.models.remote.PluginRatingStats
import ireader.domain.models.remote.PluginReview
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginResourceUsage
import ireader.domain.plugins.ResourceUsagePercentages
import ireader.presentation.ui.featurestore.PluginUpdateInfo

/**
 * State for the Plugin Details screen
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3, 13.1, 13.2, 13.3, 4.8, 4.9, 4.10
 */
@Stable
data class PluginDetailsState(
    val plugin: PluginInfo? = null,
    val reviews: List<PluginReview> = emptyList(),
    val ratingStats: PluginRatingStats? = null,
    val isLoading: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val error: String? = null,
    val installationState: InstallationState = InstallationState.NotInstalled,
    val installProgress: Float = 0f,
    val downloadProgress: Float = 0f, // Download progress from service (0.0 to 1.0)
    val showPurchaseDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val userReview: PluginReview? = null,
    val otherPluginsByDeveloper: List<PluginInfo> = emptyList(),
    val resourceUsage: PluginResourceUsage? = null,
    val resourcePercentages: ResourceUsagePercentages? = null,
    val resourceHistory: List<PluginResourceUsage> = emptyList(),
    val showEnablePluginPrompt: Boolean = false, // Show prompt to enable JS plugins in settings
    // Update-related state
    val updateAvailable: Boolean = false,
    val updateInfo: PluginUpdateInfo? = null,
    // Review submission state
    val isSubmittingReview: Boolean = false
)

/**
 * Installation state for a plugin
 */
sealed class InstallationState {
    data object NotInstalled : InstallationState()
    data class Downloading(val progress: Float = 0f) : InstallationState()
    data object Installing : InstallationState()
    data object Installed : InstallationState()
    data class Error(val message: String) : InstallationState()
}
