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
import ireader.domain.storage.CacheManager
import ireader.domain.storage.DesktopCacheManager
import ireader.domain.storage.DesktopStorageManager
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.usecases.services.StartTTSServicesUseCase
import ireader.domain.notification.PlatformNotificationManager
import ireader.domain.notification.DesktopNotificationManager
import ireader.i18n.LocalizeHelper
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.piper.AudioPlaybackEngine
import ireader.domain.services.tts_service.piper.PiperModelManager
import ireader.domain.services.ExtensionWatcherService
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


actual val DomainModule: Module = module {
    // Include sync module for sync functionality
    includes(syncModule)
    // Include ServiceModule for platform services (DownloadService, NotificationService, etc.)
    includes(ServiceModule)
    
    // FileSystem implementation for desktop
    single<ireader.core.io.FileSystem> {
        ireader.core.io.DesktopFileSystem()
    }
    
    // Plugins directory for desktop
    single(named("pluginsDir")) {
        val appDataDir = File(System.getProperty("user.home"), ".ireader")
        File(appDataDir, "plugins").apply { mkdirs() }
    }
    
    // Plugin Class Loader for desktop
    single { ireader.domain.plugins.PluginClassLoader() }
    
    factory <ScreenAlwaysOn> {
        ScreenAlwaysOnImpl()
    }
    single<FileSaver> {
       DesktopFileSaver()
    }
    single<ImportEpub> { ImportEpub(get(), get(), get(), get(), get()) }
    
    // Storage and Cache Managers
    single<StorageManager> { DesktopStorageManager() }
    single<CacheManager> { DesktopCacheManager() }
    
    single {
        DesktopGetSimpleStorage()
    }
    // New type-safe notification manager
    single<PlatformNotificationManager> {
        DesktopNotificationManager()
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

    // Service Use Cases - now properly resolved without circular dependency
    single<StartDownloadServicesUseCase> {
        StartDownloadServicesUseCase(
            bookRepo = get(),
            chapterRepo = get(),
            remoteUseCases = get(),
            localizeHelper = get(),
            extensions = get(),
            insertUseCases = get(),
            downloadUseCases = get(),
            downloadServiceState = get(),
            notificationManager = get(),
            downloadPreferences = get()
        )
    }
    
    single<StartLibraryUpdateServicesUseCase> {
        StartLibraryUpdateServicesUseCase(
            getBookUseCases = get(),
            getChapterUseCase = get(),
            remoteUseCases = get(),
            getLocalCatalog = get(),
            insertUseCases = get(),
            notificationManager = get()
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
    
    // Desktop TTS Service (legacy implementation)
    single<ireader.domain.services.tts_service.DesktopTTSService> {
        ireader.domain.services.tts_service.DesktopTTSService().apply {
            initialize()
        }
    }
    
    // Provide CommonTTSService interface using adapter
    single<ireader.domain.services.tts_service.CommonTTSService> {
        ireader.domain.services.tts_service.DesktopTTSServiceAdapter(
            service = get()
        )
    }
    
    // AI TTS Manager (stub for desktop)
    single<ireader.domain.services.tts.AITTSManager> {
        ireader.domain.services.tts.AITTSManager()
    }
    
    single<StartTTSServicesUseCase> {
        StartTTSServicesUseCase(get())
    }

    // ServiceUseCases - now can use get() since the use cases are defined above
    single<ServiceUseCases> {
        ServiceUseCases(
            startDownloadServicesUseCase = get(),
            startLibraryUpdateServicesUseCase = get(),
            startTTSServicesUseCase = get()
        )
    }
    // Network components
    single<ireader.core.http.NetworkConfig> { 
        ireader.core.http.NetworkConfig() 
    }
    
    single<HttpClients> { 
        HttpClients(
            store = get<PreferenceStoreFactory>().create("cookies"),
            networkConfig = get()
        ) 
    }
    
    single<EpubCreator> { EpubCreator(get(), get<HttpClients>().default) }
    
    // Google Fonts Downloader for desktop
    single { ireader.domain.usecases.fonts.GoogleFontsDownloader(get()) }
    
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
    
    // Payment Processor for plugin monetization
    single<ireader.domain.plugins.PaymentProcessor> {
        ireader.domain.plugins.DesktopPaymentProcessor(
            getCurrentUserId = { 
                // TODO: Get actual user ID from authentication service
                "default_user"
            }
        )
    }
    
    // Gradio TTS Manager for online TTS services
    single<ireader.domain.services.tts_service.GradioTTSManager> {
        val appPrefs: ireader.domain.preferences.prefs.AppPreferences = get()
        ireader.domain.services.tts_service.GradioTTSManager(
            httpClient = get<HttpClients>().default,
            audioPlayerFactory = { ireader.domain.services.tts_service.DesktopGradioAudioPlayer() },
            saveConfigs = { json -> appPrefs.gradioTTSConfigs().set(json) },
            loadConfigs = { appPrefs.gradioTTSConfigs().get().ifEmpty { null } }
        )
    }
}