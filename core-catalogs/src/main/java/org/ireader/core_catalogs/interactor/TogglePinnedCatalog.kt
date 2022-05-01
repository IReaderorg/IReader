

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core_catalogs.CatalogStore

class TogglePinnedCatalog(
    private val store: CatalogStore,
) {

    suspend fun await(catalog: CatalogLocal) {
        store.togglePinnedCatalog(catalog.sourceId)
    }
}
