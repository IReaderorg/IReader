package data

import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations.CURRENT_VERSION
import data.MigrationLogger as Logger

/**
 * DatabaseMigrations class that manages schema migrations between different database versions.
 * It maintains a list of migration scripts and applies them in sequence when needed.
 */
object DatabaseMigrations {
    
    /**
     * Current database schema version. Increment this when adding new migrations.
     */
    const val CURRENT_VERSION = 20
    
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
            13 -> migrateV13toV14(driver)
            14 -> migrateV14toV15(driver)
            15 -> migrateV15toV16(driver)
            16 -> migrateV16toV17(driver)
            17 -> migrateV17toV18(driver)
            18 -> migrateV18toV19(driver)
            19 -> migrateV19toV20(driver)
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
            Logger.logMigrationStart(5, 6)
            
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
            Logger.logTableCreated("bookReview")
            
            // Create indexes for book reviews
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_title ON bookReview(book_title);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_rating ON bookReview(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_review_synced ON bookReview(synced) WHERE synced = 0;", 0)
            Logger.logIndexCreated("bookReview indexes")
            
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
            Logger.logTableCreated("chapterReview")
            
            // Create indexes for chapter reviews
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_book ON chapterReview(book_title);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_book_chapter ON chapterReview(book_title, chapter_name);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_rating ON chapterReview(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_chapter_review_synced ON chapterReview(synced) WHERE synced = 0;", 0)
            Logger.logIndexCreated("chapterReview indexes")
            
            Logger.logMigrationSuccess(6)
            
        } catch (e: Exception) {
            Logger.logMigrationError(6, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 6 to version 7
     * Updates sync_queue table schema for better sync management
     */
    private fun migrateV6toV7(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(6, 7)
            
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
                Logger.logTableDropped("sync_queue")
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
            Logger.logTableCreated("sync_queue")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_status ON sync_queue(status);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_created_at ON sync_queue(created_at);", 0)
            Logger.logIndexCreated("sync_queue indexes")
            
            Logger.logMigrationSuccess(7)
            
        } catch (e: Exception) {
            Logger.logMigrationError(7, e)
        }
    }
    
    /**
     * Migration from version 7 to version 8
     * Reverts sync_queue table to simpler schema
     */
    private fun migrateV7toV8(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(7, 8)
            
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
                Logger.logTableDropped("sync_queue")
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
            Logger.logTableCreated("sync_queue")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_timestamp ON sync_queue(timestamp ASC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_book_id ON sync_queue(book_id);", 0)
            Logger.logIndexCreated("sync_queue indexes")
            
            Logger.logMigrationSuccess(8)
            
        } catch (e: Exception) {
            Logger.logMigrationError(8, e)
        }
    }
    
    /**
     * Migration from version 4 to version 5
     * Fixes sync_queue table and adds indexes for better performance
     */
    private fun migrateV4toV5(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(4, 5)
            
            // Drop and recreate sync_queue table to ensure clean state
            driver.execute(null, "DROP TABLE IF EXISTS sync_queue;", 0)
            Logger.logTableDropped("sync_queue")
            
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
            Logger.logTableCreated("sync_queue")
            
            // Create indexes for better performance
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_timestamp ON sync_queue(timestamp ASC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_queue_book_id ON sync_queue(book_id);", 0)
            Logger.logIndexCreated("sync_queue indexes")
            
            Logger.logMigrationSuccess(5)
            
        } catch (e: Exception) {
            Logger.logMigrationError(5, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 3 to version 4
     * Creates reading_statistics table for tracking reading progress
     */
    private fun migrateV3toV4(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(3, 4)
            
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
                Logger.logColumnAdded("book", "is_pinned")
            }
            
            if (!hasPinnedOrderColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN pinned_order INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "pinned_order")
            }
            
            if (!hasArchivedColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "is_archived")
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
            Logger.logTableCreated("reading_statistics")
            
            // Initialize with a single row
            val initializeStatisticsSql = """
                INSERT OR IGNORE INTO reading_statistics(_id, total_chapters_read, total_reading_time_minutes, reading_streak, last_read_date, total_words_read, books_completed)
                VALUES (1, 0, 0, 0, 0, 0, 0);
            """.trimIndent()
            
            driver.execute(null, initializeStatisticsSql, 0)
            Logger.logDebug("Initialized reading_statistics table")
            
            Logger.logMigrationSuccess(4)
            
        } catch (e: Exception) {
            Logger.logMigrationError(4, e)
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
            Logger.logMigrationStart(2, 3)
            
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
            Logger.logTableCreated("translated_chapter")
            
            // Create indices for translated_chapter
            driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_chapter_id_index ON translated_chapter(chapter_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_book_id_index ON translated_chapter(book_id);", 0)
            Logger.logIndexCreated("translated_chapter indexes")
            
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
            Logger.logTableCreated("glossary")
            
            // Create indices for glossary
            driver.execute(null, "CREATE INDEX IF NOT EXISTS glossary_book_id_index ON glossary(book_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS glossary_source_term_index ON glossary(source_term);", 0)
            Logger.logIndexCreated("glossary indexes")
            
            Logger.logMigrationSuccess(3)
            
        } catch (e: Exception) {
            Logger.logMigrationError(3, e)
            // Don't throw - the tables might already exist which is fine
        }
    }
    
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
                Logger.logInfo("History table not found, creating required tables...")
                
                // Ensure book table exists first (required by chapter foreign key)
                val createBookTableSql = """
                    CREATE TABLE IF NOT EXISTS book(
                        _id INTEGER NOT NULL PRIMARY KEY,
                        source INTEGER NOT NULL,
                        url TEXT NOT NULL,
                        artist TEXT,
                        author TEXT,
                        description TEXT,
                        genre TEXT,
                        title TEXT NOT NULL,
                        status INTEGER NOT NULL,
                        thumbnail_url TEXT,
                        favorite INTEGER NOT NULL,
                        last_update INTEGER,
                        next_update INTEGER,
                        initialized INTEGER NOT NULL,
                        viewer INTEGER NOT NULL,
                        chapter_flags INTEGER NOT NULL,
                        cover_last_modified INTEGER NOT NULL,
                        date_added INTEGER NOT NULL,
                        is_pinned INTEGER NOT NULL DEFAULT 0,
                        pinned_order INTEGER NOT NULL DEFAULT 0,
                        is_archived INTEGER NOT NULL DEFAULT 0
                    );
                """.trimIndent()
                
                driver.execute(null, createBookTableSql, 0)
                Logger.logTableCreated("book")
                
                // Create chapter table with complete schema including type column
                val createChapterTableSql = """
                    CREATE TABLE IF NOT EXISTS chapter(
                        _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        book_id INTEGER NOT NULL,
                        url TEXT NOT NULL,
                        name TEXT NOT NULL,
                        scanlator TEXT,
                        read INTEGER NOT NULL,
                        bookmark INTEGER NOT NULL,
                        last_page_read INTEGER NOT NULL,
                        chapter_number REAL NOT NULL,
                        source_order INTEGER NOT NULL,
                        date_fetch INTEGER NOT NULL,
                        date_upload INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        type INTEGER NOT NULL,
                        FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
                    );
                """.trimIndent()
                
                driver.execute(null, createChapterTableSql, 0)
                Logger.logTableCreated("chapter")
                
                // Create history table with progress column (post-migration schema)
                val createHistoryTableSql = """
                    CREATE TABLE IF NOT EXISTS history(
                        _id INTEGER NOT NULL PRIMARY KEY,
                        chapter_id INTEGER NOT NULL UNIQUE,
                        last_read INTEGER,
                        time_read INTEGER NOT NULL,
                        progress REAL DEFAULT 0.0,
                        FOREIGN KEY(chapter_id) REFERENCES chapter(_id)
                        ON DELETE CASCADE
                    );
                """.trimIndent()
                
                driver.execute(null, createHistoryTableSql, 0)
                Logger.logTableCreated("history")
                
                // Create indexes for history table
                driver.execute(null, "CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);", 0)
                Logger.logIndexCreated("history indexes")
                
                // Create chapter indexes
                driver.execute(null, "CREATE INDEX IF NOT EXISTS chapters_manga_id_index ON chapter(book_id);", 0)
                Logger.logIndexCreated("chapter indexes")
            }
            
        } catch (e: Exception) {
            Logger.logError("Error verifying required tables: ${e.message}", e)
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
                Logger.logWarning("History table still doesn't exist after verification, cannot create views")
                return
            }
            
            // Verify history table has progress column (added in migration 1)
            try {
                val columnsCheck = "PRAGMA table_info(history)"
                var hasProgressColumn = false
                driver.executeQuery(
                    identifier = null,
                    sql = columnsCheck,
                    mapper = { cursor ->
                        var result = cursor.next()
                        while (result.value) {
                            val columnName = cursor.getString(1)
                            if (columnName == "progress") {
                                hasProgressColumn = true
                            }
                            result = cursor.next()
                        }
                        result
                    },
                    parameters = 0
                )
                
                if (!hasProgressColumn) {
                    Logger.logInfo("History table missing progress column, adding it...")
                    try {
                        driver.execute(null, "ALTER TABLE history ADD COLUMN progress REAL DEFAULT 0.0;", 0)
                        Logger.logColumnAdded("history", "progress")
                    } catch (e: Exception) {
                        Logger.logWarning("Could not add progress column: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Logger.logError("Error checking history table schema: ${e.message}", e)
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
                Logger.logViewCreated("historyView")
            } catch (e: Exception) {
                Logger.logViewError("historyView", e)
                // Re-verify tables exist before giving up
                verifyRequiredTables(driver)
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
                Logger.logViewCreated("updatesView")
            } catch (e: Exception) {
                Logger.logViewError("updatesView", e)
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
            Logger.logMigrationStart(8, 9)
            
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
            Logger.logTableCreated("plugin")
            
            // Create indexes for plugin table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_type ON plugin(type);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_status ON plugin(status);", 0)
            Logger.logIndexCreated("plugin indexes")
            
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
            Logger.logTableCreated("plugin_purchase")
            
            // Create indexes for plugin_purchase table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_plugin_id ON plugin_purchase(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_user_id ON plugin_purchase(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_purchase_timestamp ON plugin_purchase(timestamp DESC);", 0)
            Logger.logIndexCreated("plugin_purchase indexes")
            
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
            Logger.logTableCreated("plugin_review")
            
            // Create indexes for plugin_review table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_plugin_id ON plugin_review(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_user_id ON plugin_review(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_rating ON plugin_review(rating DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_review_timestamp ON plugin_review(timestamp DESC);", 0)
            Logger.logIndexCreated("plugin_review indexes")
            
            Logger.logMigrationSuccess(9)
            
        } catch (e: Exception) {
            Logger.logMigrationError(9, e)
        }
    }
    
    /**
     * Migration from version 9 to version 10
     * Adds plugin_trial table for tracking trial periods
     */
    private fun migrateV9toV10(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(9, 10)
            
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
            Logger.logTableCreated("plugin_trial")
            
            // Create indexes for plugin_trial table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_plugin_id ON plugin_trial(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_user_id ON plugin_trial(user_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_trial_active ON plugin_trial(is_active);", 0)
            Logger.logIndexCreated("plugin_trial indexes")
            
            Logger.logMigrationSuccess(10)
            
        } catch (e: Exception) {
            Logger.logMigrationError(10, e)
        }
    }
    
    /**
     * Migration from version 10 to version 11
     * Adds repository_type column to repository table
     */
    private fun migrateV10toV11(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(10, 11)
            
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
                Logger.logColumnAdded("repository", "repository_type")
            }
            
            Logger.logMigrationSuccess(11)
            
        } catch (e: Exception) {
            Logger.logMigrationError(11, e)
        }
    }
    
    /**
     * Migration from version 11 to version 12
     * Removes default repository and its protection trigger
     */
    private fun migrateV11toV12(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(11, 12)
            
            // Drop the trigger that prevents deletion of system repository
            driver.execute(null, "DROP TRIGGER IF EXISTS system_repository_delete_trigger;", 0)
            Logger.logDebug("Dropped system_repository_delete_trigger")
            
            // Delete the default repository with id = -1 if it exists
            driver.execute(null, "DELETE FROM repository WHERE _id = -1;", 0)
            Logger.logDebug("Deleted default repository with id = -1")
            
            Logger.logMigrationSuccess(12)
            
        } catch (e: Exception) {
            Logger.logMigrationError(12, e)
        }
    }
    
    /**
     * Migration from version 12 to version 13
     * Adds repositoryType column to catalog table
     */
    private fun migrateV12toV13(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(12, 13)
            
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
                Logger.logColumnAdded("catalog", "repositoryType")
                
                // Update existing LNReader plugins based on their URL pattern
                driver.execute(null, "UPDATE catalog SET repositoryType = 'LNREADER' WHERE pkgUrl LIKE '%.js';", 0)
                Logger.logDebug("Updated existing LNReader plugins in catalog table")
            }
            
            Logger.logMigrationSuccess(13)
            
        } catch (e: Exception) {
            Logger.logMigrationError(13, e)
        }
    }

    /**
     * Migration from version 13 to version 14
     * Adds update_history table for tracking book updates
     */
    private fun migrateV13toV14(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(13, 14)
            
            // Create update_history table
            val createUpdateHistorySql = """
                CREATE TABLE IF NOT EXISTS update_history(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    book_title TEXT NOT NULL,
                    chapters_added INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createUpdateHistorySql, 0)
            Logger.logTableCreated("update_history")
            
            // Create indexes for update_history table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS update_history_book_id_index ON update_history(book_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS update_history_timestamp_index ON update_history(timestamp);", 0)
            Logger.logIndexCreated("update_history indexes")
            
            Logger.logMigrationSuccess(14)
            
        } catch (e: Exception) {
            Logger.logMigrationError(14, e)
        }
    }
    
    /**
     * Migration from version 14 to version 15
     * Adds sourceReport table for tracking source issues
     */
    private fun migrateV14toV15(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(14, 15)
            
            // Create sourceReport table
            val createSourceReportSql = """
                CREATE TABLE IF NOT EXISTS sourceReport (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    sourceId INTEGER NOT NULL,
                    packageName TEXT NOT NULL,
                    version TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'pending'
                );
            """.trimIndent()
            
            driver.execute(null, createSourceReportSql, 0)
            Logger.logTableCreated("sourceReport")
            
            // Create indexes for sourceReport table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS sourceReport_sourceId_index ON sourceReport(sourceId);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS sourceReport_status_index ON sourceReport(status);", 0)
            Logger.logIndexCreated("sourceReport indexes")
            
            Logger.logMigrationSuccess(15)
            
        } catch (e: Exception) {
            Logger.logMigrationError(15, e)
        }
    }
    
    /**
     * Migration from version 15 to version 16
     * Adds sourceComparison table for comparing sources
     */
    private fun migrateV15toV16(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(15, 16)
            
            // Create sourceComparison table
            val createSourceComparisonSql = """
                CREATE TABLE IF NOT EXISTS sourceComparison(
                    book_id INTEGER NOT NULL PRIMARY KEY,
                    current_source_id INTEGER NOT NULL,
                    better_source_id INTEGER,
                    chapter_difference INTEGER NOT NULL,
                    cached_at INTEGER NOT NULL,
                    dismissed_until INTEGER,
                    FOREIGN KEY(book_id) REFERENCES book (_id)
                    ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createSourceComparisonSql, 0)
            Logger.logTableCreated("sourceComparison")
            
            // Create indexes for sourceComparison table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS source_comparison_cached_at_index ON sourceComparison(cached_at);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS source_comparison_dismissed_until_index ON sourceComparison(dismissed_until);", 0)
            Logger.logIndexCreated("sourceComparison indexes")
            
            Logger.logMigrationSuccess(16)
            
        } catch (e: Exception) {
            Logger.logMigrationError(16, e)
        }
    }
    
    /**
     * Migration from version 16 to version 17
     * Adds nftWallets table for NFT wallet management
     */
    private fun migrateV16toV17(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(16, 17)
            
            // Create nftWallets table
            val createNftWalletsSql = """
                CREATE TABLE IF NOT EXISTS nftWallets(
                    userId TEXT NOT NULL PRIMARY KEY,
                    walletAddress TEXT NOT NULL,
                    lastVerified INTEGER,
                    ownsNFT INTEGER NOT NULL DEFAULT 0,
                    nftTokenId TEXT,
                    cacheExpiresAt INTEGER NOT NULL
                );
            """.trimIndent()
            
            driver.execute(null, createNftWalletsSql, 0)
            Logger.logTableCreated("nftWallets")
            
            Logger.logMigrationSuccess(17)
            
        } catch (e: Exception) {
            Logger.logMigrationError(17, e)
        }
    }
    
    /**
     * Migration from version 17 to version 18
     * Adds chapterHealth table for tracking chapter health status
     */
    private fun migrateV17toV18(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(17, 18)
            
            // Create chapterHealth table
            val createChapterHealthSql = """
                CREATE TABLE IF NOT EXISTS chapterHealth(
                    chapter_id INTEGER NOT NULL PRIMARY KEY,
                    is_broken INTEGER NOT NULL,
                    break_reason TEXT,
                    checked_at INTEGER NOT NULL,
                    repair_attempted_at INTEGER,
                    repair_successful INTEGER,
                    replacement_source_id INTEGER,
                    FOREIGN KEY(chapter_id) REFERENCES chapter (_id)
                    ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createChapterHealthSql, 0)
            Logger.logTableCreated("chapterHealth")
            
            // Create index for chapterHealth table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS chapter_health_checked_at_index ON chapterHealth(checked_at);", 0)
            Logger.logIndexCreated("chapterHealth index")
            
            Logger.logMigrationSuccess(18)
            
        } catch (e: Exception) {
            Logger.logMigrationError(18, e)
        }
    }
    
    /**
     * Migration from version 18 to version 19
     * Adds chapterReport table for user-reported chapter issues
     */
    private fun migrateV18toV19(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(18, 19)
            
            // Create chapterReport table
            val createChapterReportSql = """
                CREATE TABLE IF NOT EXISTS chapterReport (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    chapterId INTEGER NOT NULL,
                    bookId INTEGER NOT NULL,
                    issueCategory TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    resolved INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (chapterId) REFERENCES chapter(_id) ON DELETE CASCADE,
                    FOREIGN KEY (bookId) REFERENCES book(_id) ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createChapterReportSql, 0)
            Logger.logTableCreated("chapterReport")
            
            // Create indexes for chapterReport table
            driver.execute(null, "CREATE INDEX IF NOT EXISTS chapterReport_chapterId_index ON chapterReport(chapterId);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS chapterReport_bookId_index ON chapterReport(bookId);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS chapterReport_resolved_index ON chapterReport(resolved);", 0)
            Logger.logIndexCreated("chapterReport indexes")
            
            Logger.logMigrationSuccess(19)
            
        } catch (e: Exception) {
            Logger.logMigrationError(19, e)
        }
    }
    
    /**
     * Migration from version 19 to version 20
     * Migrates extension repository URLs from old /repo branch to new /repov2 branch
     * This ensures existing users automatically get the updated repository URL
     */
    private fun migrateV19toV20(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(19, 20)
            
            // Old URL pattern: https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repo
            // New URL pattern: https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repov2
            val oldRepoUrl = "https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repo"
            val newRepoUrl = "https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repov2"
            
            // Update repository table - replace old URL with new URL in the key field
            val updateRepositorySql = """
                UPDATE repository 
                SET key = REPLACE(key, '$oldRepoUrl', '$newRepoUrl')
                WHERE key LIKE '%$oldRepoUrl%';
            """.trimIndent()
            
            driver.execute(null, updateRepositorySql, 0)
            Logger.logDebug("Updated repository URLs from /repo to /repov2")
            
            Logger.logMigrationSuccess(20)
            
        } catch (e: Exception) {
            Logger.logMigrationError(20, e)
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