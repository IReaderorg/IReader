package ir.kazemcodes.infinity.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.MIGRATION_8_9
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeysDao
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases.*
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetChaptersByBookId
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetLastReadChapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetLocalChaptersByPaging
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetOneChapterById
import ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.book.*
import ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.chapter.*
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertBook
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertBooks
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertChapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertChapters
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {
    @Provides
    @Singleton
    fun provideBookDatabase(app: Application): BookDatabase {
        return Room.databaseBuilder(
            app,
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
    fun providesPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }


    @Singleton
    @Provides
    fun providesExtensions(context: Context): Extensions {
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
            deleteInLibraryBook = DeleteInLibraryBook(localBookRepository),
            setExploreModeOffForInLibraryBooks = SetExploreModeOffForInLibraryBooks(localBookRepository),
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
            getAllInLibraryBooks = GetAllInLibraryBooks(localBookRepository),
            getAllExploredBookPagingSource = GetAllExploredBookPagingSource(localBookRepository),
            getAllInLibraryPagingSource = GetAllInLibraryPagingSource(localBookRepository),
            getBookById = GetBookById(localBookRepository),
            getBooksByQueryByPagination = GetBooksByQueryByPagination(localBookRepository),
            getBooksByQueryPagingSource = GetBooksByQueryPagingSource(localBookRepository),
            GetInLibraryBooksPagingData = GetInLibraryBooksPagingData(localBookRepository),
            getAllExploredBookPagingData = GetAllExploredBookPagingData(localBookRepository = localBookRepository),
            getAllInDownloadsPagingData = GetAllInDownloadsPagingData(localBookRepository = localBookRepository)
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