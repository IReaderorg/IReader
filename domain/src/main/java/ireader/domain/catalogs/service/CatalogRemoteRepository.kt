package ireader.domain.catalogs.service

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.CatalogRemote

interface CatalogRemoteRepository {

    suspend fun getRemoteCatalogs(): List<CatalogRemote>

    fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>>

    suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>)
    suspend fun deleteAllRemoteCatalogs()
}
