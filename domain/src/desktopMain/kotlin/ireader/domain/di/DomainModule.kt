package ireader.domain.di

import ireader.core.http.HttpClients
import ireader.core.prefs.JvmPreferenceStore
import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.DesktopUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.file.DesktopFileSaver
import ireader.domain.usecases.files.DesktopGetSimpleStorage
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.usecases.services.StartTTSServicesUseCase
import ireader.i18n.LocalizeHelper
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.new


actual val DomainModule: DI.Module = org.kodein.di.DI.Module("domainModulePlatform") {
    bindProvider<ScreenAlwaysOn> {
        ScreenAlwaysOnImpl()
    }
    bindSingleton {
       DesktopFileSaver()
    }
    bindSingleton<ImportEpub> { ImportEpub(instance(),) }
    bindSingleton {
        DesktopGetSimpleStorage()
    }
    bindSingleton<GetSimpleStorage> {
        DesktopGetSimpleStorage()
    }
    bindSingleton<PlatformUiPreferences> {
        new(::DesktopUiPreferences)
    }
    bindSingleton<StartExtensionManagerService> {
        StartExtensionManagerService(di)
    }
    bindSingleton<PreferenceStore> {
        JvmPreferenceStore("ireader")
    }
    bindSingleton { LocalizeHelper() }

    bindSingleton<ServiceUseCases> {
        ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(di),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(di),
            startTTSServicesUseCase = StartTTSServicesUseCase(),
        )
    }
    bindSingleton<HttpClients> { HttpClients(JvmPreferenceStore("cookies")) }
    bindSingleton<EpubCreator> { EpubCreator() }
}