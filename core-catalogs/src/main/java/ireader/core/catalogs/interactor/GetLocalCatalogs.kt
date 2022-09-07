

package ireader.core.catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.common.data.repository.BookRepository
import ireader.common.models.entities.CatalogLocal
import ireader.core.catalogs.CatalogStore
import ireader.core.catalogs.model.CatalogSort

class GetLocalCatalogs(
    private val catalogStore: CatalogStore,
    private val libraryRepository: BookRepository,
) {

    suspend fun subscribe(sort: CatalogSort = CatalogSort.Favorites): Flow<List<CatalogLocal>> {
        val flow = catalogStore.getCatalogsFlow()

        return when (sort) {
            CatalogSort.Name -> sortByName(flow)
            CatalogSort.Favorites -> sortByFavorites(flow)
        }
    }

    private fun sortByName(catalogsFlow: Flow<List<CatalogLocal>>): Flow<List<CatalogLocal>> {
        return catalogsFlow.map { catalogs ->
            catalogs.sortedBy { it.name }
        }
    }

    private suspend fun sortByFavorites(
        catalogsFlow: Flow<List<CatalogLocal>>,
    ): Flow<List<CatalogLocal>> {
        var position = 0
        val favoriteIds = libraryRepository.findFavoriteSourceIds().associateWith { position++ }

        return catalogsFlow.map { catalogs ->
            catalogs.sortedWith(FavoritesComparator(favoriteIds).thenBy { it.name })
        }
    }

    private class FavoritesComparator(val favoriteIds: Map<Long, Int>) : Comparator<CatalogLocal> {

        override fun compare(a: CatalogLocal, b: CatalogLocal): Int {
            val pos1 = favoriteIds[a.sourceId] ?: Int.MAX_VALUE
            val pos2 = favoriteIds[b.sourceId] ?: Int.MAX_VALUE

            return pos1.compareTo(pos2)
        }
    }
}
