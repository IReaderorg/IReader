package ir.kazemcodes.infinity.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.api_feature.ParsedHttpSource
import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.base_feature.repository.RepositoryImpl
import ir.kazemcodes.infinity.explore_feature.data.RealWebnovel
import ir.kazemcodes.infinity.explore_feature.domain.use_case.*
import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.data.BookDatabase
import ir.kazemcodes.infinity.library_feature.data.ChapterDao
import ir.kazemcodes.infinity.library_feature.domain.use_case.GetLocalBookByNameUseCase
import ir.kazemcodes.infinity.library_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.library_feature.domain.use_case.book.*
import ir.kazemcodes.infinity.library_feature.domain.use_case.chapter.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideBookDatabase(app: Application): BookDatabase {
        return Room.databaseBuilder(
            app,
            BookDatabase::class.java,
            BookDatabase.DATABASE_NAME
        )
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
    fun providesRepository(
        api: ParsedHttpSource,
        bookDao: BookDao,
        chapterDao: ChapterDao
    ): Repository {
        return RepositoryImpl(
            api = api,
            bookDao = bookDao,
            chapterDao = chapterDao
        )
    }
    @Provides
    @Singleton
    fun provideLocalUseCases(repository: Repository): LocalUseCase {
        return LocalUseCase(
            getLocalBooksUseCase = GetLocalBooksUseCase(repository),
            getLocalBookByIdByIdUseCase = GetLocalBookByIdUseCase(repository),
            insertLocalBookUserCase = InsertLocalBookUserCase(repository),
            deleteLocalBookUseCase = DeleteLocalBookUseCase(repository),
            deleteAllLocalBooksUseCase = DeleteAllLocalBooksUseCase(repository),
            insertLocalChaptersUseCase = InsertLocalChaptersUseCase(repository),
            getLocalChaptersByBookNameByBookNameUseCase = GetLocalChaptersByBookNameUseCase(repository),
            getLocalChapterUseCase = GetLocalChapterUseCase(repository),
            getLocalBookByNameUseCase = GetLocalBookByNameUseCase(repository),
            insertLocalChapterContentUseCase = InsertLocalChapterContentUseCase(repository),
            getLocalChapterReadingContent = GetLocalChapterReadingContent(repository = repository)
        )
    }
    @Provides
    @Singleton
    fun providesRemoteUSeCases(repository: Repository) : RemoteUseCase {
        return RemoteUseCase(
            getRemoteBookDetailUseCase = GetRemoteBookDetailUseCase(repository),
            getRemoteBooksUseCase= GetRemoteBooksUseCase(repository),
            getRemoteChaptersUseCase= GetRemoteChaptersUseCase(repository),
            getRemoteReadingContentUseCase = GetRemoteReadingContentUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext appContext: Context) : Context{
        return appContext

    }

//    @Provides
//    @Singleton
//    fun providesRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .addConverterFactory(MoshiConverterFactory.create())
//            .build()
//            .create()
//    }

//    @Provides
//    @Singleton
//    fun provideRemoteRepository(api : ParsedHttpSource) : RemoteRepository {
//        return RemoteRepositoryImpl(api)
//    }


    @Provides
    @Singleton
    fun provideApi(): ParsedHttpSource {
        return RealWebnovel()
    }

}