package org.ireader.infinity.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.core.prefs.AndroidPreferenceStore
import org.ireader.core.prefs.PreferenceStore
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.local.MIGRATION_8_9
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.source.Extensions
import org.ireader.domain.ui.AppPreferences
import org.ireader.domain.ui.UiPreferences
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.book_usecases.*
import org.ireader.domain.use_cases.local.chapter_usecases.GetChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.GetLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.GetLocalChaptersByPaging
import org.ireader.domain.use_cases.local.chapter_usecases.GetOneChapterById
import org.ireader.domain.use_cases.local.delete_usecases.book.*
import org.ireader.domain.use_cases.local.delete_usecases.chapter.*
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters
import org.ireader.infinity.core.domain.use_cases.local.book_usecases.GetBooksByQueryPagingSource
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {


    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext appContext: Context,
    ): BookDatabase {
        return Room.databaseBuilder(
            appContext,
            BookDatabase::class.java,
            BookDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_8_9)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesBookDao(db: BookDatabase): LibraryBookDao {
        return db.libraryBookDao
    }


    @Provides
    @Singleton
    fun provideChapterDao(db: BookDatabase): LibraryChapterDao {
        return db.libraryChapterDao
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(db: BookDatabase): RemoteKeysDao {
        return db.remoteKeysDao
    }


    @Singleton
    @Provides
    fun providesExtensions(@ApplicationContext context: Context): Extensions {
        return Extensions(context)
    }

    @Singleton
    @Provides
    fun providesDeleteUseCase(
        localBookRepository: LocalBookRepository,
        localChapterRepository: LocalChapterRepository,
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
            setExploreModeOffForInLibraryBooks = SetOffExploreMode(localBookRepository)
        )
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
            getAllInLibraryBooks = GetAllInLibraryBooks(
                localBookRepository),
            getAllExploredBookPagingSource = GetAllExploredBookPagingSource(localBookRepository),
            getAllInLibraryPagingSource = GetAllInLibraryPagingSource(
                localBookRepository),
            getBookById = GetBookById(localBookRepository),
            getBooksByQueryByPagination = GetBooksByQueryByPagination(localBookRepository),
            getBooksByQueryPagingSource = GetBooksByQueryPagingSource(localBookRepository),
            GetInLibraryBooksPagingData = GetInLibraryBooksPagingData(localBookRepository),
            getAllExploredBookPagingData = GetAllExploredBookPagingData(localBookRepository = localBookRepository),
        )
    }

    @Singleton
    @Provides
    fun providesGetChapterUseCase(
        localChapterRepository: LocalChapterRepository,
    ): LocalGetChapterUseCase {
        return LocalGetChapterUseCase(
            getOneChapterById = GetOneChapterById(localChapterRepository),
            getChaptersByBookId = GetChaptersByBookId(localChapterRepository),
            getLastReadChapter = GetLastReadChapter(localChapterRepository),
            getLocalChaptersByPaging = GetLocalChaptersByPaging(localChapterRepository = localChapterRepository),
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