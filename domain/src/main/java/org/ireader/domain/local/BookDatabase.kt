package org.ireader.domain.local

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ireader.core.utils.Constants
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter

@Database(
    entities = [Book::class, Chapter::class, RemoteKeys::class],
    version = 10,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 9,
            to = 10,
            spec = BookDatabase.MIGRATION9TO10::class
        )
    ]
)
@TypeConverters(DatabaseConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val libraryChapterDao: LibraryChapterDao
    abstract val remoteKeysDao: RemoteKeysDao

    @DeleteTable(tableName = Constants.SOURCE_TABLE)
    class MIGRATION9TO10 : AutoMigrationSpec
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