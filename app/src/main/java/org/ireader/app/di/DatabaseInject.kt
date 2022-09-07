package org.ireader.app.di

import android.content.Context
import okio.FileSystem
import ireader.core.api.db.Transactions
import ireader.data.local.AppDatabase
import ireader.data.local.dao.BookCategoryDao
import ireader.data.local.dao.CatalogDao
import ireader.data.local.dao.CategoryDao
import ireader.data.local.dao.ChapterDao
import ireader.data.local.dao.DownloadDao
import ireader.data.local.dao.HistoryDao
import ireader.data.local.dao.LibraryBookDao
import ireader.data.local.dao.LibraryDao
import ireader.data.local.dao.ReaderThemeDao
import ireader.data.local.dao.RemoteKeysDao
import ireader.data.local.dao.ThemeDao
import ireader.data.local.dao.UpdatesDao
import ireader.data.repository.DatabaseTransactions
import org.koin.core.annotation.ComponentScan
import ireader.core.api.di.ISingleton
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.DatabaseInject")
class DatabaseInject {

        @Single
    fun provideBookDatabase(
        app: Context,
    ): AppDatabase {
        return AppDatabase.getInstance(app)
    }

        @Single
    fun providesBookDao(db: AppDatabase): LibraryBookDao {
        return db.libraryBookDao
    }

        @Single
    fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao

        @Single
    fun provideRemoteKeyDao(db: AppDatabase): RemoteKeysDao = db.remoteKeysDao

        @Single
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao

        @Single
    fun provideUpdatesDao(db: AppDatabase): UpdatesDao = db.updatesDao

        @Single
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao

        @Single
    fun provideBookCategoryDao(db: AppDatabase): BookCategoryDao = db.bookCategoryDao

        @Single
    fun provideThemeDao(db: AppDatabase): ThemeDao = db.themeDao
        @Single
    fun provideReaderThemeDao(db: AppDatabase): ReaderThemeDao = db.readerThemeDao

        @Single
    fun provideDownloadDao(
        db: AppDatabase,
    ): DownloadDao = db.downloadDao

        @Single
    fun provideLibraryDao(
        db: AppDatabase,
    ): LibraryDao = db.libraryDao

        @Single
    fun provideFileSystem(): FileSystem {
        return FileSystem.SYSTEM
    }

        @Single
    fun provideCatalogDao(db: AppDatabase): CatalogDao = db.catalogDao

        @Single
    fun provideTransactions(db: AppDatabase): Transactions {
        return DatabaseTransactions(db)
    }
}
