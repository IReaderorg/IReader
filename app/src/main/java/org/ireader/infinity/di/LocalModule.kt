package org.ireader.infinity.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.core.prefs.AndroidPreferenceStore
import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.UiPreferences
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.MIGRATION_8_9
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryChapterDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.repository.DownloadRepositoryImpl
import org.ireader.domain.feature_services.io.LibraryCovers
import org.ireader.domain.repository.DownloadRepository
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.repository.RemoteKeyRepository
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownload
import org.ireader.domain.use_cases.download.delete.DeleteSavedDownloadByBookId
import org.ireader.domain.use_cases.download.get.GetAllDownloadsUseCase
import org.ireader.domain.use_cases.download.get.GetAllDownloadsUseCaseByPaging
import org.ireader.domain.use_cases.download.get.GetOneSavedDownload
import org.ireader.domain.use_cases.download.insert.InsertDownload
import org.ireader.domain.use_cases.download.insert.InsertDownloads
import org.ireader.domain.use_cases.local.*
import org.ireader.domain.use_cases.local.book_usecases.*
import org.ireader.domain.use_cases.local.chapter_usecases.*
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteNotInLibraryBook
import org.ireader.domain.use_cases.local.delete_usecases.chapter.*
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import org.ireader.infinity.core.domain.use_cases.local.book_usecases.GetBooksByQueryPagingSource
import sources.Dependencies
import tachiyomi.core.prefs.PreferenceStore
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {


    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext appContext: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_8_9)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesBookDao(db: AppDatabase): LibraryBookDao {
        return db.libraryBookDao
    }


    @Provides
    @Singleton
    fun provideChapterDao(db: AppDatabase): LibraryChapterDao {
        return db.libraryChapterDao
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao {
        return db.remoteKeysDao
    }



    @Singleton
    @Provides
    fun provideDependencies(
        httpClients: tachiyomi.core.http.HttpClients,
        preferences: PreferenceStore,
    ): Dependencies {
        return Dependencies(httpClients = httpClients,
            preferences = preferences)
    }

    @Singleton
    @Provides
    fun providesDeleteUseCase(
        localBookRepository: LocalBookRepository,
        localChapterRepository: LocalChapterRepository,
        remoteKeyRepository: RemoteKeyRepository,
    ): DeleteUseCase {
        return DeleteUseCase(
            deleteAllBook = DeleteAllBooks(localBookRepository),
            deleteAllExploreBook = DeleteAllExploreBook(localBookRepository),
            deleteBookById = DeleteBookById(localBookRepository),
            deleteNotInLibraryBook = DeleteNotInLibraryBook(localBookRepository),
            deleteChapterByChapter = DeleteChapterByChapter(localChapterRepository),
            deleteChaptersByBookId = DeleteChaptersByBookId(localChapterRepository),
            deleteNotInLibraryChapters = DeleteNotInLibraryChapters(localChapterRepository),
            deleteAllChapters = DeleteAllChapters(localChapterRepository),
            deleteChapters = DeleteChapters(localChapterRepository = localChapterRepository),
            deleteAllRemoteKeys = DeleteAllRemoteKeys(remoteKeyRepository = remoteKeyRepository)
        )
    }

    @Singleton
    @Provides
    fun provideDownloadUseCase(
        downloadRepository: DownloadRepository,
    ): DownloadUseCases {
        return DownloadUseCases(
            deleteAllSavedDownload = DeleteAllSavedDownload(downloadRepository),
            deleteSavedDownload = DeleteSavedDownload(downloadRepository),
            deleteSavedDownloadByBookId = DeleteSavedDownloadByBookId(downloadRepository),
            getAllDownloadsUseCase = GetAllDownloadsUseCase(downloadRepository),
            getOneSavedDownload = GetOneSavedDownload(downloadRepository),
            insertDownload = InsertDownload(downloadRepository),
            insertDownloads = InsertDownloads(downloadRepository),
            getAllDownloadsUseCaseByPaging = GetAllDownloadsUseCaseByPaging(downloadRepository)
        )
    }

    @Singleton
    @Provides
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao)
    }

    @Singleton
    @Provides
    fun provideDownloadDao(
        database: AppDatabase,
    ): DownloadDao {
        return database.downloadDao
    }


    @Singleton
    @Provides
    fun providesInsertUseCase(
        localBookRepository: LocalBookRepository,
        localChapterRepository: LocalChapterRepository,
    ): LocalInsertUseCases {
        return LocalInsertUseCases(
            insertBook = InsertBook(localBookRepository),
            insertBooks = InsertBooks(localBookRepository),
            insertChapter = InsertChapter(localChapterRepository),
            insertChapters = InsertChapters(localChapterRepository),
        )
    }

    @Singleton
    @Provides
    fun providesGetBookUseCase(
        localBookRepository: LocalBookRepository,
    ): LocalGetBookUseCases {
        return LocalGetBookUseCases(
            subscribeAllInLibraryBooks = SubscribeAllInLibraryBooks(
                localBookRepository),
            getAllExploredBookPagingSource = GetAllExploredBookPagingSource(localBookRepository),
            getAllInLibraryPagingSource = GetAllInLibraryPagingSource(
                localBookRepository),
            subscribeBookById = SubscribeBookById(localBookRepository),
            getBooksByQueryByPagination = GetBooksByQueryByPagination(localBookRepository),
            getBooksByQueryPagingSource = GetBooksByQueryPagingSource(localBookRepository),
            SubscribeInLibraryBooksPagingData = SubscribeInLibraryBooksPagingData(
                localBookRepository),
            getAllExploredBookPagingData = GetAllExploredBookPagingData(localBookRepository = localBookRepository),
            findBookByKey = FindBookByKey(localBookRepository),
            findBooksByKey = FindBooksByKey(localBookRepository),
            findAllInLibraryBooks = FindAllInLibraryBooks(localBookRepository),
            findBookById = FindBookById(localBookRepository)
        )
    }

    @Singleton
    @Provides
    fun providesGetChapterUseCase(
        localChapterRepository: LocalChapterRepository,
    ): LocalGetChapterUseCase {
        return LocalGetChapterUseCase(
            subscribeChapterById = SubscribeChapterById(localChapterRepository),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(localChapterRepository),
            subscribeLastReadChapter = SubscribeLastReadChapter(localChapterRepository),
            getLocalChaptersByPaging = GetLocalChaptersByPaging(localChapterRepository),
            findFirstChapter = FindFirstChapter(localChapterRepository),
            findChapterByKey = FindChapterByKey(localChapterRepository),
            findChaptersByKey = FindChaptersByKey(localChapterRepository),
            findChapterById = FindChapterById(localChapterRepository),
            findChaptersByBookId = FindChaptersByBookId(localChapterRepository),
            findLastReadChapter = FindLastReadChapter(localChapterRepository)
        )
    }

    @Provides
    @Singleton
    fun providePreferences(
        preferences: PreferenceStore,
    ): AppPreferences {
        return AppPreferences(preferences)
    }

    @Provides
    @Singleton
    fun provideLibraryCovers(
        @ApplicationContext context: Context,
    ): LibraryCovers {
        return LibraryCovers(
            FileSystem.SYSTEM,
            File(context.filesDir, "library_covers").toOkioPath()
        )
    }

    @Provides
    @Singleton
    fun provideUiPreferences(
        preferences: PreferenceStore,
    ): UiPreferences {
        return UiPreferences(preferences)
    }

    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferenceStore {
        return AndroidPreferenceStore(context = context, "ui")
    }

}
