package ireader.data.core

import android.app.Application
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import ir.kazemcodes.infinityreader.Database
import ireader.data.BuildConfig


actual class DatabaseDriverFactory constructor(
  private val app: Application
) {

  actual fun create(): SqlDriver {
    return AndroidSqliteDriver(
      schema = Database.Schema,
      context = app,
      name = "ireader.db",
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