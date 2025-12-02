package ireader.presentation.ui.home.sources.extension

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.core.os.InstallStep
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.SourceStatus

/**
 * Immutable state for the Extension screen following Mihon's StateScreenModel pattern.
 * 
 * This replaces the old mutable CatalogsState interface with a single immutable data class
 * that can be efficiently updated via StateFlow.
 */
@Immutable
data class ExtensionScreenState(
    // Catalog lists
    val pinnedCatalogs: List<CatalogLocal> = emptyList(),
    val unpinnedCatalogs: List<CatalogLocal> = emptyList(),
    val remoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // All catalogs (unfiltered)
    val allPinnedCatalogs: List<CatalogLocal> = emptyList(),
    val allUnpinnedCatalogs: List<CatalogLocal> = emptyList(),
    val allRemoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // Language filtering
    val languageChoices: List<LanguageChoice> = emptyList(),
    val selectedLanguage: LanguageChoice = LanguageChoice.All,
    val selectedUserSourceLanguage: LanguageChoice = LanguageChoice.All,
    
    // Installation state
    val installSteps: Map<String, InstallStep> = emptyMap(),
    
    // UI state
    val isRefreshing: Boolean = false,
    val searchQuery: String? = null,
    val currentPagerPage: Int = 0,
    val isInSearchMode: Boolean = false,
    
    // Source health status
    val sourceStatuses: Map<Long, SourceStatus> = emptyMap(),
    val loadingSources: Set<Long> = emptySet(),
    
    // Repository filtering
    val selectedRepositoryType: String? = null,
) {
    /**
     * All installed catalogs (pinned + unpinned)
     */
    @Stable
    val allCatalogs: List<CatalogLocal>
        get() = pinnedCatalogs + unpinnedCatalogs
    
    /**
     * Check if there are any installed catalogs
     */
    @Stable
    val hasInstalledCatalogs: Boolean
        get() = pinnedCatalogs.isNotEmpty() || unpinnedCatalogs.isNotEmpty()
    
    /**
     * Check if there are any remote catalogs available
     */
    @Stable
    val hasRemoteCatalogs: Boolean
        get() = remoteCatalogs.isNotEmpty()
    
    /**
     * Check if any installation is in progress
     */
    @Stable
    val hasActiveInstallation: Boolean
        get() = installSteps.any { it.value != InstallStep.Idle && it.value != InstallStep.Success }
    
    /**
     * Get the number of pending updates
     */
    @Stable
    val pendingUpdatesCount: Int
        get() = remoteCatalogs.count { remote ->
            allCatalogs.filterIsInstance<CatalogInstalled>().any { local ->
                local.pkgName == remote.pkgName && local.versionCode < remote.versionCode
            }
        }
}

/**
 * Sealed interface for Extension screen dialogs
 */
sealed interface ExtensionDialog {
    data object None : ExtensionDialog
    data class AddRepository(val url: String = "") : ExtensionDialog
    data class SourceDetails(val sourceId: Long) : ExtensionDialog
    data class SourceLogin(val sourceId: Long) : ExtensionDialog
    data class ExtensionSecurity(val catalogPkgName: String) : ExtensionDialog
    data class ExtensionStatistics(val extensionId: Long) : ExtensionDialog
    data class ConfirmUninstall(val catalogPkgName: String, val catalogName: String) : ExtensionDialog
}

/**
 * Sealed class for Extension screen events (one-time events)
 */
sealed class ExtensionEvent {
    data class ShowSnackbar(val message: String) : ExtensionEvent()
    data class NavigateToSource(val sourceId: Long) : ExtensionEvent()
    data class NavigateToMigration(val sourceId: Long) : ExtensionEvent()
    data object RefreshComplete : ExtensionEvent()
}
