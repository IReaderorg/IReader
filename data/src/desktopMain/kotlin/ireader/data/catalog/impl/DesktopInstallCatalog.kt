package ireader.data.catalog.impl

import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.prefs.PreferenceValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DesktopInstallCatalog: InstallCatalog {
    override fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return flow {

        }
    }

    override fun await(type: PreferenceValues.Installer): CatalogInstaller {
        TODO("Not yet implemented")
    }
}