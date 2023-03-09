package org.ireader.app.di



import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.download.delete.DeleteAllSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownloadByBookId
import ireader.domain.usecases.download.delete.DeleteSavedDownloads
import ireader.domain.usecases.download.get.FindAllDownloadsUseCase
import ireader.domain.usecases.download.get.FindDownloadsUseCase
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.download.insert.InsertDownload
import ireader.domain.usecases.download.insert.InsertDownloads
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.*
import ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.usecases.local.book_usecases.FindBookById
import ireader.domain.usecases.local.book_usecases.SubscribeBookById
import ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks
import ireader.domain.usecases.local.chapter_usecases.*
import ireader.domain.usecases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.usecases.local.delete_usecases.book.DeleteBookById
import ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.usecases.local.insert_usecases.*
import ireader.domain.usecases.preferences.reader_preferences.*
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.remote.*
import ireader.domain.usecases.services.*
import ireader.domain.usecases.updates.DeleteAllUpdates
import ireader.domain.usecases.updates.SubscribeUpdates
import ireader.domain.usecases.updates.UpdateUseCases
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance


val UseCasesInject = DI.Module("usecaseModule") {

    bindSingleton<RemoteUseCases> { RemoteUseCases(
        getBookDetail = GetBookDetail(),
        getRemoteBooks = GetRemoteBooksUseCase(),
        getRemoteChapters = GetRemoteChapters(),
        getRemoteReadingContent = GetRemoteReadingContent(),
    ) }
    bindSingleton<ireader.domain.usecases.local.LocalInsertUseCases> {
        ireader.domain.usecases.local.LocalInsertUseCases(
            insertBook = InsertBook(instance()),
            insertBookAndChapters = InsertBookAndChapters(instance()),
            insertBooks = InsertBooks(instance()),
            insertChapter = InsertChapter(instance()),
            insertChapters = InsertChapters(instance()),
            updateBook = UpdateBook(instance())
        )
    }
    bindSingleton<ireader.domain.usecases.local.LocalGetBookUseCases> {
        ireader.domain.usecases.local.LocalGetBookUseCases(
            findAllInLibraryBooks = FindAllInLibraryBooks(instance()),
            findBookById = FindBookById(instance()),
            findBookByKey = ireader.domain.usecases.local.FindBookByKey(instance()),
            findBooksByKey = ireader.domain.usecases.local.FindBooksByKey(instance()),
            subscribeBookById = SubscribeBookById(instance()),
            subscribeBooksByKey = ireader.domain.usecases.local.SubscribeBooksByKey(instance()),
            SubscribeInLibraryBooks = SubscribeInLibraryBooks(instance()),
        )
    }
    bindSingleton<ireader.domain.usecases.local.LocalGetChapterUseCase> {
        ireader.domain.usecases.local.LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(instance()),
            findChapterById = FindChapterById(instance()),
            findChaptersByBookId = FindChaptersByBookId(instance()),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(instance()),
            updateLastReadTime = UpdateLastReadTime(
                insertUseCases = instance(),
                historyUseCase = instance(),
                uiPreferences = instance()
            ),
            subscribeChapterById = SubscribeChapterById(instance())
        )
    }
    bindSingleton<ireader.domain.usecases.local.DeleteUseCase> {
        ireader.domain.usecases.local.DeleteUseCase(
            deleteAllBook = DeleteAllBooks(instance()),
            deleteAllChapters = DeleteAllChapters(instance()),
            deleteBookById = DeleteBookById(instance()),
            deleteChapterByChapter = DeleteChapterByChapter(instance()),
            deleteChapters = DeleteChapters(instance()),
            deleteChaptersByBookId = DeleteChaptersByBookId(instance()),
            unFavoriteBook = UnFavoriteBook(
                instance(),
                bookCategoryRepository = instance(),
                instance()
            ),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(instance())
        )
    }
    bindSingleton<ServiceUseCases> { ServiceUseCases(
        startDownloadServicesUseCase = StartDownloadServicesUseCase(instance()),
        startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(instance()),
        startTTSServicesUseCase = StartTTSServicesUseCase(instance()),
        stopServicesUseCase = StopServiceUseCase(instance()),
    ) }
    bindSingleton<LibraryScreenPrefUseCases> { LibraryScreenPrefUseCases(
        libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(instance(), instance()),
        sortersDescUseCase = SortersDescUseCase(instance()),
        sortersUseCase = SortersUseCase(instance())
    ) }
    bindSingleton<ReaderPrefUseCases> { ReaderPrefUseCases(
        autoScrollMode = AutoScrollMode(instance()),
        brightnessStateUseCase = BrightnessStateUseCase(instance()),
        fontHeightUseCase = FontHeightUseCase(instance()),
        fontSizeStateUseCase = FontSizeStateUseCase(instance()),
        immersiveModeUseCase = ImmersiveModeUseCase(instance()),
        paragraphDistanceUseCase = ParagraphDistanceUseCase(instance()),
        paragraphIndentUseCase = ParagraphIndentUseCase(instance()),
        scrollIndicatorUseCase = ScrollIndicatorUseCase(instance()),
        scrollModeUseCase = ScrollModeUseCase(instance()),
    ) }
    bindSingleton<BrowseScreenPrefUseCase> { BrowseScreenPrefUseCase(
        browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(instance())
    ) }
    bindSingleton<HistoryUseCase> { HistoryUseCase(
        instance()
    ) }
    bindSingleton<UpdateUseCases> { UpdateUseCases(
        subscribeUpdates = SubscribeUpdates(instance()),
        deleteAllUpdates = DeleteAllUpdates(instance()),
    ) }
    bindProvider<EpubCreator> { EpubCreator(instance(), instance(),instance()) }
    bindSingleton<DownloadUseCases> {
        DownloadUseCases(
        deleteAllSavedDownload = DeleteAllSavedDownload(instance()),
        deleteSavedDownload = DeleteSavedDownload(instance()),
        deleteSavedDownloadByBookId = DeleteSavedDownloadByBookId(instance()),
        deleteSavedDownloads = DeleteSavedDownloads(instance()),
        findAllDownloadsUseCase = FindAllDownloadsUseCase(instance()),
        findDownloadsUseCase = FindDownloadsUseCase(instance()),
        insertDownload = InsertDownload(instance()),
        insertDownloads = InsertDownloads(instance()),
        subscribeDownloadsUseCase = SubscribeDownloadsUseCase(instance()),
    ) }
}