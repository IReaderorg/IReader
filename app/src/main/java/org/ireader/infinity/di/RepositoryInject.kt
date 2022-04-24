package org.ireader.infinity.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.dao.*
import org.ireader.data.repository.*
import org.ireader.domain.catalog.service.CatalogRemoteRepository
import org.ireader.domain.repository.*
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RepositoryInject {

    @Provides
    @Singleton
    fun provideRemoteKeyRepository(
        remoteKeysDao: RemoteKeysDao,
    ): RemoteKeyRepository {
        return RemoteKeyRepositoryImpl(
            dao = remoteKeysDao,
        )
    }

    @Singleton
    @Provides
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao)
    }

    @Singleton
    @Provides
    fun provideUpdatesRepository(
        updatesDao: UpdatesDao,
    ): UpdatesRepository {
        return UpdatesRepositoryImpl(updatesDao)
    }

    @Provides
    @Singleton
    fun provideCatalogRemoteRepository(catalogDao: CatalogDao): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(dao = catalogDao)
    }

    @Provides
    @Singleton
    fun providesLocalChapterRepository(chapterDao: chapterDao): LocalChapterRepository {
        return LocalChapterRepositoryImpl(chapterDao)
    }


    @Provides
    @Singleton
    fun providesLibraryRepository(
        libraryBookDao: LibraryBookDao,
        remoteKeysDao: RemoteKeysDao,
    ): LocalBookRepository {
        return LocalBookRepositoryImpl(libraryBookDao,
            remoteKeysDao = remoteKeysDao)
    }

    @Provides
    @Singleton
    fun providesHistoryRepository(
        historyDao: HistoryDao,
    ): HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }

}