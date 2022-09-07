package ireader.core.catalogs.service

import ireader.common.models.entities.CatalogRemote

interface CatalogRemoteApi {

    suspend fun fetchCatalogs(): List<CatalogRemote>
}
