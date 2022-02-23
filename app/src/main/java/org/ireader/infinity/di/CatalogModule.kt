package org.ireader.infinity.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.core.okhttp.HttpClients
import org.ireader.core.prefs.PreferenceStore
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.repository.CatalogRemoteRepositoryImpl
import org.ireader.domain.extensions.AndroidCatalogLoader
import org.ireader.domain.extensions.CatalogLoader
import org.ireader.domain.extensions.cataloge_service.*
import org.ireader.domain.extensions.cataloge_service.impl.AndroidCatalogInstallationChanges
import org.ireader.domain.extensions.cataloge_service.impl.AndroidCatalogInstaller
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CatalogModule {
    @Provides
    @Singleton
    fun providesCataloguePreferences(store: PreferenceStore): CatalogPreferences {
        return CatalogPreferences(store)
    }


    @Provides
    @Singleton
    fun provideCatalogInstallationChanges(value: AndroidCatalogInstallationChanges): CatalogInstallationChanges {
        return value
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogInstallationChanges(context: Application): AndroidCatalogInstallationChanges {
        return AndroidCatalogInstallationChanges(context = context)
    }

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
    fun provideCatalogStore(
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
    fun provideCatalogLoader(
        context: Application,
        httpClients: HttpClients,
    ): CatalogLoader {
        return AndroidCatalogLoader(context, httpClients)
    }

    @Provides
    @Singleton
    fun provideAndroidCatalogLoader(
        context: Application,
        httpClients: HttpClients,
    ): AndroidCatalogLoader {
        return AndroidCatalogLoader(context, httpClients)
    }


    @Provides
    @Singleton
    fun provideCatalogInstaller(
        context: Application,
        httpClients: HttpClients,
        installationChanges: AndroidCatalogInstallationChanges,
    ): CatalogInstaller {
        return AndroidCatalogInstaller(context, httpClients, installationChanges)
    }


}