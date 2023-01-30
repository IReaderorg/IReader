package org.ireader.app.di

import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.data.book.BookRepositoryImpl
import ireader.data.catalog.CatalogRemoteRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.category.CategoryRepositoryImpl
import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.core.AndroidDatabaseHandler
import ireader.data.core.DatabaseHandler
import ireader.data.downloads.DownloadRepositoryImpl
import ireader.data.history.HistoryRepositoryImpl
import ireader.data.pagination.PaginationRepositoryImpl
import ireader.data.repository.LibraryRepositoryImpl
import ireader.data.repository.ReaderThemeRepositoryImpl
import ireader.data.repository.ThemeRepositoryImpl
import ireader.data.repository.UpdatesRepositoryImpl
import ireader.domain.data.repository.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.RepositoryInject")
class RepositoryInject {




    @Single
    fun provideDownloadRepository(
        handler: DatabaseHandler
    ): DownloadRepository {
        return DownloadRepositoryImpl(handler)
    }
    @Single
    fun providePaginationRepository(
        handler: AndroidDatabaseHandler
    ): PaginationRepository {
        return PaginationRepositoryImpl(handler)
    }

    @Single
    fun provideUpdatesRepository(
        handler: DatabaseHandler
    ): UpdatesRepository {
        return UpdatesRepositoryImpl(handler)
    }

    @Single
    fun provideLibraryRepository(
        handler: DatabaseHandler
    ): LibraryRepository {
        return LibraryRepositoryImpl(handler)
    }
    @Single
    fun provideCategoryRepository(
        handler: DatabaseHandler
    ): CategoryRepository {
        return CategoryRepositoryImpl(handler)
    }


    @Single
    fun provideCatalogRemoteRepository(        handler: DatabaseHandler): CatalogRemoteRepository {
        return CatalogRemoteRepositoryImpl(handler)
    }


    @Single
    fun providesLocalChapterRepository(        handler: DatabaseHandler): ChapterRepository {
        return ChapterRepositoryImpl(handler)
    }


    @Single
    fun providesLibraryRepository(
        handler: DatabaseHandler
    ): BookRepository {
        return BookRepositoryImpl(
            handler
        )
    }


        @Single
    fun providesHistoryRepository(
            handler: AndroidDatabaseHandler
    ): HistoryRepository {
        return HistoryRepositoryImpl(handler)
    }


        @Single
    fun providesBookCategoryRepository(
            handler: DatabaseHandler
    ): BookCategoryRepository {
        return BookCategoryRepositoryImpl(handler)
    }


        @Single
    fun providesThemeRepository(
            handler: DatabaseHandler
    ): ThemeRepository {
        return ThemeRepositoryImpl(handler)
    }


        @Single
    fun providesReaderThemeRepository(
            handler: DatabaseHandler
    ): ReaderThemeRepository {
        return ReaderThemeRepositoryImpl(handler)
    }
}
