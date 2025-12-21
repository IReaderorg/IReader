package ireader.domain.di

import android.app.Service
import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.http.HttpClients
import okio.Path.Companion.toPath
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.source.LocalCatalogSource
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.services.downloaderService.DefaultNotificationHelper
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.update_service.UpdateService
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.file.AndroidFileSaver
import ireader.domain.usecases.file.FileSaver
import ireader.domain.usecases.files.AndroidGetSimpleStorage
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.storage.AndroidCacheManager
import ireader.domain.storage.AndroidStorageManager
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.local.LocalSourceImpl
import ireader.domain.usecases.local.RefreshLocalLibrary
import ireader.domain.usecases.preferences.AndroidReaderPrefUseCases
import ireader.domain.usecases.preferences.SelectedFontStateUseCase
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.usecases.services.StartTTSServicesUseCase
import ireader.domain.notification.PlatformNotificationManager
import ireader.domain.notification.AndroidNotificationManager
import ireader.i18n.LocalizeHelper
import ireader.domain.services.ExtensionWatcherService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

@OptIn(ExperimentalTextApi::class)
actual val DomainModule = module {
    // Include sync module for sync functionality
    includes(syncModule)
    // Include ServiceModule for platform services (DownloadService, NotificationService, etc.)
    includes(ServiceModule)
    // Include TTS v2 module for new clean TTS architecture
    includes(ireader.domain.services.tts_service.v2.ttsV2Module)
    
    // Process State Manager for handling process death
    single { ireader.domain.services.processstate.ProcessStateManager(get()) }
    
    // FileSystem implementation for Android
    single<ireader.core.io.FileSystem> {
        ireader.core.io.AndroidFileSystem(androidContext())
    }
    
    // Plugins directory for Android
    single(named("pluginsDir")) {
        File(androidContext().filesDir, "plugins").apply { mkdirs() }
    }
    
    // Plugin Class Loader for Android
    single { 
        ireader.domain.plugins.PluginClassLoader(
            cacheDir = androidContext().cacheDir
        )
    }
    
    worker {
        DownloaderService(
            androidContext(),
            get(),
        )
    }
    // New type-safe notification manager
    single<PlatformNotificationManager> {
        AndroidNotificationManager(get())
    }

    worker {
        ExtensionManagerService(
            androidContext(),
            get(),
        )
    }
    worker {
        UpdateService(
            androidContext(), get(),
        )
    }
    worker {
        LibraryUpdatesService(
            androidContext(),
            get(),
        )
    }
    worker {
        ireader.domain.usecases.backup.AutoBackupWorker(
            androidContext(),
            get(),
            get(),
            get()
        )
    }

    single {
        AutomaticBackup(
                get(),
                get(),
                get(),
                get()
        )
    }
    single<ireader.domain.usecases.backup.ScheduleAutomaticBackup> {
        ireader.domain.usecases.backup.ScheduleAutomaticBackupImpl(
                androidContext()
        )
    }
    single <TTSStateImpl> { ireader.domain.services.tts_service.TTSStateImpl() }
    factory  { ireader.domain.services.update_service.UpdateApi(get()) }

    factory  { ireader.domain.usecases.services.StartDownloadServicesUseCase(get()) }
    factory  { ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase(get()) }
    factory  { ireader.domain.usecases.services.StartTTSServicesUseCase(get()) }
    factory  {
        TextReaderPrefUseCase(
                get(),
                get()
        )
    }
    factory  {
        StartExtensionManagerService(
                get()
        )
    }
    single<ExtensionWatcherService> {
        ExtensionWatcherService()
    }
    
    // Storage and Cache Managers
    single<StorageManager> { AndroidStorageManager(get(), get()) }
    single<CacheManager> { AndroidCacheManager(get(), get()) }
    
    single<GetSimpleStorage>{ AndroidGetSimpleStorage(get(), get()) }
    single<AndroidGetSimpleStorage>{ AndroidGetSimpleStorage(get(), get()) }
    single<DefaultNotificationHelper> { DefaultNotificationHelper(get(),get()) }
    factory <ScreenAlwaysOn> {
        ScreenAlwaysOnImpl(get())
    }
    single<FileSaver> {
        AndroidFileSaver(get())
    }
    single {
        AndroidReaderPrefUseCases(
                selectedFontStateUseCase = SelectedFontStateUseCase(get(),get()),

        )
    }
    single<ImportEpub> { ImportEpub(get(), get(), get(), get(), get(), get(), get()) }
    single<PlatformUiPreferences> {
        AndroidUiPreferences(get(), get())
    }
    factory<EpubCreator> { EpubCreator(get(), get(), get(), get<HttpClients>().default) }
    single<ServiceUseCases> {
        ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(get()),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(get()),
            startTTSServicesUseCase = StartTTSServicesUseCase(get()),
        )
    }

    single<LocalizeHelper> { LocalizeHelper(get()) }
    single<PreferenceStore> {
        get<PreferenceStoreFactory>().create("ireader")
    }
    
    // Network components
    single<io.ktor.client.plugins.cookies.CookiesStorage> { 
        ireader.core.http.AcceptAllCookiesStorage() 
    }
    
    single<ireader.core.http.WebViewCookieJar> { 
        ireader.core.http.WebViewCookieJar(get())
    }
    
    single<ireader.core.http.WebViewManger> { 
        ireader.core.http.WebViewManger(androidContext())
    }
    
    single<ireader.core.http.BrowserEngine> { 
        ireader.core.http.BrowserEngine(get(), get())
    }
    
    single<ireader.core.http.NetworkConfig> { 
        ireader.core.http.NetworkConfig() 
    }
    
    single<ireader.core.http.HttpClients> { 
        ireader.core.http.HttpClients(
            context = androidContext(),
            browseEngine = get(),
            cookiesStorage = get(),
            webViewCookieJar = get(),
            preferencesStore = get(),
            webViewManager = get(),
        )
    }

    // Local Library Source
    single<LocalCatalogSource> {
        LocalSourceImpl(androidContext())
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
            localSource = get(),
            context = androidContext()
        )
    }
    
    // Payment Processor for plugin monetization
    single<ireader.domain.plugins.PaymentProcessor> {
        ireader.domain.plugins.AndroidPaymentProcessor(
            getCurrentUserId = { 
                // TODO: Get actual user ID from authentication service
                "default_user"
            }
        )
    }
    
    // Voice Storage for TTS voice models
    single<ireader.domain.storage.VoiceStorage> {
        ireader.domain.storage.AndroidVoiceStorage(androidContext().filesDir)
    }
    
    // AI TTS Manager
    single<ireader.domain.services.tts.AITTSManager> {
        ireader.domain.services.tts.AITTSManager(
            context = androidContext(),
            appPreferences = get(),
            gradioTTSManager = get()
        )
    }
    
    // TTS Download Notification Helper
    single<ireader.domain.services.tts_service.TTSDownloadNotificationHelper> {
        ireader.domain.services.tts_service.TTSDownloadNotificationHelper(
            notificationManager = get()
        )
    }
    
    // TTS Chapter Download Manager with notification support
    single<ireader.domain.services.tts_service.TTSChapterDownloadManager> {
        ireader.domain.services.tts_service.TTSChapterDownloadManager(
            notificationHelper = get()
        )
    }
    
    // TTS Chapter Cache for storing downloaded chapter audio
    single<ireader.domain.services.tts_service.TTSChapterCache> {
        val cachePath = "${androidContext().cacheDir.absolutePath}/tts_chapter_cache"
        val cacheDir = cachePath.toPath()
        ireader.domain.services.tts_service.TTSChapterCache(
            fileSystem = okio.FileSystem.SYSTEM,
            cacheDir = cacheDir
        )
    }
    
    // Gradio TTS Manager for online TTS services
    single<ireader.domain.services.tts_service.GradioTTSManager> {
        val appPrefs: ireader.domain.preferences.prefs.AppPreferences = get()
        ireader.domain.services.tts_service.GradioTTSManager(
            httpClient = get<ireader.core.http.HttpClients>().default,
            audioPlayerFactory = { ireader.domain.services.tts_service.AndroidGradioAudioPlayer(androidContext()) },
            saveConfigs = { json -> appPrefs.gradioTTSConfigs().set(json) },
            loadConfigs = { appPrefs.gradioTTSConfigs().get().ifEmpty { null } },
            pluginManager = get()  // Load TTS configs from installed GRADIO_TTS plugins
        )
    }
}