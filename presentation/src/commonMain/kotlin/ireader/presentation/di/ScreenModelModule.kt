package ireader.presentation.di

import ireader.data.characterart.GeminiImageGenerator
import ireader.presentation.ui.characterart.CharacterArtViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderSettingsViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderStatisticsViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderTTSViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderTranslationViewModel
import ireader.presentation.ui.settings.viewmodels.GradioTTSSettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Dependency injection module for new StateScreenModel implementations.
 * Provides screen models following Mihon's StateScreenModel pattern.
 */
val screenModelModule = module {

    // Main settings screen - changed to factory to reduce startup memory
    factory { 
        ireader.presentation.ui.settings.MainSettingScreenViewModel(
            uiPreferences = get(),
            supabasePreferences = get(),
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
    
    // Donation Leaderboard screen model
    factory {
        ireader.presentation.ui.leaderboard.DonationLeaderboardViewModel(
            donationLeaderboardUseCases = get()
        )
    }
    
    // Browse settings view model
    factory {
        ireader.presentation.ui.home.sources.settings.BrowseSettingsViewModel(
            browsePreferences = get(),
            getCatalogsByType = get(),
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
    
    // Glossary ViewModel - Community feature for managing book glossaries
    // Supports both local book glossaries and global glossaries with cloud sync
    factory {
        ireader.presentation.ui.community.GlossaryViewModel(
            getGlossaryByBookIdUseCase = get(),
            saveGlossaryEntryUseCase = get(),
            deleteGlossaryEntryUseCase = get(),
            exportGlossaryUseCase = get(),
            importGlossaryUseCase = get(),
            searchGlossaryUseCase = get(),
            localGetBookUseCases = get(),
            globalGlossaryUseCases = getOrNull() // Optional - may not be available if repository not configured
        )
    }
    
    // ==================== NEW: Reader Sub-ViewModels ====================
    

    /**
     * Reader settings ViewModel
     * Handles brightness, fonts, colors, and layout preferences
     * Now integrates with ReaderPreferencesController for SSOT pattern (Requirements: 4.1, 4.2)
     */
    factory {
        ReaderSettingsViewModel(
            readerPreferences = get(),
            androidUiPreferences = get(),
            platformUiPreferences = get(),
            readerUseCases = get(),
            systemInteractionService = get(),
            fontUseCase = get(),
            preferencesController = get()
        )
    }
    
    /**
     * Reader translation ViewModel
     * Handles translation and glossary management
     * IMPORTANT: translationService must be injected for notifications to work!
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
            readerPreferences = get(),
            translationService = get() // Required for notifications!
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
     * Also syncs with Reading Buddy for unified progress tracking
     */
    factory {
        ReaderStatisticsViewModel(
            trackReadingProgressUseCase = get(),
            readerPreferences = get(),
            readingBuddyUseCases = get()
        )
    }
    // Gradio TTS Settings ViewModel
    factory {
        GradioTTSSettingsViewModel(
            gradioTTSManager = get(),
            appPreferences = get()
        )
    }
    
    // AI TTS Settings ViewModel
    factory {
        ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel(
            aiTTSManager = get(),
            appPreferences = get()
        )
    }
    
    // Reading Buddy ViewModel
    factory {
        ireader.presentation.ui.readingbuddy.ReadingBuddyViewModel(
            readingBuddyUseCases = get(),
            preferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    
    // Quote Creation ViewModel - for creating quotes from reader
    factory { (params: ireader.domain.models.quote.QuoteCreationParams) ->
        ireader.presentation.ui.quote.QuoteCreationViewModel(
            localQuoteUseCases = get(),
            discordQuoteRepository = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            },
            params = params
        )
    }
    
    // My Quotes ViewModel - for viewing personal quote collection
    factory {
        ireader.presentation.ui.quote.MyQuotesViewModel(
            localQuoteUseCases = get(),
            discordQuoteRepository = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            },
            uiPreferences = get()
        )
    }
    
    // Quote Story Editor ViewModel - Instagram story-style quote editor
    factory { params ->
        ireader.presentation.ui.quote.QuoteStoryEditorViewModel(
            localQuoteUseCases = get(),
            discordQuoteRepository = get(),
            bookId = params.get<Long>(),
            initialBookTitle = params.get<String>(),
            initialChapterTitle = params.get<String>(),
            initialAuthor = params.getOrNull<String>(),
            chapterNumber = params.getOrNull<Int>(),
            currentChapterId = params.getOrNull<Long>(),
            prevChapterId = params.getOrNull<Long>(),
            nextChapterId = params.getOrNull<Long>()
        )
    }
    
    // Reading Hub ViewModel - unified statistics, buddy, and quotes
    factory {
        ireader.presentation.ui.readinghub.ReadingHubViewModel(
            statisticsRepository = get(),
            getReadingStatistics = get(),
            readingBuddyUseCases = get(),
            preferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    
    // Note: ReaderScreenViewModel is NOT registered in Koin to avoid circular dependencies
    // It should be created manually where needed with all its dependencies
    
    // ==================== Character Art Gallery ====================
    
    // GeminiImageGenerator - singleton for image generation
    single {
        GeminiImageGenerator(
            httpClient = get<ireader.core.http.HttpClients>().default
        )
    }
    
    // Unified Image Generator - supports multiple providers
    single {
        ireader.data.characterart.UnifiedImageGenerator(
            httpClient = get<ireader.core.http.HttpClients>().default
        )
    }
    
    // Character Art ViewModel
    factory {
        CharacterArtViewModel(
            repository = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            },
            geminiImageGenerator = get(),
            unifiedImageGenerator = get(),
            readerPreferences = get()
        )
    }
    
    // ==================== Feature Store ====================
    
    // Feature Store ViewModel - Plugin monetization marketplace
    factory {
        ireader.presentation.ui.featurestore.FeatureStoreViewModel(
            pluginManager = get(),
            repositoryRepository = get(),
            indexFetcher = get(),
            downloadService = get(),
            uiPreferences = get(),
            platformCapabilities = get()
        )
    }
    
    // ==================== Plugin Repository ====================
    
    // Plugin Repository ViewModel - Manage plugin sources
    factory {
        ireader.presentation.ui.pluginrepository.PluginRepositoryViewModel(
            repository = get(),
            indexFetcher = get()
        )
    }
    
    // ==================== Developer Portal ====================
    
    // Developer Portal ViewModel - For plugin developers
    factory {
        ireader.presentation.ui.developerportal.DeveloperPortalViewModel(
            repository = get(),
            getCurrentUser = get()
        )
    }
    
    // ==================== Plugin Management ====================
    
    // Plugin Management ViewModel - Manage installed plugins
    factory {
        ireader.presentation.ui.plugins.management.PluginManagementViewModel(
            pluginManager = get(),
            uiPreferences = get()
        )
    }
    
    // ==================== Text Replacement ====================
    
    // Text Replacement ViewModel - Manage text replacement rules
    factory {
        ireader.presentation.ui.settings.textreplacement.TextReplacementViewModel(
            textReplacementUseCase = get()
        )
    }
}
