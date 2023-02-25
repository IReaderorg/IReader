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
import org.koin.dsl.module


val UseCasesInject = module {

    single<RemoteUseCases>(qualifier=null) { RemoteUseCases(
        getBookDetail = GetBookDetail(),
        getRemoteBooks = GetRemoteBooksUseCase(),
        getRemoteChapters = GetRemoteChapters(),
        getRemoteReadingContent = GetRemoteReadingContent(),
    ) }
    single<LocalInsertUseCases>(qualifier=null) { LocalInsertUseCases(
        insertBook = InsertBook(get()),
        insertBookAndChapters = InsertBookAndChapters(get()),
        insertBooks = InsertBooks(get()),
        insertChapter = InsertChapter(get()),
        insertChapters = InsertChapters(get()),
        updateBook = UpdateBook(get())
    ) }
    single<LocalGetBookUseCases>(qualifier=null) { LocalGetBookUseCases(
        findAllInLibraryBooks = FindAllInLibraryBooks(get()),
        findBookById = FindBookById(get()),
        findBookByKey = FindBookByKey(get()),
        findBooksByKey = FindBooksByKey(get()),
        subscribeBookById = SubscribeBookById(get()),
        subscribeBooksByKey = SubscribeBooksByKey(get()),
        SubscribeInLibraryBooks = SubscribeInLibraryBooks(get()),
    ) }
    single<LocalGetChapterUseCase>(qualifier=null) { LocalGetChapterUseCase(
        findAllInLibraryChapters = FindAllInLibraryChapters(get()),
        findChapterById = FindChapterById(get()),
        findChaptersByBookId = FindChaptersByBookId(get()),
        subscribeChaptersByBookId = SubscribeChaptersByBookId(get()),
        updateLastReadTime = UpdateLastReadTime(insertUseCases = get(), historyUseCase = get(), uiPreferences = get()),
        subscribeChapterById = SubscribeChapterById(get())
    ) }
    single<DeleteUseCase>(qualifier=null) { DeleteUseCase(
        deleteAllBook = DeleteAllBooks(get()),
        deleteAllChapters = DeleteAllChapters(get()),
        deleteBookById = DeleteBookById(get()),
        deleteChapterByChapter = DeleteChapterByChapter(get()),
        deleteChapters = DeleteChapters(get()),
        deleteChaptersByBookId = DeleteChaptersByBookId(get()),
        unFavoriteBook = UnFavoriteBook(get(), bookCategoryRepository = get(), get()),
        deleteNotInLibraryBooks = DeleteNotInLibraryBooks(get())
    ) }
    single<ServiceUseCases>(qualifier=null) { ServiceUseCases(
        startDownloadServicesUseCase = StartDownloadServicesUseCase(get()),
        startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(get()),
        startTTSServicesUseCase = StartTTSServicesUseCase(get()),
        stopServicesUseCase = StopServiceUseCase(get()),
    ) }
    single<LibraryScreenPrefUseCases>(qualifier=null) { LibraryScreenPrefUseCases(
        libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(get(), get()),
        sortersDescUseCase = SortersDescUseCase(get()),
        sortersUseCase = SortersUseCase(get())
    ) }
    single<ReaderPrefUseCases>(qualifier=null) { ReaderPrefUseCases(
        autoScrollMode = AutoScrollMode(get()),
        backgroundColorUseCase = BackgroundColorUseCase(get()),
        brightnessStateUseCase = BrightnessStateUseCase(get()),
        fontHeightUseCase = FontHeightUseCase(get()),
        fontSizeStateUseCase = FontSizeStateUseCase(get()),
        immersiveModeUseCase = ImmersiveModeUseCase(get()),
        paragraphDistanceUseCase = ParagraphDistanceUseCase(get()),
        paragraphIndentUseCase = ParagraphIndentUseCase(get()),
        scrollIndicatorUseCase = ScrollIndicatorUseCase(get()),
        scrollModeUseCase = ScrollModeUseCase(get()),
        selectedFontStateUseCase = SelectedFontStateUseCase(get(),get()),
        textAlignmentUseCase = TextAlignmentUseCase(get()),
        textColorUseCase = TextColorUseCase(get())
    ) }
    single<BrowseScreenPrefUseCase>(qualifier=null) { BrowseScreenPrefUseCase(
        browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(get())
    ) }
    single<HistoryUseCase>(qualifier=null) { HistoryUseCase(
        get()
    ) }
    single<UpdateUseCases>(qualifier=null) { UpdateUseCases(
        subscribeUpdates = SubscribeUpdates(get()),
        deleteAllUpdates = DeleteAllUpdates(get()),
    ) }
    factory<EpubCreator>(qualifier=null) { EpubCreator(get(), get()) }
    single<DownloadUseCases>(qualifier=null) {DownloadUseCases(
        deleteAllSavedDownload = DeleteAllSavedDownload(get()),
        deleteSavedDownload = DeleteSavedDownload(get()),
        deleteSavedDownloadByBookId = DeleteSavedDownloadByBookId(get()),
        deleteSavedDownloads = DeleteSavedDownloads(get()),
        findAllDownloadsUseCase = FindAllDownloadsUseCase(get()),
        findDownloadsUseCase = FindDownloadsUseCase(get()),
        insertDownload = InsertDownload(get()),
        insertDownloads = InsertDownloads(get()),
        subscribeDownloadsUseCase = SubscribeDownloadsUseCase(get()),
    ) }
}