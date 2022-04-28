

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.core_catalogs.service.CatalogInstaller
import javax.inject.Inject

class UninstallCatalog @Inject constructor(
    private val catalogInstaller: CatalogInstaller,

) {

    suspend fun await(catalog: CatalogInstalled, onError: (Throwable) -> Unit = {}): Boolean {
        return catalogInstaller.uninstall(catalog.pkgName, onError)
    }
}
