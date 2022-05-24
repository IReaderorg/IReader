package org.ireader.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.BookCategoryDao
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.local.dao.CategoryDao
import org.ireader.data.local.dao.ChapterDao
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.FontDao
import org.ireader.data.local.dao.HistoryDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.UpdatesDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseInject {
    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext app: Context,
    ): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @InstallIn(SingletonComponent::class)
    @Module
    object DatabaseDaoModule {

        @Provides
        fun providesBookDao(db: AppDatabase): LibraryBookDao {
            return db.libraryBookDao
        }

        @Provides
        fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao

        @Provides
        fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao

        @Provides
        fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao

        @Provides
        fun provideUpdatesDao(db: AppDatabase): UpdatesDao = db.updatesDao
        @Provides
        fun provideFontDao(db: AppDatabase): FontDao = db.fontDao

        @Provides
        fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao
        @Provides
        fun provideBookCategoryDao(db: AppDatabase): BookCategoryDao = db.bookCategoryDao

        @Provides
        fun provideDownloadDao(
            db: AppDatabase,
        ): DownloadDao = db.downloadDao
        @Provides
        fun provideLibraryDao(
            db: AppDatabase,
        ): LibraryDao = db.libraryDao

        @Provides
        fun provideCatalogDao(db: AppDatabase): CatalogDao = db.catalogDao
    }
}
