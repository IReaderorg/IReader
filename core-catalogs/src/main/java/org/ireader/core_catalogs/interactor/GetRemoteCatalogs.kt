

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.service.CatalogRemoteRepository

class GetRemoteCatalogs(
    private val catalogRemoteRepository: CatalogRemoteRepository,
) {

    suspend fun await(): List<CatalogRemote> {
        return catalogRemoteRepository.getRemoteCatalogs()
    }

    fun subscribe(
        withNsfw: Boolean = true,
    ): Flow<List<CatalogRemote>> {
        return catalogRemoteRepository.getRemoteCatalogsFlow()
            .map { catalogs ->
                if (withNsfw) {
                    catalogs.distinctBy { it.sourceId }
                } else {
                    catalogs.filter { !it.nsfw }
                }
            }
    }
}
