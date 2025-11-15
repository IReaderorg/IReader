package ireader.domain.di

import android.app.Service
import androidx.compose.ui.text.ExperimentalTextApi
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
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.file.AndroidFileSaver
import ireader.domain.usecases.file.FileSaver
import ireader.domain.usecases.files.AndroidGetSimpleStorage
import ireader.domain.usecases.files.GetSimpleStorage
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
import ireader.domain.utils.NotificationManager
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
    factory {
        NotificationManager(
            get(),
        )
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

    factory<Service>() {
        TTSService()
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
    factory <TTSStateImpl> { ireader.domain.services.tts_service.TTSStateImpl() }
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
    single<GetSimpleStorage>{ AndroidGetSimpleStorage(get()) }
    single<AndroidGetSimpleStorage>{ AndroidGetSimpleStorage(get()) }
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
    single<ImportEpub> { ImportEpub(get(), get(),get(),get(),get()) }
    single<PlatformUiPreferences> {
        AndroidUiPreferences(get(), get())
    }
    factory<EpubCreator> { EpubCreator(get(), get(), get()) }
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
    
    // Wallet Integration for donations only
    single<ireader.domain.services.WalletIntegrationManager> {
        ireader.domain.services.AndroidWalletIntegrationManager(androidContext())
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
}