package ireader.domain.catalogs.service

import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal

interface CatalogLoader {

    suspend fun loadAll(): List<CatalogLocal>

    fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally?

    fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide?
}
