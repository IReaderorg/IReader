package org.ireader.infinity.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.core.utils.Constants
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.local.MIGRATION_8_9
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.local.dao.SourceTowerDao
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.source.Extensions
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.book_usecases.*
import org.ireader.domain.use_cases.local.chapter_usecases.GetLastReadChapter
import org.ireader.infinity.core.domain.use_cases.local.DeleteUseCase
import org.ireader.infinity.core.domain.use_cases.local.LocalInsertUseCases
import org.ireader.infinity.core.domain.use_cases.local.book_usecases.*
import org.ireader.infinity.core.domain.use_cases.local.chapter_usecases.GetChaptersByBookId
import org.ireader.infinity.core.domain.use_cases.local.chapter_usecases.GetLocalChaptersByPaging
import org.ireader.infinity.core.domain.use_cases.local.chapter_usecases.GetOneChapterById
import org.ireader.infinity.core.domain.use_cases.local.delete_usecases.book.*
import org.ireader.infinity.core.domain.use_cases.local.delete_usecases.chapter.*
import org.ireader.infinity.core.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.infinity.core.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.infinity.core.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.infinity.core.domain.use_cases.local.insert_usecases.InsertChapters
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {


    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext appContext: Context
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
    fun provideSourceDao(database: BookDatabase): SourceTowerDao {
        return database.sourceTowerDao
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
    fun providesPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
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
            setExploreModeOffForInLibraryBooks = SetExploreModeOffForInLibraryBooks(
                localBookRepository),
            deleteChapterByChapter = DeleteChapterByChapter(localChapterRepository),
            deleteChaptersByBookId = DeleteChaptersByBookId(localChapterRepository),
            deleteNotInLibraryChapters = DeleteNotInLibraryChapters(localChapterRepository),
            deleteAllChapters = DeleteAllChapters(localChapterRepository),
            deleteChapters = DeleteChapters(localChapterRepository = localChapterRepository)
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
            getAllInDownloadsPagingData = GetAllInDownloadsPagingData(
                localBookRepository = localBookRepository),
            getBookByIdDirectly = GetBookByIdDirectly(localBookRepository)
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
}