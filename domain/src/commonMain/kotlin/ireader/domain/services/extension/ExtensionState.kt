package ireader.domain.services.extension

import androidx.compose.runtime.Stable
import ireader.core.os.InstallStep
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote

/**
 * Stable data class representing the complete state of extension operations.
 * This is the single source of truth for all extension-related data.
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5, 5.1
 */
@Stable
data class ExtensionState(
    // ========== Extension Lists ==========
    
    /**
     * List of installed extensions (pinned + unpinned).
     */
    val installedExtensions: List<CatalogLocal> = emptyList(),
    
    /**
     * List of available extensions from remote repositories.
     */
    val availableExtensions: List<CatalogRemote> = emptyList(),
    
    /**
     * List of installed extensions that have updates available.
     */
    val updatableExtensions: List<CatalogInstalled> = emptyList(),
    
    /**
     * Pinned catalogs (filtered).
     */
    val pinnedCatalogs: List<CatalogLocal> = emptyList(),
    
    /**
     * Unpinned catalogs (filtered).
     */
    val unpinnedCatalogs: List<CatalogLocal> = emptyList(),
    
    /**
     * Remote catalogs (filtered).
     */
    val remoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // ========== Unfiltered Lists ==========
    
    /**
     * All pinned catalogs (unfiltered).
     */
    val allPinnedCatalogs: List<CatalogLocal> = emptyList(),
    
    /**
     * All unpinned catalogs (unfiltered).
     */
    val allUnpinnedCatalogs: List<CatalogLocal> = emptyList(),
    
    /**
     * All remote catalogs (unfiltered).
     */
    val allRemoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // ========== Filter State ==========
    
    /**
     * Current filter applied to extensions.
     */
    val filter: ExtensionFilter = ExtensionFilter.All,
    
    /**
     * Current search query.
     */
    val searchQuery: String? = null,
    
    /**
     * Selected language codes filter (null or empty means all languages).
     */
    val selectedLanguageCodes: Set<String>? = null,
    
    /**
     * Selected repository type filter.
     */
    val selectedRepositoryType: String? = null,
    
    /**
     * Available language codes.
     */
    val availableLanguages: Set<String> = emptySet(),
    
    // ========== Loading States ==========
    
    /**
     * True when loading extensions.
     */
    val isLoading: Boolean = false,
    
    /**
     * True when refreshing extensions from remote.
     */
    val isRefreshing: Boolean = false,
    
    /**
     * True when checking for updates.
     */
    val isCheckingUpdates: Boolean = false,
    
    // ========== Installation State ==========
    
    /**
     * Map of package names to their current installation step.
     */
    val installSteps: Map<String, InstallStep> = emptyMap(),
    
    // ========== Error State ==========
    
    /**
     * Current error, or null if no error.
     * Requirements: 4.2, 4.5
     */
    val error: ExtensionError? = null
) {
    // ========== Computed Properties ==========
    
    /**
     * Total count of installed extensions.
     */
    val installedCount: Int
        get() = pinnedCatalogs.size + unpinnedCatalogs.size
    
    /**
     * Total count of available extensions.
     */
    val availableCount: Int
        get() = remoteCatalogs.size
    
    /**
     * Count of extensions with available updates.
     */
    val updatableCount: Int
        get() = updatableExtensions.size
    
    /**
     * True if any installation is in progress.
     */
    val hasActiveInstallation: Boolean
        get() = installSteps.any { it.value != InstallStep.Idle && it.value != InstallStep.Success }
    
    /**
     * True if there are any installed extensions.
     */
    val hasInstalledExtensions: Boolean
        get() = installedExtensions.isNotEmpty()
    
    /**
     * True if there are any available extensions.
     */
    val hasAvailableExtensions: Boolean
        get() = availableExtensions.isNotEmpty()
    
    /**
     * True if there are updates available.
     */
    val hasUpdates: Boolean
        get() = updatableExtensions.isNotEmpty()
    
    /**
     * True if any loading operation is in progress.
     */
    val isAnyLoading: Boolean
        get() = isLoading || isRefreshing || isCheckingUpdates
    
    /**
     * True if there's an active error.
     */
    val hasError: Boolean
        get() = error != null
}
