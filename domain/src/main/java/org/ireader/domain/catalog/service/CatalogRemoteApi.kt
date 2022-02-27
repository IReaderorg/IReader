package org.ireader.domain.catalog.service

import org.ireader.domain.models.entities.CatalogRemote

interface CatalogRemoteApi {

    suspend fun fetchCatalogs(): List<CatalogRemote>

}
