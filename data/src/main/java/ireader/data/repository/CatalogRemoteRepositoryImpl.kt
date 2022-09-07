package ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import ireader.core.catalogs.service.CatalogRemoteRepository
import ireader.data.local.dao.CatalogDao

class CatalogRemoteRepositoryImpl(
    private val dao: CatalogDao,
) : CatalogRemoteRepository {

    override suspend fun getRemoteCatalogs(): List<ireader.common.models.entities.CatalogRemote> {
        return dao.findAll()
    }

    override fun getRemoteCatalogsFlow(): Flow<List<ireader.common.models.entities.CatalogRemote>> {
        return dao.subscribeAll().distinctUntilChanged()
    }

    override suspend fun insertRemoteCatalogs(catalogs: List<ireader.common.models.entities.CatalogRemote>) {
        dao.insert(catalogs)
    }

    override suspend fun deleteAllRemoteCatalogs() {
        return dao.deleteAll()
    }
}
