package org.ireader.domain.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ireader.domain.local.dao.DownloadDao
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.SavedDownload

@Database(
    entities = [Book::class, Chapter::class, RemoteKeys::class, SavedDownload::class],
    version = 10,
    exportSchema = true,
)
@TypeConverters(DatabaseConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val libraryChapterDao: LibraryChapterDao
    abstract val remoteKeysDao: RemoteKeysDao
    abstract val downloadDao: DownloadDao

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