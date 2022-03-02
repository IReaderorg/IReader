package org.ireader.infinity.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import okhttp3.OkHttpClient
import org.ireader.data.catalog.AndroidCatalogInstallationChanges
import org.ireader.data.catalog.AndroidCatalogInstaller
import org.ireader.data.catalog.AndroidCatalogLoader
import org.ireader.data.catalog.CatalogGithubApi
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.repository.CatalogRemoteRepositoryImpl
import org.ireader.domain.catalog.CatalogInterceptors
import org.ireader.domain.catalog.interactor.*
import org.ireader.domain.catalog.service.*
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.presentation.feature_sources.presentation.extension.CatalogsStateImpl
import tachiyomi.core.http.HttpClients
import tachiyomi.core.prefs.PreferenceStore
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CatalogModule {

    @Provides
    @Singleton
    fun provideCatalogRemoteRepository(catalogDao: CatalogDao): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(dao = catalogDao)
    }

    @Provides
    @Singleton
    fun provideCatalogDao(db: AppDatabase): CatalogDao {
        return db.catalogDao
    }

    @Provides
    @Singleton
    fun provideCatalogLoader(
        context: Application,
        httpClients: HttpClients,
        client: OkHttpClient,
    ): CatalogLoader {
        return AndroidCatalogLoader(context = context, httpClients, client)
    }

    @Provides
    @Singleton
    fun provideCatalogsState(): CatalogsStateImpl {
        return CatalogsStateImpl()
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogInstallationChanges(context: Application): AndroidCatalogInstallationChanges {
        return AndroidCatalogInstallationChanges(context)
    }

    @Provides
    @Singleton
    fun provideCatalogInstallationChanges(context: Application): CatalogInstallationChanges {
        return AndroidCatalogInstallationChanges(context)
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogInstaller(
        context: Application,
        httpClient: HttpClient,
        installationChanges: AndroidCatalogInstallationChanges,
    ): CatalogInstaller {
        return AndroidCatalogInstaller(context, httpClient, installationChanges)
    }

    @Provides
    @Singleton
    fun provideCatalogRemoteApi(
        httpClient: HttpClient,
    ): CatalogRemoteApi {
        return CatalogGithubApi(httpClient)
    }

    @Provides
    @Singleton
    fun provideCatalogInterceptor(
        catalogStore: CatalogStore,
        localCatalogs: GetLocalCatalogs,
        remoteCatalogs: GetRemoteCatalogs,
        localBookRepository: LocalBookRepository,
        catalogRemoteRepository: CatalogRemoteRepository,
        catalogInstaller: CatalogInstaller,
        catalogPreferences: CatalogPreferences,
        catalogRemoteApi: CatalogRemoteApi,
        installCatalog: InstallCatalog,
    ): CatalogInterceptors {
        return CatalogInterceptors(
            getInstalledCatalog = GetInstalledCatalog(catalogStore),
            getCatalogsByType = GetCatalogsByType(localCatalogs, remoteCatalogs),
            getLocalCatalog = GetLocalCatalog(catalogStore),
            getLocalCatalogs = GetLocalCatalogs(catalogStore, localBookRepository),
            getRemoteCatalogs = GetRemoteCatalogs(catalogRemoteRepository),
            installCatalog = InstallCatalog(catalogInstaller),
            syncRemoteCatalogs = SyncRemoteCatalogs(catalogRemoteRepository,
                catalogRemoteApi,
                catalogPreferences),
            togglePinnedCatalog = TogglePinnedCatalog(catalogStore),
            uninstallCatalog = UninstallCatalog(catalogInstaller),
            updateCatalog = UpdateCatalog(catalogRemoteRepository, installCatalog = installCatalog),
        )
    }

    @Provides
    @Singleton
    fun provideCatalogPreferences(store: PreferenceStore): CatalogPreferences {
        return CatalogPreferences(store)
    }

    @Provides
    @Singleton
    fun providesGetRemoteCatalogs(catalogRemoteRepository: CatalogRemoteRepository): GetRemoteCatalogs {
        return GetRemoteCatalogs(catalogRemoteRepository)
    }

    @Provides
    @Singleton
    fun providesCatalogStore(
        loader: CatalogLoader,
        catalogPreferences: CatalogPreferences,
        catalogRemoteRepository: CatalogRemoteRepository,
        installationChanges: CatalogInstallationChanges,
    ): CatalogStore {
        return CatalogStore(loader,
            catalogPreferences,
            catalogRemoteRepository,
            installationChanges)
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
    fun providesHttpClient(): HttpClient {
        return HttpClient()
    }


}