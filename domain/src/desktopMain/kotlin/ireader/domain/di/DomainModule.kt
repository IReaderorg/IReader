package ireader.domain.di

import ireader.core.http.HttpClients
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.PreferenceStoreFactory
import ireader.domain.preferences.prefs.DesktopUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.file.DesktopFileSaver
import ireader.domain.usecases.file.FileSaver
import ireader.domain.usecases.local.LocalSourceImpl
import ireader.domain.usecases.local.RefreshLocalLibrary
import ireader.core.source.LocalCatalogSource
import java.io.File
import ireader.domain.usecases.files.DesktopGetSimpleStorage
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.usecases.services.StartTTSServicesUseCase
import ireader.domain.utils.NotificationManager
import ireader.i18n.LocalizeHelper
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import ireader.domain.services.tts_service.piper.PiperModelManager
import ireader.domain.services.ExtensionWatcherService
import org.koin.core.module.Module
import org.koin.dsl.module


actual val DomainModule: Module = module {
    // Include sync module for sync functionality
    includes(syncModule)
    
    factory <ScreenAlwaysOn> {
        ScreenAlwaysOnImpl()
    }
    single<FileSaver> {
       DesktopFileSaver()
    }
    single<ImportEpub> { ImportEpub(get(), get(), get()) }
    single {
        DesktopGetSimpleStorage()
    }
    factory  {
        NotificationManager(
        )
    }
    single<GetSimpleStorage> {
        DesktopGetSimpleStorage()
    }
    single<PlatformUiPreferences> {
        DesktopUiPreferences(get())
    }
    single<StartExtensionManagerService> {
        StartExtensionManagerService(get(), get(), get(), get())
    }
    single<ExtensionWatcherService> {
        ExtensionWatcherService()
    }
    single<PreferenceStore> {
        get<PreferenceStoreFactory>().create("ireader")
    }
    single { LocalizeHelper() }

    single<StartDownloadServicesUseCase> {
        StartDownloadServicesUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    
    single<StartLibraryUpdateServicesUseCase> {
        StartLibraryUpdateServicesUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    
    // Piper TTS Components
    single<PiperSpeechSynthesizer> {
        PiperSpeechSynthesizer()
    }
    
    single<AudioPlaybackEngine> {
        AudioPlaybackEngine()
    }
    
    single<PiperModelManager> {
        val appDataDir = File(System.getProperty("user.home"), ".ireader")
        PiperModelManager(appDataDir)
    }
    
    single<ireader.domain.services.tts_service.DesktopTTSService> {
        ireader.domain.services.tts_service.DesktopTTSService().apply {
            initialize()
        }
    }
    
    single<StartTTSServicesUseCase> {
        StartTTSServicesUseCase(get())
    }

    single<ServiceUseCases> {
        ServiceUseCases(
            startDownloadServicesUseCase = get(),
            startLibraryUpdateServicesUseCase = get(),
            startTTSServicesUseCase = get(),
        )
    }
    single<HttpClients> { HttpClients(get<PreferenceStoreFactory>().create("cookies")) }
    single<EpubCreator> { EpubCreator(get()) }
    
    single<ireader.domain.usecases.backup.ScheduleAutomaticBackup> {
        ireader.domain.usecases.backup.ScheduleAutomaticBackupImpl()
    }
    
    // Local Library Source
    single<LocalCatalogSource> {
        val appDataDir = File(System.getProperty("user.home"), ".ireader")
        LocalSourceImpl(appDataDir)
    }
    
    factory {
        RefreshLocalLibrary(
            localSource = get(),
            bookRepository = get(),
            chapterRepository = get()
        )
    }
    
    factory {
        ireader.domain.usecases.local.OpenLocalFolder(
            localSource = get()
        )
    }
    
    // Wallet Integration
    single<ireader.domain.services.WalletIntegrationManager> {
        ireader.domain.services.DesktopWalletIntegrationManager()
    }
}