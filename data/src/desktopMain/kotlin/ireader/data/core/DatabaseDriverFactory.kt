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
        
        // Custom schema creation that handles errors more gracefully
        try {
            createTablesManually(driver)
        } catch (e: Exception) {
            println("Error creating tables manually: ${e.message}")
            e.printStackTrace()
            
            // Fall back to standard schema creation
            try {
                Database.Schema.create(driver)
            } catch (innerE: Exception) {
                println("Error in standard schema creation: ${innerE.message}")
                innerE.printStackTrace()
            }
        }
        
        return driver
    }
    
    /**
     * Creates tables manually to ensure they exist before views are created
     */
    private fun createTablesManually(driver: SqlDriver) {
        // Create core tables first
        val tableCreationStatements = listOf(
            // Book table
            """
            CREATE TABLE IF NOT EXISTS book (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                source INTEGER NOT NULL,
                thumbnail_url TEXT, 
                url TEXT NOT NULL,
                title TEXT NOT NULL,
                artist TEXT,
                author TEXT,
                description TEXT,
                genre TEXT,
                status INTEGER NOT NULL,
                cover_last_modified INTEGER NOT NULL,
                date_added INTEGER NOT NULL,
                viewer INTEGER NOT NULL,
                favorite INTEGER NOT NULL,
                source_id INTEGER NOT NULL,
                publisher TEXT,
                favorite_date INTEGER NOT NULL
            )
            """,
            
            // Chapter table
            """
            CREATE TABLE IF NOT EXISTS chapter (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                book_id INTEGER NOT NULL,
                url TEXT NOT NULL,
                name TEXT NOT NULL,
                read INTEGER NOT NULL,
                bookmark INTEGER NOT NULL,
                last_page_read INTEGER NOT NULL,
                chapter_number REAL NOT NULL,
                source_order INTEGER NOT NULL,
                date_fetch INTEGER NOT NULL,
                date_upload INTEGER NOT NULL,
                content TEXT,
                scanlator TEXT,
                FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
            )
            """,
            
            // History table
            """
            CREATE TABLE IF NOT EXISTS history (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                chapter_id INTEGER NOT NULL UNIQUE,
                last_read INTEGER,
                time_read INTEGER NOT NULL,
                progress REAL DEFAULT 0.0,
                FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE
            )
            """
        )
        
        // Execute table creation statements
        tableCreationStatements.forEach { sql ->
            try {
                driver.execute(null, sql.trimIndent(), 0)
                println("Created table with: ${sql.lines().first().trim()}")
            } catch (e: Exception) {
                // Log but continue with other tables
                println("Error creating table: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Create indices
        try {
            driver.execute(null, "CREATE INDEX IF NOT EXISTS history_chapter_id_index ON history(chapter_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);", 0)
        } catch (e: Exception) {
            println("Error creating indices: ${e.message}")
        }
        
        // Now let the standard schema creation handle the rest
        try {
            Database.Schema.create(driver)
            println("Standard schema creation completed")
        } catch (e: Exception) {
            // This may fail for tables we've already created, which is fine
            println("Standard schema creation resulted in: ${e.message}")
        }
    }
}
