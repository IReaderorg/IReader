package org.ireader.core_catalogs.service

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote

interface CatalogRemoteRepository {

    suspend fun getRemoteCatalogs(): List<CatalogRemote>

    fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>>

    suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>)
    suspend fun deleteAllRemoteCatalogs()

}

