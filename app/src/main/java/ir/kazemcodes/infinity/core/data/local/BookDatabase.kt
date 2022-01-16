package ir.kazemcodes.infinity.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.kazemcodes.infinity.core.data.local.dao.BookDao
import ir.kazemcodes.infinity.core.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.models.ChapterEntity

@Database(
    entities = [BookEntity::class, ChapterEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(DatabaseConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
    abstract val chapterDao: ChapterDao


    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}