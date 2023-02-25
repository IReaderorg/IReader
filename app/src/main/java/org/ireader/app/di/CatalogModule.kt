package org.ireader.app.di

import android.app.Application
import io.ktor.client.plugins.cookies.*
import ireader.core.http.BrowserEngine
import ireader.core.http.HttpClients
import ireader.core.http.WebViewCookieJar
import ireader.core.http.WebViewManger
import ireader.core.os.PackageInstaller
import ireader.core.prefs.PreferenceStore
import ireader.data.catalog.impl.*
import ireader.data.catalog.impl.interactor.InstallCatalogImpl
import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.*
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.presentation.imageloader.coil.CoilLoaderFactory



import org.koin.dsl.module

val CatalogModule = module {
    single<AndroidCatalogInstallationChanges>(qualifier=null) { AndroidCatalogInstallationChanges(get()) }
    single<CatalogInstallationChanges>(qualifier=null) { AndroidCatalogInstallationChanges(get()) }
    single<CatalogInstaller>(qualifier=null) { AndroidCatalogInstaller(get(),get(),get(),get(),get(),get()) }
    single<AndroidLocalInstaller>(qualifier=null) { AndroidLocalInstaller(get(),get(),get(),get(),get(),get()) }
    single<CatalogPreferences>(qualifier=null) { CatalogPreferences(get()) }
    single<CoverCache>(qualifier=null) { CoverCache(get(),get()) }
    single<CoilLoaderFactory>(qualifier=null) { CoilLoaderFactory(get(),get(),get(),get()) }
    single<CatalogStore>(qualifier=null) { CatalogStore(AndroidCatalogLoader(get(),get(),get(),get()),get(),get(),get()) }
    single<PackageInstaller>(qualifier=null) { PackageInstaller(get()) }
    single<WebViewCookieJar>(qualifier=null) { WebViewCookieJar(get()) }
    single<HttpClients>(qualifier=null) { HttpClients(get(),BrowserEngine(get(), get()),get(),get()) }
    single<AndroidCatalogInstaller>(qualifier=null) { AndroidCatalogInstaller(get(),get(),get(),get(),get(),get()) }
    single<GetCatalogsByType>(qualifier=null) { GetCatalogsByType(get(),get()) }
    single<GetRemoteCatalogs>(qualifier=null) { GetRemoteCatalogs(get()) }
    single<GetLocalCatalogs>(qualifier=null) { GetLocalCatalogs(get(),get()) }
    single<GetLocalCatalog>(qualifier=null) { GetLocalCatalog(get()) }
    single<UpdateCatalog>(qualifier=null) { UpdateCatalog(get(),get()) }
    single<InstallCatalog>(qualifier=null) { InstallCatalogImpl(get(),get(),get()) }
    single<TogglePinnedCatalog>(qualifier=null) { TogglePinnedCatalog(get()) }
    single<SyncRemoteCatalogs>(qualifier=null) { SyncRemoteCatalogs(get(),CatalogGithubApi(get(),get()),get()) }
}
