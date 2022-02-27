package org.ireader.domain.catalog

import org.ireader.domain.catalog.interactor.*

data class CatalogInterceptors(
    val getCatalogsByType: GetCatalogsByType,
    val getInstalledCatalog: GetInstalledCatalog,
    val getLocalCatalog: GetLocalCatalog,
    val getLocalCatalogs: GetLocalCatalogs,
    val getRemoteCatalogs: GetRemoteCatalogs,
    val installCatalog: InstallCatalog,
    val syncRemoteCatalogs: SyncRemoteCatalogs,
    val togglePinnedCatalog: TogglePinnedCatalog,
    val uninstallCatalog: UninstallCatalog,
    val updateCatalog: UpdateCatalog,
)
