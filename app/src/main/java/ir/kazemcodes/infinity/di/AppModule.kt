package ir.kazemcodes.infinity.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.core.data.local.dao.BookDao
import ir.kazemcodes.infinity.core.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.core.data.repository.PreferencesHelper
import ir.kazemcodes.infinity.core.data.repository.RepositoryImpl
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {


    @Provides
    @Singleton
    fun providesRepository(
        bookDao: BookDao,
        chapterDao: ChapterDao,
        context: Context,
        remoteUseCase: RemoteUseCase,
        preferencesHelper: PreferencesHelper,
    ): Repository {
        return RepositoryImpl(
            bookDao = bookDao,
            chapterDao = chapterDao,
            context = context,
            remoteUseCase = remoteUseCase,
            preferences = preferencesHelper
        )
    }


    @Provides
    @Singleton
    fun providesContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Singleton
    @Provides
    fun providesMosh(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }




}