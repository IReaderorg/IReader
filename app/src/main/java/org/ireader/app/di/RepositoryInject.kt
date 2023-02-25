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


import org.koin.dsl.module


val repositoryInjectModule = module {
    single<DownloadRepository>(qualifier=null) { DownloadRepositoryImpl(get()) }
    single<PaginationRepository>(qualifier=null) { PaginationRepositoryImpl(get()) }
    single<UpdatesRepository>(qualifier=null) { UpdatesRepositoryImpl(get()) }
    single<LibraryRepository>(qualifier=null) { LibraryRepositoryImpl(get()) }
    single<CategoryRepository>(qualifier=null) { CategoryRepositoryImpl(get()) }
    single<CatalogRemoteRepository>(qualifier=null) { CatalogRemoteRepositoryImpl(get()) }
    single<ChapterRepository>(qualifier=null) { ChapterRepositoryImpl(get()) }
    single<BookRepository>(qualifier=null) { BookRepositoryImpl(get()) }
    single<HistoryRepository>(qualifier=null) { HistoryRepositoryImpl(get()) }
    single<BookCategoryRepository>(qualifier=null) { BookCategoryRepositoryImpl(get()) }
    single<ThemeRepository>(qualifier=null) { ThemeRepositoryImpl(get()) }
    single<ReaderThemeRepository>(qualifier=null) { ReaderThemeRepositoryImpl(get()) }
}