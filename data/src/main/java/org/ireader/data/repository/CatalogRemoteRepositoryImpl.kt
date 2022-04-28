package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import org.ireader.data.local.dao.CatalogDao
import javax.inject.Inject

class CatalogRemoteRepositoryImpl @Inject constructor(
    private val dao: CatalogDao,
) : CatalogRemoteRepository {

    override suspend fun getRemoteCatalogs(): List<org.ireader.common_models.entities.CatalogRemote> {
        return dao.findAll()
    }

    override fun getRemoteCatalogsFlow(): Flow<List<org.ireader.common_models.entities.CatalogRemote>> {
        return dao.subscribeAll().distinctUntilChanged()
    }


    override suspend fun insertRemoteCatalogs(catalogs: List<org.ireader.common_models.entities.CatalogRemote>) {
        dao.insert(catalogs)
    }


    override suspend fun deleteAllRemoteCatalogs() {
        return dao.deleteAll()
    }
}
