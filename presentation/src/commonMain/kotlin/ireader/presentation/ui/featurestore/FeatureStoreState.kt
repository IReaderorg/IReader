package ireader.presentation.ui.featurestore

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginType
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.SortOrder

/**
 * State for the Feature Store screen
 */
data class FeatureStoreState(
    val plugins: List<PluginInfo> = emptyList(),
    val filteredPlugins: List<PluginInfo> = emptyList(),
    val featuredPlugins: List<PluginInfo> = emptyList(),
    val selectedCategory: PluginType? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.POPULARITY,
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val minRating: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    /** Map of plugin ID to download progress (0.0 to 1.0) */
    val downloadProgress: Map<String, DownloadProgress> = emptyMap(),
    /** Timestamp of last successful fetch from remote */
    val lastFetchTime: Long = 0L,
    /** Whether current data is from cache */
    val isFromCache: Boolean = false,
    /** Map of plugin ID to available update info */
    val availableUpdates: Map<String, PluginUpdateInfo> = emptyMap(),
    /** Plugins that have updates available */
    val pluginsWithUpdates: List<PluginInfo> = emptyList()
)

/**
 * Information about an available plugin update
 */
data class PluginUpdateInfo(
    val pluginId: String,
    val pluginName: String,
    val currentVersion: String,
    val currentVersionCode: Int,
    val newVersion: String,
    val newVersionCode: Int,
    val downloadUrl: String?,
    val changeLog: String? = null
)

/**
 * Download progress state for a plugin
 */
data class DownloadProgress(
    val pluginId: String,
    val pluginName: String,
    val progress: Float = 0f,  // 0.0 to 1.0
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val error: String? = null
)

/**
 * Download status enum
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    INSTALLING,
    COMPLETED,
    FAILED
}
