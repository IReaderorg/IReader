package org.ireader.domain.extensions.cataloge_service

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.CatalogRemote


interface CatalogRemoteRepository {

    suspend fun getRemoteCatalogs(): List<CatalogRemote>

    fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>>

    suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>)

    suspend fun deleteAllRemoteCatalogs(catalogs: List<CatalogRemote>)

}
