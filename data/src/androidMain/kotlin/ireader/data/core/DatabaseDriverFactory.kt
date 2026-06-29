package ireader.data.core

import android.app.Application
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
      .callback(AndroidSqliteDriver.Callback(schema))
      .build()

    val openHelper: SupportSQLiteOpenHelper = factory.create(configuration)
    return AndroidSqliteDriver(openHelper)
  }

}
