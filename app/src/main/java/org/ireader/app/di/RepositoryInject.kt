package org.ireader.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_data.repository.LibraryRepository
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import org.ireader.data.local.dao.BookCategoryDao
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.local.dao.CategoryDao
import org.ireader.data.local.dao.ChapterDao
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.HistoryDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.ThemeDao
import org.ireader.data.local.dao.UpdatesDao
import org.ireader.data.repository.BookCategoryRepositoryImpl
import org.ireader.data.repository.BookRepositoryImpl
import org.ireader.data.repository.CatalogRemoteRepositoryImpl
import org.ireader.data.repository.CategoryRepositoryImpl
import org.ireader.data.repository.ChapterRepositoryImpl
import org.ireader.data.repository.DownloadRepositoryImpl
import org.ireader.data.repository.HistoryRepositoryImpl
import org.ireader.data.repository.LibraryRepositoryImpl
import org.ireader.data.repository.RemoteKeyRepositoryImpl
import org.ireader.data.repository.ThemeRepositoryImpl
import org.ireader.data.repository.UpdatesRepositoryImpl
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

    @Singleton
    @Provides
    fun provideLibraryRepository(
        dao:LibraryDao,
    ): LibraryRepository {
        return LibraryRepositoryImpl(dao)
    }
    @Singleton
    @Provides
    fun provideCategoryRepository(
        dao:CategoryDao,
    ): CategoryRepository {
        return CategoryRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideCatalogRemoteRepository(catalogDao: CatalogDao): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(dao = catalogDao)
    }

    @Provides
    @Singleton
    fun providesLocalChapterRepository(ChapterDao: ChapterDao): org.ireader.common_data.repository.ChapterRepository {
        return ChapterRepositoryImpl(ChapterDao)
    }

    @Provides
    @Singleton
    fun providesLibraryRepository(
        libraryBookDao: LibraryBookDao,
        remoteKeysDao: RemoteKeysDao,
    ): org.ireader.common_data.repository.BookRepository {
        return BookRepositoryImpl(
            libraryBookDao,
            remoteKeysDao = remoteKeysDao
        )
    }

    @Provides
    @Singleton
    fun providesHistoryRepository(
        historyDao: HistoryDao,
    ): org.ireader.common_data.repository.HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }

    @Provides
    @Singleton
    fun providesBookCategoryRepository(
        dao: BookCategoryDao,
    ): BookCategoryRepository {
        return BookCategoryRepositoryImpl(dao)
    }


    @Provides
    @Singleton
    fun providesThemeRepository(
        dao: ThemeDao,
    ): ThemeRepository {
        return ThemeRepositoryImpl(dao)
    }

}
