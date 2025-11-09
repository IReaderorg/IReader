package ireader.domain.di

import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.backup.CloudBackupManager
import ireader.domain.usecases.backup.CloudProvider
import ireader.domain.usecases.backup.CloudStorageProvider
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.DropboxProvider
import ireader.domain.usecases.backup.GoogleDriveProvider
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.fonts.FontUseCase
import ireader.domain.usecases.preferences.apperance.NightModePreferencesUseCase
import ireader.domain.usecases.preferences.reader_preferences.DohPrefUseCase
import ireader.domain.usecases.preferences.services.LastUpdateTime
import ireader.domain.usecases.remote.GetBookDetail
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import ireader.domain.usecases.remote.GetRemoteReadingContent
import ireader.domain.usecases.translate.TranslationEnginesManager
import org.koin.dsl.module


val DomainServices = module {

    single<DownloadServiceStateImpl> { ireader.domain.services.downloaderService.DownloadServiceStateImpl() }


    single { ireader.domain.preferences.prefs.PlayerPreferences(get()) }
    single { ireader.domain.preferences.prefs.DownloadPreferences(get()) }




    factory  {
        CreateBackup(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory  {
        RestoreBackup(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    
    // Cloud Backup Providers
    single { DropboxProvider() }
    single { GoogleDriveProvider() }
    
    single {
        CloudBackupManager(
            providers = mapOf(
                CloudProvider.DROPBOX to get<DropboxProvider>(),
                CloudProvider.GOOGLE_DRIVE to get<GoogleDriveProvider>()
            )
        )
    }
    factory  { CategoriesUseCases(get(), get(), get(), get()) }
    factory  { CreateCategoryWithName(get()) }
    factory  { ReorderCategory(get()) }
    factory  { SubscribeDownloadsUseCase(get()) }


    factory  { FontUseCase(get()) }
    factory  { ireader.domain.usecases.fonts.FontManagementUseCase(get()) }


    factory  { ireader.domain.usecases.local.FindBooksByKey(get()) }
    factory  { ireader.domain.usecases.local.SubscribeBooksByKey(get()) }
    factory  { ireader.domain.usecases.local.FindBookByKey(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.GetLibraryCategory(get(), get()) }
    factory  {
        ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase(
            get(),
            get()
        )
    }
    factory  { ireader.domain.usecases.local.book_usecases.SubscribeBookById(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindBookById(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindDuplicateBook(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks(get()) }
    factory  {
        ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime(
            get(),
            get(),
            get()
        )
    }
    factory  { ireader.domain.usecases.local.delete_usecases.book.DeleteBookById(get()) }
    factory  {
        ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook(
            get(),
            get(),
            get()
        )
    }
    factory  {
        ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks(
            get()
        )
    }
    factory  {
        ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId(
            get()
        )
    }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBook(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.UpdateBook(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBooks(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertChapter(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertChapters(get()) }
    factory  {
        NightModePreferencesUseCase(
            get()
        )
    }
    factory  { DohPrefUseCase(get()) }

    factory  { LastUpdateTime(get()) }

    factory  { GetBookDetail() }
    factory  { GetRemoteBooksUseCase() }
    factory  { GetRemoteChapters() }
    factory  { GetRemoteReadingContent() }
    factory  { ireader.domain.usecases.remote.GlobalSearchUseCase(get()) }

    factory  {
        TranslationEnginesManager(
            get(),
            get()
        )
    }
    factory  { ireader.domain.catalogs.interactor.GetInstalledCatalog(get()) }

    factory  { ireader.domain.usecases.updates.SubscribeUpdates(get()) }
    factory  { ireader.domain.usecases.updates.DeleteAllUpdates(get()) }


}