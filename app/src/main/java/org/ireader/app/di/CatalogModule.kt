package org.ireader.app.di


import ireader.core.http.BrowserEngine
import ireader.core.http.HttpClients
import ireader.core.http.WebViewCookieJar
import ireader.core.os.PackageInstaller
import ireader.data.catalog.impl.AndroidCatalogInstallationChanges
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.data.catalog.impl.CatalogGithubApi
import ireader.data.catalog.impl.interactor.InstallCatalogImpl
import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.*
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.image.cache.CoverCache
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val CatalogModule = DI.Module("catalogModule") {
    bindSingleton<AndroidCatalogInstallationChanges> { AndroidCatalogInstallationChanges(instance()) }
    bindSingleton<CatalogInstallationChanges> { instance<AndroidCatalogInstallationChanges>() }
    bindSingleton<CatalogInstaller> { AndroidCatalogInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<AndroidLocalInstaller> { AndroidLocalInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<CatalogPreferences> { CatalogPreferences(instance()) }
    bindSingleton<CoverCache> { CoverCache(instance(),instance()) }
    bindSingleton<CoilLoaderFactory> { CoilLoaderFactory(instance(),instance(),instance(),instance()) }
    bindSingleton<CatalogStore> { CatalogStore(instance(),instance(),instance(),instance()) }
    bindSingleton<PackageInstaller> { PackageInstaller(instance()) }
    bindSingleton<WebViewCookieJar> { WebViewCookieJar(instance()) }
    bindSingleton<HttpClients> { HttpClients(instance(),BrowserEngine(instance(), instance()),instance(),instance()) }
    bindSingleton<AndroidCatalogInstaller> { AndroidCatalogInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<GetCatalogsByType> { GetCatalogsByType(instance(),instance()) }
    bindSingleton<GetRemoteCatalogs> { GetRemoteCatalogs(instance()) }
    bindSingleton<GetLocalCatalogs> { GetLocalCatalogs(instance(),instance()) }
    bindSingleton<GetLocalCatalog> { GetLocalCatalog(instance()) }
    bindSingleton<UpdateCatalog> { UpdateCatalog(instance(),instance()) }
    bindSingleton<InstallCatalog> { InstallCatalogImpl(instance(),instance(),instance()) }
    bindSingleton<TogglePinnedCatalog> { TogglePinnedCatalog(instance()) }
    bindSingleton<SyncRemoteCatalogs> { SyncRemoteCatalogs(instance(),CatalogGithubApi(instance(),instance()),instance()) }
}
