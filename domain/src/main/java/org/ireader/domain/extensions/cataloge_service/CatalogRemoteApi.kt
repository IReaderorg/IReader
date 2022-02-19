package org.ireader.domain.extensions.cataloge_service

import org.ireader.domain.models.entities.CatalogRemote

interface CatalogRemoteApi {

    suspend fun fetchCatalogs(): List<CatalogRemote>

}
