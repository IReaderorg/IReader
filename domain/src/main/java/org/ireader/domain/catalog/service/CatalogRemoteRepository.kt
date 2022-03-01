package org.ireader.domain.catalog.service

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.CatalogRemote

interface CatalogRemoteRepository {

    suspend fun getRemoteCatalogs(): List<CatalogRemote>

    fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>>

    suspend fun setRemoteCatalogs(catalogs: List<CatalogRemote>)
    suspend fun deleteAllRemoteCatalogs()

}

