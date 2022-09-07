package org.ireader.app.di

import android.app.Application
import io.ktor.client.plugins.cookies.CookiesStorage
import ireader.common.data.repository.BookRepository
import ireader.core.api.http.BrowserEngine
import ireader.core.api.http.HttpClients
import ireader.core.api.http.WebViewCookieJar
import ireader.core.api.http.WebViewManger
import ireader.core.api.os.PackageInstaller
import ireader.core.api.prefs.PreferenceStore
import ireader.core.catalogs.CatalogPreferences
import ireader.core.catalogs.CatalogStore
import ireader.core.catalogs.interactor.GetCatalogsByType
import ireader.core.catalogs.interactor.GetLocalCatalog
import ireader.core.catalogs.interactor.GetLocalCatalogs
import ireader.core.catalogs.interactor.GetRemoteCatalogs
import ireader.core.catalogs.interactor.InstallCatalog
import ireader.core.catalogs.interactor.SyncRemoteCatalogs
import ireader.core.catalogs.interactor.TogglePinnedCatalog
import ireader.core.catalogs.interactor.UninstallCatalog
import ireader.core.catalogs.interactor.UpdateCatalog
import ireader.core.catalogs.service.CatalogInstaller
import ireader.core.catalogs.service.CatalogRemoteRepository
import ireader.data.catalog.AndroidCatalogLoader
import ireader.data.catalog.AndroidCatalogInstaller
import ireader.data.catalog.CatalogGithubApi
import ireader.ui.imageloader.coil.CoilLoaderFactory
import ireader.ui.imageloader.coil.cache.CoverCache
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("org.ireader.app.di.CatalogModule")
class CatalogModule {

        @Single
    fun provideAndroidCatalogInstallationChanges(context: Application): ireader.data.catalog.AndroidCatalogInstallationChanges {
        return ireader.data.catalog.AndroidCatalogInstallationChanges(context)
    }

        @Single
    fun provideAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: ireader.data.catalog.AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller
    ): CatalogInstaller {
        return AndroidCatalogInstaller(context, httpClient, installationChanges, packageInstaller)
    }

        @Single
    fun provideCatalogPreferences(
        store: PreferenceStore
    ): CatalogPreferences {
        return CatalogPreferences(store)
    }

        @Single
    fun provideCoverCache(context: Application): CoverCache {
        return CoverCache(context)
    }

        @Single
    fun provideImageLoader(
        context: Application,
        coverCache: CoverCache,
        client: HttpClients,
        catalogStore: CatalogStore,
    ): CoilLoaderFactory {
        return CoilLoaderFactory(
            client = client,
            context = context,
            coverCache = coverCache,
            catalogStore = catalogStore,
        )
    }

        @Single
    fun providesCatalogStore(
        catalogPreferences: CatalogPreferences,
        catalogRemoteRepository: CatalogRemoteRepository,
        installationChanges: ireader.data.catalog.AndroidCatalogInstallationChanges,
        context: Application,
        httpClients: HttpClients,
    ): CatalogStore {
        return CatalogStore(
            AndroidCatalogLoader(context, httpClients),
            catalogPreferences,
            catalogRemoteRepository,
            installationChanges
        )
    }

        @Single
    fun providesPackageInstaller(
        app: Application
    ): PackageInstaller {
        return PackageInstaller(
            app
        )
    }

        @Single
    fun providesWebViewCookieJar(
        cookiesStorage: CookiesStorage,
    ): WebViewCookieJar {
        return WebViewCookieJar(cookiesStorage)
    }

        @Single
    fun providesHttpClients(
        context: Application,
        cookiesStorage: CookiesStorage,
        webViewManger: WebViewManger,
        webViewCookieJar: WebViewCookieJar,
    ): HttpClients {
        return HttpClients(
            context,
            BrowserEngine(webViewManger, webViewCookieJar),
            cookiesStorage,
            webViewCookieJar
        )
    }

        @Single
    fun providesAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: ireader.data.catalog.AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller
    ): AndroidCatalogInstaller {
        return AndroidCatalogInstaller(
            context,
            httpClient,
            installationChanges,
            packageInstaller
        )
    }

        @Single
    fun providesGetCatalogsByType(
        localCatalogs: GetLocalCatalogs,
        remoteCatalogs: GetRemoteCatalogs,
    ): GetCatalogsByType {
        return GetCatalogsByType(localCatalogs, remoteCatalogs)
    }

        @Single
    fun providesGetRemoteCatalogs(
        catalogRemoteRepository: CatalogRemoteRepository,
    ): GetRemoteCatalogs {
        return GetRemoteCatalogs(catalogRemoteRepository)
    }

        @Single
    fun providesGetLocalCatalogs(
        catalogStore: CatalogStore,
        libraryRepository: BookRepository,
    ): GetLocalCatalogs {
        return GetLocalCatalogs(catalogStore, libraryRepository)
    }

        @Single
    fun providesGetLocalCatalog(
        store: CatalogStore
    ): GetLocalCatalog {
        return GetLocalCatalog(store)
    }

        @Single
    fun providesUpdateCatalog(
        catalogRemoteRepository: CatalogRemoteRepository,
        installCatalog: InstallCatalog,
    ): UpdateCatalog {
        return UpdateCatalog(catalogRemoteRepository, installCatalog)
    }

        @Single
    fun providesInstallCatalog(
        catalogInstaller: CatalogInstaller,
    ): InstallCatalog {
        return InstallCatalog(catalogInstaller)
    }

        @Single
    fun providesUninstallCatalog(
        catalogInstaller: CatalogInstaller,
    ): UninstallCatalog {
        return UninstallCatalog(catalogInstaller)
    }

        @Single
    fun providesTogglePinnedCatalog(
        store: CatalogStore,
    ): TogglePinnedCatalog {
        return TogglePinnedCatalog(store)
    }

        @Single
    fun providesSyncRemoteCatalogs(
        catalogRemoteRepository: CatalogRemoteRepository,
        catalogPreferences: CatalogPreferences,
        httpClient: HttpClients,
    ): SyncRemoteCatalogs {
        return SyncRemoteCatalogs(
            catalogRemoteRepository,
            CatalogGithubApi(httpClient),
            catalogPreferences
        )
    }
}

