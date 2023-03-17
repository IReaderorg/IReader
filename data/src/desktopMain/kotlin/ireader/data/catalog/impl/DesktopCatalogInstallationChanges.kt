package ireader.data.catalog.impl

import ireader.domain.catalogs.service.CatalogInstallationChange
import ireader.domain.catalogs.service.CatalogInstallationChanges
import kotlinx.coroutines.flow.MutableSharedFlow

class DesktopCatalogInstallationChanges: CatalogInstallationChanges {
    override val flow = MutableSharedFlow<CatalogInstallationChange>(
        extraBufferCapacity = Int.MAX_VALUE
    )

    fun notifyAppInstall(pkgName: String) {
        flow.tryEmit(CatalogInstallationChange.LocalInstall(pkgName))
    }

    fun notifyAppUninstall(pkgName: String) {
        flow.tryEmit(CatalogInstallationChange.LocalUninstall(pkgName))
    }
}