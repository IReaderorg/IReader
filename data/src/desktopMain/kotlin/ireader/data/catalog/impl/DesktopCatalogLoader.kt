package ireader.data.catalog.impl

import ireader.core.source.TestSource
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal

class DesktopCatalogLoader : CatalogLoader {
    override suspend fun loadAll(): List<CatalogLocal> {
        return listOf(
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
                CatalogBundled(TestSource(),),
        )
    }

    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        TODO("Not yet implemented")
    }

    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? {
        TODO("Not yet implemented")
    }

}