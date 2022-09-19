package ireader.domain.catalogs.interactor

import ireader.common.models.entities.CatalogRemote
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.prefs.PreferenceValues
import kotlinx.coroutines.flow.Flow

interface InstallCatalog {

    fun await(catalog: CatalogRemote): Flow<InstallStep>
    fun await(type: PreferenceValues.Installer): CatalogInstaller
}