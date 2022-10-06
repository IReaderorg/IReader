package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.CatalogInstalled

interface UninstallCatalogs {
    suspend fun await(catalog: CatalogInstalled)
}