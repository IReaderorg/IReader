package ireader.data.core

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import ir.kazemcodes.infinityreader.Database
import java.util.*

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val driver = JdbcSqliteDriver(
            url = JdbcSqliteDriver.IN_MEMORY+ "/tmp/IReader.db",
            properties = Properties().apply {
                put("foreign_keys", "true")
            }
        )
        Database.Schema.create(driver)
        return driver
    }
}
