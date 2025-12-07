package ireader.presentation.core.di

import android.content.Context
import ireader.domain.image.CoverCache
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper

import ireader.presentation.imageloader.CoilLoaderFactory
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

    factory <WebViewPageStateImpl> { WebViewPageStateImpl() }

    single { LocaleHelper(get(),get()) }
    single { ireader.presentation.ui.book.helpers.PlatformHelper(get()) }
    single<IUseController> { IUseController() }
    single<CoilLoaderFactory>(createdAtStart = true) {
        CoilLoaderFactory(
            get(),
            get(),
            get(),
        )
    }
    single<CoverCache> { CoverCache(get<Context>()) }
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader(get()) }
    
    // Material You dynamic colors support
    single<ireader.presentation.core.theme.DynamicColorScheme> { 
        ireader.presentation.core.theme.AndroidDynamicColorScheme(get()) 
    }

}