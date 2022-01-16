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
import ir.kazemcodes.infinity.core.data.local.dao.BookDao
import ir.kazemcodes.infinity.core.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.book.*
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter.*
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
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
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesBookDao(db: BookDatabase): BookDao {
        return db.bookDao
    }

    @Provides
    @Singleton
    fun provideChapterDao(db: BookDatabase): ChapterDao {
        return db.chapterDao
    }
    @Provides
    @Singleton
    fun provideLocalUseCases(repository: Repository): LocalUseCase {
        return LocalUseCase(
            getAllLocalBooksUseCase = GetAllLocalBooksUseCase(repository),
            getLocalBookByIdByIdUseCase = GetLocalBookByIdUseCase(repository),
            insertLocalBookUserCase = InsertLocalBookUserCase(repository),
            deleteLocalBookUseCase = DeleteLocalBookUseCase(repository),
            deleteAllLocalBooksUseCase = DeleteAllLocalBooksUseCase(repository),
            insertLocalChaptersUseCase = InsertLocalChaptersUseCase(repository),
            getLocalChaptersByBookNameUseCase = GetLocalChaptersByBookNameUseCase(repository),
            getLocalBookByNameUseCase = GetLocalBookByNameUseCase(repository),
            updateLocalChapterContentUseCase = UpdateLocalChapterContentUseCase(repository),
            getLocalChapterReadingContentUseCase = GetLocalChapterReadingContentUseCase(repository = repository),
            deleteChaptersUseCase = DeleteLocalChaptersUseCase(repository = repository),
            updateLocalChaptersContentUseCase = UpdateLocalChaptersContentUseCase(repository = repository),
            getAllLocalChaptersUseCase = GetLocalAllChaptersUseCase(repository),
            deleteAllLocalChaptersUseCase = DeleteAllLocalChaptersUseCase(repository),
            deleteNotInLibraryLocalChaptersUseCase = DeleteNotInLibraryLocalChaptersUseCase(repository),
            deleteNotInLibraryBooksUseCase = DeleteNotInLibraryLocalBooksUseCase(repository),
            getInLibraryBooksUseCase = GetInLibraryBooksUseCase(repository),
            getLastReadChapterUseCase = GetLastReadChapterUseCase(repository = repository),
            deleteLastReadChapterChaptersUseCase = DeleteLastReadChapterChaptersUseCase(repository),
            setLastReadChaptersUseCase = SetLastReadChaptersUseCase(repository),
            updateLocalBookUserCase = UpdateLocalBookUserCase(repository),
            updateAddToLibraryChaptersContentUseCase = UpdateAddToLibraryChaptersContentUseCase(repository)
        )
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
}