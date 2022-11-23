

package ireader.domain.catalogs.interactor

import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.entities.CatalogInstalled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Factory

@Factory
class UpdateCatalog(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val installCatalog: InstallCatalog,
) {

    suspend fun await(catalog: CatalogInstalled): Flow<InstallStep> {
        val catalogs = catalogRemoteRepository.getRemoteCatalogs()

        val catalogToUpdate = catalogs.find { it.pkgName == catalog.pkgName }
        return if (catalogToUpdate == null) {
            emptyFlow()
        } else {
            installCatalog.await(catalogToUpdate)
        }
    }
}
