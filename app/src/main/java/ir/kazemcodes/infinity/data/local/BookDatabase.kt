package ir.kazemcodes.infinity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ir.kazemcodes.infinity.data.local.dao.BookDao
import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.domain.models.BookEntity
import ir.kazemcodes.infinity.domain.models.ChapterEntity

@Database(
    entities = [BookEntity::class, ChapterEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
    abstract val chapterDao: ChapterDao


    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}