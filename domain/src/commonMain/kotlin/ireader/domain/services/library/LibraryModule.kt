package ireader.domain.services.library

import org.koin.dsl.module

/**
 * DI module for Library Controller and related dependencies.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
val libraryModule = module {
    /**
     * LibraryChangeNotifier as singleton - lightweight change notification system.
     * 
     * This solves the pagination vs reactivity problem:
     * - Pagination loads data on-demand (efficient for large libraries)
     * - But we still need to know WHEN data changed to reload
     * - LibraryChangeNotifier provides that signal without loading all data
     * 
     * Repositories call notifyChange() when books are modified.
     * ViewModel observes changes and reloads pagination when needed.
     */
    single { LibraryChangeNotifier() }
    
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
