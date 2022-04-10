package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.ireader.data.local.dao.CatalogDao
import org.ireader.domain.catalog.service.CatalogRemoteRepository
import org.ireader.domain.models.entities.CatalogRemote
import javax.inject.Inject

class CatalogRemoteRepositoryImpl @Inject constructor(
    private val dao: CatalogDao,
) : CatalogRemoteRepository {

    override suspend fun getRemoteCatalogs(): List<CatalogRemote> {
        return dao.findAll()
    }

    override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
        return dao.subscribeAll().distinctUntilChanged()
    }


    override suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>) {
        dao.insert(catalogs)
    }


    override suspend fun deleteAllRemoteCatalogs() {
        return dao.deleteAll()
    }
}
