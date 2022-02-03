package ir.kazemcodes.infinity.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeysDao
import ir.kazemcodes.infinity.core.data.local.dao.SourceTowerDao
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.RemoteKeys
import ir.kazemcodes.infinity.core.domain.models.SourceEntity

@Database(
    entities = [Book::class, Chapter::class, RemoteKeys::class,SourceEntity::class],
    version = 9,
    exportSchema = true,

)
@TypeConverters(DatabaseConverter::class)

abstract class BookDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val libraryChapterDao: LibraryChapterDao
    abstract val remoteKeysDao : RemoteKeysDao
    abstract val sourceTowerDao : SourceTowerDao



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