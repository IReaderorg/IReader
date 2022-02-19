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
        Updates::class,
        RemoteKeys::class,
    ],
    version = 11,
    exportSchema = true,
)
@TypeConverters(DatabaseConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val libraryChapterDao: LibraryChapterDao
    abstract val remoteKeysDao: RemoteKeysDao
    abstract val downloadDao: DownloadDao
    abstract val catalogDao: CatalogDao

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