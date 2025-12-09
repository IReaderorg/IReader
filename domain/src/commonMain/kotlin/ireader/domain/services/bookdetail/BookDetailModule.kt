package ireader.domain.services.bookdetail

import org.koin.dsl.module

/**
 * Koin module for the BookDetail Controller and its dependencies.
 * 
 * This module provides:
 * - BookDetailController as a factory (each screen gets its own instance)
 * 
 * Requirements: 3.1, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */
val bookDetailModule = module {
    
    // ========== BookDetailController ==========
    
    /**
     * BookDetailController - The central coordinator for book detail screen operations.
     * 
     * This is registered as a FACTORY because each BookDetail screen instance
     * should have its own controller to manage its specific book's state.
     * 
     * Dependencies: BookRepository, ChapterRepository, HistoryUseCase
     */
    factory {
        BookDetailController(
            bookRepository = get(),
            chapterRepository = get(),
            historyUseCase = get()
        )
    }
}
