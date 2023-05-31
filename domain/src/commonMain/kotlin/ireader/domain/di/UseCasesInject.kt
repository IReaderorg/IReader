package ireader.domain.di



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
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.usecases.local.book_usecases.FindBookById
import ireader.domain.usecases.local.book_usecases.SubscribeBookById
import ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks
import ireader.domain.usecases.local.chapter_usecases.FindAllInLibraryChapters
import ireader.domain.usecases.local.chapter_usecases.FindChapterById
import ireader.domain.usecases.local.chapter_usecases.FindChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.SubscribeChapterById
import ireader.domain.usecases.local.chapter_usecases.SubscribeChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime
import ireader.domain.usecases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.usecases.local.delete_usecases.book.DeleteBookById
import ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.usecases.local.insert_usecases.InsertBook
import ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters
import ireader.domain.usecases.local.insert_usecases.InsertBooks
import ireader.domain.usecases.local.insert_usecases.InsertChapter
import ireader.domain.usecases.local.insert_usecases.InsertChapters
import ireader.domain.usecases.local.insert_usecases.UpdateBook
import ireader.domain.usecases.preferences.reader_preferences.AutoScrollMode
import ireader.domain.usecases.preferences.reader_preferences.BackgroundColorUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrightnessStateUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.preferences.reader_preferences.FontHeightUseCase
import ireader.domain.usecases.preferences.reader_preferences.FontSizeStateUseCase
import ireader.domain.usecases.preferences.reader_preferences.ImmersiveModeUseCase
import ireader.domain.usecases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import ireader.domain.usecases.preferences.reader_preferences.ParagraphDistanceUseCase
import ireader.domain.usecases.preferences.reader_preferences.ParagraphIndentUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.preferences.reader_preferences.ScrollIndicatorUseCase
import ireader.domain.usecases.preferences.reader_preferences.ScrollModeUseCase
import ireader.domain.usecases.preferences.reader_preferences.SortersDescUseCase
import ireader.domain.usecases.preferences.reader_preferences.SortersUseCase
import ireader.domain.usecases.preferences.reader_preferences.TextAlignmentUseCase
import ireader.domain.usecases.preferences.reader_preferences.TextColorUseCase
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.remote.GetBookDetail
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import ireader.domain.usecases.remote.GetRemoteReadingContent
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.updates.DeleteAllUpdates
import ireader.domain.usecases.updates.SubscribeUpdates
import ireader.domain.usecases.updates.UpdateUseCases
import org.koin.dsl.module


val UseCasesInject = module {

    single<RemoteUseCases> { RemoteUseCases(
        getBookDetail = GetBookDetail(),
        getRemoteBooks = GetRemoteBooksUseCase(),
        getRemoteChapters = GetRemoteChapters(),
        getRemoteReadingContent = GetRemoteReadingContent(),
    ) }
    single<ireader.domain.usecases.local.LocalInsertUseCases> {
        ireader.domain.usecases.local.LocalInsertUseCases(
            insertBook = InsertBook(get()),
            insertBookAndChapters = InsertBookAndChapters(get()),
            insertBooks = InsertBooks(get()),
            insertChapter = InsertChapter(get()),
            insertChapters = InsertChapters(get()),
            updateBook = UpdateBook(get())
        )
    }
    single<ireader.domain.usecases.local.LocalGetBookUseCases> {
        ireader.domain.usecases.local.LocalGetBookUseCases(
            findAllInLibraryBooks = FindAllInLibraryBooks(get()),
            findBookById = FindBookById(get()),
            findBookByKey = ireader.domain.usecases.local.FindBookByKey(get()),
            findBooksByKey = ireader.domain.usecases.local.FindBooksByKey(get()),
            subscribeBookById = SubscribeBookById(get()),
            subscribeBooksByKey = ireader.domain.usecases.local.SubscribeBooksByKey(get()),
            SubscribeInLibraryBooks = SubscribeInLibraryBooks(get()),
        )
    }
    single<ireader.domain.usecases.local.LocalGetChapterUseCase> {
        ireader.domain.usecases.local.LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(get()),
            findChapterById = FindChapterById(get()),
            findChaptersByBookId = FindChaptersByBookId(get()),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(get()),
            updateLastReadTime = UpdateLastReadTime(
                insertUseCases = get(),
                historyUseCase = get(),
                uiPreferences = get()
            ),
            subscribeChapterById = SubscribeChapterById(get())
        )
    }
    single<ireader.domain.usecases.local.DeleteUseCase> {
        ireader.domain.usecases.local.DeleteUseCase(
            deleteAllBook = DeleteAllBooks(get()),
            deleteAllChapters = DeleteAllChapters(get()),
            deleteBookById = DeleteBookById(get()),
            deleteChapterByChapter = DeleteChapterByChapter(get()),
            deleteChapters = DeleteChapters(get()),
            deleteChaptersByBookId = DeleteChaptersByBookId(get()),
            unFavoriteBook = UnFavoriteBook(
                get(),
                bookCategoryRepository = get(),
                get()
            ),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(get())
        )
    }

    single<LibraryScreenPrefUseCases> { LibraryScreenPrefUseCases(
        libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(get(), get()),
        sortersDescUseCase = SortersDescUseCase(get()),
        sortersUseCase = SortersUseCase(get())
    ) }
    single<ReaderPrefUseCases> { ReaderPrefUseCases(
        autoScrollMode = AutoScrollMode(get()),
        brightnessStateUseCase = BrightnessStateUseCase(get()),
        fontHeightUseCase = FontHeightUseCase(get()),
        fontSizeStateUseCase = FontSizeStateUseCase(get()),
        immersiveModeUseCase = ImmersiveModeUseCase(get()),
        paragraphDistanceUseCase = ParagraphDistanceUseCase(get()),
        paragraphIndentUseCase = ParagraphIndentUseCase(get()),
        scrollIndicatorUseCase = ScrollIndicatorUseCase(get()),
        scrollModeUseCase = ScrollModeUseCase(get()),
        textColorUseCase = TextColorUseCase(get()),
        textAlignmentUseCase = TextAlignmentUseCase(get()),
        backgroundColorUseCase = BackgroundColorUseCase(get())
    ) }
    single<BrowseScreenPrefUseCase> { BrowseScreenPrefUseCase(
        browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(get())
    ) }
    single<HistoryUseCase> { HistoryUseCase(
        get()
    ) }
    single<UpdateUseCases> { UpdateUseCases(
        subscribeUpdates = SubscribeUpdates(get()),
        deleteAllUpdates = DeleteAllUpdates(get()),
    ) }

    single<DownloadUseCases> {
        DownloadUseCases(
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