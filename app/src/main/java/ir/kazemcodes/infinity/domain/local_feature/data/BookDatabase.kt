package ir.kazemcodes.infinity.domain.local_feature.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ir.kazemcodes.infinity.domain.local_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.domain.local_feature.domain.model.ChapterEntity

@Database(
    entities = [BookEntity::class,ChapterEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao : BookDao
    abstract val chapterDao : ChapterDao


    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}