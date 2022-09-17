package ireader.domain.catalogs.interactor

import ireader.common.models.entities.CatalogInstalled

interface UninstallCatalogs {
    suspend fun await(catalog: CatalogInstalled)
}