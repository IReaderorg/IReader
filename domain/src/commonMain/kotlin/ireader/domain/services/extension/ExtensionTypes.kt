package ireader.domain.services.extension

import androidx.compose.runtime.Stable
import ireader.core.os.InstallStep
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote

/**
 * Consolidated types for the Extension Controller.
 * Contains Command, State, Event, Error, and Filter definitions.
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */

// ============================================================================
// COMMANDS
// ============================================================================

/**
 * Sealed class representing all commands that can be dispatched to the Extension Controller.
 * All extension operations are expressed as commands for predictable state management.
 */
sealed class ExtensionCommand {
    // Lifecycle Commands
    /** Load extensions and subscribe to reactive updates. */
    object LoadExtensions : ExtensionCommand()
    /** Clean up resources, cancel subscriptions, and reset state. */
    object Cleanup : ExtensionCommand()
    
    // Installation Commands
    /** Install an extension from remote catalog. */
    data class InstallExtension(val catalog: CatalogRemote) : ExtensionCommand()
    /** Uninstall an installed extension. */
    data class UninstallExtension(val catalog: CatalogInstalled) : ExtensionCommand()
    /** Update an installed extension. */
    data class UpdateExtension(val catalog: CatalogInstalled) : ExtensionCommand()
    /** Cancel an ongoing installation job. */
    data class CancelInstallation(val catalog: Catalog) : ExtensionCommand()
    
    // Filter Commands
    /** Set the language filter for extensions. */
    data class SetFilter(val filter: ExtensionFilter) : ExtensionCommand()
    /** Set the search query for filtering extensions. */
    data class SetSearchQuery(val query: String?) : ExtensionCommand()
    /** Set the repository type filter. */
    data class SetRepositoryType(val repositoryType: String?) : ExtensionCommand()
    
    // Update Commands
    /** Check for available extension updates. */
    object CheckUpdates : ExtensionCommand()
    /** Refresh extensions from remote repositories. */
    object RefreshExtensions : ExtensionCommand()
    /** Batch update all extensions with available updates. */
    object BatchUpdateExtensions : ExtensionCommand()
    
    // Catalog Commands
    /** Toggle pinned status of a catalog. */
    data class TogglePinned(val catalog: Catalog) : ExtensionCommand()
    /** Clear the current error state. */
    object ClearError : ExtensionCommand()
}

// ============================================================================
// FILTER
// ============================================================================

/**
 * Filter options for extensions.
 */
sealed class ExtensionFilter {
    object All : ExtensionFilter()
    data class ByLanguage(val languageCodes: Set<String>) : ExtensionFilter()
    data class ByRepository(val repositoryType: String) : ExtensionFilter()
    data class Combined(val languageCodes: Set<String>?, val repositoryType: String?) : ExtensionFilter()
}

// ============================================================================
// STATE
// ============================================================================

/**
 * Stable data class representing the complete state of extension operations.
 * This is the single source of truth for all extension-related data.
 */
@Stable
data class ExtensionState(
    // Extension Lists
    val installedExtensions: List<CatalogLocal> = emptyList(),
    val availableExtensions: List<CatalogRemote> = emptyList(),
    val updatableExtensions: List<CatalogInstalled> = emptyList(),
    val pinnedCatalogs: List<CatalogLocal> = emptyList(),
    val unpinnedCatalogs: List<CatalogLocal> = emptyList(),
    val remoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // Unfiltered Lists
    val allPinnedCatalogs: List<CatalogLocal> = emptyList(),
    val allUnpinnedCatalogs: List<CatalogLocal> = emptyList(),
    val allRemoteCatalogs: List<CatalogRemote> = emptyList(),
    
    // Filter State
    val filter: ExtensionFilter = ExtensionFilter.All,
    val searchQuery: String? = null,
    val selectedLanguageCodes: Set<String>? = null,
    val selectedRepositoryType: String? = null,
    val availableLanguages: Set<String> = emptySet(),
    
    // Loading States
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCheckingUpdates: Boolean = false,
    
    // Installation State
    val installSteps: Map<String, InstallStep> = emptyMap(),
    
    // Error State
    val error: ExtensionError? = null
) {
    // Computed Properties
    val installedCount: Int get() = pinnedCatalogs.size + unpinnedCatalogs.size
    val availableCount: Int get() = remoteCatalogs.size
    val updatableCount: Int get() = updatableExtensions.size
    val hasActiveInstallation: Boolean get() = installSteps.any { it.value != InstallStep.Idle && it.value != InstallStep.Success }
    val hasInstalledExtensions: Boolean get() = installedExtensions.isNotEmpty()
    val hasAvailableExtensions: Boolean get() = availableExtensions.isNotEmpty()
    val hasUpdates: Boolean get() = updatableExtensions.isNotEmpty()
    val isAnyLoading: Boolean get() = isLoading || isRefreshing || isCheckingUpdates
    val hasError: Boolean get() = error != null
}

// ============================================================================
// EVENTS
// ============================================================================

/**
 * Sealed class representing one-time events emitted by the Extension Controller.
 * These events are used for UI feedback and should be consumed once.
 */
sealed class ExtensionEvent {
    /** An error occurred during an extension operation. */
    data class Error(val error: ExtensionError) : ExtensionEvent()
    /** Extensions were successfully loaded. */
    data class ExtensionsLoaded(val installedCount: Int, val availableCount: Int) : ExtensionEvent()
    /** An extension was successfully installed. */
    data class InstallComplete(val catalog: CatalogRemote) : ExtensionEvent()
    /** An extension was successfully uninstalled. */
    data class UninstallComplete(val pkgName: String) : ExtensionEvent()
    /** An extension was successfully updated. */
    data class UpdateComplete(val catalog: CatalogInstalled) : ExtensionEvent()
    /** Extension updates are available. */
    data class UpdatesAvailable(val count: Int) : ExtensionEvent()
    /** All extensions are up to date. */
    object AllUpToDate : ExtensionEvent()
    /** Extensions were refreshed from remote. */
    object RefreshComplete : ExtensionEvent()
    /** Batch update completed. */
    data class BatchUpdateComplete(val successCount: Int, val totalCount: Int) : ExtensionEvent()
    /** Installation progress update. */
    data class InstallProgress(val pkgName: String, val progress: Float) : ExtensionEvent()
    /** Show a snackbar message. */
    data class ShowSnackbar(val message: String) : ExtensionEvent()
}

// ============================================================================
// ERRORS
// ============================================================================

/**
 * Sealed class representing all possible errors in extension operations.
 * Used for type-safe error handling across the Extension Controller.
 */
sealed class ExtensionError {
    /** Failed to load extensions from the catalog store. */
    data class LoadFailed(val message: String) : ExtensionError()
    /** Failed to install an extension. */
    data class InstallFailed(val pkgName: String, val message: String) : ExtensionError()
    /** Failed to uninstall an extension. */
    data class UninstallFailed(val pkgName: String, val message: String) : ExtensionError()
    /** Failed to update an extension. */
    data class UpdateFailed(val pkgName: String, val message: String) : ExtensionError()
    /** Network error occurred during extension operations. */
    data class NetworkError(val message: String) : ExtensionError()
    /** Failed to check for updates. */
    data class CheckUpdatesFailed(val message: String) : ExtensionError()
    /** Failed to refresh extensions from remote. */
    data class RefreshFailed(val message: String) : ExtensionError()
    
    /** Returns a user-friendly error message. */
    fun toUserMessage(): String = when (this) {
        is LoadFailed -> "Failed to load extensions: $message"
        is InstallFailed -> "Failed to install $pkgName: $message"
        is UninstallFailed -> "Failed to uninstall $pkgName: $message"
        is UpdateFailed -> "Failed to update $pkgName: $message"
        is NetworkError -> "Network error: $message"
        is CheckUpdatesFailed -> "Failed to check for updates: $message"
        is RefreshFailed -> "Failed to refresh extensions: $message"
    }
}
