package ireader.presentation.core.di

// Enhanced Settings ViewModels
import ireader.presentation.core.ScreenContentViewModel
import ireader.presentation.core.theme.AppThemeViewModel
import ireader.presentation.di.screenModelModule
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.home.sources.settings.BrowseSettingsViewModel
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderStatisticsViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderTTSViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderTranslationViewModel
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettingViewModel
import ireader.presentation.ui.settings.appearance.AppearanceViewModel
import ireader.presentation.ui.settings.appearance.SettingsAppearanceViewModel
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import ireader.presentation.ui.settings.backups.CloudBackupViewModel
import ireader.presentation.ui.settings.category.CategoryScreenViewModel
import ireader.presentation.ui.settings.data.SettingsDataViewModel
import ireader.presentation.ui.settings.downloader.DownloadStateImpl
import ireader.presentation.ui.settings.downloader.DownloaderViewModel
import ireader.presentation.ui.settings.downloads.SettingsDownloadViewModel
import ireader.presentation.ui.settings.font_screens.FontScreenStateImpl
import ireader.presentation.ui.settings.font_screens.FontScreenViewModel
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import ireader.presentation.ui.settings.library.SettingsLibraryViewModel
import ireader.presentation.ui.settings.notifications.SettingsNotificationViewModel
import ireader.presentation.ui.settings.reader.ReaderSettingScreenViewModel
import ireader.presentation.ui.settings.reader.SettingsReaderViewModel
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import ireader.presentation.ui.settings.security.SecuritySettingsViewModel
import ireader.presentation.ui.settings.security.SettingsSecurityViewModel
import ireader.presentation.ui.settings.statistics.StatisticsViewModel
import ireader.presentation.ui.settings.tracking.SettingsTrackingViewModel
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel
import org.koin.dsl.module

val PresentationModules = module {


    // State objects removed - ViewModels now use Mihon-style MutableStateFlow internally
    factory   { BackupScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory   { CloudBackupViewModel(get(), get()) }

    factory   { AdvanceSettingViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory <DownloadStateImpl> { DownloadStateImpl() }
    factory <FontScreenStateImpl> { FontScreenStateImpl() }

    factory  { ScreenContentViewModel(get(), get(), get()) }
    single<AppThemeViewModel> { AppThemeViewModel(get(), get(), get(), get()) }

    // ExploreViewModel - Simplified with ExploreUseCases aggregate (Requirements: 4.2, 4.4)
    // Reduced from 10 parameters to 6 using ExploreUseCases aggregate
    factory<ExploreViewModel> { (params: ExploreViewModel.Param) -> 
        ExploreViewModel(
            exploreUseCases = get(),           // Aggregate: groups 5 use cases (remote, insert, findDuplicate, openLocalFolder, browseScreenPref)
            catalogStore = get(),
            param = params,
            libraryPreferences = get(),
            syncUseCases = getOrNull(),
            filterStateManager = getOrNull()
        )
    }
    // Changed from single to factory - these ViewModels are heavy and should be created on-demand
    // Updated to Mihon-style StateFlow pattern (no separate state impl needed)
    factory  { HistoryViewModel(get(), get(), get()) }
    // LibraryViewModel - Simplified with aggregates (Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.4)
    // Reduced from 26 parameters to 9 using LibraryUseCases and PlatformServices aggregates
    factory  { 
        LibraryViewModel(
            libraryUseCases = get(),           // Aggregate: groups 11 use cases
            platformServices = get(),          // Aggregate: groups 5 platform services
            libraryPreferences = get(),
            libraryScreenPrefUseCases = get(),
            serviceUseCases = get(),
            syncUseCases = getOrNull(),
            downloadService = get(),
            localizeHelper = get(),
            libraryController = get()          // SSOT for library state
        )
    }
    // ExtensionViewModel - Simplified with ExtensionUseCases aggregate (Requirements: 1.3, 1.4, 1.5)
    // Reduced from 17 parameters to 5 using ExtensionUseCases aggregate
    factory { 
        ExtensionViewModel(
            extensionUseCases = get(),         // Aggregate: groups 13 use cases
            uiPreferences = get(),
            startExtensionManagerService = get(),
            catalogStore = get(),
            browsePreferences = get(),
            deleteUserSource = get()
        )
    }
    factory { (param: GlobalSearchViewModel.Param) ->
        GlobalSearchViewModel(
            catalogStore = get(),
            insertUseCases = get(),
            getInstalledCatalog = get(),
            remoteUseCases = get(),
            param = param
        )
    }
    
    // Browse Settings ViewModel
    factory { BrowseSettingsViewModel(get(), get()) }
    
    // Migration ViewModel
    factory { ireader.presentation.ui.home.sources.migration.MigrationViewModel(get(), get(), get()) }


    // Changed from single to factory - created on-demand when user navigates to updates
    // Updated to Mihon-style StateFlow pattern (no separate state impl needed)
    factory  { UpdatesViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // BookDetailViewModel - Simplified with BookDetailUseCases aggregate (Requirements: 1.1, 1.4, 1.5)
    // Reduced from 28 parameters to 21 using BookDetailUseCases aggregate
    // Now includes BookDetailController for SSOT pattern (Requirements: 3.1, 3.3, 3.4, 3.5)
    factory<BookDetailViewModel> { (params: BookDetailViewModel.Param) -> 
        BookDetailViewModel(
            bookDetailUseCases = get(),        // Aggregate: groups 12 use cases
            getLocalCatalog = get(),
            applicationScope = get(),
            createEpub = get(),
            readerPreferences = get(),
            param = params,
            checkSourceAvailabilityUseCase = get(),
            migrateToSourceUseCase = get(),
            catalogStore = get(),
            syncUseCases = getOrNull(),
            downloadService = get(),
            bookUseCases = get(),
            chapterUseCases = get(),
            clipboardService = get(),
            shareService = get(),
            platformHelper = get(),
            bookPrefetchService = getOrNull(),
            translationService = getOrNull(),
            chapterController = get(),
            bookController = get(),
            bookDetailController = get(),      // BookDetailController for SSOT pattern
            localizeHelper = get()             // For UiText localization
        )
    }
    // Changed from single to factory - settings screen is not always needed
    factory  { 
        MainSettingScreenViewModel(
            uiPreferences = get(),
            supabasePreferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    factory  { AppearanceViewModel(get(), get()) }

    factory  { CategoryScreenViewModel(get(), get(), get(), get(), get()) }
    factory  { DownloaderViewModel(get(), get(), get(), get(),get()) }
    factory  { FontScreenViewModel(get(), get(), get(), get(), get()) }
    factory  { GeneralSettingScreenViewModel(get(), get(), get(), get(), get(),get()) }
    factory  { TranslationSettingsViewModel(get(), get(), getOrNull(), get()) }
    factory  { ReaderSettingScreenViewModel(get(), get(), get()) }
    factory  { SourceRepositoryViewModel(get(), get()) }
    factory  { SecuritySettingsViewModel(get(), get()) }
    factory  { StatisticsViewModel(get()) }
    factory  { ireader.presentation.ui.settings.donation.DonationViewModel(get(), get()) }
    factory  { AITTSSettingsViewModel(get(), get()) }
    factory  { ireader.presentation.ui.settings.donation.DonationTriggerViewModel(get()) }
    
    // Enhanced Settings ViewModels following Mihon's patterns
    factory  { SettingsAppearanceViewModel(get()) }
    factory  { SettingsReaderViewModel(get()) }
    factory  { SettingsLibraryViewModel(get()) }
    factory  { SettingsDownloadViewModel(get()) }
    factory  { SettingsSecurityViewModel(get()) }
    factory  { SettingsNotificationViewModel(get()) }
    factory  { SettingsTrackingViewModel(get()) }
    factory  { SettingsDataViewModel(get()) }
    
    // Authentication ViewModels
    factory  { ireader.presentation.ui.settings.auth.AuthViewModel(get()) }
    factory  { ireader.presentation.ui.settings.auth.ProfileViewModel(get(), getOrNull()) }
    
    // Sync ViewModels
    factory  { ireader.presentation.ui.settings.sync.SupabaseConfigViewModel(get(), get(), getOrNull(), getOrNull()) }
    
    // Badge ViewModels
    factory { ireader.presentation.ui.settings.badges.store.BadgeStoreViewModel(get(), get()) }
    factory { ireader.presentation.ui.settings.badges.nft.NFTBadgeViewModel(get(), get(), get(), get(), get()) }
    factory { ireader.presentation.ui.settings.badges.manage.BadgeManagementViewModel(get(), get(), get()) }
    
    // Admin ViewModels
    factory { 
        ireader.presentation.ui.settings.admin.AdminBadgeVerificationViewModel(
            getPendingPaymentProofsUseCase = get(),
            verifyPaymentProofUseCase = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    
    // Admin User Panel ViewModel
    factory {
        ireader.presentation.ui.settings.admin.AdminUserPanelViewModel(
            adminUserUseCases = get()
        )
    }
    
    // Unified Image Generator
    single {
        ireader.data.characterart.UnifiedImageGenerator(
            httpClient = get<ireader.core.http.HttpClients>().default
        )
    }
    
    // Character Art Gallery ViewModel
    factory {
        ireader.presentation.ui.characterart.CharacterArtViewModel(
            repository = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            },
            geminiImageGenerator = getOrNull(),
            unifiedImageGenerator = get(),
            readerPreferences = get()
        )
    }
    
    // Voice Selection ViewModel
    factory { ireader.presentation.ui.settings.viewmodels.VoiceSelectionViewModel(get(), get(), get(), get()) }
    
    // Plugin ViewModels
    factory { (pluginId: String) -> 
        ireader.presentation.ui.plugins.details.PluginDetailsViewModel(
            pluginId = pluginId,
            pluginManager = get(),
            monetizationService = get(),
            getCurrentUserId = { 
                "default_user"
            },
            pluginRepository = get(),
            remoteRepository = get(),
            uiPreferences = get(),
            repositoryRepository = get(),
            indexFetcher = get(),
            downloadService = get()
        )
    }
    factory {
        ireader.presentation.ui.plugins.marketplace.PluginMarketplaceViewModel(
            pluginManager = get(),
            repositoryRepository = get(),
            indexFetcher = get()
        )
    }
    factory {
        ireader.presentation.ui.plugins.required.RequiredPluginViewModel(
            pluginManager = get(),
            repositoryRepository = get(),
            indexFetcher = get(),
            catalogStore = get(),
            downloadService = get()
        )
    }


    // Reader sub-viewmodels
    // NOTE: ReaderTranslationViewModel, ReaderTTSViewModel, ReaderStatisticsViewModel are registered
    // in screenModelModule (included at the end of this module) with proper named parameters.
    // Only ReaderSettingsViewModel is registered here.
    // ReaderSettingsViewModel with ReaderPreferencesController for SSOT pattern (Requirements: 4.1, 4.2)
    factory { ireader.presentation.ui.reader.viewmodel.ReaderSettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // ReaderScreenViewModel - Simplified with ReaderUseCasesAggregate (Requirements: 1.2, 1.4, 1.5)
    // Reduced from 40+ parameters to 25 using ReaderUseCasesAggregate
    factory<ReaderScreenViewModel> { (params: ReaderScreenViewModel.Param) -> 
        ReaderScreenViewModel(
            readerUseCasesAggregate = get(),   // Aggregate: groups 18 use cases
            getLocalCatalog = get(),
            readerUseCases = get(),
            readerPreferences = get(),
            androidUiPreferences = get(),
            platformUiPreferences = get(),
            uiPreferences = get(),
            screenAlwaysOnUseCase = get(),
            webViewManger = get(),
            readerThemeRepository = get(),
            translationEnginesManager = get(),
            fontManagementUseCase = get(),
            fontUseCase = get(),
            chapterHealthChecker = get(),
            chapterHealthRepository = get(),
            autoRepairChapterUseCase = get(),
            params = params,
            systemInteractionService = get(),
            // ChapterController - Reader's own instance (factory creates new instance)
            chapterController = get(),
            // ReaderPreferencesController - single source of truth for reader preferences
            preferencesController = get(),
            // TTSController - singleton for syncing chapter when returning from TTS screen
            ttsController = get(),
            settingsViewModel = get(),
            translationViewModel = get(),
            ttsViewModel = get(),
            statisticsViewModel = get(),
        )
    }

    // User Sources ViewModels
    factory { 
        ireader.presentation.ui.sourcecreator.UserSourcesListViewModel(
            getUserSources = get(),
            deleteUserSource = get(),
            toggleUserSourceEnabled = get(),
            importExportUserSources = get(),
            catalogStore = get()
        )
    }
    factory {
        ireader.presentation.ui.sourcecreator.SourceCreatorViewModel(
            getUserSource = get(),
            saveUserSource = get(),
            validateUserSource = get(),
            importExportUserSources = get(),
            catalogStore = get()
        )
    }
    factory {
        ireader.presentation.ui.sourcecreator.legado.LegadoSourceImportViewModel(
            httpClient = get(),
            saveUserSource = get(),
            catalogStore = get()
        )
    }

    // New StateScreenModel implementations following Mihon's pattern
    includes(screenModelModule)

}
