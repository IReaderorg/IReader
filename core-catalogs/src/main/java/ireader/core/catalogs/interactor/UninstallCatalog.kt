

package ireader.core.catalogs.interactor

import ireader.common.models.entities.CatalogInstalled
import ireader.core.catalogs.service.CatalogInstaller

class UninstallCatalog(
    private val catalogInstaller: CatalogInstaller,

) {

    suspend fun await(catalog: CatalogInstalled) {
        catalogInstaller.uninstall(catalog.pkgName)
    }
}
