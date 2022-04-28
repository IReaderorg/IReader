

package org.ireader.core_catalogs.interactor

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core_catalogs.CatalogStore
import javax.inject.Inject

class GetLocalCatalog @Inject constructor(
    private val store: CatalogStore,
) {

    fun get(sourceId: Long): CatalogLocal? {
        return store.get(sourceId)
    }
}
