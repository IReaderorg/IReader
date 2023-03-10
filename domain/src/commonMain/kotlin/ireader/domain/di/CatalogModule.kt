package ireader.domain.di


import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val CatalogModule = DI.Module("catalogModule") {



    bindSingleton<CatalogPreferences> { CatalogPreferences(instance()) }

    bindSingleton<CatalogStore> { CatalogStore(instance(),instance(),instance(),instance()) }



    bindSingleton<GetCatalogsByType> { GetCatalogsByType(instance(),instance()) }
    bindSingleton<GetRemoteCatalogs> { GetRemoteCatalogs(instance()) }
    bindSingleton<GetLocalCatalogs> { GetLocalCatalogs(instance(),instance()) }
    bindSingleton<GetLocalCatalog> { GetLocalCatalog(instance()) }
    bindSingleton<UpdateCatalog> { UpdateCatalog(instance(),instance()) }

    bindSingleton<TogglePinnedCatalog> { TogglePinnedCatalog(instance()) }

}
