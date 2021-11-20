package ir.kazemcodes.infinity.library_feature.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao : BookDao

    companion object {
        const val DATABASE_NAME = "infinity_db"
    }

}