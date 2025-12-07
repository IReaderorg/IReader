package ireader.domain.services.library

import org.koin.dsl.module

/**
 * DI module for Library Controller and related dependencies.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
val libraryModule = module {
    /**
     * LibraryController as singleton - ensures single source of truth
     * across all screens that need library state.
     */
    single { 
        LibraryController(
            libraryRepository = get(),
            categoryRepository = get()
        )
    }
}
