package ireader.domain.di

import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific service module
 */
actual val platformServiceModule: Module = module {
    // TTS V2 Service Starter (no-op on iOS)
    single { TTSV2ServiceStarter() }
}
