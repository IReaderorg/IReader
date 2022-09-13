

package ireader.domain.catalogs.interactor

import ireader.common.models.entities.CatalogInstalled
import ireader.domain.catalogs.service.CatalogInstaller

class UninstallCatalog(
    private val catalogInstaller: CatalogInstaller,

    ) {

    suspend fun await(catalog: CatalogInstalled) {
        catalogInstaller.uninstall(catalog.pkgName)
    }
}
