package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.data.local.dao.CatalogDao
import org.ireader.domain.extensions.cataloge_service.CatalogRemoteRepository
import org.ireader.domain.models.entities.CatalogRemote
import javax.inject.Inject

class CatalogRemoteRepositoryImpl @Inject constructor(
    private val dao: CatalogDao,
) : CatalogRemoteRepository {

    override suspend fun getRemoteCatalogs(): List<CatalogRemote> {
        return dao.findAll()
    }

    override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
        return dao.subscribeAll()
    }

    override suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>) {
        dao.insertAll(catalogs)
    }

    override suspend fun deleteAllRemoteCatalogs(catalogs: List<CatalogRemote>) {
        return dao.deleteAll()
    }
}
