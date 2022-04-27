package org.ireader.library.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.dao.*
import org.ireader.data.repository.*
import org.ireader.core.catalog.service.CatalogRemoteRepository
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RepositoryInject {

    @Provides
    @Singleton
    fun provideRemoteKeyRepository(
        remoteKeysDao: RemoteKeysDao,
    ): org.ireader.common_data.repository.RemoteKeyRepository {
        return RemoteKeyRepositoryImpl(
            dao = remoteKeysDao,
        )
    }

    @Singleton
    @Provides
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
    ): org.ireader.common_data.repository.DownloadRepository {
        return DownloadRepositoryImpl(downloadDao)
    }

    @Singleton
    @Provides
    fun provideUpdatesRepository(
        updatesDao: UpdatesDao,
    ): org.ireader.common_data.repository.UpdatesRepository {
        return UpdatesRepositoryImpl(updatesDao)
    }

    @Provides
    @Singleton
    fun provideCatalogRemoteRepository(catalogDao: CatalogDao): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(dao = catalogDao)
    }

    @Provides
    @Singleton
    fun providesLocalChapterRepository(chapterDao: chapterDao): org.ireader.common_data.repository.LocalChapterRepository {
        return LocalChapterRepositoryImpl(chapterDao)
    }


    @Provides
    @Singleton
    fun providesLibraryRepository(
        libraryBookDao: LibraryBookDao,
        remoteKeysDao: RemoteKeysDao,
    ): org.ireader.common_data.repository.LocalBookRepository {
        return LocalBookRepositoryImpl(libraryBookDao,
            remoteKeysDao = remoteKeysDao)
    }

    @Provides
    @Singleton
    fun providesHistoryRepository(
        historyDao: HistoryDao,
    ): org.ireader.common_data.repository.HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }

}