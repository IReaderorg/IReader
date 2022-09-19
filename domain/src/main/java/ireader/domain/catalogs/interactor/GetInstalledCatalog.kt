

package ireader.domain.catalogs.interactor

import ireader.common.models.entities.CatalogInstalled
import ireader.domain.catalogs.CatalogStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@org.koin.core.annotation.Factory
class GetInstalledCatalog(
    private val catalogStore: CatalogStore,
) {

    fun get(pkgName: String): CatalogInstalled? {
        return catalogStore.catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }
            as? CatalogInstalled
    }

    fun subscribe(pkgName: String): Flow<CatalogInstalled?> {
        return catalogStore.getCatalogsFlow()
            .map { catalogs ->
                catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName } as? CatalogInstalled
            }
            .distinctUntilChanged()
    }
    fun get() : List<CatalogInstalled> {
        return catalogStore.catalogs.filterIsInstance<CatalogInstalled>()
    }
}
