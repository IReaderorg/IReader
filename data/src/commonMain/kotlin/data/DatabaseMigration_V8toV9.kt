package data

import app.cash.sqldelight.db.SqlDriver

/**
 * Migration from version 8 to version 9
 * Ensures sync_queue table has the correct schema
 * This fixes issues where the table might exist with wrong schema
 */
fun migrateV8toV9(driver: SqlDriver) {
    try {
        println("Starting migration from version 8 to 9...")
        
        // Check if sync_queue table exists
        var syncQueueExists = false
        var hasCorrectSchema = false
        
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='sync_queue'",
            mapper = { cursor ->
                val result = cursor.next()
                syncQueueExists = result.value
                result
            },
            parameters = 0
        )
        
        if (syncQueueExists) {
            // Check if the table has the correct schema by checking for book_id column
            try {
                driver.executeQuery(
                    identifier = null,
                    sql = "PRAGMA table_info(sync_queue)",
                    mapper = { cursor ->
                        var result = cursor.next()
                        while (result.value) {
                            val columnName = cursor.getString(1)
                            if (columnName == "book_id") {
                                hasCorrectSchema = true
                            }
                            result = cursor.next()
                        }
                        result
                    },
                    parameters = 0
                )
            } catch (e: Exception) {
                println("Error checking sync_queue schema: ${e.message}")
            }
            
            if (!hasCorrectSchema) {
                println("sync_queue table has incorrect schema, recreating...")
                // Drop the old sync_queue table
                driver.execute(null, "DROP TABLE IF EXISTS sync_queue;", 0)
                println("Dropped old sync_queue table")
            } else {
                println("sync_queue table already has correct schema")
                return
            }
        }
        
        // Create sync_queue table with correct schema (matching sync_queue.sq)
        val createSyncQueueSql = """
            CREATE TABLE IF NOT EXISTS sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id TEXT NOT NULL,
                data TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                retry_count INTEGER DEFAULT 0
            );
        """.trimIndent()
        
        driver.execute(null, createSyncQueueSql, 0)
        println("Created sync_queue table with correct schema")
        
        // Create indexes for better performance
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_timestamp ON sync_queue(timestamp ASC);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_book_id ON sync_queue(book_id);", 0)
        println("Created indexes for sync_queue table")
        
        println("Successfully migrated to version 9")
        
    } catch (e: Exception) {
        println("Error migrating to version 9: ${e.message}")
        e.printStackTrace()
        // Don't throw - allow the app to continue even if migration fails
    }
}
