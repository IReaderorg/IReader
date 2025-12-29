package ireader.domain.di

import ireader.domain.usecases.book.*
import ireader.domain.usecases.chapter.*
import ireader.domain.usecases.category.*
import ireader.domain.usecases.download.*
import ireader.domain.usecases.download.delete.*
import ireader.domain.usecases.download.get.*
import ireader.domain.usecases.download.insert.*
import ireader.domain.usecases.download.update.*
import ireader.domain.usecases.history.*
import ireader.domain.usecases.migration.BookMatcher
import ireader.domain.usecases.migration.MigrateBookUseCase
import ireader.domain.usecases.migration.BatchMigrationUseCase
import ireader.domain.usecases.migration.SearchMigrationTargetsUseCase
import ireader.domain.usecases.notification.NotificationManagerUseCase
import ireader.domain.usecases.prefetch.BookPrefetchService
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
    
    // Individual book use cases for BookUseCases aggregate
    singleOf(::GetBookByIdUseCase)
    singleOf(::GetBooksInLibraryUseCase)
    singleOf(::UpdateBookUseCase)
    singleOf(::DeleteBookUseCase)
    singleOf(::UpdateBookPinStatusUseCase)
    singleOf(::UpdateBookArchiveStatusUseCase)
    singleOf(::SearchBooksUseCase)
    singleOf(::UpdateChapterPageUseCase)
    
    // BookUseCases aggregate
    single {
        BookUseCases(
            getBookById = get(),
            getBooksInLibrary = get(),
            updateBook = get(),
            deleteBook = get(),
            toggleFavorite = get(),
            updatePinStatus = get(),
            updateArchiveStatus = get(),
            searchBooks = get(),
            addToLibrary = get(),
            removeFromLibrary = get(),
            updateChapterPage = get(),
        )
    }
    
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
    
    // Individual chapter use cases for ChapterUseCases aggregate
    singleOf(::GetChaptersByBookIdUseCase)
    singleOf(::GetChapterByIdUseCase)
    singleOf(::GetLastReadChapterUseCase)
    singleOf(::UpdateChapterReadStatusUseCase)
    singleOf(::UpdateChapterBookmarkStatusUseCase)
    singleOf(::DeleteChaptersUseCase)
    
    // ChapterUseCases aggregate
    single {
        ChapterUseCases(
            getChaptersByBookId = get(),
            getChapterById = get(),
            getLastReadChapter = get(),
            updateReadStatus = get(),
            updateBookmarkStatus = get(),
            deleteChapters = get()
        )
    }
    
    // Individual category use cases for CategoryUseCases aggregate
    singleOf(::GetCategoriesUseCase)
    singleOf(::GetCategoryByIdUseCase)
    singleOf(::CreateCategoryUseCase)
    singleOf(::UpdateCategoryUseCase)
    singleOf(::DeleteCategoryUseCase)
    singleOf(::ReorderCategory)
    singleOf(::AssignBookToCategoryUseCase)
    singleOf(::RemoveBookFromCategoryUseCase)
    singleOf(::AutoCategorizeBookUseCase)
    singleOf(::ManageCategoryAutoRulesUseCase)
    
    // CategoryUseCases aggregate
    single {
        CategoryUseCases(
            getCategories = get(),
            getCategoryById = get(),
            createCategory = get(),
            updateCategory = get(),
            deleteCategory = get(),
            reorderCategories = get(),
            assignBookToCategory = get(),
            removeBookFromCategory = get(),
            autoCategorizeBook = get(),
            manageAutoRules = get(),
        )
    }
    
    // Individual download use cases for DownloadUseCases aggregate
    singleOf(::DownloadChapterUseCase)
    singleOf(::DownloadChaptersUseCase)
    singleOf(::CancelDownloadUseCase)
    singleOf(::PauseDownloadUseCase)
    singleOf(::ResumeDownloadUseCase)
    singleOf(::GetDownloadStatusUseCase)
    singleOf(::SubscribeDownloadsUseCase)
    singleOf(::InsertDownload)
    singleOf(::InsertDownloads)
    singleOf(::DeleteSavedDownload)
    singleOf(::DeleteAllSavedDownload)
    singleOf(::DeleteSavedDownloads)
    singleOf(::UpdateDownloadPriority)
    
    // DownloadUseCases aggregate
    single {
        DownloadUseCases(
            downloadChapter = get(),
            downloadChapters = get(),
            downloadUnreadChapters = get(),
            cancelDownload = get(),
            pauseDownload = get(),
            resumeDownload = get(),
            getDownloadStatus = get(),
            subscribeDownloadsUseCase = get(),
            insertDownload = get(),
            insertDownloads = get(),
            deleteSavedDownload = get(),
            deleteAllSavedDownload = get(),
            deleteSavedDownloads = get(),
            updateDownloadPriority = get()
        )
    }
    
    // Individual history use cases for HistoryUseCases aggregate
    singleOf(::GetHistoryUseCase)
    singleOf(::UpdateHistoryUseCase)
    singleOf(::DeleteHistoryUseCase)
    singleOf(::ClearHistoryUseCase)
    
    // HistoryUseCases aggregate
    single {
        HistoryUseCases(
            getHistory = get(),
            getLastReadNovel = get(),
            updateHistory = get(),
            deleteHistory = get(),
            clearHistory = get()
        )
    }
    
    // Leaderboard use cases
    single {
        ireader.domain.usecases.leaderboard.LeaderboardUseCases(
            leaderboardRepository = get(),
            statisticsRepository = get(),
            remoteRepository = get(),
            uiPreferences = get()
        )
    }
    
    // Donation Leaderboard use cases
    single {
        ireader.domain.usecases.leaderboard.DonationLeaderboardUseCases(
            donationLeaderboardRepository = get(),
            remoteRepository = get(),
            uiPreferences = get()
        )
    }
    
    // Book Prefetch Service - for faster book detail loading
    singleOf(::BookPrefetchService)
}