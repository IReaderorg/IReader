package ireader.domain.di

import android.app.Service
import ireader.domain.services.downloaderService.DefaultNotificationHelper
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.fonts.FontUseCase
import ireader.domain.usecases.preferences.apperance.NightModePreferencesUseCase
import ireader.domain.usecases.preferences.reader_preferences.DohPrefUseCase
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.preferences.services.LastUpdateTime
import ireader.domain.usecases.remote.GetBookDetail
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import ireader.domain.usecases.remote.GetRemoteReadingContent
import ireader.domain.usecases.translate.TranslationEnginesManager
import org.kodein.di.*


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

    bindSingleton<DefaultNotificationHelper> { new(::DefaultNotificationHelper) }
    bindSingleton<DownloadServiceStateImpl> { ireader.domain.services.downloaderService.DownloadServiceStateImpl() }
    bindSingleton {
        AutomaticBackup(
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

    bindProvider {
        CreateBackup(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider {
        RestoreBackup(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
        )
    }
    bindProvider { CategoriesUseCases(instance(), instance()) }
    bindProvider { CreateCategoryWithName(instance()) }
    bindProvider { ReorderCategory(instance()) }
    bindProvider { SubscribeDownloadsUseCase(instance()) }

    bindProvider { ImportEpub(instance(), instance(), instance()) }
    bindProvider { FontUseCase(instance()) }
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
        NightModePreferencesUseCase(
            instance()
        )
    }
    bindProvider { DohPrefUseCase(instance()) }
    bindProvider {
        TextReaderPrefUseCase(
            instance(),
            instance()
        )
    }
    bindProvider { LastUpdateTime(instance()) }

    bindProvider { GetBookDetail() }
    bindProvider { GetRemoteBooksUseCase() }
    bindProvider { GetRemoteChapters() }
    bindProvider { GetRemoteReadingContent() }
    bindProvider { ireader.domain.usecases.services.StartDownloadServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StartTTSServicesUseCase(instance()) }
    bindProvider { ireader.domain.usecases.services.StopServiceUseCase(instance()) }
    bindProvider {
        TranslationEnginesManager(
            instance(),
            instance()
        )
    }
    bindProvider { ireader.domain.catalogs.interactor.GetInstalledCatalog(instance()) }

    bindProvider { ireader.domain.usecases.updates.SubscribeUpdates(instance()) }
    bindProvider { ireader.domain.usecases.updates.DeleteAllUpdates(instance()) }
}