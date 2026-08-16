package ireader.presentation.core.di

import ireader.core.http.WebViewManger
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.settings.tracking.DesktopTrackingSyncScheduler
import ireader.presentation.ui.settings.tracking.TrackingSyncScheduler
import ireader.presentation.utils.cover.DesktopCoverColorExtractor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val presentationPlatformModule: Module = module {
    single { LocaleHelper(get()) }
    single { ireader.presentation.ui.book.helpers.PlatformHelper() }
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader() }
    single { WebViewManger() }
    single<IUseController> { IUseController() }
    
    // Dynamic colors not supported on desktop
    single<ireader.presentation.core.theme.DynamicColorScheme> { 
        ireader.presentation.core.theme.DesktopDynamicColorScheme() 
    }
    
    // Tracking sync scheduler using Java Timer
    single<TrackingSyncScheduler> { DesktopTrackingSyncScheduler() }
    
    // Cover color extractor for cover-based dynamic theme
    single<ireader.domain.utils.cover.CoverColorExtractor> { DesktopCoverColorExtractor() }
}