package ireader.domain.services.extension

import org.koin.dsl.module

/**
 * Koin module for the Extension Controller and its dependencies.
 * 
 * This module provides:
 * - ExtensionController as a singleton (single source of truth for extension state)
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */
val extensionModule = module {
    
    // ========== ExtensionController ==========
    
    /**
     * ExtensionController - The central coordinator for all extension-level operations.
     * 
     * This is registered as a SINGLETON to ensure all screens share the same
     * instance and receive consistent state updates.
     * 
     * Dependencies: CatalogStore, GetCatalogsByType, InstallCatalog, UninstallCatalog,
     *               UpdateCatalog, SyncRemoteCatalogs, TogglePinnedCatalog
     */
    single {
        ExtensionController(
            catalogStore = get(),
            getCatalogsByType = get(),
            installCatalog = get(),
            uninstallCatalog = get(),
            updateCatalog = get(),
            syncRemoteCatalogs = get(),
            togglePinnedCatalog = get()
        )
    }
}
