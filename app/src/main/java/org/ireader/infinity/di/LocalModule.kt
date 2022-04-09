package org.ireader.infinity.di

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.core.prefs.AndroidPreferenceStore
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.MIGRATION_11_12
import org.ireader.data.local.MIGRATION_8_9
import org.ireader.data.local.dao.*
import org.ireader.data.repository.DownloadRepositoryImpl
import org.ireader.data.repository.UpdatesRepositoryImpl
import org.ireader.domain.feature_service.io.LibraryCovers
import org.ireader.domain.repository.DownloadRepository
import org.ireader.domain.repository.UpdatesRepository
import tachiyomi.core.prefs.PreferenceStore
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {


    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext app: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_8_9,
                MIGRATION_11_12
            )
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    @Singleton
    fun provideTextToSpeech(context: Context): TextToSpeech {
        return TextToSpeech(context) {

        }
    }

    @Provides
    @Singleton
    fun providesBookDao(db: AppDatabase): LibraryBookDao {
        return db.libraryBookDao
    }


    @Provides
    @Singleton
    fun provideChapterDao(db: AppDatabase): LibraryChapterDao {
        return db.libraryChapterDao
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao {
        return db.remoteKeysDao
    }

    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase): HistoryDao {
        return db.historyDao
    }

    @Provides
    @Singleton
    fun provideUpdatesDao(db: AppDatabase): UpdatesDao {
        return db.updatesDao
    }


    @Singleton
    @Provides
    fun provideDownloadRepository(
        downloadDao: DownloadDao,
    ): DownloadRepository {
        return DownloadRepositoryImpl(downloadDao)
    }

    @Singleton
    @Provides
    fun provideUpdatesRepository(
        updatesDao: UpdatesDao,
    ): UpdatesRepository {
        return UpdatesRepositoryImpl(updatesDao)
    }

    @Singleton
    @Provides
    fun provideDownloadDao(
        database: AppDatabase,
    ): DownloadDao {
        return database.downloadDao
    }


    @Provides
    @Singleton
    fun provideLibraryCovers(
        @ApplicationContext context: Context,
    ): LibraryCovers {
        return LibraryCovers(
            FileSystem.SYSTEM,
            File(context.filesDir, "library_covers").toOkioPath()
        )
    }


    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferenceStore {
        return AndroidPreferenceStore(context = context, "ui")
    }

}
