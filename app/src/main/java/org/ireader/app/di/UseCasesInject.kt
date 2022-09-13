package org.ireader.app.di

import android.content.Context
import ireader.core.api.db.Transactions
import ireader.core.api.http.WebViewCookieJar
import ireader.core.ui.preferences.AppPreferences
import ireader.core.ui.preferences.LibraryPreferences
import ireader.core.ui.preferences.ReaderPreferences
import ireader.core.ui.preferences.UiPreferences
import ireader.domain.data.repository.*
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
import ireader.domain.image.cache.CoverCache
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
            findChaptersByBookId = FindChaptersByBookId(chapterRepository),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(chapterRepository),
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
            uiPreferences: UiPreferences
    ): UpdateUseCases {
        return UpdateUseCases(
            subscribeUpdates = SubscribeUpdates(updatesRepository),
            deleteAllUpdates = DeleteAllUpdates(uiPreferences),
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
