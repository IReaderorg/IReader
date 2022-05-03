package org.ireader.app.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.common_data.repository.LocalBookRepository
import org.ireader.core_api.http.BrowseEngine
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.os.PackageInstaller
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.core_catalogs.CatalogPreferences
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_catalogs.interactor.GetCatalogsByType
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_catalogs.interactor.GetLocalCatalogs
import org.ireader.core_catalogs.interactor.GetRemoteCatalogs
import org.ireader.core_catalogs.interactor.InstallCatalog
import org.ireader.core_catalogs.interactor.SyncRemoteCatalogs
import org.ireader.core_catalogs.interactor.TogglePinnedCatalog
import org.ireader.core_catalogs.interactor.UninstallCatalog
import org.ireader.core_catalogs.interactor.UpdateCatalog
import org.ireader.core_catalogs.service.CatalogInstaller
import org.ireader.core_catalogs.service.CatalogLoader
import org.ireader.core_catalogs.service.CatalogRemoteApi
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import org.ireader.data.catalog.AndroidCatalogInstallationChanges
import org.ireader.data.catalog.AndroidCatalogInstaller
import org.ireader.data.catalog.AndroidCatalogLoader
import org.ireader.data.catalog.CatalogGithubApi
import org.ireader.sources.extension.CatalogsStateImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CatalogModule {

    @Provides
    @Singleton
    fun provideCatalogLoader(
        context: Application,
        httpClients: HttpClients,
    ): CatalogLoader {
        return AndroidCatalogLoader(context = context, httpClients)
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogInstallationChanges(context: Application): AndroidCatalogInstallationChanges {
        return AndroidCatalogInstallationChanges(context)
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller
    ): CatalogInstaller {
        return AndroidCatalogInstaller(context, httpClient, installationChanges, packageInstaller)
    }

    @Provides
    @Singleton
    fun provideCatalogRemoteApi(
        httpClient: HttpClients,
    ): CatalogRemoteApi {
        return CatalogGithubApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideCatalogPreferences(
        store: PreferenceStore
    ): CatalogPreferences {
        return CatalogPreferences(store)
    }

    @Provides
    @Singleton
    fun providesCatalogStore(
        loader: CatalogLoader,
        catalogPreferences: CatalogPreferences,
        catalogRemoteRepository: CatalogRemoteRepository,
        installationChanges: AndroidCatalogInstallationChanges,
    ): CatalogStore {
        return CatalogStore(
            loader,
            catalogPreferences,
            catalogRemoteRepository,
            installationChanges
        )
    }

    @Provides
    @Singleton
    fun providesPackageInstaller(
        app: Application
    ): PackageInstaller {
        return PackageInstaller(
            app
        )
    }

    @Provides
    @Singleton
    fun providesHttpClients(
        context: Application,
        browseEngine: BrowseEngine
    ): HttpClients {
        return HttpClients(
            context,
            browseEngine
        )
    }

    @Provides
    @Singleton
    fun providesAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
        packageInstaller: PackageInstaller
    ): AndroidCatalogInstaller {
        return AndroidCatalogInstaller(
            context,
            httpClient,
            installationChanges,
            packageInstaller
        )
    }
    @Provides
    @Singleton
    fun providesCatalogsStateImpl(
    ): CatalogsStateImpl {
        return CatalogsStateImpl()
    }
    @Provides
    @Singleton
    fun providesGetCatalogsByType(
        localCatalogs: GetLocalCatalogs,
        remoteCatalogs: GetRemoteCatalogs,
    ): GetCatalogsByType {
        return GetCatalogsByType(localCatalogs, remoteCatalogs)
    }
    @Provides
    @Singleton
    fun providesGetRemoteCatalogs(
        catalogRemoteRepository: CatalogRemoteRepository,
    ): GetRemoteCatalogs {
        return GetRemoteCatalogs(catalogRemoteRepository)
    }
    @Provides
    @Singleton
    fun providesGetLocalCatalogs(
        catalogStore: CatalogStore,
        libraryRepository: LocalBookRepository,
    ): GetLocalCatalogs {
        return GetLocalCatalogs(catalogStore, libraryRepository)
    }
    @Provides
    @Singleton
    fun providesGetLocalCatalog(
        store: CatalogStore
    ): GetLocalCatalog {
        return GetLocalCatalog(store)
    }
    @Provides
    @Singleton
    fun providesUpdateCatalog(
        catalogRemoteRepository: CatalogRemoteRepository,
        installCatalog: InstallCatalog,
    ): UpdateCatalog {
        return UpdateCatalog(catalogRemoteRepository, installCatalog)
    }
    @Provides
    @Singleton
    fun providesInstallCatalog(
        catalogInstaller: CatalogInstaller,
    ): InstallCatalog {
        return InstallCatalog(catalogInstaller)
    }
    @Provides
    @Singleton
    fun providesUninstallCatalog(
        catalogInstaller: CatalogInstaller,
    ): UninstallCatalog {
        return UninstallCatalog(catalogInstaller)
    }

    @Provides
    @Singleton
    fun providesTogglePinnedCatalog(
        store: CatalogStore,
    ): TogglePinnedCatalog {
        return TogglePinnedCatalog(store)
    }
    @Provides
    @Singleton
    fun providesSyncRemoteCatalogs(
        catalogRemoteRepository: CatalogRemoteRepository,
        catalogRemoteApi: CatalogRemoteApi,
        catalogPreferences: CatalogPreferences,
    ): SyncRemoteCatalogs {
        return SyncRemoteCatalogs(catalogRemoteRepository, catalogRemoteApi, catalogPreferences)
    }

}
