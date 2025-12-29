package ireader.domain.di

import ireader.domain.usecases.book.*
import ireader.domain.usecases.category.*
import ireader.domain.usecases.chapter.*
import ireader.domain.usecases.download.*
import ireader.domain.usecases.history.*
import ireader.domain.usecases.sync.*
import org.koin.dsl.module

/**
 * Koin module for repository-based use cases
 * These use cases provide a clean abstraction over repository operations
 */
val repositoryUseCaseModule = module {
    
    // Book Use Cases
    single { GetBookByIdUseCase(get()) }
    single { GetBooksInLibraryUseCase(get()) }
    single { UpdateBookUseCase(get()) }
    single { DeleteBookUseCase(get(), get(), get()) }
    single { UpdateBookPinStatusUseCase(get()) }
    single { UpdateBookArchiveStatusUseCase(get()) }
    single { SearchBooksUseCase(get()) }
    single { UpdateChapterPageUseCase(get()) }
    
    // Book Use Cases Aggregate
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
            updateChapterPage = get()
        )
    }
    
    // Chapter Use Cases
    single { GetChaptersByBookIdUseCase(get()) }
    single { GetChapterByIdUseCase(get()) }
    single { GetLastReadChapterUseCase(get()) }
    single { UpdateChapterReadStatusUseCase(get()) }
    single { UpdateChapterBookmarkStatusUseCase(get()) }
    single { DeleteChaptersUseCase(get()) }
    
    // Chapter Use Cases Aggregate
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
    
    // Category Use Cases
    single { GetCategoriesUseCase(get()) }
    single { GetCategoryByIdUseCase(get()) }
    single { CreateCategoryUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get(), get()) }
    single { AssignBookToCategoryUseCase(get()) }
    single { RemoveBookFromCategoryUseCase(get()) }
    single { AutoCategorizeBookUseCase(get(), get()) }
    single { ManageCategoryAutoRulesUseCase(get(), get()) }
    
    // Category Use Cases Aggregate
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
    
    // Download Use Cases
    single { DownloadChapterUseCase(get()) }
    single { DownloadChaptersUseCase(get()) }
    single { CancelDownloadUseCase(get()) }
    single { PauseDownloadUseCase(get()) }
    single { ResumeDownloadUseCase(get()) }
    single { GetDownloadStatusUseCase(get()) }
    single { ireader.domain.usecases.download.insert.InsertDownload(get()) }
    single { ireader.domain.usecases.download.insert.InsertDownloads(get()) }
    single { ireader.domain.usecases.download.delete.DeleteSavedDownload(get()) }
    
    // Download Use Cases Aggregate
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
    
    // History Use Cases
    single { GetHistoryUseCase(get()) }
    single { UpdateHistoryUseCase(get()) }
    single { DeleteHistoryUseCase(get()) }
    single { ClearHistoryUseCase(get()) }
    
    // History Use Cases Aggregate
    single {
        HistoryUseCases(
            getHistory = get(),
            getLastReadNovel = get(),
            updateHistory = get(),
            deleteHistory = get(),
            clearHistory = get()
        )
    }
    
    // Sync Use Cases (new additions)
    single { SyncLibraryUseCase(getOrNull()) }
    single { CheckSyncAvailabilityUseCase(getOrNull()) }
    
    // Note: SyncUseCases aggregate is already defined in DomainModules
    // The new use cases can be accessed individually or added to the existing aggregate
}
