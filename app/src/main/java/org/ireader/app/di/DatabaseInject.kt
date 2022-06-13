package org.ireader.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.FileSystem
import org.ireader.core_api.db.Transactions
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.BookCategoryDao
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.local.dao.CategoryDao
import org.ireader.data.local.dao.ChapterDao
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.HistoryDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.UpdatesDao
import org.ireader.data.repository.DatabaseTransactions
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

    @Singleton
    @Provides
    fun providesBookDao(db: AppDatabase): LibraryBookDao {
        return db.libraryBookDao
    }

    @Singleton
    @Provides
    fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao

    @Singleton
    @Provides
    fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao

    @Singleton
    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao

    @Singleton
    @Provides
    fun provideUpdatesDao(db: AppDatabase): UpdatesDao = db.updatesDao

    @Singleton
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao

    @Singleton
    @Provides
    fun provideBookCategoryDao(db: AppDatabase): BookCategoryDao = db.bookCategoryDao

    @Singleton
    @Provides
    fun provideDownloadDao(
        db: AppDatabase,
    ): DownloadDao = db.downloadDao

    @Singleton
    @Provides
    fun provideLibraryDao(
        db: AppDatabase,
    ): LibraryDao = db.libraryDao

    @Singleton
    @Provides
    fun provideFileSystem(): FileSystem {
        return FileSystem.SYSTEM
    }

    @Singleton
    @Provides
    fun provideCatalogDao(db: AppDatabase): CatalogDao = db.catalogDao

    @Singleton
    @Provides
    fun provideTransactions(db: AppDatabase): Transactions {
        return DatabaseTransactions(db)
    }
}
