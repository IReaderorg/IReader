package ireader.domain.di


import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.catalogs.interactor.GetRemoteCatalogs
import ireader.domain.catalogs.interactor.TogglePinnedCatalog
import ireader.domain.catalogs.interactor.UpdateCatalog
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.usecases.services.LoadJSPluginsInBackgroundUseCase
import org.koin.dsl.module

val CatalogModule = module {



    single<CatalogPreferences> { CatalogPreferences(get()) }

    single<CatalogStore> { CatalogStore(get(),get(),get(),get(),get()) }

    single {
        ReaderPreferences(
                get(),
        )
    }


    factory <GetDefaultRepo> {
        GetDefaultRepo(
                get(),
                get()
        )
    }
    single<GetCatalogsByType> { GetCatalogsByType(get(),get()) }
    single<GetRemoteCatalogs> { GetRemoteCatalogs(get()) }
    single<GetLocalCatalogs> { GetLocalCatalogs(get(),get()) }
    single<GetLocalCatalog> { GetLocalCatalog(get()) }
    single<UpdateCatalog> { UpdateCatalog(get(),get()) }

    single<TogglePinnedCatalog> { TogglePinnedCatalog(get()) }
    
    single<LoadJSPluginsInBackgroundUseCase> {
        LoadJSPluginsInBackgroundUseCase(get(), get())
    }

}
