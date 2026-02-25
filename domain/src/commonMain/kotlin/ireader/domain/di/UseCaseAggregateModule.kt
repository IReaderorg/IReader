package ireader.domain.di

import ireader.domain.services.platform.PlatformServices
import ireader.domain.usecases.book.BookDetailUseCases
import ireader.domain.usecases.explore.ExploreUseCases
import ireader.domain.usecases.extension.ExtensionUseCases
import ireader.domain.usecases.library.LibraryUseCases
import ireader.domain.usecases.reader.ReaderUseCasesAggregate
import ireader.domain.usecases.translation.TranslationUseCases
import org.koin.dsl.module

/**
 * DI module for use case aggregates.
 * These aggregates reduce ViewModel constructor complexity.
 * 
 * Requirements: 4.5 - Use case aggregates registered in DI
 */
val useCaseAggregateModule = module {
    
    // Library aggregate - used by LibraryViewModel
    // Reduces constructor params from 26 to ~8
    // Requirements: 4.1
    factory {
        LibraryUseCases(
            getBooks = get(),
            getChapters = get(),
            insert = get(),
            delete = get(),
            categories = get(),
            markAsRead = get(),
            downloadUnread = get(),
            archiveBook = get(),
            getLastRead = get(),
            getLibraryCategory = get(),
            importEpub = get(),
            importPdf = get()
        )
    }
    
    // Explore aggregate - used by ExploreViewModel
    // Reduces constructor params from 10 to 5
    // Requirements: 4.2
    factory {
        ExploreUseCases(
            remote = get(),
            insert = get(),
            findDuplicate = get(),
            openLocalFolder = get(),
            browseScreenPref = get(),
            exploreBook = get()
        )
    }
    
    // Translation aggregate - used by ReaderTranslationViewModel
    // Groups glossary and chapter translation operations
    // Requirements: 4.3
    factory {
        TranslationUseCases(
            translateChapter = get(),
            translateParagraph = get(),
            getTranslated = get(),
            getGlossary = get(),
            saveGlossary = get(),
            deleteGlossary = get(),
            exportGlossary = get(),
            importGlossary = get()
        )
    }
    
    // Platform services aggregate - used by multiple ViewModels
    // Groups clipboard, share, file system, haptic, and network services
    // Requirements: 4.3
    factory {
        PlatformServices(
            clipboard = get(),
            share = get(),
            fileSystem = get(),
            haptic = get(),
            network = get()
        )
    }
    
    // BookDetail aggregate - used by BookDetailViewModel
    // Reduces constructor params from 28+ to ~12
    // Requirements: 1.1, 1.4, 1.5
    factory {
        BookDetailUseCases(
            getBookUseCases = get(),
            getChapterUseCase = get(),
            insertUseCases = get(),
            deleteUseCase = get(),
            remoteUseCases = get(),
            historyUseCase = get(),
            getLastReadChapter = get(),
            markChapterAsRead = get(),
            downloadChapters = get(),
            exportNovelAsEpub = get(),
            exportBookAsEpub = get(),
            getBookReviews = get(),
            getTranslatedChaptersByBookId = get()
        )
    }
    
    // Reader aggregate - used by ReaderScreenViewModel
    // Reduces constructor params from 40+ to ~15
    // Requirements: 1.2, 1.4, 1.5
    factory {
        ReaderUseCasesAggregate(
            getBookUseCases = get(),
            getChapterUseCase = get(),
            insertUseCases = get(),
            remoteUseCases = get(),
            historyUseCase = get(),
            preloadChapter = get(),
            bookmarkChapter = get(),
            reportBrokenChapter = get(),
            trackReadingProgress = get(),
            translateChapterWithStorage = get(),
            translateParagraph = get(),
            getTranslatedChapter = get(),
            getGlossaryByBookId = get(),
            saveGlossaryEntry = get(),
            deleteGlossaryEntry = get(),
            exportGlossary = get(),
            importGlossary = get(),
            contentFilter = get(),
            chapterRepository = get()
        )
    }
    
    // Content filter use case - used by Reader and TTS screens
    factory {
        ireader.domain.usecases.reader.ContentFilterUseCase(
            readerPreferences = get(),
            repository = getOrNull() // Optional - uses preferences fallback if not available
        )
    }
    
    // Text replacement use case - used by Reader and TTS screens
    factory {
        ireader.domain.usecases.reader.TextReplacementUseCase(
            readerPreferences = get(),
            repository = getOrNull() // Optional
        )
    }
    
    // Extension aggregate - used by ExtensionViewModel
    // Reduces constructor params from 17 to ~10
    // Requirements: 1.3, 1.4, 1.5
    factory {
        ExtensionUseCases(
            getCatalogsByType = get(),
            updateCatalog = get(),
            installCatalog = get(),
            uninstallCatalog = get(),
            togglePinnedCatalog = get(),
            syncRemoteCatalogs = get(),
            sourceHealthChecker = get(),
            sourceCredentialsRepository = get(),
            extensionWatcherService = get(),
            catalogSourceRepository = get(),
            extensionManager = getOrNull(),
            extensionSecurityManager = getOrNull(),
            extensionRepositoryManager = getOrNull()
        )
    }
}
