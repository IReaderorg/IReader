

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.model.CatalogSort

class GetCatalogsByType(
    private val localCatalogs: GetLocalCatalogs,
    private val remoteCatalogs: GetRemoteCatalogs,
) {

    suspend fun subscribe(
        sort: CatalogSort = CatalogSort.Favorites,
        excludeRemoteInstalled: Boolean = false,
        withNsfw: Boolean = true,
    ): Flow<Catalogs> {
        val localFlow = localCatalogs.subscribe(sort)
        val remoteFlow = remoteCatalogs.subscribe(withNsfw = withNsfw)
        return localFlow.combine(remoteFlow) { local, remote ->
            val (pinned, unpinned) = local
                .sortedByDescending { it.hasUpdate }
                .partition { it.isPinned }

            if (excludeRemoteInstalled) {
                val installedPkgs = local
                    .asSequence()
                    .filterIsInstance<CatalogInstalled>()
                    .map { it.pkgName }
                    .toSet()

                Catalogs(pinned, unpinned, remote.filter { it.pkgName !in installedPkgs })
            } else {
                Catalogs(pinned, unpinned, remote)
            }
        }
    }

    data class Catalogs(
        val pinned: List<CatalogLocal>,
        val unpinned: List<CatalogLocal>,
        val remote: List<CatalogRemote>,
    )
}
