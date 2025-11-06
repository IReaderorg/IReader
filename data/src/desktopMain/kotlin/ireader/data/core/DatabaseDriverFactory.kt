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
        val dbFile = File(dbDir, "/ireader.db")
        print(dbFile.absolutePath)
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        val driver = JdbcSqliteDriver(
            url = JdbcSqliteDriver.IN_MEMORY.plus(dbFile.absolutePath),
            properties = Properties().apply {
                put("foreign_keys", "true")
            }
        )
        
        // Use SQLDelight's schema creation - it handles everything correctly
        try {
            Database.Schema.create(driver)
            println("Database schema created successfully")
        } catch (e: Exception) {
            println("Error creating database schema (may already exist): ${e.message}")
            // This is fine - the database may already exist
        }
        
        return driver
    }
}
