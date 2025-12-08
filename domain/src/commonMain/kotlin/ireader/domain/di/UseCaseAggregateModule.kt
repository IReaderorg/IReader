package ireader.domain.di

import ireader.domain.services.platform.PlatformServices
import ireader.domain.usecases.explore.ExploreUseCases
import ireader.domain.usecases.library.LibraryUseCases
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
            importEpub = get()
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
            browseScreenPref = get()
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
}
