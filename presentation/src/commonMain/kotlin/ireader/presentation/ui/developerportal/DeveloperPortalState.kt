package ireader.presentation.ui.developerportal

import ireader.domain.plugins.DeveloperPlugin
import ireader.domain.plugins.PluginAccessGrant
import ireader.domain.plugins.PluginStats

/**
 * State for Developer Portal screen
 */
data class DeveloperPortalState(
    val isDeveloper: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val plugins: List<DeveloperPlugin> = emptyList(),
    val selectedPlugin: DeveloperPlugin? = null,
    val pluginGrants: List<PluginAccessGrant> = emptyList(),
    val pluginStats: PluginStats? = null,
    val showGrantDialog: Boolean = false,
    val grantUsername: String = "",
    val grantReason: String = "",
    val isGranting: Boolean = false,
    val grantError: String? = null,
    val grantSuccess: Boolean = false,
    val remainingGrants: Int = 0
)

/**
 * Tab options for Developer Portal
 */
enum class DeveloperPortalTab {
    PLUGINS,
    GRANTS,
    ANALYTICS
}
