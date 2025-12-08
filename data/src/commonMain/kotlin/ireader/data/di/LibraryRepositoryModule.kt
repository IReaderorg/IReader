package ireader.data.di

import ireader.data.category.CategoryRepositoryImpl
import ireader.data.downloads.DownloadRepositoryImpl
import ireader.data.history.HistoryRepositoryImpl
import ireader.data.repository.LibraryRepositoryImpl
import ireader.data.repository.UpdatesRepositoryImpl
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.data.repository.UpdatesRepository
import org.koin.dsl.module

/**
 * DI module for library-related repositories.
 * Contains LibraryRepository, CategoryRepository, DownloadRepository, UpdatesRepository, and HistoryRepository.
 */
val libraryRepositoryModule = module {
    single<LibraryRepository> { LibraryRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<DownloadRepository> { DownloadRepositoryImpl(get()) }
    single<UpdatesRepository> { UpdatesRepositoryImpl(get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get(), getOrNull()) }
}
