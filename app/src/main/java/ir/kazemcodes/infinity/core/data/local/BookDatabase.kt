package ir.kazemcodes.infinity.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeys
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeysDao
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.models.ChapterEntity

@Database(
    entities = [BookEntity::class, ChapterEntity::class,RemoteKeys::class, ExploreBook::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(DatabaseConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val libraryChapterDao: LibraryChapterDao
    abstract val remoteKeysDao : RemoteKeysDao


    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}