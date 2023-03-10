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
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.*
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val repositoryInjectModule = DI.Module("repositoryModule") {
    bindSingleton<DownloadRepository> { DownloadRepositoryImpl(instance()) }
    bindSingleton<UpdatesRepository> { UpdatesRepositoryImpl(instance()) }
    bindSingleton<LibraryRepository> { LibraryRepositoryImpl(instance()) }
    bindSingleton<CategoryRepository> { CategoryRepositoryImpl(instance()) }
    bindSingleton<CatalogRemoteRepository> { CatalogRemoteRepositoryImpl(instance()) }
    bindSingleton<ChapterRepository> { ChapterRepositoryImpl(instance()) }
    bindSingleton<BookRepository> { BookRepositoryImpl(instance()) }
    bindSingleton<HistoryRepository> { HistoryRepositoryImpl(instance()) }
    bindSingleton<BookCategoryRepository> { BookCategoryRepositoryImpl(instance()) }
    bindSingleton<ThemeRepository> { ThemeRepositoryImpl(instance()) }
    bindSingleton<ReaderThemeRepository> { ReaderThemeRepositoryImpl(instance()) }
}