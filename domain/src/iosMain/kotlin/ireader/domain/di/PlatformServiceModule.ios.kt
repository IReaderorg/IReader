package ireader.domain.di

import ireader.domain.services.platform.ClipboardService
import ireader.domain.services.platform.FileSystemService
import ireader.domain.services.platform.HapticService
import ireader.domain.services.platform.IosClipboardService
import ireader.domain.services.platform.IosFileSystemService
import ireader.domain.services.platform.IosNetworkService
import ireader.domain.services.platform.IosShareService
import ireader.domain.services.platform.IosSystemInteractionService
import ireader.domain.services.platform.NetworkService
import ireader.domain.services.platform.ShareService
import ireader.domain.services.platform.SystemInteractionService
import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific service module
 */
actual val platformServiceModule: Module = module {
    single { TTSV2ServiceStarter() }
    single<ClipboardService> { IosClipboardService() }
    single<ShareService> { IosShareService() }
    single<FileSystemService> { IosFileSystemService() }
    single<HapticService> { NoOpHapticService() }
    single<NetworkService> { IosNetworkService() }
    single<SystemInteractionService> { IosSystemInteractionService() }
}
