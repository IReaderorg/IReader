package ireader.domain.services.book

import org.koin.dsl.module

/**
 * Koin module for the Book Controller and its dependencies.
 * 
 * This module provides:
 * - BookController as a singleton (single source of truth for book state)
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4 - Controllers registered as singletons in DI
 */
val bookModule = module {
    
    // ========== BookController ==========
    
    /**
     * BookController - The central coordinator for all book-level operations.
     * 
     * This is registered as a SINGLETON to ensure all screens share the same
     * instance and receive consistent state updates.
     * 
     * Dependencies: BookRepository, CategoryRepository, ChapterRepository, HistoryUseCase
     */
    single {
        BookController(
            bookRepository = get(),
            categoryRepository = get(),
            chapterRepository = get(),
            historyUseCase = get()
        )
    }
}
