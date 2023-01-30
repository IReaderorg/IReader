

package ireader.domain.catalogs.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.model.CatalogSort

class GetLocalCatalogs(
    private val catalogStore: CatalogStore,
    private val libraryRepository: BookRepository,
) {

    val catalogs = catalogStore.catalogs

    suspend fun subscribe(sort: CatalogSort = CatalogSort.Favorites): Flow<List<CatalogLocal>> {
        val flow = catalogStore.getCatalogsFlow()

        return when (sort) {
            CatalogSort.Name -> sortByName(flow)
            CatalogSort.Favorites -> sortByFavorites(flow)
        }
    }
    fun find(sourceId: Long?) : CatalogLocal? {
        return catalogStore.get(sourceId)
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
