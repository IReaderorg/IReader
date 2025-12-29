package ireader.domain.services.library

import org.koin.dsl.module

/**
 * DI module for Library Controller and related dependencies.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
val libraryModule = module {
    // NOTE: LibraryChangeNotifier is now registered in repositoryInjectModule
    // because it must be available BEFORE repositories are created.
    // Repositories use getOrNull<LibraryChangeNotifier>() to notify changes.
    // If LibraryChangeNotifier is registered after repositories, they get null.
    
    /**
     * LibraryController as singleton - ensures single source of truth
     * across all screens that need library state.
     * 
     * LAZY LOADING: createdAtStart=false ensures this is not created during app startup.
     * The controller is created on-demand when first accessed (e.g., when user opens Library).
     * 
     * NOTE: LibraryController no longer loads books directly to prevent OOM with large libraries.
     * Book loading is handled by LibraryViewModel via pagination.
     */
    single(createdAtStart = false) { 
        LibraryController(
            categoryRepository = get()
        )
    }
}
