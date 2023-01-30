

package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.CatalogLocal
import ireader.domain.catalogs.CatalogStore

class GetLocalCatalog(
    private val store: CatalogStore,
) {

    fun get(sourceId: Long): CatalogLocal? {
        return store.get(sourceId)
    }
}
