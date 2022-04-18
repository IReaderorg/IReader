package org.ireader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ireader.data.local.dao.*
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.*

@Database(
    entities = [
        Book::class,
        CatalogRemote::class,
        Category::class,
        Chapter::class,
        SavedDownload::class,
        History::class,
        Update::class,
        RemoteKeys::class,
    ],
    version = 17,
    exportSchema = true,
)
@TypeConverters(DatabaseConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val chapterDao: chapterDao
    abstract val remoteKeysDao: RemoteKeysDao
    abstract val downloadDao: DownloadDao
    abstract val catalogDao: CatalogDao
    abstract val historyDao: HistoryDao
    abstract val updatesDao: UpdatesDao

    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE book_table ADD COLUMN beingDownloaded INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE book_table RENAME COLUMN download TO isDownloaded")
    }
}
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE library ADD COLUMN beingDownloaded INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE library RENAME COLUMN download TO isDownloaded")
    }
}
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE library ADD COLUMN tableId INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_12_11 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE history_table RENAME TO history")
    }
}