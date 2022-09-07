package org.ireader.app.di

import android.content.Context
import ireader.common.data.repository.BookCategoryRepository
import ireader.common.data.repository.BookRepository
import ireader.common.data.repository.CategoryRepository
import ireader.common.data.repository.ChapterRepository
import ireader.common.data.repository.DownloadRepository
import ireader.common.data.repository.HistoryRepository
import ireader.common.data.repository.RemoteKeyRepository
import ireader.common.data.repository.UpdatesRepository
import ireader.core.api.db.Transactions
import ireader.core.api.http.WebViewCookieJar
import ireader.core.ui.preferences.AppPreferences
import ireader.core.ui.preferences.LibraryPreferences
import ireader.core.ui.preferences.ReaderPreferences
import ireader.core.ui.preferences.UiPreferences
import ireader.domain.use_cases.download.DownloadUseCases
import ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import ireader.domain.use_cases.download.delete.DeleteSavedDownload
import ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import ireader.domain.use_cases.download.delete.DeleteSavedDownloads
import ireader.domain.use_cases.download.get.FindAllDownloadsUseCase
import ireader.domain.use_cases.download.get.FindDownloadsUseCase
import ireader.domain.use_cases.download.get.SubscribeDownloadsUseCase
import ireader.domain.use_cases.download.insert.InsertDownload
import ireader.domain.use_cases.download.insert.InsertDownloads
import ireader.domain.use_cases.epub.EpubCreator
import ireader.domain.use_cases.history.HistoryUseCase
import ireader.domain.use_cases.local.DeleteUseCase
import ireader.domain.use_cases.local.FindBookByKey
import ireader.domain.use_cases.local.FindBooksByKey
import ireader.domain.use_cases.local.LocalGetBookUseCases
import ireader.domain.use_cases.local.LocalGetChapterUseCase
import ireader.domain.use_cases.local.LocalInsertUseCases
import ireader.domain.use_cases.local.SubscribeBooksByKey
import ireader.domain.use_cases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.use_cases.local.book_usecases.FindBookById
import ireader.domain.use_cases.local.book_usecases.SubscribeBookById
import ireader.domain.use_cases.local.book_usecases.SubscribeInLibraryBooks
import ireader.domain.use_cases.local.chapter_usecases.FindAllInLibraryChapters
import ireader.domain.use_cases.local.chapter_usecases.FindChapterById
import ireader.domain.use_cases.local.chapter_usecases.FindChapterByKey
import ireader.domain.use_cases.local.chapter_usecases.FindChaptersByBookId
import ireader.domain.use_cases.local.chapter_usecases.FindChaptersByKey
import ireader.domain.use_cases.local.chapter_usecases.FindFirstChapter
import ireader.domain.use_cases.local.chapter_usecases.FindLastReadChapter
import ireader.domain.use_cases.local.chapter_usecases.SubscribeChapterById
import ireader.domain.use_cases.local.chapter_usecases.SubscribeChaptersByBookId
import ireader.domain.use_cases.local.chapter_usecases.SubscribeLastReadChapter
import ireader.domain.use_cases.local.chapter_usecases.UpdateLastReadTime
import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.use_cases.local.insert_usecases.InsertBook
import ireader.domain.use_cases.local.insert_usecases.InsertBookAndChapters
import ireader.domain.use_cases.local.insert_usecases.InsertBooks
import ireader.domain.use_cases.local.insert_usecases.InsertChapter
import ireader.domain.use_cases.local.insert_usecases.InsertChapters
import ireader.domain.use_cases.local.insert_usecases.UpdateBook
import ireader.domain.use_cases.preferences.reader_preferences.AutoScrollMode
import ireader.domain.use_cases.preferences.reader_preferences.BackgroundColorUseCase
import ireader.domain.use_cases.preferences.reader_preferences.BrightnessStateUseCase
import ireader.domain.use_cases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import ireader.domain.use_cases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.use_cases.preferences.reader_preferences.FontHeightUseCase
import ireader.domain.use_cases.preferences.reader_preferences.FontSizeStateUseCase
import ireader.domain.use_cases.preferences.reader_preferences.ImmersiveModeUseCase
import ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import ireader.domain.use_cases.preferences.reader_preferences.ParagraphDistanceUseCase
import ireader.domain.use_cases.preferences.reader_preferences.ParagraphIndentUseCase
import ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.use_cases.preferences.reader_preferences.ScrollIndicatorUseCase
import ireader.domain.use_cases.preferences.reader_preferences.ScrollModeUseCase
import ireader.domain.use_cases.preferences.reader_preferences.SelectedFontStateUseCase
import ireader.domain.use_cases.preferences.reader_preferences.SortersDescUseCase
import ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase
import ireader.domain.use_cases.preferences.reader_preferences.TextAlignmentUseCase
import ireader.domain.use_cases.preferences.reader_preferences.TextColorUseCase
import ireader.domain.use_cases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.use_cases.remote.GetBookDetail
import ireader.domain.use_cases.remote.GetRemoteBooksUseCase
import ireader.domain.use_cases.remote.GetRemoteChapters
import ireader.domain.use_cases.remote.GetRemoteReadingContent
import ireader.domain.use_cases.remote.RemoteUseCases
import ireader.domain.use_cases.remote.key.ClearExploreMode
import ireader.domain.use_cases.remote.key.DeleteAllExploredBook
import ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import ireader.domain.use_cases.remote.key.FindAllPagedExploreBooks
import ireader.domain.use_cases.remote.key.InsertAllExploredBook
import ireader.domain.use_cases.remote.key.InsertAllRemoteKeys
import ireader.domain.use_cases.remote.key.PrepareExploreMode
import ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import ireader.domain.use_cases.remote.key.SubScribeAllPagedExploreBooks
import ireader.domain.use_cases.services.ServiceUseCases
import ireader.domain.use_cases.services.StartDownloadServicesUseCase
import ireader.domain.use_cases.services.StartLibraryUpdateServicesUseCase
import ireader.domain.use_cases.services.StartTTSServicesUseCase
import ireader.domain.use_cases.services.StopServiceUseCase
import ireader.domain.use_cases.updates.DeleteAllUpdates
import ireader.domain.use_cases.updates.DeleteUpdates
import ireader.domain.use_cases.updates.SubscribeUpdates
import ireader.domain.use_cases.updates.UpdateUseCases
import ireader.ui.imageloader.coil.cache.CoverCache
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.UseCasesInject")
class UseCasesInject {


        @Single
    fun provideRemoteUseCase(
        webViewCookieJar: WebViewCookieJar
    ): RemoteUseCases {
        return RemoteUseCases(
            getBookDetail = GetBookDetail(),
            getRemoteBooks = GetRemoteBooksUseCase(),
            getRemoteChapters = GetRemoteChapters(),
            getRemoteReadingContent = GetRemoteReadingContent(),
        )
    }

        @Single
    fun provideLocalInsertUseCases(
        chapterRepository: ChapterRepository,
        bookRepository: BookRepository
    ): LocalInsertUseCases {
        return LocalInsertUseCases(
            insertBook = InsertBook(bookRepository),
            insertBookAndChapters = InsertBookAndChapters(bookRepository),
            insertBooks = InsertBooks(bookRepository),
            insertChapter = InsertChapter(chapterRepository),
            insertChapters = InsertChapters(chapterRepository),
            updateBook = UpdateBook(bookRepository)
        )
    }


        @Single
    fun provideLocalGetBookUseCases(
        bookRepository: BookRepository
    ): LocalGetBookUseCases {
        return LocalGetBookUseCases(
            findAllInLibraryBooks = FindAllInLibraryBooks(bookRepository),
            findBookById = FindBookById(bookRepository),
            findBookByKey = FindBookByKey(bookRepository),
            findBooksByKey = FindBooksByKey(bookRepository),
            subscribeBookById = SubscribeBookById(bookRepository),
            subscribeBooksByKey = SubscribeBooksByKey(bookRepository),
            SubscribeInLibraryBooks = SubscribeInLibraryBooks(bookRepository),
        )
    }


        @Single
    fun provideLocalChapterUseCase(
        chapterRepository: ChapterRepository,
        historyUseCase: HistoryUseCase,
        insertUseCases: LocalInsertUseCases,
        uiPreferences: UiPreferences
    ): LocalGetChapterUseCase {
        return LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(chapterRepository),
            findChapterById = FindChapterById(chapterRepository),
            findChapterByKey = FindChapterByKey(chapterRepository),
            findChaptersByBookId = FindChaptersByBookId(chapterRepository),
            findChaptersByKey = FindChaptersByKey(chapterRepository),
            findFirstChapter = FindFirstChapter(chapterRepository),
            findLastReadChapter = FindLastReadChapter(chapterRepository),
            subscribeChapterById = SubscribeChapterById(chapterRepository),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(chapterRepository),
            subscribeLastReadChapter = SubscribeLastReadChapter(chapterRepository),
            updateLastReadTime = UpdateLastReadTime(insertUseCases = insertUseCases, historyUseCase = historyUseCase, uiPreferences = uiPreferences)
        )
    }

        @Single
    fun provideDeleteUseCase(
        chapterRepository: ChapterRepository,
        bookRepository: BookRepository,
        remoteKeyRepository: RemoteKeyRepository,
        bookCategoryRepository: BookCategoryRepository,
        transactions: Transactions
    ): DeleteUseCase {
        return DeleteUseCase(
            deleteAllBook = DeleteAllBooks(bookRepository),
            deleteAllChapters = DeleteAllChapters(chapterRepository),
            deleteAllExploreBook = DeleteAllExploreBook(bookRepository),
            deleteAllRemoteKeys = DeleteAllRemoteKeys(remoteKeyRepository),
            deleteBookById = DeleteBookById(bookRepository),
            deleteBooks = DeleteBooks(bookRepository),
            deleteChapterByChapter = DeleteChapterByChapter(chapterRepository),
            deleteChapters = DeleteChapters(chapterRepository),
            deleteChaptersByBookId = DeleteChaptersByBookId(chapterRepository),
            unFavoriteBook = UnFavoriteBook(bookRepository, bookCategoryRepository = bookCategoryRepository, transactions),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(bookRepository)
        )
    }


        @Single
    fun providesRemoteKeyUseCase(
        remoteKeyRepository: RemoteKeyRepository
    ): RemoteKeyUseCase {
        return RemoteKeyUseCase(
            deleteAllExploredBook = DeleteAllExploredBook(remoteKeyRepository),
            clearExploreMode = ClearExploreMode(remoteKeyRepository),
            deleteAllRemoteKeys = DeleteAllRemoteKeys(remoteKeyRepository),
            findAllPagedExploreBooks = FindAllPagedExploreBooks(remoteKeyRepository),
            insertAllExploredBook = InsertAllExploredBook(remoteKeyRepository),
            insertAllRemoteKeys = InsertAllRemoteKeys(remoteKeyRepository),
            prepareExploreMode = PrepareExploreMode(remoteKeyRepository),
            subScribeAllPagedExploreBooks = SubScribeAllPagedExploreBooks(remoteKeyRepository),
        )
    }


        @Single
    fun providesServiceUseCases(
         context: Context
    ): ServiceUseCases {
        return ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(context),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(context),
            startTTSServicesUseCase = StartTTSServicesUseCase(context),
            stopServicesUseCase = StopServiceUseCase(context),
        )
    }

        @Single
    fun providesLibraryScreenPrefUseCases(
        appPreferences: AppPreferences,
        libraryPreferences: LibraryPreferences,
        categoryRepository: CategoryRepository
    ): LibraryScreenPrefUseCases {
        return LibraryScreenPrefUseCases(
            libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(libraryPreferences, categoryRepository),
            sortersDescUseCase = SortersDescUseCase(appPreferences),
            sortersUseCase = SortersUseCase(appPreferences)
        )
    }

        @Single
    fun providesReaderPrefUseCases(
        prefs: ReaderPreferences
    ): ReaderPrefUseCases {
        return ReaderPrefUseCases(
            autoScrollMode = AutoScrollMode(prefs),
            backgroundColorUseCase = BackgroundColorUseCase(prefs),
            brightnessStateUseCase = BrightnessStateUseCase(prefs),
            fontHeightUseCase = FontHeightUseCase(prefs),
            fontSizeStateUseCase = FontSizeStateUseCase(prefs),
            immersiveModeUseCase = ImmersiveModeUseCase(prefs),
            paragraphDistanceUseCase = ParagraphDistanceUseCase(prefs),
            paragraphIndentUseCase = ParagraphIndentUseCase(prefs),
            scrollIndicatorUseCase = ScrollIndicatorUseCase(prefs),
            scrollModeUseCase = ScrollModeUseCase(prefs),
            selectedFontStateUseCase = SelectedFontStateUseCase(prefs),
            textAlignmentUseCase = TextAlignmentUseCase(prefs),
            textColorUseCase = TextColorUseCase(prefs)
        )
    }


        @Single
    fun providesBrowseScreenPrefUseCase(
        appPreferences: AppPreferences
    ): BrowseScreenPrefUseCase {
        return BrowseScreenPrefUseCase(
            browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(appPreferences)
        )
    }

        @Single
    fun providesHistoryUseCase(
        historyRepository: HistoryRepository
    ): HistoryUseCase {
        return HistoryUseCase(
            historyRepository
        )
    }


        @Single
    fun providesUpdateUseCases(
        updatesRepository: UpdatesRepository,
    ): UpdateUseCases {
        return UpdateUseCases(
            subscribeUpdates = SubscribeUpdates(updatesRepository),
            deleteAllUpdates = DeleteAllUpdates(updatesRepository),
            deleteUpdates = DeleteUpdates(updatesRepository),
        )
    }

    @Factory
    fun providesEpubCreator(
        coverCache: CoverCache,
        chapterRepository: ChapterRepository
    ): EpubCreator {
        return EpubCreator(coverCache, chapterRepository)
    }


        @Single
    fun providesDownloadUseCases(
        downloadRepository: DownloadRepository
    ): DownloadUseCases {
        return DownloadUseCases(
            deleteAllSavedDownload = DeleteAllSavedDownload(downloadRepository),
            deleteSavedDownload = DeleteSavedDownload(downloadRepository),
            deleteSavedDownloadByBookId = DeleteSavedDownloadByBookId(downloadRepository),
            deleteSavedDownloads = DeleteSavedDownloads(downloadRepository),
            findAllDownloadsUseCase = FindAllDownloadsUseCase(downloadRepository),
            findDownloadsUseCase = FindDownloadsUseCase(downloadRepository),
            insertDownload = InsertDownload(downloadRepository),
            insertDownloads = InsertDownloads(downloadRepository),
            subscribeDownloadsUseCase = SubscribeDownloadsUseCase(downloadRepository),
        )
    }
}
