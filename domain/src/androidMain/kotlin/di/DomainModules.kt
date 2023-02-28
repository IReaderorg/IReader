package ireader.domain.di

import android.app.Service
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val DomainServices = DI.Module("domainModule") {
    bindProvider {
        DownloaderService(
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton {
        ExtensionManagerService(
            instance(),
            instance(),

        )
    }
    bindSingleton {
        UpdateService(instance(), instance())
    }
    bindSingleton {
        LibraryUpdatesService(
            instance(),
            instance(),
        )
    }

    bindProvider<Service>() {
        TTSService()
    }

    bindSingleton { ireader.domain.services.downloaderService.DefaultNotificationHelper(instance()) }
    bindSingleton<DownloadServiceStateImpl> { ireader.domain.services.downloaderService.DownloadServiceStateImpl() }
    bindSingleton {
        ireader.domain.usecases.backup.AutomaticBackup(
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton { ireader.domain.usecases.files.GetSimpleStorage(instance(), instance()) }
    bindSingleton { ireader.domain.preferences.prefs.PlayerPreferences(instance()) }
    bindProvider {
        ireader.domain.services.extensions_insstaller_service.GetDefaultRepo(
            instance(),
            instance()
        )
    }
    bindProvider {
        ireader.domain.services.extensions_insstaller_service.interactor.StartExtensionManagerService(
            instance()
        )
    }
    bindProvider<TTSStateImpl> { ireader.domain.services.tts_service.TTSStateImpl() }
    bindProvider { ireader.domain.services.update_service.UpdateApi(instance()) }
    bindProvider { ireader.domain.usecases.backup.BackUpUseCases(instance()) }
    bindProvider {
        ireader.domain.usecases.backup.CreateBackup(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider {
        ireader.domain.usecases.backup.RestoreBackup(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.category.CategoriesUseCases(instance(), instance()) }
    bindProvider { ireader.domain.usecases.category.CreateCategoryWithName(instance()) }
    bindProvider { ireader.domain.usecases.category.ReorderCategory(instance()) }
    bindProvider { ireader.domain.usecases.download.get.SubscribeDownloadsUseCase(instance()) }

    bindProvider { ireader.domain.usecases.epub.importer.ImportEpub(instance(), instance(), instance()) }
    bindProvider { ireader.domain.usecases.fonts.FontUseCase(instance()) }
    bindProvider { ireader.domain.usecases.history.HistoryPagingUseCase(instance()) }

    bindProvider { ireader.domain.usecases.local.FindBooksByKey(instance()) }
    bindProvider { ireader.domain.usecases.local.SubscribeBooksByKey(instance()) }
    bindProvider { ireader.domain.usecases.local.FindBookByKey(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.GetLibraryCategory(instance()) }
    bindProvider {
        ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase(
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.local.book_usecases.SubscribeBookById(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.FindBookById(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.FindDuplicateBook(instance()) }
    bindProvider { ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks(instance()) }
    bindProvider {
        ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime(
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.local.delete_usecases.book.DeleteBookById(instance()) }
    bindProvider {
        ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook(
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider {
        ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks(
            instance()
        )
    }
    bindProvider {
        ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId(
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.local.insert_usecases.InsertBook(instance()) }
    bindProvider { ireader.domain.usecases.local.insert_usecases.UpdateBook(instance()) }
    bindProvider { ireader.domain.usecases.local.insert_usecases.InsertBooks(instance()) }
    bindProvider { ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters(instance()) }
    bindProvider { ireader.domain.usecases.local.insert_usecases.InsertChapter(instance()) }
    bindProvider { ireader.domain.usecases.local.insert_usecases.InsertChapters(instance()) }
    bindProvider {
        ireader.domain.usecases.preferences.apperance.NightModePreferencesUseCase(
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.preferences.reader_preferences.DohPrefUseCase(instance()) }
    bindProvider {
        ireader.domain.usecases.preferences.reader_preferences.TextReaderPrefUseCase(
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.usecases.preferences.services.LastUpdateTime(instance()) }
    bindProvider { ireader.domain.usecases.reader.ScreenAlwaysOn() }
    bindProvider { ireader.domain.usecases.remote.GetBookDetail() }
    bindProvider { ireader.domain.usecases.remote.GetRemoteBooksUseCase() }
    bindProvider { ireader.domain.usecases.remote.GetRemoteChapters() }
    bindProvider { ireader.domain.usecases.remote.GetRemoteReadingContent() }
    bindProvider { ireader.domain.usecases.services.StartDownloadServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartTTSServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StopServiceUseCase(instance()) }
    bindProvider {
        ireader.domain.usecases.translate.TranslationEnginesManager(
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.catalogs.interactor.GetInstalledCatalog(instance()) }

    bindProvider { ireader.domain.usecases.updates.SubscribeUpdates(instance()) }
    bindProvider { ireader.domain.usecases.updates.DeleteAllUpdates(instance()) }
}