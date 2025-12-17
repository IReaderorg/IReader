package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin registry interface for discovering and managing plugins.
 * Provides plugin discovery, dependency resolution, and lifecycle management.
 */
interface PluginRegistry {
    /**
     * Get all installed plugins.
     */
    fun getInstalledPlugins(): List<PluginInfo>
    
    /**
     * Get plugins by type.
     */
    fun getPluginsByType(type: PluginType): List<PluginInfo>
    
    /**
     * Get a specific plugin by ID.
     */
    fun getPlugin(pluginId: String): PluginInfo?
    
    /**
     * Get enabled plugins.
     */
    fun getEnabledPlugins(): List<PluginInfo>
    
    /**
     * Check if a plugin is installed.
     */
    fun isInstalled(pluginId: String): Boolean
    
    /**
     * Check if a plugin is enabled.
     */
    fun isEnabled(pluginId: String): Boolean
    
    /**
     * Enable a plugin.
     */
    suspend fun enablePlugin(pluginId: String): PluginOperationResult
    
    /**
     * Disable a plugin.
     */
    suspend fun disablePlugin(pluginId: String): PluginOperationResult
    
    /**
     * Install a plugin from file.
     */
    suspend fun installPlugin(pluginFile: ByteArray): PluginOperationResult
    
    /**
     * Install a plugin from URL.
     */
    suspend fun installPluginFromUrl(url: String): PluginOperationResult
    
    /**
     * Uninstall a plugin.
     */
    suspend fun uninstallPlugin(pluginId: String): PluginOperationResult
    
    /**
     * Update a plugin.
     */
    suspend fun updatePlugin(pluginId: String): PluginOperationResult
    
    /**
     * Check for plugin updates.
     */
    suspend fun checkForUpdates(): List<PluginUpdateInfo>
    
    /**
     * Get plugin instance (loaded plugin).
     */
    fun <T : Plugin> getPluginInstance(pluginId: String): T?
    
    /**
     * Register a plugin listener.
     */
    fun addListener(listener: PluginRegistryListener)
    
    /**
     * Remove a plugin listener.
     */
    fun removeListener(listener: PluginRegistryListener)
}

/**
 * Plugin information.
 */
@Serializable
data class PluginInfo(
    /** Plugin manifest */
    val manifest: PluginManifest,
    /** Installation state */
    val state: PluginState,
    /** Installation timestamp */
    val installedAt: Long,
    /** Last updated timestamp */
    val updatedAt: Long,
    /** Plugin file path */
    val filePath: String,
    /** Plugin size in bytes */
    val sizeBytes: Long,
    /** Dependencies */
    val dependencies: List<PluginDependency> = emptyList(),
    /** Whether update is available */
    val updateAvailable: Boolean = false,
    /** Latest available version */
    val latestVersion: String? = null
)

/**
 * Plugin state.
 */
@Serializable
enum class PluginState {
    /** Plugin is installed and enabled */
    ENABLED,
    /** Plugin is installed but disabled */
    DISABLED,
    /** Plugin is being installed */
    INSTALLING,
    /** Plugin is being updated */
    UPDATING,
    /** Plugin has an error */
    ERROR,
    /** Plugin dependencies not met */
    DEPENDENCIES_NOT_MET
}

/**
 * Plugin dependency.
 */
@Serializable
data class PluginDependency(
    /** Dependency plugin ID */
    val pluginId: String,
    /** Minimum required version */
    val minVersion: String,
    /** Maximum supported version (optional) */
    val maxVersion: String? = null,
    /** Whether dependency is optional */
    val optional: Boolean = false
)

/**
 * Plugin update information.
 */
@Serializable
data class PluginUpdateInfo(
    /** Plugin ID */
    val pluginId: String,
    /** Current version */
    val currentVersion: String,
    /** Latest version */
    val latestVersion: String,
    /** Changelog */
    val changelog: String? = null,
    /** Download URL */
    val downloadUrl: String,
    /** Update size in bytes */
    val sizeBytes: Long
)

/**
 * Result of plugin operations.
 */
sealed class PluginOperationResult {
    data class Success(val message: String? = null) : PluginOperationResult()
    data class Error(val error: PluginOperationError) : PluginOperationResult()
    
    fun isSuccess(): Boolean = this is Success
}

/**
 * Plugin operation errors.
 */
@Serializable
sealed class PluginOperationError {
    data class PluginNotFound(val pluginId: String) : PluginOperationError()
    data class AlreadyInstalled(val pluginId: String) : PluginOperationError()
    data class IncompatibleVersion(val required: String, val actual: String) : PluginOperationError()
    data class DependencyNotMet(val dependency: PluginDependency) : PluginOperationError()
    data class InstallationFailed(val reason: String) : PluginOperationError()
    data class PermissionDenied(val permission: PluginPermission) : PluginOperationError()
    data class NetworkError(val reason: String) : PluginOperationError()
    data class InvalidPlugin(val reason: String) : PluginOperationError()
    data class StorageError(val reason: String) : PluginOperationError()
    data class Unknown(val reason: String) : PluginOperationError()
}

/**
 * Plugin registry listener.
 */
interface PluginRegistryListener {
    fun onPluginInstalled(pluginInfo: PluginInfo)
    fun onPluginUninstalled(pluginId: String)
    fun onPluginEnabled(pluginId: String)
    fun onPluginDisabled(pluginId: String)
    fun onPluginUpdated(pluginInfo: PluginInfo)
    fun onPluginError(pluginId: String, error: PluginOperationError)
}

/**
 * Plugin repository for discovering available plugins.
 */
interface PluginRepository {
    /**
     * Get repository information.
     */
    fun getRepositoryInfo(): RepositoryInfo
    
    /**
     * Search for plugins.
     */
    suspend fun searchPlugins(query: String, filters: PluginSearchFilters = PluginSearchFilters()): List<PluginListingInfo>
    
    /**
     * Get featured plugins.
     */
    suspend fun getFeaturedPlugins(): List<PluginListingInfo>
    
    /**
     * Get plugins by category.
     */
    suspend fun getPluginsByCategory(category: PluginCategory): List<PluginListingInfo>
    
    /**
     * Get plugin details.
     */
    suspend fun getPluginDetails(pluginId: String): PluginDetailInfo?
    
    /**
     * Get plugin download URL.
     */
    suspend fun getDownloadUrl(pluginId: String, version: String? = null): String?
    
    /**
     * Refresh repository data.
     */
    suspend fun refresh()
}

/**
 * Repository information.
 */
@Serializable
data class RepositoryInfo(
    val id: String,
    val name: String,
    val url: String,
    val description: String? = null,
    val pluginCount: Int,
    val lastUpdated: Long
)

/**
 * Plugin search filters.
 */
@Serializable
data class PluginSearchFilters(
    val types: List<PluginType> = emptyList(),
    val categories: List<PluginCategory> = emptyList(),
    val platforms: List<Platform> = emptyList(),
    val monetization: List<PluginMonetizationType> = emptyList(),
    val sortBy: PluginSortOption = PluginSortOption.RELEVANCE,
    val page: Int = 1,
    val pageSize: Int = 20
)

/**
 * Plugin categories.
 */
@Serializable
enum class PluginCategory {
    THEMES,
    TTS_ENGINES,
    TRANSLATION,
    AI_FEATURES,
    CATALOG_SOURCES,
    IMAGE_PROCESSING,
    SYNC_SERVICES,
    COMMUNITY,
    UTILITIES,
    EXPERIMENTAL
}

/**
 * Plugin sort options.
 */
@Serializable
enum class PluginSortOption {
    RELEVANCE,
    DOWNLOADS,
    RATING,
    NEWEST,
    UPDATED,
    NAME
}

/**
 * Plugin listing information (for repository browsing).
 */
@Serializable
data class PluginListingInfo(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val type: PluginType,
    val category: PluginCategory,
    val iconUrl: String? = null,
    val downloadCount: Int = 0,
    val rating: Float? = null,
    val ratingCount: Int = 0,
    val monetization: PluginMonetizationType = PluginMonetizationType.FREE,
    val platforms: List<Platform>,
    val lastUpdated: Long
)

/**
 * Detailed plugin information (for plugin detail page).
 */
@Serializable
data class PluginDetailInfo(
    val listing: PluginListingInfo,
    val fullDescription: String? = null,
    val changelog: String? = null,
    val screenshotUrls: List<String> = emptyList(),
    val permissions: List<PluginPermission>,
    val dependencies: List<PluginDependency> = emptyList(),
    val minIReaderVersion: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val sourceCodeUrl: String? = null,
    val supportUrl: String? = null,
    val reviews: List<PluginReview> = emptyList()
)

/**
 * Plugin review.
 */
@Serializable
data class PluginReview(
    val id: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val timestamp: Long,
    val version: String
)

/**
 * Monetization types for plugin search.
 */
@Serializable
enum class PluginMonetizationType {
    FREE,
    PREMIUM,
    FREEMIUM
}