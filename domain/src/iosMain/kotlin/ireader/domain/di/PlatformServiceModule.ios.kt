package ireader.domain.di

import ireader.core.update.AppUpdateChecker
import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import ireader.presentation.ui.update.AppUpdateChecker
import ireader.presentation.ui.update.IosAppUpdateChecker
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific service module
 */
actual val platformServiceModule: Module = module {
    // TTS V2 Service Starter (no-op on iOS)
    single { TTSV2ServiceStarter() }
    
    // App Update Checker (iOS uses App Store)
    single<AppUpdateChecker> { IosAppUpdateChecker() }
}
