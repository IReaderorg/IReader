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
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        val driver = JdbcSqliteDriver(
            url = JdbcSqliteDriver.IN_MEMORY.plus(dbFile.absolutePath),
            properties = Properties().apply {
                put("foreign_keys", "true")
                put("journal_mode", "WAL")
                put("synchronous", "NORMAL")
                put("cache_size", "-64000")
                put("temp_store", "MEMORY")
                put("mmap_size", "268435456")
            }
        )
        
        try {
            driver.execute(null, "PRAGMA journal_mode = WAL;", 0)
            driver.execute(null, "PRAGMA synchronous = NORMAL;", 0)
            driver.execute(null, "PRAGMA temp_store = MEMORY;", 0)
            driver.execute(null, "PRAGMA cache_size = -64000;", 0)
            driver.execute(null, "PRAGMA mmap_size = 268435456;", 0)
        } catch (_: Exception) {
            // Ignore PRAGMA execution errors
        }
        
        // Use SQLDelight's schema creation - it handles everything correctly
        try {
            Database.Schema.create(driver)
        } catch (_: Exception) {
            // This is fine - the database may already exist
        }
        
        return driver
    }
}
