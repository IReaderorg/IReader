package ireader.domain.di

import ireader.domain.usecases.book.AddToLibrary
import ireader.domain.usecases.book.GetBook
import ireader.domain.usecases.book.GetCategories
import ireader.domain.usecases.book.GetChapters
import ireader.domain.usecases.book.RemoveFromLibrary
import ireader.domain.usecases.book.ToggleFavorite
import ireader.domain.usecases.book.UpdateBook
import ireader.domain.usecases.migration.BookMatcher
import ireader.domain.usecases.migration.MigrateBookUseCase
import ireader.domain.usecases.migration.BatchMigrationUseCase
import ireader.domain.usecases.migration.SearchMigrationTargetsUseCase
import ireader.domain.usecases.download.DownloadManagerUseCase
import ireader.domain.usecases.download.BatchDownloadUseCase
import ireader.domain.usecases.download.DownloadCacheUseCase
import ireader.domain.usecases.notification.NotificationManagerUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Dependency injection module for new use cases following Mihon's pattern.
 * Provides use case/interactor layer for clean business logic separation.
 */
val useCaseModule = module {
    
    // Core book use cases
    singleOf(::GetBook)
    singleOf(::GetChapters)
    singleOf(::GetCategories)
    singleOf(::UpdateBook)
    
    // Library management use cases
    singleOf(::AddToLibrary)
    singleOf(::RemoveFromLibrary)
    singleOf(::ToggleFavorite)
    
    // Migration use cases
    singleOf(::BookMatcher)
    singleOf(::MigrateBookUseCase)
    singleOf(::BatchMigrationUseCase)
    singleOf(::SearchMigrationTargetsUseCase)
    
    // Download use cases
    singleOf(::DownloadManagerUseCase)
    singleOf(::BatchDownloadUseCase)
    singleOf(::DownloadCacheUseCase)
    
    // Notification use cases
    singleOf(::NotificationManagerUseCase)
}