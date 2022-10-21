

package ireader.domain.catalogs.service

import kotlinx.coroutines.flow.SharedFlow

interface CatalogInstallationChanges {
    val flow: SharedFlow<CatalogInstallationChange>
}

sealed class CatalogInstallationChange {
    abstract val pkgName: String

    data class SystemInstall(override val pkgName: String) : CatalogInstallationChange()

    data class SystemUninstall(override val pkgName: String) : CatalogInstallationChange()

    data class LocalInstall(override val pkgName: String) : CatalogInstallationChange()

    data class LocalUninstall(override val pkgName: String) : CatalogInstallationChange()
}
