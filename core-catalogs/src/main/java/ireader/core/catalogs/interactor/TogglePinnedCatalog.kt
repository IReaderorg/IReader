

package ireader.core.catalogs.interactor

import ireader.common.models.entities.Catalog
import ireader.core.catalogs.CatalogStore

class TogglePinnedCatalog(
    private val store: CatalogStore,
) {

    suspend fun await(catalog: Catalog) {
        store.togglePinnedCatalog(catalog.sourceId)
    }
}
