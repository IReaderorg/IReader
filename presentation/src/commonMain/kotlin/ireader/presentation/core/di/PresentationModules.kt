package ireader.presentation.core.di


import ireader.presentation.core.ScreenContentViewModel
import ireader.presentation.core.theme.AppThemeViewModel
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.book.viewmodel.ChapterStateImpl
import ireader.presentation.ui.book.viewmodel.DetailStateImpl
import ireader.presentation.ui.home.explore.viewmodel.BooksState
import ireader.presentation.ui.home.explore.viewmodel.ExploreStateImpl
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import ireader.presentation.ui.home.history.viewmodel.HistoryStateImpl
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.ui.home.library.viewmodel.LibraryStateImpl
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.home.sources.extension.CatalogsStateImpl
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchStateImpl
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.updates.viewmodel.UpdateStateImpl
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderScreenPreferencesStateImpl
import ireader.presentation.ui.reader.viewmodel.ReaderScreenStateImpl
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettingViewModel
import ireader.presentation.ui.settings.appearance.AppearanceViewModel
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import ireader.presentation.ui.settings.category.CategoryScreenViewModel
import ireader.presentation.ui.settings.downloader.DownloadStateImpl
import ireader.presentation.ui.settings.downloader.DownloaderViewModel
import ireader.presentation.ui.settings.font_screens.FontScreenStateImpl
import ireader.presentation.ui.settings.font_screens.FontScreenViewModel
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import ireader.presentation.ui.settings.reader.ReaderSettingScreenViewModel
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import ireader.presentation.ui.settings.statistics.StatisticsViewModel
import org.koin.dsl.module

val PresentationModules = module {

    single { BooksState() }
    single<HistoryStateImpl> { HistoryStateImpl() }
    single<LibraryStateImpl> { LibraryStateImpl() }
    single<CatalogsStateImpl> { CatalogsStateImpl() }
    single<UpdateStateImpl> { UpdateStateImpl() }
    factory   { BackupScreenViewModel(get(),get(),get(),get(),get(),get(),get()) }
    factory <ExploreStateImpl> { ExploreStateImpl() }
    factory <GlobalSearchStateImpl> { GlobalSearchStateImpl() }
    factory   { AdvanceSettingViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory <DownloadStateImpl> { DownloadStateImpl() }
    factory <FontScreenStateImpl> { FontScreenStateImpl() }

    factory  { ScreenContentViewModel(get()) }
    single<AppThemeViewModel> { AppThemeViewModel(get(), get(), get(), get()) }

    factory<ExploreViewModel> { ExploreViewModel(get(), get(), get(), get(),get(), get(), get(),get(),get(),get()) }
    factory  { HistoryViewModel(get(), get(), get()) }
    factory  { LibraryViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory  { ExtensionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory<GlobalSearchViewModel> { GlobalSearchViewModel(get(), get(), get(), get(), get(), get()) }

    factory  { UpdatesViewModel(get(), get(), get(), get(), get(), get(), get()) }

    factory<BookDetailViewModel>  { BookDetailViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory  { MainSettingScreenViewModel(get()) }
    factory  { AppearanceViewModel(get(), get()) }

    factory  { CategoryScreenViewModel(get(), get(), get(),get(),get()) }
    factory  { DownloaderViewModel(get(), get(), get(), get()) }
    factory  { FontScreenViewModel(get(), get(), get(), get(),get()) }
    factory  { GeneralSettingScreenViewModel(get(), get(), get(), get()) }
    factory  { TranslationSettingsViewModel(get(), get()) }
    factory  { ReaderSettingScreenViewModel(get(), get(), get()) }
    factory  { SourceRepositoryViewModel(get(), get()) }
    factory  { StatisticsViewModel(get()) }


    factory <ChapterStateImpl> { ChapterStateImpl() }
    factory <DetailStateImpl> { DetailStateImpl() }

    factory <ReaderScreenStateImpl> { ReaderScreenStateImpl() }
    factory <ReaderScreenPreferencesStateImpl> { ReaderScreenPreferencesStateImpl() }

    factory< ReaderScreenViewModel>  { ReaderScreenViewModel(
        get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),
        get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),
        // Translation use cases
        get(),get(),get(),get(),get(),get(),get(),
        // Statistics use case
        get(),
        // Params and scope
        get(),get()
    ) }


}