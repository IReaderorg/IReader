package ir.kazemcodes.infinity.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.api_feature.network.NetworkHelper
import ir.kazemcodes.infinity.domain.local_feature.data.BookDao
import ir.kazemcodes.infinity.domain.local_feature.data.BookDatabase
import ir.kazemcodes.infinity.domain.local_feature.data.ChapterDao
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.GetLocalBookByNameUseCase
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.book.*
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter.*
import ir.kazemcodes.infinity.domain.network.utils.MemoryCookieJar
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.repository.RepositoryImpl
import ir.kazemcodes.infinity.domain.use_cases.remote.*
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
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
        bookDao: BookDao,
        chapterDao: ChapterDao
    ): Repository {
        return RepositoryImpl(
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
            UpdateLocalChapterContentUseCase = UpdateLocalChapterContentUseCase(repository),
            getLocalChapterReadingContentUseCase = GetLocalChapterReadingContentUseCase(repository = repository),
            deleteChaptersUseCase = DeleteLocalChaptersUseCase(repository = repository)
        )
    }

    @Provides
    @Singleton
    fun providesRemoteUSeCases(): RemoteUseCase {
        return RemoteUseCase(
            getRemoteBookDetailUseCase = GetRemoteBookDetailUseCase(),
            getRemoteBooksUseCase = GetRemoteBooksUseCase(),
            getRemoteChaptersUseCase = GetRemoteChaptersUseCase(),
            getRemoteReadingContentUseCase = GetRemoteReadingContentUseCase(),
            getSearchedBooksUseCase = GetRemoteSearchBookUseCase()
        )
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }


//    @Provides
//    @Singleton
//    fun provideRemoteRepository(api : ParsedHttpSource) : RemoteRepository {
//        return RemoteRepositoryImpl(api)
//    }

//
//    @Provides
//    @Singleton
//    fun provideApi(): ParsedHttpSource {
//        return WuxiaorldApi()
//    }

    @Singleton
    @Provides
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        return OkHttpClient.Builder().apply {
            networkInterceptors().add(
                HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BASIC)
                }
            )
            cookieJar(cookieJar)
        }.build()
    }
    @Singleton
    @Provides
    fun providesMosh() : Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun providesNetworkHelper(context: Context) : NetworkHelper {
        return NetworkHelper(context = context)
    }



}