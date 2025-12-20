package ireader.data.core

import android.app.Application
import android.os.Build
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
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
          // Use FULL synchronous mode to ensure data persists even if app is killed
          // NORMAL mode can lose data in WAL mode if app crashes before checkpoint
          setPragma(db, "synchronous = FULL")
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