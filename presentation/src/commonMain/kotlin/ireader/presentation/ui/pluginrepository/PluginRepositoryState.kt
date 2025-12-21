package ireader.presentation.ui.pluginrepository

import androidx.compose.runtime.Stable

/**
 * State for the Plugin Repository screen
 */
@Stable
data class PluginRepositoryState(
    val repositories: List<PluginRepository> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * Plugin repository data
 */
@Stable
data class PluginRepository(
    val id: Long = 0,
    val url: String,
    val name: String,
    val enabled: Boolean = true,
    val pluginCount: Int = 0,
    val lastUpdated: Long = 0L,
    val isOfficial: Boolean = false,
    val lastError: String? = null
)

/**
 * Plugin entry from repository index
 */
@Stable
data class PluginEntry(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val author: String,
    val type: String,
    val downloadUrl: String,
    val iconUrl: String? = null,
    val isInstalled: Boolean = false,
    val installedVersion: String? = null,
    val hasUpdate: Boolean = false
)
