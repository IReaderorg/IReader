package org.ireader.infinity.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryChapterDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.repository.LocalBookRepositoryImpl
import org.ireader.data.repository.LocalChapterRepositoryImpl
import org.ireader.data.repository.RemoteRepositoryImpl
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.repository.RemoteRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {



    @Provides
    @Singleton
    fun providesLocalChapterRepository(libraryChapterDao: LibraryChapterDao): LocalChapterRepository {
        return LocalChapterRepositoryImpl(libraryChapterDao)
    }


    @Provides
    @Singleton
    fun providesLibraryRepository(
        libraryBookDao: LibraryBookDao,
        libraryChapterDao: LibraryChapterDao,
        database: AppDatabase,
        remoteKeysDao: RemoteKeysDao,
    ): LocalBookRepository {
        return LocalBookRepositoryImpl(libraryBookDao,
            libraryChapterDao,
            database,
            remoteKeysDao = remoteKeysDao)
    }

    @Provides
    @Singleton
    fun providesRemoteBookRepository(
        keysDao: RemoteKeysDao,
        database: AppDatabase,
    ): RemoteRepository {
        return RemoteRepositoryImpl(remoteKeysDao = keysDao, database = database)
    }

    @Singleton
    @Provides
    fun providesMosh(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }


}
