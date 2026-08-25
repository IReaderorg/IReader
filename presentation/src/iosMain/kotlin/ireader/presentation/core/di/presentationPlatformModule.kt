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

    // No native image decoder on iOS; extractor returns null so cover theme
    // simply stays off (UI falls back to the regular app theme)
    single<ireader.domain.utils.cover.CoverColorExtractor> {
        object : ireader.domain.utils.cover.CoverColorExtractor {
            override suspend fun extractDominantColor(coverUrl: String, sourceId: Long?) = null
            override suspend fun extractDominantColorFromBitmap(byteArray: ByteArray) = null
        }
    }
}
