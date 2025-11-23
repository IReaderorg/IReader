package data

import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations.CURRENT_VERSION

/**
 * DatabaseMigrations class that manages schema migrations between different database versions.
 * It maintains a list of migration scripts and applies them in sequence when needed.
 */
object DatabaseMigrations {
    
    /**
     * Current database schema version. Increment this when adding new migrations.
     */
    const val CURRENT_VERSION = 13
    
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
                        // For specific errors, try to recover
                        if (e.message?.contains("table history_new already exists") == true) {
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
                // Try to at least initialize views even if migrations failed
                try {
                    initializeViews(driver)
                } catch (viewErr: Exception) {
                    // Ignore view errors
                }
                throw e
            }
        }
    }
    
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
            2 -> migrateV2toV3(driver)
            3 -> migrateV3toV4(driver)
            4 -> migrateV4toV5(driver)
            5 -> migrateV5toV6(driver)
            6 -> migrateV6toV7(driver)
            7 -> migrateV7toV8(driver)
            8 -> migrateV8toV9(driver)
            9 -> migrateV9toV10(driver)
            10 -> migrateV10toV11(driver)
            11 -> migrateV11toV12(driver)
            12 -> migrateV12toV13(driver)
            // Add more migration cases as the database evolves
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
     * Migration from version 5 to version 6
     * Adds book and chapter review tables
     */
    private fun migrateV5toV6(driver: SqlDriver) {
        try {
            println("Starting migration from version 5 to 6...")
            
            // Create book reviews table
            val createBookReviewSql = """
                CREATE TABLE IF NOT EXISTS bookReview (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_title TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    review_text TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    synced INTEGER NOT NULL DEFAULT 0,
                    UNIQUE(book_title)
                );
            """.trimIndent()
            
            driver.execute(null, createBookReviewSql, 0)
            println("Created bookReview table")
            
            // Create indexes for book reviews
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_title ON bookReview(book_title);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_rating ON bookReview(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_synced ON bookReview(synced) WHERE synced = 0;", 0)
            println("Created indexes for bookReview table")
            
            // Create chapter reviews table
            val createChapterReviewSql = """
                CREATE TABLE IF NOT EXISTS chapterReview (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_title TEXT NOT NULL,
                    chapter_name TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    review_text TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    synced INTEGER NOT NULL DEFAULT 0,
                    UNIQUE(book_title, chapter_name)
                );
            """.trimIndent()
            
            driver.execute(null, createChapterReviewSql, 0)
            println("Created chapterReview table")
            
            // Create indexes for chapter reviews
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_book ON chapterReview(book_title);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_book_chapter ON chapterReview(book_title, chapter_name);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_rating ON chapterReview(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_synced ON chapterReview(synced) WHERE synced = 0;", 0)
            println("Created indexes for chapterReview table")
            
            println("Successfully migrated to version 6")
            
        } catch (e: Exception) {
            println("Error migrating to version 6: ${e.message}")
            e.printStackTrace()
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 6 to version 7
     * Updates sync_queue table schema for better sync management
     */
    private fun migrateV6toV7(driver: SqlDriver) {
        try {
            println("Starting migration from version 6 to 7...")
            
            // Check if sync_queue table exists
            var syncQueueExists = false
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
                driver.execute(null, "DROP TABLE IF EXISTS sync_queue;", 0)
                println("Dropped old sync_queue table")
            }
            
            // Create new sync_queue table with updated schema
            val createSyncQueueSql = """
                CREATE TABLE sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    operation TEXT NOT NULL,
                    entity_type TEXT NOT NULL,
                    entity_id TEXT NOT NULL,
                    data TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    retry_count INTEGER NOT NULL DEFAULT 0,
                    last_error TEXT,
                    status TEXT NOT NULL DEFAULT 'pending'
                );
            """.trimIndent()
            
            driver.execute(null, createSyncQueueSql, 0)
            println("Created new sync_queue table with updated schema")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_status ON sync_queue(status);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_created_at ON sync_queue(created_at);", 0)
            println("Created indexes for sync_queue table")
            
            println("Successfully migrated to version 7")
            
        } catch (e: Exception) {
            println("Error migrating to version 7: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 7 to version 8
     * Reverts sync_queue table to simpler schema
     */
    private fun migrateV7toV8(driver: SqlDriver) {
        try {
            println("Starting migration from version 7 to 8...")
            
            // Check if sync_queue table exists
            var syncQueueExists = false
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
                driver.execute(null, "DROP TABLE IF EXISTS sync_queue;", 0)
                println("Dropped old sync_queue table")
            }
            
            // Create sync_queue table with final schema (matching sync_queue.sq)
            val createSyncQueueSql = """
                CREATE TABLE sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id TEXT NOT NULL,
                    data TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    retry_count INTEGER DEFAULT 0
                );
            """.trimIndent()
            
            driver.execute(null, createSyncQueueSql, 0)
            println("Created sync_queue table with final schema")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_timestamp ON sync_queue(timestamp ASC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_book_id ON sync_queue(book_id);", 0)
            println("Created indexes for sync_queue table")
            
            println("Successfully migrated to version 8")
            
        } catch (e: Exception) {
            println("Error migrating to version 8: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 4 to version 5
     * Fixes sync_queue table and adds indexes for better performance
     */
    private fun migrateV4toV5(driver: SqlDriver) {
        try {
            println("Starting migration from version 4 to 5...")
            
            // Drop and recreate sync_queue table to ensure clean state
            driver.execute(null, "DROP TABLE IF EXISTS sync_queue;", 0)
            println("Dropped old sync_queue table")
            
            // Create sync_queue table with proper schema
            val createSyncQueueSql = """
                CREATE TABLE sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id TEXT NOT NULL,
                    data TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    retry_count INTEGER DEFAULT 0
                );
            """.trimIndent()
            
            driver.execute(null, createSyncQueueSql, 0)
            println("Created new sync_queue table")
            
            // Create indexes for better performance
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_timestamp ON sync_queue(timestamp ASC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_book_id ON sync_queue(book_id);", 0)
            println("Created indexes for sync_queue table")
            
            println("Successfully migrated to version 5")
            
        } catch (e: Exception) {
            println("Error migrating to version 5: ${e.message}")
            e.printStackTrace()
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 3 to version 4
     * Creates reading_statistics table for tracking reading progress
     */
    private fun migrateV3toV4(driver: SqlDriver) {
        try {
                        // Check if columns already exist
            val columnsCheck = "PRAGMA table_info(book)"
            var hasPinnedColumn = false
            var hasPinnedOrderColumn = false
            var hasArchivedColumn = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        when (columnName) {
                            "is_pinned" -> hasPinnedColumn = true
                            "pinned_order" -> hasPinnedOrderColumn = true
                            "is_archived" -> hasArchivedColumn = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            // Add missing columns
            if (!hasPinnedColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0;", 0)
                println("Added is_pinned column to book table")
            }
            
            if (!hasPinnedOrderColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN pinned_order INTEGER NOT NULL DEFAULT 0;", 0)
                println("Added pinned_order column to book table")
            }
            
            if (!hasArchivedColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0;", 0)
                println("Added is_archived column to book table")
            }

            // Create reading_statistics table
            val createReadingStatisticsSql = """
                CREATE TABLE IF NOT EXISTS reading_statistics(
                    _id INTEGER NOT NULL PRIMARY KEY,
                    total_chapters_read INTEGER NOT NULL DEFAULT 0,
                    total_reading_time_minutes INTEGER NOT NULL DEFAULT 0,
                    reading_streak INTEGER NOT NULL DEFAULT 0,
                    last_read_date INTEGER,
                    total_words_read INTEGER NOT NULL DEFAULT 0,
                    books_completed INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent()
            
            driver.execute(null, createReadingStatisticsSql, 0)
            println("Created reading_statistics table")
            
            // Initialize with a single row
            val initializeStatisticsSql = """
                INSERT OR IGNORE INTO reading_statistics(_id, total_chapters_read, total_reading_time_minutes, reading_streak, last_read_date, total_words_read, books_completed)
                VALUES (1, 0, 0, 0, 0, 0, 0);
            """.trimIndent()
            
            driver.execute(null, initializeStatisticsSql, 0)
            println("Initialized reading_statistics table")
            
            println("Successfully migrated to version 4")
            
        } catch (e: Exception) {
            println("Error migrating to version 4: ${e.message}")
            e.printStackTrace()
            // Don't throw - the table might already exist which is fine
        }
    }
    
    /**
     * Migration from version 2 to version 3
     * Adds translation and glossary tables
     * This migration applies the SQL from 2.sqm migration file
     */
    private fun migrateV2toV3(driver: SqlDriver) {
        try {
            // Create translated_chapter table
            val createTranslatedChapterSql = """
                CREATE TABLE IF NOT EXISTS translated_chapter(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    chapter_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    source_language TEXT NOT NULL,
                    target_language TEXT NOT NULL,
                    translator_engine_id INTEGER NOT NULL,
                    translated_content TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY(chapter_id) REFERENCES chapter (_id) ON DELETE CASCADE,
                    FOREIGN KEY(book_id) REFERENCES book (_id) ON DELETE CASCADE,
                    UNIQUE(chapter_id, target_language, translator_engine_id)
                );
            """.trimIndent()
            
            driver.execute(null, createTranslatedChapterSql, 0)
            
            // Create indices for translated_chapter
            driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_chapter_id_index ON translated_chapter(chapter_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_book_id_index ON translated_chapter(book_id);", 0)
            
            // Create glossary table
            val createGlossarySql = """
                CREATE TABLE IF NOT EXISTS glossary(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    source_term TEXT NOT NULL,
                    target_term TEXT NOT NULL,
                    term_type TEXT NOT NULL,
                    notes TEXT,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY(book_id) REFERENCES book (_id) ON DELETE CASCADE,
                    UNIQUE(book_id, source_term)
                );
            """.trimIndent()
            
            driver.execute(null, createGlossarySql, 0)
            
            // Create indices for glossary
            driver.execute(null, "CREATE INDEX IF NOT EXISTS glossary_book_id_index ON glossary(book_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS glossary_source_term_index ON glossary(source_term);", 0)
            
            println("Successfully created translation tables")
            
        } catch (e: Exception) {
            println("Error creating translation tables: ${e.message}")
            e.printStackTrace()
            // Don't throw - the tables might already exist which is fine
        }
    }
    
    private fun verifyRequiredTables(driver: SqlDriver) {
        try {
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
                }
                
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
                } catch (e: Exception) {
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
                }
                
                driver.execute(null, "CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);", 0)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Initialize or recreate database views
     * This should be called after running all migrations to ensure views are properly set up
     */
    private fun initializeViews(driver: SqlDriver) {
        try {
            verifyRequiredTables(driver)
            
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
            
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            
            val historyTableExists = checkTableExists(driver, "history")
            if (!historyTableExists) {
                return
            }
            
            val categoryInitSql ="""
                INSERT OR IGNORE INTO categories(_id, name, sort, flags) VALUES (0, "", -1, 0);
                INSERT OR IGNORE INTO categories(_id, name, sort, flags) VALUES (-1, "", -1, 0);
                CREATE TRIGGER IF NOT EXISTS system_category_delete_trigger BEFORE DELETE
                ON categories
                BEGIN SELECT CASE
                    WHEN old._id <= 0 THEN
                        RAISE(ABORT, "System category can't be deleted")
                    END;
                END;
            """.trimIndent()
            
            try {
                driver.execute(null, categoryInitSql, 0)
            } catch (e: Exception) {
                // Ignore
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
            
            try {
                driver.execute(null, historyViewSql, 0)
            } catch (e: Exception) {
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
                    chapter.content IS NOT '' AS downlaoded,
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
            
            try {
                driver.execute(null, updatesViewSql, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='view'",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
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
            // Ignore
        }
        return exists
    }

    fun forceViewReinit(driver: SqlDriver) {
        try {
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            initializeViews(driver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 8 to version 9
     * Adds plugin system tables (plugin, plugin_purchase, plugin_review)
     */
    private fun migrateV8toV9(driver: SqlDriver) {
        try {
            println("Starting migration from version 8 to 9...")
            
            // Create plugin table
            val createPluginSql = """
                CREATE TABLE IF NOT EXISTS plugin (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    version TEXT NOT NULL,
                    version_code INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    author TEXT NOT NULL,
                    description TEXT NOT NULL,
                    icon_url TEXT,
                    status TEXT NOT NULL,
                    install_date INTEGER NOT NULL,
                    last_update INTEGER,
                    manifest_json TEXT NOT NULL
                );
            """.trimIndent()
            
            driver.execute(null, createPluginSql, 0)
            println("Created plugin table")
            
            // Create indexes for plugin table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_type ON plugin(type);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_status ON plugin(status);", 0)
            println("Created indexes for plugin table")
            
            // Create plugin_purchase table
            val createPluginPurchaseSql = """
                CREATE TABLE IF NOT EXISTS plugin_purchase (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    feature_id TEXT,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    user_id TEXT NOT NULL,
                    receipt_data TEXT,
                    FOREIGN KEY (plugin_id) REFERENCES plugin(id) ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createPluginPurchaseSql, 0)
            println("Created plugin_purchase table")
            
            // Create indexes for plugin_purchase table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_plugin_id ON plugin_purchase(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_user_id ON plugin_purchase(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_timestamp ON plugin_purchase(timestamp DESC);", 0)
            println("Created indexes for plugin_purchase table")
            
            // Create plugin_review table
            val createPluginReviewSql = """
                CREATE TABLE IF NOT EXISTS plugin_review (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    user_id TEXT NOT NULL,
                    rating REAL NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
                    review_text TEXT,
                    timestamp INTEGER NOT NULL,
                    helpful INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (plugin_id) REFERENCES plugin(id) ON DELETE CASCADE,
                    UNIQUE(plugin_id, user_id)
                );
            """.trimIndent()
            
            driver.execute(null, createPluginReviewSql, 0)
            println("Created plugin_review table")
            
            // Create indexes for plugin_review table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_plugin_id ON plugin_review(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_user_id ON plugin_review(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_rating ON plugin_review(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_timestamp ON plugin_review(timestamp DESC);", 0)
            println("Created indexes for plugin_review table")
            
            println("Successfully migrated to version 9")
            
        } catch (e: Exception) {
            println("Error migrating to version 9: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 9 to version 10
     * Adds plugin_trial table for tracking trial periods
     */
    private fun migrateV9toV10(driver: SqlDriver) {
        try {
            println("Starting migration from version 9 to 10...")
            
            // Create plugin_trial table
            val createPluginTrialSql = """
                CREATE TABLE IF NOT EXISTS plugin_trial (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    user_id TEXT NOT NULL,
                    start_date INTEGER NOT NULL,
                    expiration_date INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (plugin_id) REFERENCES plugin(id) ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createPluginTrialSql, 0)
            println("Created plugin_trial table")
            
            // Create indexes for plugin_trial table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_plugin_id ON plugin_trial(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_user_id ON plugin_trial(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_active ON plugin_trial(is_active);", 0)
            println("Created indexes for plugin_trial table")
            
            println("Successfully migrated to version 10")
            
        } catch (e: Exception) {
            println("Error migrating to version 10: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 10 to version 11
     * Adds repository_type column to repository table
     */
    private fun migrateV10toV11(driver: SqlDriver) {
        try {
            println("Starting migration from version 10 to 11...")
            
            // Check if repository_type column already exists
            val columnsCheck = "PRAGMA table_info(repository)"
            var hasRepositoryTypeColumn = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName == "repository_type") {
                            hasRepositoryTypeColumn = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            // Add repository_type column if it doesn't exist
            if (!hasRepositoryTypeColumn) {
                driver.execute(null, "ALTER TABLE repository ADD COLUMN repository_type TEXT NOT NULL DEFAULT 'IREADER';", 0)
                println("Added repository_type column to repository table")
            }
            
            println("Successfully migrated to version 11")
            
        } catch (e: Exception) {
            println("Error migrating to version 11: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 11 to version 12
     * Removes default repository and its protection trigger
     */
    private fun migrateV11toV12(driver: SqlDriver) {
        try {
            println("Starting migration from version 11 to 12...")
            
            // Drop the trigger that prevents deletion of system repository
            driver.execute(null, "DROP TRIGGER IF EXISTS system_repository_delete_trigger;", 0)
            println("Dropped system_repository_delete_trigger")
            
            // Delete the default repository with id = -1 if it exists
            driver.execute(null, "DELETE FROM repository WHERE _id = -1;", 0)
            println("Deleted default repository with id = -1")
            
            println("Successfully migrated to version 12")
            
        } catch (e: Exception) {
            println("Error migrating to version 12: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Migration from version 12 to version 13
     * Adds repositoryType column to catalog table
     */
    private fun migrateV12toV13(driver: SqlDriver) {
        try {
            println("Starting migration from version 12 to 13...")
            
            // Check if repositoryType column already exists
            val columnsCheck = "PRAGMA table_info(catalog)"
            var hasRepositoryTypeColumn = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName == "repositoryType") {
                            hasRepositoryTypeColumn = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            // Add repositoryType column if it doesn't exist
            if (!hasRepositoryTypeColumn) {
                driver.execute(null, "ALTER TABLE catalog ADD COLUMN repositoryType TEXT NOT NULL DEFAULT 'IREADER';", 0)
                println("Added repositoryType column to catalog table")
                
                // Update existing LNReader plugins based on their URL pattern
                driver.execute(null, "UPDATE catalog SET repositoryType = 'LNREADER' WHERE pkgUrl LIKE '%.js';", 0)
                println("Updated existing LNReader plugins in catalog table")
            }
            
            println("Successfully migrated to version 13")
            
        } catch (e: Exception) {
            println("Error migrating to version 13: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cleanupInterruptedMigration(driver: SqlDriver, version: Int) {
        when (version) {
            1 -> {
                try {
                    driver.execute(null, "DROP TABLE IF EXISTS history_new;", 0)
                    
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
                    
                    if (hasProgressColumn) {
                        return
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            2 -> {
                try {
                    driver.execute(null, "DROP TABLE IF EXISTS book_backup;", 0)
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }
} 