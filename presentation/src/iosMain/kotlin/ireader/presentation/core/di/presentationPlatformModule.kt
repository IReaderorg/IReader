package ireader.presentation.core.di

import ireader.presentation.core.theme.IosDynamicColorScheme
import ireader.presentation.core.theme.IUseController
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.settings.tracking.IosTrackingSyncScheduler
import ireader.presentation.ui.settings.tracking.TrackingSyncScheduler
import ireader.presentation.ui.update.AppUpdateChecker
import ireader.presentation.ui.update.IosAppUpdateChecker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val presentationPlatformModule: Module = module {
    single { LocaleHelper(get()) }
    single { ireader.presentation.ui.book.helpers.PlatformHelper() }
    single<PlatformReaderSettingReader> { PlatformReaderSettingReader() }
    single<IUseController> { IUseController() }
    
    // Dynamic colors not supported on iOS (no Material You)
    single<ireader.presentation.core.theme.DynamicColorScheme> { 
        IosDynamicColorScheme() 
    }
    
    // App update checker for iOS
    single<AppUpdateChecker> { IosAppUpdateChecker() }
    
    // Tracking sync scheduler using NSTimer
    single<TrackingSyncScheduler> { IosTrackingSyncScheduler() }
}
