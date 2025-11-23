package ireader.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual fun createTestDriver(): SqlDriver {
    // Use JDBC driver for Android unit tests (runs on JVM)
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}
