package ireader.presentation.core.di

import android.content.Context
import ireader.domain.image.CoverCache
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.presentation.core.PlatformHelper
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.core.ui.SecuritySettingViewModel
import ireader.presentation.imageloader.CoilLoaderFactory
import ireader.presentation.ui.home.tts.TTSViewModel
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.web.WebViewPageModel
import ireader.presentation.ui.web.WebViewPageStateImpl
import org.koin.dsl.module


actual val presentationPlatformModule = module  {
    factory<ireader.presentation.ui.web.AutoFetchDetector> { 
        ireader.presentation.ui.web.DefaultAutoFetchDetector() 
    }
    
    factory<WebViewPageModel> { params ->
        WebViewPageModel(
            insertUseCases = get(),
            getBookUseCases = get(),
            getChapterUseCase = get(),
            extensions = get(),
            remoteUseCases = get(),
            param = params.get(),
            webpageImpl = get(),
            webViewManager = get(),
            autoFetchDetector = get(),
            localizeHelper = get()
        )
    }

    // Register WebscrapingTranslateEngine for Android platform
    factory { WebscrapingTranslateEngine(get(), get()) }

    factory<TTSViewModel>  { TTSViewModel(get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get(),get()) }

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
        )
    }
    single<CoverCache> { CoverCache(get<Context>(), get())}
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader(get()) }
    
    // Material You dynamic colors support
    single<ireader.presentation.core.theme.DynamicColorScheme> { 
        ireader.presentation.core.theme.AndroidDynamicColorScheme(get()) 
    }

}