package ir.kazemcodes.infinity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ir.kazemcodes.infinity.domain.model.book.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 1
)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao : BookDao

    companion object {
        const val DATABASE_NAME = "infinity_db"
    }
}