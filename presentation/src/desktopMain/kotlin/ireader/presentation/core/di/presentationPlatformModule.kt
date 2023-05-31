package ireader.presentation.core.di

import ireader.core.http.WebViewManger
import ireader.presentation.core.PlatformHelper
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import org.koin.core.module.Module
import org.koin.dsl.module

actual val presentationPlatformModule: Module = module {
    single { LocaleHelper() }
    single { PlatformHelper() }
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader() }
    single { WebViewManger() }
    single<IUseController> { IUseController() }
}