package ireader.data.core

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ir.kazemcodes.infinityreader.Database


actual class DatabaseDriverFactory constructor(
  private val app: Application
) {

  actual fun create(): SqlDriver {
    val schema = Database.Schema
    val factory: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory()

    val configuration = SupportSQLiteOpenHelper.Configuration.builder(app)
      .name("ireader.db")
      .callback(object : AndroidSqliteDriver.Callback(schema) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
          super.onConfigure(db)
          db.enableWriteAheadLogging()
          try {
            db.execSQL("PRAGMA synchronous = NORMAL")
            db.execSQL("PRAGMA temp_store = MEMORY")
            db.execSQL("PRAGMA cache_size = -64000")
            db.execSQL("PRAGMA mmap_size = 268435456")
            db.execSQL("PRAGMA foreign_keys = ON")
          } catch (_: Exception) {
            // Ignore PRAGMA errors on older SQLite engines
          }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
          super.onOpen(db)
          AndroidDatabaseOptimizations.applyOptimalPragmas(db)
        }
      })
      .build()

    val openHelper: SupportSQLiteOpenHelper = factory.create(configuration)
    return AndroidSqliteDriver(openHelper)
  }

}
