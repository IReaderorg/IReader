package org.ireader.app.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.core_api.http.HttpClients
import org.ireader.core_api.os.PackageInstaller
import org.ireader.core_catalogs.CatalogPreferences
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_catalogs.service.*
import org.ireader.data.catalog.AndroidCatalogInstallationChanges
import org.ireader.data.catalog.AndroidCatalogInstaller
import org.ireader.data.catalog.AndroidCatalogLoader
import org.ireader.data.catalog.CatalogGithubApi
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
    fun provideCatalogInstallationChanges(context: Application): CatalogInstallationChanges {
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
        return AndroidCatalogInstaller(context, httpClient, installationChanges,packageInstaller)
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
    fun providesCatalogStore(
        loader: CatalogLoader,
        catalogPreferences: CatalogPreferences,
        catalogRemoteRepository: CatalogRemoteRepository,
        installationChanges: AndroidCatalogInstallationChanges,
    ): CatalogStore {
        return CatalogStore(loader,
            catalogPreferences,
            catalogRemoteRepository,
            installationChanges)
    }



}