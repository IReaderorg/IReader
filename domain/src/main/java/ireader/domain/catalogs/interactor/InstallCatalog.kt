

package ireader.domain.catalogs.interactor

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.CatalogRemote
import ireader.core.api.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller

class InstallCatalog(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return catalogInstaller.install(catalog)
    }
}
