package ireader.data.catalog.impl

import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.models.entities.CatalogInstalled

/**
 * iOS implementation of UninstallCatalogs interactor
 */
class IosUninstallCatalogs(
    private val installer: IosCatalogInstaller,
) : UninstallCatalogs {
    
    override suspend fun await(catalog: CatalogInstalled) {
        installer.uninstall(catalog.pkgName)
    }
}
