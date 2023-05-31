package ireader.domain.di

import android.app.Service
import androidx.compose.ui.text.ExperimentalTextApi
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
import ireader.domain.usecases.preferences.*
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import ireader.domain.usecases.services.*
import ireader.domain.utils.NotificationManager
import ireader.i18n.LocalizeHelper
import org.koin.dsl.module

@OptIn(ExperimentalTextApi::class)
actual val DomainModule = module {
    factory  {
        DownloaderService(
                get(),
                get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory  {
        NotificationManager(
            get(),
        )
    }
    single {
        ExtensionManagerService(
                get(),
                get(),
            get(),
            get(),
            get(),
            get(),
            get(),
                )
    }
    single {
        UpdateService(get()  ,       get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        LibraryUpdatesService(
                get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    factory <Service>() {
        TTSService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
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
    single<ImportEpub> { ImportEpub(get(), get(),get(),get()) }
    single<PlatformUiPreferences> {
        AndroidUiPreferences(get(),get())
    }
    factory <EpubCreator> { EpubCreator(get(), get(),get()) }
    single<ServiceUseCases> { ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(get()),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(get()),
            startTTSServicesUseCase = StartTTSServicesUseCase(get()),
    ) }

    single<LocalizeHelper> { LocalizeHelper(get()) }






}