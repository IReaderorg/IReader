package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ir.kazemcodes.infinityreader.Database
import ireader.core.storage.AppDir
import java.io.File
import java.util.*

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val dbDir = File(AppDir, "database/")
        if (!dbDir.exists()) {
            AppDir.mkdirs()
        }
        val dbFile = File(dbDir, "ireader.db")
        val driver = JdbcSqliteDriver(
            url = JdbcSqliteDriver.IN_MEMORY.plus(dbFile.absolutePath),
            properties = Properties().apply {
                put("foreign_keys", "true")
            }
        )
        Database.Schema.create(driver)
        return driver
    }
}
