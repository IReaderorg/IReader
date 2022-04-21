package org.ireader.infinity.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.dao.HistoryDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.chapterDao
import org.ireader.data.repository.HistoryRepositoryImpl
import org.ireader.data.repository.LocalBookRepositoryImpl
import org.ireader.data.repository.LocalChapterRepositoryImpl
import org.ireader.domain.repository.HistoryRepository
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {


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
