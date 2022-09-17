package ireader.domain.catalogs.interactor

import ireader.common.models.entities.CatalogRemote
import ireader.core.api.os.InstallStep
import kotlinx.coroutines.flow.Flow

interface InstallCatalog {

    fun await(catalog: CatalogRemote): Flow<InstallStep>
}
