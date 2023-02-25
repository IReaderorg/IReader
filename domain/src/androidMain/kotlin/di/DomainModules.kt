package ireader.domain.di

import android.app.Service
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.extensions_insstaller_service.ExtensionManagerService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import ireader.i18n.ModulesMetaData
import org.koin.androidx.workmanager.dsl.worker


import org.koin.dsl.bind
import org.koin.dsl.module



val DomainServices = module {
    worker {
        DownloaderService(get(),get(),get(),get(),get(),get(),get(),get(),get(),get())
    }
    worker {
        ExtensionManagerService(get(),get(),get(),get(),get(),get(),get())
    }
    worker {
        UpdateService(get(), get(),get(),get())
    }
    worker {
        LibraryUpdatesService(get(), get(),get(),get(),get(),get(),get(),get(),get())
    }
    single<Service> (createdAtStart=true){
        TTSService()
    }

    single(qualifier=null) { ireader.domain.services.downloaderService.DefaultNotificationHelper(get()) }
    single(qualifier=null) { ireader.domain.services.downloaderService.DownloadServiceStateImpl() } bind(ireader.domain.services.downloaderService.DownloadServiceState::class)
    single(qualifier=null) { ireader.domain.usecases.backup.AutomaticBackup(get(),get(),get(),get()) }
    single(qualifier=null) { ireader.domain.usecases.files.GetSimpleStorage(get(),get()) }
    single(qualifier=null) { ireader.domain.preferences.prefs.PlayerPreferences(get()) }
    factory(qualifier=null) { ireader.domain.services.extensions_insstaller_service.GetDefaultRepo(get(),get()) }
    factory(qualifier=null) { ireader.domain.services.extensions_insstaller_service.interactor.StartExtensionManagerService(get()) }
    factory(qualifier=null) { ireader.domain.services.tts_service.TTSStateImpl() } bind(ireader.domain.services.tts_service.TTSState::class)
    factory(qualifier=null) { ireader.domain.services.update_service.UpdateApi(get()) }
    factory(qualifier=null) { ireader.domain.usecases.backup.BackUpUseCases(get()) }
    factory(qualifier=null) { ireader.domain.usecases.backup.CreateBackup(get(),get(),get(),get(),get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.backup.RestoreBackup(get(),get(),get(),get(),get(),get(),get(),get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.category.CategoriesUseCases(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.category.CreateCategoryWithName(get()) }
    factory(qualifier=null) { ireader.domain.usecases.category.ReorderCategory(get()) }
    factory(qualifier=null) { ireader.domain.usecases.download.get.SubscribeDownloadsUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.epub.EpubCreator(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.epub.importer.ImportEpub(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.fonts.FontUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.history.HistoryPagingUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.history.HistoryUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.FindBooksByKey(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.SubscribeBooksByKey(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.FindBookByKey(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.GetLibraryCategory(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.SubscribeBookById(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.FindBookById(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.FindDuplicateBook(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime(get(),get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.delete_usecases.book.DeleteBookById(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook(get(),get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.InsertBook(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.UpdateBook(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.InsertBooks(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.InsertChapter(get()) }
    factory(qualifier=null) { ireader.domain.usecases.local.insert_usecases.InsertChapters(get()) }
    factory(qualifier=null) { ireader.domain.usecases.preferences.apperance.NightModePreferencesUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.preferences.reader_preferences.DohPrefUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.preferences.reader_preferences.TextReaderPrefUseCase(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.preferences.services.LastUpdateTime(get()) }
    factory(qualifier=null) { ireader.domain.usecases.reader.ScreenAlwaysOn() }
    factory(qualifier=null) { ireader.domain.usecases.remote.GetBookDetail() }
    factory(qualifier=null) { ireader.domain.usecases.remote.GetRemoteBooksUseCase() }
    factory(qualifier=null) { ireader.domain.usecases.remote.GetRemoteChapters() }
    factory(qualifier=null) { ireader.domain.usecases.remote.GetRemoteReadingContent() }
    factory(qualifier=null) { ireader.domain.usecases.services.StartDownloadServicesUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.services.StartTTSServicesUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.services.StopServiceUseCase(get()) }
    factory(qualifier=null) { ireader.domain.usecases.translate.TranslationEnginesManager(get(),get()) }
    factory(qualifier=null) { ireader.domain.catalogs.interactor.GetInstalledCatalog(get()) }
    factory(qualifier=null) { ireader.domain.catalogs.interactor.UpdateCatalog(get(),get()) }
    factory(qualifier=null) { ireader.domain.usecases.updates.SubscribeUpdates(get()) }
    factory(qualifier=null) { ireader.domain.usecases.updates.DeleteAllUpdates(get()) }
}