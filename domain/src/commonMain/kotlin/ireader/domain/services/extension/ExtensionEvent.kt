package ireader.domain.services.extension

import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogRemote

/**
 * Sealed class representing one-time events emitted by the Extension Controller.
 * These events are used for UI feedback and should be consumed once.
 * 
 * Requirements: 3.4, 3.5, 4.2, 4.3
 */
sealed class ExtensionEvent {
    /**
     * An error occurred during an extension operation.
     * Requirements: 4.2, 4.3
     */
    data class Error(val error: ExtensionError) : ExtensionEvent()
    
    /**
     * Extensions were successfully loaded.
     */
    data class ExtensionsLoaded(
        val installedCount: Int,
        val availableCount: Int
    ) : ExtensionEvent()
    
    /**
     * An extension was successfully installed.
     */
    data class InstallComplete(val catalog: CatalogRemote) : ExtensionEvent()
    
    /**
     * An extension was successfully uninstalled.
     */
    data class UninstallComplete(val pkgName: String) : ExtensionEvent()
    
    /**
     * An extension was successfully updated.
     */
    data class UpdateComplete(val catalog: CatalogInstalled) : ExtensionEvent()
    
    /**
     * Extension updates are available.
     */
    data class UpdatesAvailable(val count: Int) : ExtensionEvent()
    
    /**
     * All extensions are up to date.
     */
    object AllUpToDate : ExtensionEvent()
    
    /**
     * Extensions were refreshed from remote.
     */
    object RefreshComplete : ExtensionEvent()
    
    /**
     * Batch update completed.
     */
    data class BatchUpdateComplete(val successCount: Int, val totalCount: Int) : ExtensionEvent()
    
    /**
     * Installation progress update.
     */
    data class InstallProgress(val pkgName: String, val progress: Float) : ExtensionEvent()
    
    /**
     * Show a snackbar message.
     */
    data class ShowSnackbar(val message: String) : ExtensionEvent()
}
