

package ireader.domain.catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.catalogs.model.CatalogSort

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
