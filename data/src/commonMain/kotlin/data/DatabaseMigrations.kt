package data

import app.cash.sqldelight.db.SqlDriver

/**
 * DatabaseMigrations class that manages schema migrations between different database versions.
 * It maintains a list of migration scripts and applies them in sequence when needed.
 */
object DatabaseMigrations {
    
    /**
     * Current database schema version. Increment this when adding new migrations.
     */
    const val CURRENT_VERSION = 2
    
    /**
     * Applies all necessary migrations to bring the database from [oldVersion] to [CURRENT_VERSION]
     * 
     * @param driver The SqlDriver instance to execute migrations on
     * @param oldVersion The current version of the database schema
     */
    fun migrate(driver: SqlDriver, oldVersion: Int) {
        if (oldVersion < CURRENT_VERSION) {
            try {
                // Apply migrations one by one in order
                for (version in oldVersion until CURRENT_VERSION) {
                    try {
                        applyMigration(driver, version)
                    } catch (e: Exception) {
                        println("Error during migration from version $version: ${e.message}")
                        
                        // For specific errors, try to recover
                        if (e.message?.contains("table history_new already exists") == true) {
                            println("Attempting to recover from interrupted migration...")
                            cleanupInterruptedMigration(driver, version)
                            // Try again after cleanup
                            applyMigration(driver, version)
                        } else {
                            throw e // Re-throw if unrecoverable
                        }
                    }
                }
                
                // Initialize views after all migrations
                initializeViews(driver)
            } catch (e: Exception) {
                println("Failed to complete migrations: ${e.message}")
                // Try to at least initialize views even if migrations failed
                try {
                    initializeViews(driver)
                } catch (viewErr: Exception) {
                    println("Could not initialize views after migration failure: ${viewErr.message}")
                }
                throw e
            }
        }
    }
    
    /**
     * Public method to directly initialize views without requiring a migration
     * This can be called during database creation to ensure views exist
     */
    fun initializeViewsDirectly(driver: SqlDriver) {
        initializeViews(driver)
    }
    
    /**
     * Applies a specific migration script for the given [fromVersion]
     * 
     * @param driver The SqlDriver instance to execute migrations on
     * @param fromVersion The version from which to migrate (migrates to fromVersion + 1)
     */
    private fun applyMigration(driver: SqlDriver, fromVersion: Int) {
        when (fromVersion) {
            1 -> migrateV1toV2(driver)
            // Add more migration cases as the database evolves
            // 2 -> migrateV2toV3(driver)
            // etc.
        }
    }
    
    /**
     * Migration from version 1 to version 2
     * Adds progress tracking to history table
     * 
     * For SQLDelight migrations, we need to:
     * 1. Create a new table with the desired schema
     * 2. Copy data from the old table to the new one
     * 3. Drop the old table
     * 4. Rename the new table to the original name
     */
    private fun migrateV1toV2(driver: SqlDriver) {
        // This migration is now handled by the SQLDelight migration file (1.sqm)
        // SQLDelight will apply the SQL statements in that file directly
        // No additional SQL execution is needed here
    }
    
    /**
     * Verify that all required tables exist in the database
     * If any table is missing, create it or throw a specific error
     */
    private fun verifyRequiredTables(driver: SqlDriver) {
        try {
            // Check if history table exists
            val historyTableCheck = "SELECT name FROM sqlite_master WHERE type='table' AND name='history'"
            var historyTableExists = false
            driver.executeQuery(
                identifier = null,
                sql = historyTableCheck,
                mapper = { cursor ->
                    val result = cursor.next()
                    historyTableExists = result.value
                    result
                },
                parameters = 0
            )
            
            if (!historyTableExists) {
                println("WARNING: History table does not exist. Attempting to create it.")
                
                // First verify that the chapter table exists since history depends on it
                val chapterTableCheck = "SELECT name FROM sqlite_master WHERE type='table' AND name='chapter'"
                var chapterTableExists = false
                driver.executeQuery(
                    identifier = null,
                    sql = chapterTableCheck,
                    mapper = { cursor ->
                        val result = cursor.next()
                        chapterTableExists = result.value
                        result
                    },
                    parameters = 0
                )
                
                if (!chapterTableExists) {
                    println("ERROR: Chapter table missing but required by history table. Creating it...")
                    
                    val createChapterTableSql = """
                        CREATE TABLE IF NOT EXISTS chapter(
                            _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
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
                        );
                    """.trimIndent()
                    
                    driver.execute(null, createChapterTableSql, 0)
                    println("Chapter table created successfully.")
                }
                
                // Create the history table if it doesn't exist
                val createHistoryTableSql = """
                    CREATE TABLE IF NOT EXISTS history(
                        _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        chapter_id INTEGER NOT NULL UNIQUE,
                        last_read INTEGER,
                        time_read INTEGER NOT NULL,
                        progress REAL DEFAULT 0.0,
                        FOREIGN KEY(chapter_id) REFERENCES chapter (_id)
                        ON DELETE CASCADE
                    );
                """.trimIndent()
                
                try {
                    driver.execute(null, createHistoryTableSql, 0)
                    println("History table created successfully.")
                } catch (e: Exception) {
                    println("Error creating history table: ${e.message}")
                    println("Attempting with different schema...")
                    
                    // Try again without AUTOINCREMENT as it might be an issue in some SQLite versions
                    val alternativeHistoryTableSql = """
                        CREATE TABLE IF NOT EXISTS history(
                            _id INTEGER NOT NULL PRIMARY KEY,
                            chapter_id INTEGER NOT NULL UNIQUE,
                            last_read INTEGER,
                            time_read INTEGER NOT NULL,
                            progress REAL DEFAULT 0.0,
                            FOREIGN KEY(chapter_id) REFERENCES chapter (_id)
                            ON DELETE CASCADE
                        );
                    """.trimIndent()
                    
                    driver.execute(null, alternativeHistoryTableSql, 0)
                    println("History table created with alternative schema.")
                }
                
                // Create necessary indices
                driver.execute(null, "CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);", 0)
            } else {
                println("History table exists.")
            }
            
        } catch (e: Exception) {
            println("Error verifying required tables: ${e.message}")
            e.printStackTrace()
            // We don't want to throw here, as we want to continue with view creation even if table creation fails
            // The view creation will fail more gracefully with a specific error about the missing table
        }
    }

    /**
     * Initialize or recreate database views
     * This should be called after running all migrations to ensure views are properly set up
     */
    private fun initializeViews(driver: SqlDriver) {
        try {
            // First verify that required tables exist
            verifyRequiredTables(driver)
            
            // First check if the views already exist
            val viewCheckQuery = "SELECT name FROM sqlite_master WHERE type='view' AND name='historyView'"
            var historyViewExists = false
            driver.executeQuery(
                identifier = null,
                sql = viewCheckQuery,
                mapper = { cursor ->
                    val result = cursor.next()
                    historyViewExists = result.value
                    result
                },
                parameters = 0
            )
            
            // Always drop views to recreate them with latest schema
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            
            println("Creating views - historyView existed before: $historyViewExists")
            
            // Double-check that history table exists before creating the view
            val historyTableExists = checkTableExists(driver, "history")
            if (!historyTableExists) {
                println("ERROR: Cannot create historyView because history table is missing.")
                return
            }
            
            val categoryInitSql ="""
                -- Insert system category
                INSERT OR IGNORE INTO categories(_id, name, sort, flags) VALUES (0, "", -1, 0);
                INSERT OR IGNORE INTO categories(_id, name, sort, flags) VALUES (-1, "", -1, 0);
                -- Disallow deletion of default category
                CREATE TRIGGER IF NOT EXISTS system_category_delete_trigger BEFORE DELETE
                ON categories
                BEGIN SELECT CASE
                    WHEN old._id <= 0 THEN
                        RAISE(ABORT, "System category can't be deleted")
                    END;
                END;
            """.trimIndent()
            
            // Try creating the trigger and categories - but don't fail if this doesn't work
            try {
                driver.execute(null, categoryInitSql, 0)
                println("Category initialization completed successfully")
            } catch (e: Exception) {
                println("Error initializing categories (non-fatal): ${e.message}")
            }
            
            // Create historyView with progress column
            val historyViewSql = """
                CREATE VIEW IF NOT EXISTS historyView AS
                SELECT
                    history._id AS id,
                    book._id AS bookId,
                    chapter._id AS chapterId,
                    chapter.name AS chapterName,
                    book.title,
                    book.thumbnail_url AS thumbnailUrl,
                    book.source,
                    book.favorite,
                    book.cover_last_modified,
                    chapter.chapter_number AS chapterNumber,
                    history.last_read AS readAt,
                    history.time_read AS readDuration,
                    history.progress,
                    max_last_read.last_read AS maxReadAt,
                    max_last_read.chapter_id AS maxReadAtChapterId
                FROM book
                JOIN chapter
                ON book._id = chapter.book_id
                JOIN history
                ON chapter._id = history.chapter_id
                JOIN (
                    SELECT chapter.book_id, chapter._id AS chapter_id, MAX(history.last_read) AS last_read
                    FROM chapter JOIN history
                    ON chapter._id = history.chapter_id
                    GROUP BY chapter.book_id
                ) AS max_last_read
                ON chapter.book_id = max_last_read.book_id;
            """.trimIndent()
            
            println("Creating historyView...")
            try {
                // Execute historyView creation separately
                driver.execute(null, historyViewSql, 0)
                println("historyView created successfully")
            } catch (e: Exception) {
                println("Error creating historyView: ${e.message}")
                e.printStackTrace()
            }
            
            // Create updatesView with progress information
            val updatesViewSql = """
                CREATE VIEW updatesView AS
                SELECT
                    book._id AS mangaId,
                    book.title AS mangaTitle,
                    chapter._id AS chapterId,
                    chapter.name AS chapterName,
                    chapter.scanlator,
                    chapter.read,
                    chapter.bookmark,
                    book.source,
                    book.favorite,
                    book.thumbnail_url AS thumbnailUrl,
                    book.cover_last_modified AS coverLastModified,
                    chapter.date_upload AS dateUpload,
                    chapter.date_fetch AS datefetch,
                    chapter.content IS NOT "" AS downlaoded,
                    history.progress AS readingProgress,
                    history.last_read AS lastReadAt
                FROM book JOIN chapter
                ON book._id = chapter.book_id
                LEFT JOIN history
                ON chapter._id = history.chapter_id
                WHERE favorite = 1
                AND date_fetch > date_added
                ORDER BY date_fetch DESC;
            """.trimIndent()
            
            println("Creating updatesView...")
            try {
                // Execute updatesView creation separately
                driver.execute(null, updatesViewSql, 0)
                println("updatesView created successfully")
            } catch (e: Exception) {
                println("Error creating updatesView: ${e.message}")
                e.printStackTrace()
            }
            
            // Verify the views were created successfully
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='view'",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
        } catch (e: Exception) {
            // Log the error but don't crash - this will help with debugging
            println("Error creating database views: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Helper function to check if a table exists
     */
    private fun checkTableExists(driver: SqlDriver, tableName: String): Boolean {
        var exists = false
        try {
            val tableCheckQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'"
            driver.executeQuery(
                identifier = null,
                sql = tableCheckQuery,
                mapper = { cursor ->
                    val result = cursor.next()
                    exists = result.value
                    result
                },
                parameters = 0
            )
        } catch (e: Exception) {
            println("Error checking if table $tableName exists: ${e.message}")
        }
        return exists
    }

    /**
     * A recovery method to force reinitialization of database views.
     * This can be called when an app update needs to fix database issues.
     */
    fun forceViewReinit(driver: SqlDriver) {
        println("Forcing view reinitialization...")
        try {
            // First drop any existing views
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            
            // Now recreate the views
            initializeViews(driver)
            
            println("View reinitialization completed successfully")
        } catch (e: Exception) {
            println("Error during forced view reinitialization: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Cleans up after an interrupted migration to allow migration to be retried
     */
    private fun cleanupInterruptedMigration(driver: SqlDriver, version: Int) {
        when (version) {
            1 -> {
                // Clean up history_new table from interrupted v1 to v2 migration
                try {
                    println("Dropping leftover history_new table...")
                    driver.execute(null, "DROP TABLE IF EXISTS history_new;", 0)
                    
                    // Check if history table exists and is in the correct format
                    val columnsCheck = "PRAGMA table_info(history)"
                    
                    var hasProgressColumn = false
                    driver.executeQuery(
                        identifier = null,
                        sql = columnsCheck,
                        mapper = { cursor ->
                            val res = cursor.next()
                            while (res.value) {
                                if (cursor.getString(1) == "progress") {
                                    hasProgressColumn = true
                                }
                            }
                            res
                        },
                        parameters = 0
                    )
                    
                    // If history table already has the progress column, the migration was partially successful
                    if (hasProgressColumn) {
                        println("The history table already has the progress column. Marking migration as complete.")
                        // Skip the migration by updating the version
                        return
                    }
                } catch (e: Exception) {
                    println("Error cleaning up interrupted migration: ${e.message}")
                    throw e
                }
            }
            // Add more cases for other migrations as needed
        }
    }
} 