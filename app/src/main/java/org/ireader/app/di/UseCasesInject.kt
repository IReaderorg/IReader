package org.ireader.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_data.repository.BookRepository
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_data.repository.ChapterRepository
import org.ireader.common_data.repository.DownloadRepository
import org.ireader.common_data.repository.HistoryRepository
import org.ireader.common_data.repository.RemoteKeyRepository
import org.ireader.common_data.repository.UpdatesRepository
import org.ireader.core_api.db.Transactions
import org.ireader.core_api.http.WebViewCookieJar
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.core_ui.preferences.LibraryPreferences
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloads
import org.ireader.domain.use_cases.download.get.FindAllDownloadsUseCase
import org.ireader.domain.use_cases.download.get.FindDownloadsUseCase
import org.ireader.domain.use_cases.download.get.SubscribeDownloadsUseCase
import org.ireader.domain.use_cases.download.insert.InsertDownload
import org.ireader.domain.use_cases.download.insert.InsertDownloads
import org.ireader.domain.use_cases.epub.EpubCreator
import org.ireader.domain.use_cases.history.HistoryUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.FindBookByKey
import org.ireader.domain.use_cases.local.FindBooksByKey
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.SubscribeBooksByKey
import org.ireader.domain.use_cases.local.book_usecases.FindAllInLibraryBooks
import org.ireader.domain.use_cases.local.book_usecases.FindBookById
import org.ireader.domain.use_cases.local.book_usecases.SubscribeBookById
import org.ireader.domain.use_cases.local.book_usecases.SubscribeInLibraryBooks
import org.ireader.domain.use_cases.local.chapter_usecases.FindAllInLibraryChapters
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindFirstChapter
import org.ireader.domain.use_cases.local.chapter_usecases.FindLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.UpdateLastReadTime
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.UnFavoriteBook
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBookAndChapters
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters
import org.ireader.domain.use_cases.local.insert_usecases.UpdateBook
import org.ireader.domain.use_cases.preferences.reader_preferences.AutoScrollMode
import org.ireader.domain.use_cases.preferences.reader_preferences.BackgroundColorUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.BrightnessStateUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.BrowseScreenPrefUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.FontHeightUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.FontSizeStateUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ImmersiveModeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ParagraphDistanceUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ParagraphIndentUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ScrollIndicatorUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ScrollModeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SelectedFontStateUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersDescUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.TextAlignmentUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.TextColorUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import org.ireader.domain.use_cases.remote.GetBookDetail
import org.ireader.domain.use_cases.remote.GetRemoteBooksUseCase
import org.ireader.domain.use_cases.remote.GetRemoteChapters
import org.ireader.domain.use_cases.remote.GetRemoteReadingContent
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.remote.key.ClearExploreMode
import org.ireader.domain.use_cases.remote.key.DeleteAllExploredBook
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import org.ireader.domain.use_cases.remote.key.FindAllPagedExploreBooks
import org.ireader.domain.use_cases.remote.key.InsertAllExploredBook
import org.ireader.domain.use_cases.remote.key.InsertAllRemoteKeys
import org.ireader.domain.use_cases.remote.key.PrepareExploreMode
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import org.ireader.domain.use_cases.remote.key.SubScribeAllPagedExploreBooks
import org.ireader.domain.use_cases.services.ServiceUseCases
import org.ireader.domain.use_cases.services.StartDownloadServicesUseCase
import org.ireader.domain.use_cases.services.StartLibraryUpdateServicesUseCase
import org.ireader.domain.use_cases.services.StartTTSServicesUseCase
import org.ireader.domain.use_cases.services.StopServiceUseCase
import org.ireader.domain.use_cases.updates.DeleteAllUpdates
import org.ireader.domain.use_cases.updates.DeleteUpdates
import org.ireader.domain.use_cases.updates.SubscribeUpdates
import org.ireader.domain.use_cases.updates.UpdateUseCases
import org.ireader.image_loader.coil.cache.CoverCache
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UseCasesInject {


    @Provides
    @Singleton
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
    @Provides
    @Singleton
    fun provideLocalInsertUseCases(
       chapterRepository: org.ireader.common_data.repository.ChapterRepository,
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


    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun provideLocalChapterUseCase(
        chapterRepository: org.ireader.common_data.repository.ChapterRepository,
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
    @Provides
    @Singleton
    fun provideDeleteUseCase(
        chapterRepository: org.ireader.common_data.repository.ChapterRepository,
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
            unFavoriteBook = UnFavoriteBook(bookRepository, bookCategoryRepository = bookCategoryRepository,transactions),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(bookRepository)
        )
    }

    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun providesServiceUseCases(
      @ApplicationContext context: Context
    ): ServiceUseCases {
        return ServiceUseCases(
            startDownloadServicesUseCase = StartDownloadServicesUseCase(context),
            startLibraryUpdateServicesUseCase = StartLibraryUpdateServicesUseCase(context),
            startTTSServicesUseCase = StartTTSServicesUseCase(context),
            stopServicesUseCase = StopServiceUseCase(context),
        )
    }
    @Provides
    @Singleton
    fun providesLibraryScreenPrefUseCases(
        appPreferences: AppPreferences,
        libraryPreferences: LibraryPreferences,
        categoryRepository: CategoryRepository
    ): LibraryScreenPrefUseCases {
        return LibraryScreenPrefUseCases(
          libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(libraryPreferences,categoryRepository),
            sortersDescUseCase = SortersDescUseCase(appPreferences),
            sortersUseCase = SortersUseCase(appPreferences)
        )
    }
    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun providesBrowseScreenPrefUseCase(
        appPreferences: AppPreferences
    ): BrowseScreenPrefUseCase {
        return BrowseScreenPrefUseCase(
            browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(appPreferences)
        )
    }
    @Provides
    @Singleton
    fun providesHistoryUseCase(
        historyRepository: HistoryRepository
    ): HistoryUseCase {
        return HistoryUseCase(
            historyRepository
        )
    }

    @Provides
    @Singleton
    fun providesUpdateUseCases(
        updatesRepository: UpdatesRepository,
    ): UpdateUseCases {
        return UpdateUseCases(
            subscribeUpdates = SubscribeUpdates(updatesRepository),
            deleteAllUpdates = DeleteAllUpdates(updatesRepository),
            deleteUpdates = DeleteUpdates(updatesRepository),
        )
    }




    @Provides
    fun providesEpubCreator(
      coverCache: CoverCache,
        chapterRepository: ChapterRepository
    ): EpubCreator {
        return EpubCreator(coverCache, chapterRepository)
    }

    @Provides
    @Singleton
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