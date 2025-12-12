package ireader.domain.services.preferences

import org.koin.dsl.module

/**
 * Koin module for the ReaderPreferencesController and its dependencies.
 * 
 * This module provides:
 * - ReaderPreferencesController as a singleton (single source of truth for reader preferences)
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
val preferencesModule = module {
    
    // ========== ReaderPreferencesController ==========
    
    /**
     * ReaderPreferencesController - The central coordinator for all reader preference operations.
     * 
     * This is registered as a SINGLETON to ensure all screens share the same
     * instance and receive consistent state updates.
     * 
     * LAZY LOADING: createdAtStart=false ensures this is not created during app startup.
     * Preferences are loaded lazily when first accessed via ensurePreferencesLoaded().
     * 
     * Dependencies: ReaderPreferences
     */
    single(createdAtStart = false) {
        ReaderPreferencesController(
            readerPreferences = get()
        )
    }
}
