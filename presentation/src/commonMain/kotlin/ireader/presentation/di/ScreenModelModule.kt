package ireader.presentation.di

import ireader.presentation.ui.settings.statistics.StatsScreenModel
import ireader.presentation.ui.reader.viewmodel.*
import ireader.presentation.ui.reader.viewmodel.subviewmodels.ReaderChapterViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Dependency injection module for new StateScreenModel implementations.
 * Provides screen models following Mihon's StateScreenModel pattern.
 */
val screenModelModule = module {
    
    // Statistics screen model
    factoryOf(::StatsScreenModel)
    
    // Main settings screen - singleton to prevent recreation
    single { 
        ireader.presentation.ui.settings.MainSettingScreenViewModel(
            uiPreferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    
    // Leaderboard screen model
    factory {
        ireader.presentation.ui.leaderboard.LeaderboardViewModel(
            leaderboardUseCases = get()
        )
    }
    
    // Browse settings view model
    factory {
        ireader.presentation.ui.home.sources.settings.BrowseSettingsViewModel(
            browsePreferences = get(),
            getCatalogsByType = get(),
            get()
        )
    }
    
    // Community screens
    factory {
        ireader.presentation.ui.community.PopularBooksViewModel(
            popularBooksRepository = get(),
            bookRepository = get()
        )
    }
    
    factory {
        ireader.presentation.ui.community.AllReviewsViewModel(
            allReviewsRepository = get()
        )
    }
    
    // ==================== NEW: Reader Sub-ViewModels ====================
    
    /**
     * Reader chapter management ViewModel
     * Handles chapter navigation, loading, and preloading
     */
    factory {
        ReaderChapterViewModel(
            getChapterUseCase = get(),
            remoteUseCases = get(),
            historyUseCase = get(),
            preloadChapterUseCase = get(), bookMarkChapterUseCase = get(), insertUseCases = get()
        )
    }
    
    /**
     * Reader settings ViewModel
     * Handles brightness, fonts, colors, and layout preferences
     */
    factory {
        ReaderSettingsViewModel(
            readerPreferences = get(),
            androidUiPreferences = get(),
            platformUiPreferences = get(),
            readerUseCases = get(),
            systemInteractionService = get(),
            fontUseCase = get()
        )
    }
    
    /**
     * Reader translation ViewModel
     * Handles translation and glossary management
     */
    factory {
        ReaderTranslationViewModel(
            translateChapterWithStorageUseCase = get(),
            translateParagraphUseCase = get(),
            getTranslatedChapterUseCase = get(),
            getGlossaryByBookIdUseCase = get(),
            saveGlossaryEntryUseCase = get(),
            deleteGlossaryEntryUseCase = get(),
            exportGlossaryUseCase = get(),
            importGlossaryUseCase = get(),
            translationEnginesManager = get(),
            readerPreferences = get()
        )
    }
    
    /**
     * Reader TTS ViewModel
     * Handles text-to-speech playback control
     */
    factory {
        ReaderTTSViewModel(
            ttsService = get(),
            readerPreferences = get()
        )
    }
    
    /**
     * Reader statistics ViewModel
     * Handles reading time tracking and statistics
     */
    factory {
        ReaderStatisticsViewModel(
            trackReadingProgressUseCase = get(),
            readerPreferences = get()
        )
    }
    
    // Note: ReaderScreenViewModel is NOT registered in Koin to avoid circular dependencies
    // It should be created manually where needed with all its dependencies
}