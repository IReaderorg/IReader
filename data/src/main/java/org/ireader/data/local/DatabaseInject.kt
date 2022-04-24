package org.ireader.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.dao.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomDatabaseModule {
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

    @InstallIn(SingletonComponent::class)
    @Module
    object DatabaseDaoModule {

        @Provides
        fun providesBookDao(db: AppDatabase): LibraryBookDao {
            return db.libraryBookDao
        }


        @Provides
        fun provideChapterDao(db: AppDatabase): chapterDao =  db.chapterDao

        @Provides
        fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao  =  db.remoteKeysDao

        @Provides
        fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao

        @Provides
        fun provideUpdatesDao(db: AppDatabase): UpdatesDao = db.updatesDao

        @Provides
        fun provideDownloadDao(
            db: AppDatabase,
        ): DownloadDao = db.downloadDao

        @Provides
        fun provideCatalogDao(db: AppDatabase): CatalogDao = db.catalogDao

    }
}