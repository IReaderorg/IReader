package ireader.data.local

import android.app.Application
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import data.Book
import data.Chapter
import data.History
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import ir.kazemcodes.infinityreader.Database
import ireader.data.BuildConfig
import ireader.data.book.bookGenresConverter
import ireader.data.chapter.chapterContentConvertor
import java.util.*

class DatabaseDriverFactory constructor(
  private val app: Application
) {

  fun create(): SqlDriver {
    return AndroidSqliteDriver(
      schema = Database.Schema,
      context = app,
      name = "infinity_db.db",
      factory = if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Support database inspector in Android Studio
        FrameworkSQLiteOpenHelperFactory()
      } else {
        RequerySQLiteOpenHelperFactory()
      },
      callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
          super.onOpen(db)
          setPragma(db, "foreign_keys = ON")
          setPragma(db, "journal_mode = WAL")
          setPragma(db, "synchronous = NORMAL")
        }
        private fun setPragma(db: SupportSQLiteDatabase, pragma: String) {
          val cursor = db.query("PRAGMA $pragma")
          cursor.moveToFirst()
          cursor.close()
        }

        override fun onCorruption(db: SupportSQLiteDatabase) {
          super.onCorruption(db)
        }


      },
    )
  }

}

fun createDatabase(driver: SqlDriver): Database {
  return Database(
    driver = driver,
    bookAdapter = Book.Adapter(
      genreAdapter = bookGenresConverter
    ),
    chapterAdapter = Chapter.Adapter(
      chapterContentConvertor
    ),
  )
}

val dateAdapter = object : ColumnAdapter<Date, Long> {
  override fun decode(databaseValue: Long): Date = Date(databaseValue)
  override fun encode(value: Date): Long = value.time
}