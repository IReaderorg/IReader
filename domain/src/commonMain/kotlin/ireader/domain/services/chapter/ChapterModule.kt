package ireader.domain.services.chapter

import ireader.domain.usecases.chapter.controller.GetChaptersUseCase
import ireader.domain.usecases.chapter.controller.GetChaptersUseCaseImpl
import ireader.domain.usecases.chapter.controller.LoadChapterContentUseCase
import ireader.domain.usecases.chapter.controller.LoadChapterContentUseCaseImpl
import ireader.domain.usecases.chapter.controller.NavigateChapterUseCase
import ireader.domain.usecases.chapter.controller.NavigateChapterUseCaseImpl
import ireader.domain.usecases.chapter.controller.UpdateProgressUseCase
import ireader.domain.usecases.chapter.controller.UpdateProgressUseCaseImpl
import org.koin.dsl.module

/**
 * Koin module for the Unified Chapter Controller and its dependencies.
 * 
 * This module provides:
 * - ChapterController as a factory (each screen gets its own instance)
 * - All use case implementations required by ChapterController
 * 
 * Note: ChapterNotifier is registered in chapterRepositoryModule (data layer)
 * since it's used by ChapterRepository. It's injected here via get().
 * 
 * Requirements: 5.5 - The Chapter_Controller SHALL be injectable via dependency injection framework
 */
val chapterModule = module {
    
    // ========== Use Case Implementations ==========
    
    /**
     * GetChaptersUseCase - Provides reactive subscriptions and one-shot queries for chapter data.
     * Dependencies: ChapterRepository
     */
    single<GetChaptersUseCase> {
        GetChaptersUseCaseImpl(
            chapterRepository = get()
        )
    }
    
    /**
     * LoadChapterContentUseCase - Handles local content retrieval and remote fetching.
     * Dependencies: ChapterRepository
     */
    single<LoadChapterContentUseCase> {
        LoadChapterContentUseCaseImpl(
            chapterRepository = get()
        )
    }
    
    /**
     * NavigateChapterUseCase - Provides methods to find next and previous chapters.
     * Dependencies: ChapterRepository
     */
    single<NavigateChapterUseCase> {
        NavigateChapterUseCaseImpl(
            chapterRepository = get()
        )
    }
    
    /**
     * UpdateProgressUseCase - Handles last read chapter tracking and paragraph position.
     * Dependencies: HistoryRepository, ChapterRepository, UiPreferences
     */
    single<UpdateProgressUseCase> {
        UpdateProgressUseCaseImpl(
            historyRepository = get(),
            chapterRepository = get(),
            uiPreferences = get()
        )
    }
    
    // ========== ChapterController ==========
    
    /**
     * ChapterController - The central coordinator for all chapter operations.
     * 
     * Registered as FACTORY so each screen (Reader, TTS) gets its own instance.
     * This prevents cross-screen sync bugs where TTS navigation affects Reader.
     * 
     * Sync between screens happens only on explicit events (e.g., TTS onPop).
     * 
     * Dependencies: All use cases above + BookRepository + ChapterNotifier
     * Note: ChapterNotifier is registered in chapterRepositoryModule (data layer)
     */
    factory {
        ChapterController(
            getChaptersUseCase = get(),
            loadChapterContentUseCase = get(),
            updateProgressUseCase = get(),
            navigateChapterUseCase = get(),
            bookRepository = get(),
            chapterNotifier = get()
        )
    }
}
