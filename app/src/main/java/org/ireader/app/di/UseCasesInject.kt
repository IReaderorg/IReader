package org.ireader.app.di

import android.content.Context
import ireader.common.data.repository.*
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
import ireader.domain.use_cases.local.*
import ireader.domain.use_cases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.use_cases.local.book_usecases.FindBookById
import ireader.domain.use_cases.local.book_usecases.SubscribeBookById
import ireader.domain.use_cases.local.book_usecases.SubscribeInLibraryBooks
import ireader.domain.use_cases.local.chapter_usecases.*
import ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.use_cases.local.insert_usecases.*
import ireader.domain.use_cases.preferences.reader_preferences.*
import ireader.domain.use_cases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.use_cases.remote.*
import ireader.domain.use_cases.services.*
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
        bookCategoryRepository: BookCategoryRepository,
        transactions: Transactions
    ): DeleteUseCase {
        return DeleteUseCase(
            deleteAllBook = DeleteAllBooks(bookRepository),
            deleteAllChapters = DeleteAllChapters(chapterRepository),
            deleteBookById = DeleteBookById(bookRepository),
            deleteChapterByChapter = DeleteChapterByChapter(chapterRepository),
            deleteChapters = DeleteChapters(chapterRepository),
            deleteChaptersByBookId = DeleteChaptersByBookId(chapterRepository),
            unFavoriteBook = UnFavoriteBook(bookRepository, bookCategoryRepository = bookCategoryRepository, transactions),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(bookRepository)
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
