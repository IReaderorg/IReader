package ireader.domain.di


import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.*
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val CatalogModule = DI.Module("catalogModule") {



    bindSingleton<CatalogPreferences> { CatalogPreferences(instance()) }

    bindSingleton<CatalogStore> { CatalogStore(instance(),instance(),instance(),instance()) }

    bindSingleton {
        ReaderPreferences(
                instance(),
        )
    }


    bindProvider<GetDefaultRepo> {
        GetDefaultRepo(
                instance(),
                instance()
        )
    }
    bindSingleton<GetCatalogsByType> { GetCatalogsByType(instance(),instance()) }
    bindSingleton<GetRemoteCatalogs> { GetRemoteCatalogs(instance()) }
    bindSingleton<GetLocalCatalogs> { GetLocalCatalogs(instance(),instance()) }
    bindSingleton<GetLocalCatalog> { GetLocalCatalog(instance()) }
    bindSingleton<UpdateCatalog> { UpdateCatalog(instance(),instance()) }

    bindSingleton<TogglePinnedCatalog> { TogglePinnedCatalog(instance()) }

}
