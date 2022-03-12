package org.ireader.infinity.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.data.catalog.AndroidCatalogInstallationChanges
import org.ireader.data.catalog.AndroidCatalogInstaller
import org.ireader.data.catalog.AndroidCatalogLoader
import org.ireader.data.catalog.CatalogGithubApi
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.repository.CatalogRemoteRepositoryImpl
import org.ireader.domain.catalog.service.*
import org.ireader.presentation.feature_sources.presentation.extension.CatalogsStateImpl
import tachiyomi.core.http.HttpClients
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
    ): CatalogLoader {
        return AndroidCatalogLoader(context = context, httpClients)
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
        httpClient: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
    ): CatalogInstaller {
        return AndroidCatalogInstaller(context, httpClient, installationChanges)
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