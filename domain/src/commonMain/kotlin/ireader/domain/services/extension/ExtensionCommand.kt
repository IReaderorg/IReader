package ireader.domain.services.extension

import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogRemote

/**
 * Sealed class representing all commands that can be dispatched to the Extension Controller.
 * All extension operations are expressed as commands for predictable state management.
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5
 */
sealed class ExtensionCommand {
    // ========== Lifecycle Commands ==========
    
    /**
     * Load extensions and subscribe to reactive updates.
     * Requirements: 3.2
     */
    object LoadExtensions : ExtensionCommand()
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    object Cleanup : ExtensionCommand()
    
    // ========== Installation Commands ==========
    
    /**
     * Install an extension from remote catalog.
     * Requirements: 3.2
     */
    data class InstallExtension(val catalog: CatalogRemote) : ExtensionCommand()
    
    /**
     * Uninstall an installed extension.
     * Requirements: 3.2
     */
    data class UninstallExtension(val catalog: CatalogInstalled) : ExtensionCommand()
    
    /**
     * Update an installed extension.
     * Requirements: 3.2
     */
    data class UpdateExtension(val catalog: CatalogInstalled) : ExtensionCommand()
    
    /**
     * Cancel an ongoing installation job.
     */
    data class CancelInstallation(val catalog: Catalog) : ExtensionCommand()
    
    // ========== Filter Commands ==========
    
    /**
     * Set the language filter for extensions.
     * Requirements: 3.2
     */
    data class SetFilter(val filter: ExtensionFilter) : ExtensionCommand()
    
    /**
     * Set the search query for filtering extensions.
     */
    data class SetSearchQuery(val query: String?) : ExtensionCommand()
    
    /**
     * Set the repository type filter.
     */
    data class SetRepositoryType(val repositoryType: String?) : ExtensionCommand()
    
    // ========== Update Commands ==========
    
    /**
     * Check for available extension updates.
     * Requirements: 3.2
     */
    object CheckUpdates : ExtensionCommand()
    
    /**
     * Refresh extensions from remote repositories.
     */
    object RefreshExtensions : ExtensionCommand()
    
    /**
     * Batch update all extensions with available updates.
     */
    object BatchUpdateExtensions : ExtensionCommand()
    
    // ========== Catalog Commands ==========
    
    /**
     * Toggle pinned status of a catalog.
     */
    data class TogglePinned(val catalog: Catalog) : ExtensionCommand()
    
    /**
     * Clear the current error state.
     */
    object ClearError : ExtensionCommand()
}

/**
 * Filter options for extensions.
 */
sealed class ExtensionFilter {
    object All : ExtensionFilter()
    data class ByLanguage(val languageCodes: Set<String>) : ExtensionFilter()
    data class ByRepository(val repositoryType: String) : ExtensionFilter()
    data class Combined(val languageCodes: Set<String>?, val repositoryType: String?) : ExtensionFilter()
}
