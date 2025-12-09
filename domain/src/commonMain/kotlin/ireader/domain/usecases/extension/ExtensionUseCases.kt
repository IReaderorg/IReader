package ireader.domain.usecases.extension

import ireader.domain.catalogs.interactor.ExtensionManager
import ireader.domain.catalogs.interactor.ExtensionRepositoryManager
import ireader.domain.catalogs.interactor.ExtensionSecurityManager
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.catalogs.interactor.TogglePinnedCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.interactor.UpdateCatalog
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.data.repository.SourceCredentialsRepository
import ireader.domain.services.ExtensionWatcherService
import ireader.domain.services.SourceHealthChecker

/**
 * Aggregate class for ExtensionViewModel use cases.
 * Groups related use cases to reduce constructor parameter count.
 * 
 * This aggregate reduces ExtensionViewModel constructor parameters from 17 to ~10.
 * 
 * Requirements: 1.3, 1.4, 1.5
 * - 1.3: ExtensionViewModel accepts no more than 10 constructor parameters
 * - 1.4: Use case aggregate registered as factory in DI module
 * - 1.5: Maintains same functionality as individual use case injection
 */
data class ExtensionUseCases(
    /** Use case for getting catalogs by type (installed, remote, etc.) */
    val getCatalogsByType: GetCatalogsByType,
    
    /** Use case for updating a catalog */
    val updateCatalog: UpdateCatalog,
    
    /** Use case for installing a catalog */
    val installCatalog: InstallCatalog,
    
    /** Use case for uninstalling catalogs */
    val uninstallCatalog: UninstallCatalogs,
    
    /** Use case for toggling pinned status of a catalog */
    val togglePinnedCatalog: TogglePinnedCatalog,
    
    /** Use case for syncing remote catalogs */
    val syncRemoteCatalogs: SyncRemoteCatalogs,
    
    /** Service for checking source health */
    val sourceHealthChecker: SourceHealthChecker,
    
    /** Repository for source credentials */
    val sourceCredentialsRepository: SourceCredentialsRepository,
    
    /** Service for watching extension changes */
    val extensionWatcherService: ExtensionWatcherService,
    
    /** Repository for catalog sources */
    val catalogSourceRepository: CatalogSourceRepository,
    
    /** Manager for extension operations (optional, Android-only) */
    val extensionManager: ExtensionManager? = null,
    
    /** Manager for extension security (optional, Android-only) */
    val extensionSecurityManager: ExtensionSecurityManager? = null,
    
    /** Manager for extension repositories (optional, Android-only) */
    val extensionRepositoryManager: ExtensionRepositoryManager? = null
)
