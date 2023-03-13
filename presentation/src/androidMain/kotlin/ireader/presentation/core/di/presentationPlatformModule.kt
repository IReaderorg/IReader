package ireader.presentation.core.di

import ireader.presentation.core.PlatformHelper
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.SecuritySettingViewModel
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.book.viewmodel.ChapterStateImpl
import ireader.presentation.ui.book.viewmodel.DetailStateImpl
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.reader.viewmodel.ReaderScreenPreferencesStateImpl
import ireader.presentation.ui.reader.viewmodel.ReaderScreenStateImpl
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.settings.advance.AdvanceSettingViewModel
import ireader.presentation.ui.settings.backups.BackupScreenViewModel
import ireader.presentation.ui.video.VideoScreenViewModel
import ireader.presentation.ui.video.component.PlayerCreator
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.core.PlayerStateImpl
import ireader.presentation.ui.web.WebViewPageModel
import ireader.presentation.ui.web.WebViewPageStateImpl
import org.kodein.di.*

actual val presentationPlatformModule: DI.Module = DI.Module("androidPresentationModule") {
    bindFactory< VideoScreenViewModel.Param, VideoScreenViewModel>  { VideoScreenViewModel(instance(),instance(),instance(),instance(),instance(),instance(),instance(),it) }
    bindFactory< WebViewPageModel.Param, WebViewPageModel> { WebViewPageModel(instance(),instance(),instance(),instance(),instance(),it,instance(),instance()) }
    bindProvider  { BackupScreenViewModel(instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance()) }
    bindProvider  { AdvanceSettingViewModel(instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance()) }
    bindFactory< ReaderScreenViewModel.Param, ReaderScreenViewModel>  { ReaderScreenViewModel(instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),it,instance(),) }
    bindFactory< TTSViewModel.Param, TTSViewModel>  { TTSViewModel(instance(),it,instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance()) }
    bindProvider<ChapterStateImpl> { ChapterStateImpl() }
    bindProvider<DetailStateImpl> { DetailStateImpl() }
    bindProvider<PlatformReaderSettingReader> { PlatformReaderSettingReader(instance(),instance(),instance()) }
    bindProvider<ReaderScreenStateImpl> { ReaderScreenStateImpl() }
    bindProvider<ReaderScreenPreferencesStateImpl> { ReaderScreenPreferencesStateImpl() }
    bindProvider { PlayerCreator(instance()) }
    bindProvider { MediaState(instanceOrNull(),instance(),di) }
    bindProvider<PlayerState> { PlayerStateImpl(instance(),instance()) }
    bindProvider<WebViewPageStateImpl> { WebViewPageStateImpl() }
    bindProvider  { SecuritySettingViewModel(instance()) }
    bindFactory< BookDetailViewModel.Param, BookDetailViewModel>  { BookDetailViewModel(instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),instance(),it,instance(),instance()) }
    bindSingleton { LocaleHelper(instance(),instance()) }
    bindSingleton { PlatformHelper(instance()) }
    bindSingleton<CoilLoaderFactory> { CoilLoaderFactory(instance(), instance(), instance(), instance()) }

}