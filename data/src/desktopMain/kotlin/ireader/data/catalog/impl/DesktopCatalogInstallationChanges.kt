package ireader.data.catalog.impl

import ireader.domain.catalogs.service.CatalogInstallationChange
import ireader.domain.catalogs.service.CatalogInstallationChanges
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class DesktopCatalogInstallationChanges: CatalogInstallationChanges {
    override val flow: SharedFlow<CatalogInstallationChange> = MutableSharedFlow()
}