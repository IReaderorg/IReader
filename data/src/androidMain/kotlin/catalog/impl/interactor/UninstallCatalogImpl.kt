

package ireader.data.catalog.impl.interactor

import ireader.domain.models.entities.CatalogInstalled
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.catalogs.interactor.UninstallCatalogs



class UninstallCatalogImpl(private val androidCatalogInstaller: AndroidCatalogInstaller, private val androidLocalInstaller: AndroidLocalInstaller) :
    UninstallCatalogs {

    override suspend fun await(catalog: CatalogInstalled) {
        when (catalog) {
            is CatalogInstalled.SystemWide -> {
                androidCatalogInstaller.uninstall(catalog.pkgName)
            }
            else -> {
                androidLocalInstaller.uninstall(catalog.pkgName)
            }
        }

    }
}
