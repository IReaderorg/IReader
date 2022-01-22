package ir.kazemcodes.infinity.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.kazemcodes.infinity.core.data.local.dao.*
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.models.ChapterEntity
import ir.kazemcodes.infinity.core.domain.models.SourceEntity

@Database(
    entities = [BookEntity::class, ChapterEntity::class,RemoteKeys::class, ExploreBook::class,SourceEntity::class],
    version = 5,
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