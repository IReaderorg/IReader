

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.core_catalogs.service.CatalogInstaller

class UninstallCatalog(
    private val catalogInstaller: CatalogInstaller,

) {

    suspend fun await(catalog: CatalogInstalled) {
        catalogInstaller.uninstall(catalog.pkgName)
    }
}
