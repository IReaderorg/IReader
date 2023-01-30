

package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.Catalog
import ireader.domain.catalogs.CatalogStore

class TogglePinnedCatalog(
    private val store: CatalogStore,
) {

    suspend fun await(catalog: Catalog) {
        store.togglePinnedCatalog(catalog.sourceId)
    }
}
