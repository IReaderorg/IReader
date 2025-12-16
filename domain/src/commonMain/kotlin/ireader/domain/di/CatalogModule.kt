package ireader.domain.di


import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.catalogs.interactor.GetRemoteCatalogs
import ireader.domain.catalogs.interactor.TogglePinnedCatalog
import ireader.domain.catalogs.interactor.UpdateCatalog
import ireader.domain.community.CommunityPreferences
import ireader.domain.community.CommunityRepository
import ireader.domain.community.CommunityRepositoryImpl
import ireader.domain.community.CommunitySource
import ireader.domain.community.cloudflare.communityTranslationModule
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.usecases.services.LoadJSPluginsInBackgroundUseCase
import org.koin.dsl.module

val CatalogModule = module {
    // Preferences - lightweight, can be singleton
    // Must be registered BEFORE communityTranslationModule which depends on it
    single<CommunityPreferences> { CommunityPreferences(get()) }
    
    // Include Cloudflare-based community translation module
    // This depends on CommunityPreferences, so it must come after
    includes(communityTranslationModule)
    single<CommunityRepository> { CommunityRepositoryImpl(get(), get(),get()) }
    single<CommunitySource> { CommunitySource(get()) }
    single<CatalogPreferences> { CatalogPreferences(get()) }

    // CatalogStore - now lazy-initialized internally, safe as singleton
    // The actual catalog loading is deferred until first access
    single<CatalogStore> { CatalogStore(get(),get(),get(),get(),get(),get()) }

    single {
        ReaderPreferences(
                get(),
        )
    }

    // Use cases - factory for on-demand creation
    factory <GetDefaultRepo> {
        GetDefaultRepo(
                get(),
                get()
        )
    }
    factory<GetCatalogsByType> { GetCatalogsByType(get(),get()) }
    factory<GetRemoteCatalogs> { GetRemoteCatalogs(get()) }
    factory<GetLocalCatalogs> { GetLocalCatalogs(get(),get()) }
    factory<GetLocalCatalog> { GetLocalCatalog(get()) }
    factory<UpdateCatalog> { UpdateCatalog(get(),get()) }
    factory<TogglePinnedCatalog> { TogglePinnedCatalog(get()) }
    
    factory<LoadJSPluginsInBackgroundUseCase> {
        LoadJSPluginsInBackgroundUseCase(get(), get())
    }

}
