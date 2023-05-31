package ireader.presentation.core.di

import ireader.presentation.core.PlatformHelper
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.SecuritySettingViewModel
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.video.VideoScreenViewModel
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.core.PlayerStateImpl
import ireader.presentation.ui.web.WebViewPageModel
import ireader.presentation.ui.web.WebViewPageStateImpl
import org.koin.dsl.module


actual val presentationPlatformModule = module  {
    factory<VideoScreenViewModel>  { VideoScreenViewModel(get(),get(),get(),get(),get(),get(),get(),get()) }
    factory<WebViewPageModel> { WebViewPageModel(get(),get(),get(),get(),get(),get(),get(),get()) }



    factory<TTSViewModel>  { TTSViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }

    factory  { MediaState(getOrNull(),get(),get()) }
    factory <PlayerState> { PlayerStateImpl(get(),get()) }
    factory <WebViewPageStateImpl> { WebViewPageStateImpl() }
    factory   { SecuritySettingViewModel(get()) }

    single { LocaleHelper(get(),get()) }
    single { PlatformHelper(get()) }
    single<IUseController> { IUseController() }
    single<CoilLoaderFactory>(createdAtStart = true) {
        CoilLoaderFactory(
            get(),
            get(),
            get(),
            get()
        )
    }
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader(get()) }

}