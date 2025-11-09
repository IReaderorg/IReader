package ireader.data.di

import ireader.data.book.BookRepositoryImpl
import ireader.data.catalog.CatalogRemoteRepositoryImpl
import ireader.data.category.BookCategoryRepositoryImpl
import ireader.data.category.CategoryRepositoryImpl
import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.downloads.DownloadRepositoryImpl
import ireader.data.history.HistoryRepositoryImpl
import ireader.data.repository.LibraryRepositoryImpl
import ireader.data.repository.ReaderThemeRepositoryImpl
import ireader.data.repository.ThemeRepositoryImpl
import ireader.data.repository.UpdatesRepositoryImpl
import ireader.data.repository.SourceCredentialsRepositoryImpl
import ireader.data.services.SourceHealthCheckerImpl
import ireader.data.statistics.ReadingStatisticsRepositoryImpl
import ireader.data.translation.GlossaryRepositoryImpl
import ireader.data.translation.TranslatedChapterRepositoryImpl
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.SourceCredentialsRepository
import ireader.domain.services.SourceHealthChecker
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.data.repository.UpdatesRepository
import org.koin.dsl.module


val repositoryInjectModule = module {
    single<DownloadRepository> { DownloadRepositoryImpl(get()) }
    single<UpdatesRepository> { UpdatesRepositoryImpl(get()) }
    single<LibraryRepository> { LibraryRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<CatalogRemoteRepository> { CatalogRemoteRepositoryImpl(get()) }
    single<ChapterRepository> { ChapterRepositoryImpl(get()) }
    single<BookRepository> { BookRepositoryImpl(get(), get<BookCategoryRepository>()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    single<BookCategoryRepository> { BookCategoryRepositoryImpl(get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<ReaderThemeRepository> { ReaderThemeRepositoryImpl(get()) }
    
    // Translation repositories
    single<TranslatedChapterRepository> { TranslatedChapterRepositoryImpl(get()) }
    single<GlossaryRepository> { GlossaryRepositoryImpl(get()) }
    
    // Source health checker
    single<SourceHealthChecker> { SourceHealthCheckerImpl(get()) }
    
    // Source credentials repository
    single<SourceCredentialsRepository> { SourceCredentialsRepositoryImpl(get()) }
    
    // Reading statistics repository
    single<ReadingStatisticsRepository> { ReadingStatisticsRepositoryImpl(get()) }
}