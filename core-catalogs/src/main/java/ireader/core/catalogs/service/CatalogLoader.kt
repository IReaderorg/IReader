package ireader.core.catalogs.service

import ireader.common.models.entities.CatalogInstalled
import ireader.common.models.entities.CatalogLocal

interface CatalogLoader {

    suspend fun loadAll(): List<CatalogLocal>

    fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally?

    fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide?
}
