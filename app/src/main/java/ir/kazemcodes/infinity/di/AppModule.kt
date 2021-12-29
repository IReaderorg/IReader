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
import ir.kazemcodes.infinity.data.local.BookDatabase
import ir.kazemcodes.infinity.data.local.dao.BookDao
import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.data.network.utils.MemoryCookieJar
import ir.kazemcodes.infinity.data.repository.RepositoryImpl
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.use_cases.datastore.*
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.book.*
import ir.kazemcodes.infinity.domain.use_cases.local.chapter.*
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
    fun providesRepository(
        bookDao: BookDao,
        chapterDao: ChapterDao,
        context: Context,
        remoteUseCase: RemoteUseCase
    ): Repository {
        return RepositoryImpl(
            bookDao = bookDao,
            chapterDao = chapterDao,
            context = context,
            remoteUseCase = remoteUseCase
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
            getLocalChaptersByBookNameByBookNameUseCase = GetLocalChaptersByBookNameUseCase(
                repository),
            getLocalBookByNameUseCase = GetLocalBookByNameUseCase(repository),
            UpdateLocalChapterContentUseCase = UpdateLocalChapterContentUseCase(repository),
            getLocalChapterReadingContentUseCase = GetLocalChapterReadingContentUseCase(repository = repository),
            deleteChaptersUseCase = DeleteLocalChaptersUseCase(repository = repository),
            UpdateLocalChaptersContentUseCase = UpdateLocalChaptersContentUseCase(repository = repository)
        )
    }

    @Provides
    @Singleton
    fun providesRemoteUSeCases(): RemoteUseCase {
        return RemoteUseCase(
            getRemoteBookDetailUseCase = GetRemoteBookDetailUseCase(),
            getRemoteLatestUpdateLatestBooksUseCase = GetRemoteLatestBooksUseCase(),
            getRemoteChaptersUseCase = GetRemoteChaptersUseCase(),
            getRemoteReadingContentUseCase = GetRemoteReadingContentUseCase(),
            getSearchedBooksUseCase = GetRemoteSearchBookUseCase(),
            getRemoteMostPopularBooksUseCase = GetRemoteMostPopularBooksUseCase()
        )
    }

    @Provides
    @Singleton
    fun provideDataStoreUseCase(repository: Repository): DataStoreUseCase {
        return DataStoreUseCase(
            readSelectedFontStateUseCase = ReadSelectedFontStateUseCase(repository),
            saveSelectedFontStateUseCase = SaveSelectedFontStateUseCase(repository),
            readFontSizeStateUseCase = ReadFontSizeStateUseCase(repository),
            saveFontSizeStateUseCase = SaveFontSizeStateUseCase(repository),
            readBrightnessStateUseCase = ReadBrightnessStateUseCase(repository),
            saveBrightnessStateUseCase = SaveBrightnessStateUseCase(repository),
            saveLibraryLayoutUseCase = SaveLibraryLayoutTypeStateUseCase(repository),
            readLibraryLayoutUseCase = ReadLibraryLayoutTypeStateUseCase(repository),
            saveBrowseLayoutUseCase = SaveBrowseLayoutTypeStateUseCase(repository),
            readBrowseLayoutUseCase = ReadBrowseLayoutTypeStateUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }


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

        }
            .build()
    }

    @Singleton
    @Provides
    fun providesMosh(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun providesNetworkHelper(context: Context): NetworkHelper {
        return NetworkHelper(context = context)
    }


}