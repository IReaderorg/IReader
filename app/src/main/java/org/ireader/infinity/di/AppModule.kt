package org.ireader.infinity.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.data.repository.*
import org.ireader.domain.feature_services.notification.DefaultNotificationHelper
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.local.dao.SourceTowerDao
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.repository.LocalSourceRepository
import org.ireader.domain.repository.Repository
import org.ireader.infinity.core.domain.repository.RemoteRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {


    @Provides
    @Singleton
    fun providesRepository(
        localChapterRepository: LocalChapterRepository,
        localBookRepository: LocalBookRepository,
        remoteRepository: RemoteRepository,
        bookDatabase: BookDatabase,
    ): Repository {
        return RepositoryImpl(
            localChapterRepository = localChapterRepository,
            localBookRepository = localBookRepository,
            remoteRepository = remoteRepository,
            database = bookDatabase
        )
    }

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
        database: BookDatabase,
        remoteKeysDao: RemoteKeysDao,
    ): LocalBookRepository {
        return LocalBookRepositoryImpl(libraryBookDao,
            libraryChapterDao,
            database,
            remoteKeysDao = remoteKeysDao)
    }


    @Provides
    @Singleton
    fun provideLocalSourceRepository(sourceTowerDao: SourceTowerDao): LocalSourceRepository {
        return LocalSourceRepositoryImpl(sourceTowerDao)
    }

    @Provides
    @Singleton
    fun providesRemoteBookRepository(
        keysDao: RemoteKeysDao,
    ): RemoteRepository {
        return RemoteRepositoryImpl(remoteKeysDao = keysDao)
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
    fun provideNotificationHelper(@ApplicationContext context: Context) : DefaultNotificationHelper {
        return DefaultNotificationHelper(context)
    }


}
