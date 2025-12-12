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
     * 
     * LAZY LOADING: createdAtStart=false ensures this is not created during app startup.
     * The controller is created on-demand when first accessed (e.g., when user opens Library).
     */
    single(createdAtStart = false) { 
        LibraryController(
            libraryRepository = get(),
            categoryRepository = get()
        )
    }
}
