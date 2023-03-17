package ireader.domain.di

import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.backup.CreateBackup
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
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val DomainServices = DI.Module("domainModule") {

    bindSingleton<DownloadServiceStateImpl> { ireader.domain.services.downloaderService.DownloadServiceStateImpl() }


    bindSingleton { ireader.domain.preferences.prefs.PlayerPreferences(instance()) }




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


    bindProvider { FontUseCase(instance()) }


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

    bindProvider { LastUpdateTime(instance()) }

    bindProvider { GetBookDetail() }
    bindProvider { GetRemoteBooksUseCase() }
    bindProvider { GetRemoteChapters() }
    bindProvider { GetRemoteReadingContent() }

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