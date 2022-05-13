

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.Catalog
import org.ireader.core_catalogs.CatalogStore

class TogglePinnedCatalog(
    private val store: CatalogStore,
) {

    suspend fun await(catalog: Catalog) {
        store.togglePinnedCatalog(catalog.sourceId)
    }
}
