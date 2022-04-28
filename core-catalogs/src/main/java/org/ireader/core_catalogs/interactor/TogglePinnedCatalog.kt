

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core_catalogs.CatalogStore
import javax.inject.Inject

class TogglePinnedCatalog @Inject constructor(
    private val store: CatalogStore,
) {

    suspend fun await(catalog: CatalogLocal) {
        store.togglePinnedCatalog(catalog.sourceId)
    }
}
