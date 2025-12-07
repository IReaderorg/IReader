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
     * Dependencies: ReaderPreferences
     */
    single {
        ReaderPreferencesController(
            readerPreferences = get()
        )
    }
}
