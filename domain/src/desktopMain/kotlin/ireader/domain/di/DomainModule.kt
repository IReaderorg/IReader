package ireader.domain.di

import ireader.core.prefs.JvmPreferenceStore
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.DesktopUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.usecases.file.DesktopFileSaver
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.*
import ireader.i18n.LocalizeHelper
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.new


actual val DomainModule: DI.Module = org.kodein.di.DI.Module("domainModulePlatform") {
    bindProvider<ScreenAlwaysOn> {
        ScreenAlwaysOnImpl()
    }
    bindSingleton {
       DesktopFileSaver()
    }
    bindSingleton<PlatformUiPreferences> {
        new(::DesktopUiPreferences)
    }
    bindSingleton<StartExtensionManagerService> {
        new(::StartExtensionManagerService)
    }
    bindSingleton<PreferenceStore> {
        JvmPreferenceStore("ireader")
    }
    bindSingleton { LocalizeHelper() }
    bindSingleton<ServiceUseCases> { ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(),
            startTTSServicesUseCase = StartTTSServicesUseCase(),
            stopServicesUseCase = StopServiceUseCase(),
    ) }
}