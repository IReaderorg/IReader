package ireader.presentation.core.di


import androidx.lifecycle.ViewModel
import ireader.domain.services.tts_service.TTSState
import ireader.i18n.ModulesMetaData
import ireader.presentation.core.ScreenContentViewModel
import ireader.presentation.core.theme.AppThemeViewModel
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.SecuritySettingViewModel
import ireader.presentation.ui.book.viewmodel.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.home.explore.viewmodel.BooksState
import ireader.presentation.ui.home.explore.viewmodel.ExploreState
import ireader.presentation.ui.home.explore.viewmodel.ExploreStateImpl
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import ireader.presentation.ui.home.history.viewmodel.HistoryState
import ireader.presentation.ui.home.history.viewmodel.HistoryStateImpl
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.ui.home.library.viewmodel.LibraryState
import ireader.presentation.ui.home.library.viewmodel.LibraryStateImpl
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import ireader.presentation.ui.home.sources.extension.CatalogsState
import ireader.presentation.ui.home.sources.extension.CatalogsStateImpl
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchState
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchStateImpl
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.home.updates.viewmodel.UpdateState
import ireader.presentation.ui.home.updates.viewmodel.UpdateStateImpl
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import ireader.presentation.ui.reader.viewmodel.*
import ireader.presentation.ui.settings.AdvanceSettingViewModel
import ireader.presentation.ui.settings.MainSettingScreenViewModel
import ireader.presentation.ui.settings.appearance.AppearanceViewModel
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import ireader.presentation.ui.settings.category.CategoryScreenViewModel
import ireader.presentation.ui.settings.downloader.DownloadState
import ireader.presentation.ui.settings.downloader.DownloadStateImpl
import ireader.presentation.ui.settings.downloader.DownloaderViewModel
import ireader.presentation.ui.settings.font_screens.FontScreenState
import ireader.presentation.ui.settings.font_screens.FontScreenStateImpl
import ireader.presentation.ui.settings.font_screens.FontScreenViewModel
import ireader.presentation.ui.settings.general.GeneralSettingScreenViewModel
import ireader.presentation.ui.settings.reader.ReaderSettingScreenViewModel
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import ireader.presentation.ui.video.VideoScreenViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.core.PlayerStateImpl
import ireader.presentation.ui.web.WebViewPageModel
import ireader.presentation.ui.web.WebViewPageState
import ireader.presentation.ui.web.WebViewPageStateImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

val PresentationModules = module {
    single(qualifier=null) { LocaleHelper(get(),get()) }
    single(qualifier=null) { BooksState() }
    single(qualifier=null) { HistoryStateImpl() } bind(HistoryState::class)
    single(qualifier=null) { LibraryStateImpl() } bind(LibraryState::class)
    single(qualifier=null) { CatalogsStateImpl() } bind(CatalogsState::class)
    single(qualifier=null) { UpdateStateImpl() } bind(UpdateState::class)
    factory(qualifier=null) { ChapterStateImpl() } bind(ChapterState::class)
    factory(qualifier=null) { DetailStateImpl() } bind(DetailState::class)
    factory(qualifier=null) { ExploreStateImpl() } bind(ExploreState::class)
    factory(qualifier=null) { GlobalSearchStateImpl() } bind(GlobalSearchState::class)
    factory(qualifier=null) { ReaderPrefFunctionsImpl() } bind(ReaderPrefFunctions::class)
    factory(qualifier=null) { ReaderScreenStateImpl() } bind(ReaderScreenState::class)
    factory(qualifier=null) { ReaderScreenPreferencesStateImpl() } bind(ReaderScreenPreferencesState::class)
    factory(qualifier=null) { DownloadStateImpl() } bind(DownloadState::class)
    factory(qualifier=null) { FontScreenStateImpl() } bind(FontScreenState::class)
    factory(qualifier=null) { PlayerCreator(get()) }
    factory(qualifier=null) { MediaState(getOrNull(),get()) }
    factory(qualifier=null) { PlayerStateImpl(get(),get()) } bind(PlayerState::class)
    factory(qualifier=null) { WebViewPageStateImpl() } bind(WebViewPageState::class)
    factory (qualifier=null) { ScreenContentViewModel(get()) } bind(BaseViewModel::class)
    single (qualifier=null) { AppThemeViewModel(get(),get(),get()) }
    factory (qualifier=null) { SecuritySettingViewModel(get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { BookDetailViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        DetailState::class,
        ChapterState::class))
    factory (qualifier=null) { ExploreViewModel(get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        ExploreState::class))
    factory (qualifier=null) { HistoryViewModel(get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        HistoryState::class))
    factory (qualifier=null) { LibraryViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        LibraryState::class))
    factory (qualifier=null) { ExtensionViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        CatalogsState::class))
    factory (qualifier=null) { GlobalSearchViewModel(get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        GlobalSearchState::class))
    factory (qualifier=null) { TTSViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        TTSState::class))
    factory (qualifier=null) { UpdatesViewModel(get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        UpdateState::class))
    factory (qualifier=null) { ReaderScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        ReaderScreenPreferencesState::class,
        ReaderScreenState::class,
        ReaderPrefFunctions::class))
    factory (qualifier=null) { AdvanceSettingViewModel(get(),get(),get(),get(),get(),get(),get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { MainSettingScreenViewModel(get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { AppearanceViewModel(get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { BackupScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { CategoryScreenViewModel(get(),get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { DownloaderViewModel(get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        DownloadState::class))
    factory (qualifier=null) { FontScreenViewModel(get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        FontScreenState::class))
    factory (qualifier=null) { GeneralSettingScreenViewModel(get(),get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { ReaderSettingScreenViewModel(get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { SourceRepositoryViewModel(get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { VideoScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get()) } bind(BaseViewModel::class)
    factory (qualifier=null) { WebViewPageModel(get(),get(),get(),get(),get(),get(),get(),get()) } binds(arrayOf(
        BaseViewModel::class,
        WebViewPageState::class))
}