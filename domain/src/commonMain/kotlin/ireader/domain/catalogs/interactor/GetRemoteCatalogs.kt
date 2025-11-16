

package ireader.domain.catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.catalogs.service.CatalogRemoteRepository

class GetRemoteCatalogs(
    private val catalogRemoteRepository: CatalogRemoteRepository,
) {

    suspend fun await(): List<CatalogRemote> {
        return catalogRemoteRepository.getRemoteCatalogs()
    }

    fun subscribe(
        withNsfw: Boolean = true,
        repositoryType: String? = null, // Filter by repository type
    ): Flow<List<CatalogRemote>> {
        return catalogRemoteRepository.getRemoteCatalogsFlow()
            .map { catalogs ->
                var filteredCatalogs = if (withNsfw) {
                    catalogs.distinctBy { it.sourceId }
                } else {
                    catalogs.filter { !it.nsfw }
                }
                
                // Filter by repository type if specified
                if (repositoryType != null) {
                    filteredCatalogs = filteredCatalogs.filter { catalog ->
                        catalog.repositoryType.equals(repositoryType, ignoreCase = true)
                    }
                }
                
                filteredCatalogs
            }
    }
}
