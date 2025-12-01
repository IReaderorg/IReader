package ireader.presentation.core.di

// Enhanced Settings ViewModels
import ireader.presentation.core.ScreenContentViewModel
import ireader.presentation.core.theme.AppThemeViewModel
import ireader.presentation.di.screenModelModule
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import ireader.presentation.ui.home.history.viewmodel.HistoryStateImpl
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.ui.home.library.viewmodel.LibraryStateImpl
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.home.sources.browse.BrowseSettingsViewModel
import ireader.presentation.ui.home.sources.extension.CatalogsStateImpl
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchStateImpl
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.updates.viewmodel.UpdateStateImpl
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderScreenPreferencesStateImpl
import ireader.presentation.ui.reader.viewmodel.ReaderScreenStateImpl
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


    // Changed state objects from single to factory - each screen should have its own state
    factory<HistoryStateImpl> { HistoryStateImpl() }
    factory<LibraryStateImpl> { LibraryStateImpl() }
    factory<CatalogsStateImpl> { CatalogsStateImpl() }
    factory<UpdateStateImpl> { UpdateStateImpl() }
    factory   { BackupScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory   { CloudBackupViewModel(get(), get()) }

    factory <GlobalSearchStateImpl> { GlobalSearchStateImpl() }
    factory   { AdvanceSettingViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),) }
    factory <DownloadStateImpl> { DownloadStateImpl() }
    factory <FontScreenStateImpl> { FontScreenStateImpl() }

    factory  { ScreenContentViewModel(get()) }
    single<AppThemeViewModel> { AppThemeViewModel(get(), get(), get(), get()) }

    factory<ExploreViewModel> { (params: ExploreViewModel.Param) -> 
        ExploreViewModel(
            remoteUseCases = get(),
            catalogStore = get(),
            browseScreenPrefUseCase = get(),
            insertUseCases = get(),
            param = params,
            findDuplicateBook = get(),
            libraryPreferences = get(),
            openLocalFolder = get(),
            syncUseCases = getOrNull(),
            filterStateManager = getOrNull()
        )
    }
    // Changed from single to factory - these ViewModels are heavy and should be created on-demand
    factory  { HistoryViewModel(get(), get(), get(),get(),) }
    factory  { LibraryViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), getOrNull(), get(), get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory  { ExtensionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), getOrNull(), getOrNull(), getOrNull(), get()) }
    factory<GlobalSearchViewModel> { GlobalSearchViewModel(get(), get(), get(), get(), get(), get()) }
    
    // Browse Settings ViewModel
    factory { BrowseSettingsViewModel(get()) }
    
    // Migration ViewModel
    factory { ireader.presentation.ui.home.sources.migration.MigrationViewModel(get(), get(), get()) }


    // Changed from single to factory - created on-demand when user navigates to updates
    factory  { UpdatesViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

    factory<BookDetailViewModel>  { (params: BookDetailViewModel.Param) -> BookDetailViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),params,get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),) }
    // Changed from single to factory - settings screen is not always needed
    factory  { 
        MainSettingScreenViewModel(
            uiPreferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    factory  { AppearanceViewModel(get(), get()) }

    factory  { CategoryScreenViewModel(get(), get(), get(), get(), get()) }
    factory  { DownloaderViewModel(get(), get(), get(), get(),get()) }
    factory  { FontScreenViewModel(get(), get(), get(), get()) }
    factory  { GeneralSettingScreenViewModel(get(), get(), get(), get(), get()) }
    factory  { TranslationSettingsViewModel(get(), get()) }
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
            uiPreferences = get()
        )
    }
    factory { 
        ireader.presentation.ui.plugins.marketplace.PluginMarketplaceViewModel(
            pluginManager = get()
        )
    }


    factory <ReaderScreenStateImpl> { ReaderScreenStateImpl() }
    factory <ReaderScreenPreferencesStateImpl> { ReaderScreenPreferencesStateImpl() }

    // Reader sub-viewmodels
    factory { ReaderTranslationViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ReaderTTSViewModel(get(), get()) }
    factory { ReaderStatisticsViewModel(get(), get()) }

    factory< ReaderScreenViewModel>  { (params: ReaderScreenViewModel.Param) -> ReaderScreenViewModel(
        get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),
        get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),
        // Translation use cases
        get(),get(),get(),get(),get(),get(),get(),get(),
        // Statistics use case
        get(),
        // Report use case
        get(),
        // Font management use case
        get(),
        // Font use case
        get(),
        // Chapter health
        get(),get(),get(),
        // Params
        params,
        // Platform services and sub-viewmodels
        get(),get(),get(),get(),get(),
    ) }

    // New StateScreenModel implementations following Mihon's pattern
    includes(screenModelModule)

}