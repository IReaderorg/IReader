package ireader.domain.catalogs.service

import ireader.domain.models.entities.CatalogRemote

interface CatalogRemoteApi {

    suspend fun fetchCatalogs(): List<CatalogRemote>
}
