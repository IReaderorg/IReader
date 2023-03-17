package ireader.data.catalog.impl

import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.models.entities.CatalogInstalled

class DesktopUninstallCatalogs(
    private val installer: DesktopCatalogInstaller,
): UninstallCatalogs {
    override suspend fun await(catalog: CatalogInstalled) {
        installer.uninstall(catalog.pkgName)
    }
}