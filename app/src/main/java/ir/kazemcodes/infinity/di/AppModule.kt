package ir.kazemcodes.infinity.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.data.local.BookDatabase
import ir.kazemcodes.infinity.data.local.repository.LocalRepositoryImpl
import ir.kazemcodes.infinity.data.remote.ParsedHttpSource
import ir.kazemcodes.infinity.data.remote.RealWebnovel
import ir.kazemcodes.infinity.data.remote.repository.RemoteRepositoryImpl
import ir.kazemcodes.infinity.domain.repository.LocalRepository
import ir.kazemcodes.infinity.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.domain.use_case.InfinityUseCases
import ir.kazemcodes.infinity.domain.use_case.local.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application): BookDatabase {
        return Room.databaseBuilder(
            app,
            BookDatabase::class.java,
            BookDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideBookRepository(db: BookDatabase): LocalRepository {
        return LocalRepositoryImpl(db.bookDao)
    }

    @Provides
    @Singleton
    fun provideLocalUseCases(repository: LocalRepository): InfinityUseCases {
        return InfinityUseCases(
            getBooksUseCase = GetBooksUseCase(repository),
            getBookUseCase = GetBookUseCase(repository),
            addBookUserCase = AddBookUserCase(repository),
            deleteBookUseCase = DeleteBookUseCase(repository),
            deleteAllBooksUseCase = DeleteAllBooksUseCase(repository),
        )
    }

    @Provides
    @Singleton
    fun providesRetrofit() : Retrofit {
        return  Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideRemoteRepository(api : ParsedHttpSource) : RemoteRepository {
        return RemoteRepositoryImpl(api)
    }


    @Provides
    @Singleton
    fun provideApi() : ParsedHttpSource {
        return RealWebnovel()
    }




}