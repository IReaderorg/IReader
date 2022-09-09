package org.ireader.app.di

import ireader.common.data.repository.BookCategoryRepository
import ireader.common.data.repository.BookRepository
import ireader.common.data.repository.CategoryRepository
import ireader.common.data.repository.ChapterRepository
import ireader.common.data.repository.DownloadRepository
import ireader.common.data.repository.HistoryRepository
import ireader.common.data.repository.LibraryRepository
import ireader.common.data.repository.ReaderThemeRepository
import ireader.common.data.repository.ThemeRepository
import ireader.common.data.repository.UpdatesRepository
import ireader.core.catalogs.service.CatalogRemoteRepository
import ireader.data.local.dao.BookCategoryDao
import ireader.data.local.dao.CatalogDao
import ireader.data.local.dao.CategoryDao
import ireader.data.local.dao.ChapterDao
import ireader.data.local.dao.DownloadDao
import ireader.data.local.dao.HistoryDao
import ireader.data.local.dao.LibraryBookDao
import ireader.data.local.dao.LibraryDao
import ireader.data.local.dao.ReaderThemeDao
import ireader.data.local.dao.RemoteKeysDao
import ireader.data.local.dao.ThemeDao
import ireader.data.local.dao.UpdatesDao
import ireader.data.repository.BookCategoryRepositoryImpl
import ireader.data.repository.BookRepositoryImpl
import ireader.data.repository.CatalogRemoteRepositoryImpl
import ireader.data.repository.CategoryRepositoryImpl
import ireader.data.repository.ChapterRepositoryImpl
import ireader.data.repository.DownloadRepositoryImpl
import ireader.data.repository.HistoryRepositoryImpl
import ireader.data.repository.LibraryRepositoryImpl
import ireader.data.repository.ReaderThemeRepositoryImpl
import ireader.data.repository.ThemeRepositoryImpl
import ireader.data.repository.UpdatesRepositoryImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.RepositoryInject")
class RepositoryInject {




    @Single
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao)
    }

    @Single
    fun provideUpdatesRepository(
        updatesDao: UpdatesDao,
    ): UpdatesRepository {
        return UpdatesRepositoryImpl(updatesDao)
    }

    @Single
    fun provideLibraryRepository(
        dao: LibraryDao,
    ): LibraryRepository {
        return LibraryRepositoryImpl(dao)
    }
    @Single
    fun provideCategoryRepository(
        dao: CategoryDao,
    ): CategoryRepository {
        return CategoryRepositoryImpl(dao)
    }


    @Single
    fun provideCatalogRemoteRepository(catalogDao: CatalogDao): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(dao = catalogDao)
    }


    @Single
    fun providesLocalChapterRepository(ChapterDao: ChapterDao): ChapterRepository {
        return ChapterRepositoryImpl(ChapterDao)
    }


    @Single
    fun providesLibraryRepository(
        libraryBookDao: LibraryBookDao,
        remoteKeysDao: RemoteKeysDao,
    ): BookRepository {
        return BookRepositoryImpl(
            libraryBookDao,
            remoteKeysDao = remoteKeysDao
        )
    }


        @Single
    fun providesHistoryRepository(
        historyDao: HistoryDao,
    ): HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }


        @Single
    fun providesBookCategoryRepository(
        dao: BookCategoryDao,
    ): BookCategoryRepository {
        return BookCategoryRepositoryImpl(dao)
    }


        @Single
    fun providesThemeRepository(
        dao: ThemeDao,
    ): ThemeRepository {
        return ThemeRepositoryImpl(dao)
    }


        @Single
    fun providesReaderThemeRepository(
        dao: ReaderThemeDao,
    ): ReaderThemeRepository {
        return ReaderThemeRepositoryImpl(dao)
    }
}
