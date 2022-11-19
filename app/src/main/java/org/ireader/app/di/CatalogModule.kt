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
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("org.ireader.app.di.CatalogModule")
class CatalogModule {

    @Single
    fun provideAndroidCatalogInstallationChanges(context: Application): AndroidCatalogInstallationChanges {
        return AndroidCatalogInstallationChanges(context)
    }

    @Single
    fun provideAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller,
        simpleStorage: GetSimpleStorage,
        uiPreferences: UiPreferences
    ): CatalogInstaller {
        return AndroidCatalogInstaller(
            context,
            httpClient,
            installationChanges,
            packageInstaller,
            simpleStorage,
            uiPreferences
        )
    }

    @Single
    fun provideInAppCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
        getSimpleStorage: GetSimpleStorage,
        uiPreferences: UiPreferences
    ): AndroidLocalInstaller {
        return AndroidLocalInstaller(context, httpClient, installationChanges, getSimpleStorage,uiPreferences)
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
        installationChanges: AndroidCatalogInstallationChanges,
        context: Application,
        httpClients: HttpClients,
        uiPreferences: UiPreferences,
        getSimpleStorage: GetSimpleStorage
    ): CatalogStore {
        return CatalogStore(
            AndroidCatalogLoader(context, httpClients, uiPreferences, getSimpleStorage),
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
        installationChanges: AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller,
        simpleStorage: GetSimpleStorage,
        uiPreferences: UiPreferences
    ): AndroidCatalogInstaller {
        return AndroidCatalogInstaller(
            context,
            httpClient,
            installationChanges,
            packageInstaller,
            simpleStorage,
            uiPreferences
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
        androidCatalogInstaller: AndroidCatalogInstaller,
        androidLocalInstaller: AndroidLocalInstaller,
        uiPreferences: UiPreferences
    ): InstallCatalog {
        return InstallCatalogImpl(androidCatalogInstaller, androidLocalInstaller, uiPreferences)
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
        getDefaultRepo: GetDefaultRepo
    ): SyncRemoteCatalogs {
        return SyncRemoteCatalogs(
            catalogRemoteRepository,
            CatalogGithubApi(httpClient, getDefaultRepo),
            catalogPreferences
        )
    }
}

