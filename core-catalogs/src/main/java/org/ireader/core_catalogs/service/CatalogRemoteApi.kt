package org.ireader.core_catalogs.service

import org.ireader.common_models.entities.CatalogRemote

interface CatalogRemoteApi {

    suspend fun fetchCatalogs(): List<CatalogRemote>
}
