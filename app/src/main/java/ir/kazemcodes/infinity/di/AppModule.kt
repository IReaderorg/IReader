package ir.kazemcodes.infinity.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.base_feature.repository.RepositoryImpl
import ir.kazemcodes.infinity.explore_feature.data.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.RealWebnovel
import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.data.BookDatabase
import ir.kazemcodes.infinity.library_feature.domain.use_case.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
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
    fun providesRepository(api: ParsedHttpSource, bookDao: BookDao): Repository {
        return RepositoryImpl(api = api, dao = bookDao)
    }
//    @Provides
//    @Singleton
//    fun provideLocalRepository(db: BookDatabase): LocalRepository {
//        return LocalRepositoryImpl(db.bookDao)
//    }

    @Provides
    @Singleton
    fun provideLocalUseCases(repository: Repository): LocalUseCase {
        return LocalUseCase(
            getLocalBooksUseCase = GetLocalBooksUseCase(repository),
            GetLocalBookUseCase = GetLocalBookUseCase(repository),
            insertLocalBookUserCase = InsertLocalBookUserCase(repository),
            deleteLocalBookUseCase = DeleteLocalBookUseCase(repository),
            deleteAllLocalBooksUseCase = DeleteAllLocalBooksUseCase(repository),
        )
    }

    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

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