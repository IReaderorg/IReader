package ireader.data.catalog.impl

import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.prefs.PreferenceValues
import kotlinx.coroutines.flow.Flow

/**
 * iOS implementation of InstallCatalog interactor
 */
class IosInstallCatalog(
    private val installer: IosCatalogInstaller,
) : InstallCatalog {
    
    override fun await(catalog: CatalogRemote): Flow<InstallStep> = installer.install(catalog)

    override fun await(type: PreferenceValues.Installer): CatalogInstaller = installer
}
