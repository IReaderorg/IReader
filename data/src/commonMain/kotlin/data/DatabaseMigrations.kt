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
    const val CURRENT_VERSION = 37
    
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
            20 -> migrateV20toV21(driver)
            21 -> migrateV21toV22(driver)
            22 -> migrateV22toV23(driver)
            23 -> migrateV23toV24(driver)
            24 -> migrateV24toV25(driver)
            25 -> migrateV25toV26(driver)
            26 -> migrateV26toV27(driver)
            27 -> migrateV27toV28(driver)
            28 -> migrateV28toV29(driver)
            29 -> migrateV29toV30(driver)
            30 -> migrateV30toV31(driver)
            31 -> migrateV31toV32(driver)
            32 -> migrateV32toV33(driver)
            33 -> migrateV33toV34(driver)
            34 -> migrateV34toV35(driver)
            35 -> migrateV35toV36(driver)
            36 -> migrateV36toV37(driver)
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
            
            // Create historyView with progress column and customCover
            val historyViewSql = """
                CREATE VIEW IF NOT EXISTS historyView AS
                SELECT
                    history._id AS id,
                    book._id AS bookId,
                    chapter._id AS chapterId,
                    chapter.name AS chapterName,
                    book.title,
                    book.thumbnail_url AS thumbnailUrl,
                    COALESCE(book.custom_cover, '') AS customCover,
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
            
            // Create updatesView with progress information and customCover
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
                    COALESCE(book.custom_cover, '') AS customCover,
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
            // First ensure all required columns exist
            ensureRequiredColumns(driver)
            
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            initializeViews(driver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Ensures all required columns exist in the database tables.
     * This is a repair mechanism for cases where migrations may have failed silently.
     * Public so it can be called from JvmDatabaseHandler.repairDatabase()
     */
    fun ensureRequiredColumns(driver: SqlDriver) {
        try {
            // Check and add chapter_page column to book table if missing
            val columnsCheck = "PRAGMA table_info(book)"
            var hasChapterPageColumn = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName == "chapter_page") {
                            hasChapterPageColumn = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            if (!hasChapterPageColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN chapter_page INTEGER NOT NULL DEFAULT 1;", 0)
                println("[DatabaseMigrations] Added missing chapter_page column to book table")
            }
            
            // Ensure category_auto_rules table exists
            ensureCategoryAutoRulesTable(driver)
        } catch (e: Exception) {
            println("[DatabaseMigrations] Error ensuring required columns: ${e.message}")
        }
    }
    
    /**
     * Ensures the category_auto_rules table exists.
     * This is called from ensureRequiredColumns to handle cases where the migration didn't run.
     */
    private fun ensureCategoryAutoRulesTable(driver: SqlDriver) {
        try {
            // Check if table exists
            var tableExists = false
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='category_auto_rules'",
                mapper = { cursor ->
                    val result = cursor.next()
                    tableExists = result.value
                    result
                },
                parameters = 0
            )
            
            if (!tableExists) {
                // Create category_auto_rules table
                val createCategoryAutoRulesTableSql = """
                    CREATE TABLE IF NOT EXISTS category_auto_rules(
                        _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        category_id INTEGER NOT NULL,
                        rule_type TEXT NOT NULL,
                        value TEXT NOT NULL,
                        is_enabled INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(category_id) REFERENCES categories (_id)
                        ON DELETE CASCADE
                    );
                """.trimIndent()
                
                driver.execute(null, createCategoryAutoRulesTableSql, 0)
                println("[DatabaseMigrations] Created missing category_auto_rules table")
                
                // Create indexes
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_category_auto_rules_category_id ON category_auto_rules(category_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_category_auto_rules_type ON category_auto_rules(rule_type);", 0)
                println("[DatabaseMigrations] Created category_auto_rules indexes")
            }
        } catch (e: Exception) {
            println("[DatabaseMigrations] Error ensuring category_auto_rules table: ${e.message}")
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

    /**
     * Migration from version 20 to version 21
     * Adds multiple new tables: user_source, character tables, global_glossary, 
     * piperVoice, reader_theme, reading analytics, custom fonts, and plugin tables
     */
    private fun migrateV20toV21(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(20, 21)
            
            // ==================== User Source Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS user_source (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    source_url TEXT NOT NULL UNIQUE,
                    source_name TEXT NOT NULL,
                    source_group TEXT NOT NULL DEFAULT '',
                    source_type INTEGER NOT NULL DEFAULT 0,
                    enabled INTEGER NOT NULL DEFAULT 1,
                    lang TEXT NOT NULL DEFAULT 'en',
                    custom_order INTEGER NOT NULL DEFAULT 0,
                    comment TEXT NOT NULL DEFAULT '',
                    last_update_time INTEGER NOT NULL DEFAULT 0,
                    header TEXT NOT NULL DEFAULT '',
                    search_url TEXT NOT NULL DEFAULT '',
                    explore_url TEXT NOT NULL DEFAULT '',
                    rule_search TEXT NOT NULL DEFAULT '{}',
                    rule_book_info TEXT NOT NULL DEFAULT '{}',
                    rule_toc TEXT NOT NULL DEFAULT '{}',
                    rule_content TEXT NOT NULL DEFAULT '{}',
                    rule_explore TEXT NOT NULL DEFAULT '{}'
                );
            """.trimIndent(), 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_user_source_enabled ON user_source(enabled);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_user_source_group ON user_source(source_group);", 0)
            Logger.logTableCreated("user_source")
            
            // ==================== Character Tables ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    aliases TEXT NOT NULL DEFAULT '',
                    description TEXT NOT NULL DEFAULT '',
                    image_url TEXT,
                    role TEXT NOT NULL DEFAULT 'UNKNOWN',
                    traits TEXT NOT NULL DEFAULT '',
                    tags TEXT NOT NULL DEFAULT '',
                    book_ids TEXT NOT NULL DEFAULT '',
                    series_id TEXT,
                    first_appearance_id TEXT,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    is_user_created INTEGER NOT NULL DEFAULT 0,
                    confidence REAL NOT NULL DEFAULT 1.0,
                    metadata TEXT NOT NULL DEFAULT '{}'
                );
            """.trimIndent(), 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_character_name ON character(name);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_character_series ON character(series_id);", 0)
            Logger.logTableCreated("character")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character_relationship (
                    id TEXT NOT NULL PRIMARY KEY,
                    character1_id TEXT NOT NULL,
                    character2_id TEXT NOT NULL,
                    relationship_type TEXT NOT NULL,
                    custom_type TEXT,
                    description TEXT NOT NULL DEFAULT '',
                    strength REAL NOT NULL DEFAULT 0.5,
                    is_symmetric INTEGER NOT NULL DEFAULT 1,
                    book_ids TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY (character1_id) REFERENCES character(id) ON DELETE CASCADE,
                    FOREIGN KEY (character2_id) REFERENCES character(id) ON DELETE CASCADE
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("character_relationship")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character_appearance (
                    id TEXT NOT NULL PRIMARY KEY,
                    character_id TEXT NOT NULL,
                    book_id INTEGER NOT NULL,
                    chapter_id INTEGER NOT NULL,
                    chapter_title TEXT NOT NULL,
                    paragraph_index INTEGER NOT NULL,
                    text_snippet TEXT NOT NULL,
                    appearance_type TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("character_appearance")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character_note (
                    id TEXT NOT NULL PRIMARY KEY,
                    character_id TEXT NOT NULL,
                    content TEXT NOT NULL,
                    book_id INTEGER,
                    chapter_id INTEGER,
                    note_type TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("character_note")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character_timeline_event (
                    id TEXT NOT NULL PRIMARY KEY,
                    character_id TEXT NOT NULL,
                    book_id INTEGER NOT NULL,
                    chapter_id INTEGER NOT NULL,
                    event_type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    order_index INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY (character_id) REFERENCES character(id) ON DELETE CASCADE
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("character_timeline_event")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS character_group (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    character_ids TEXT NOT NULL DEFAULT '',
                    group_type TEXT NOT NULL,
                    book_ids TEXT NOT NULL DEFAULT '',
                    image_url TEXT,
                    created_at INTEGER NOT NULL
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("character_group")
            
            // ==================== Global Glossary Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS global_glossary(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    book_key TEXT NOT NULL,
                    book_title TEXT NOT NULL,
                    source_term TEXT NOT NULL,
                    target_term TEXT NOT NULL,
                    term_type TEXT NOT NULL,
                    notes TEXT,
                    source_language TEXT NOT NULL DEFAULT 'auto',
                    target_language TEXT NOT NULL DEFAULT 'en',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    synced_at INTEGER,
                    remote_id TEXT,
                    UNIQUE(book_key, source_term)
                );
            """.trimIndent(), 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS global_glossary_book_key_index ON global_glossary(book_key);", 0)
            Logger.logTableCreated("global_glossary")
            
            // ==================== Piper Voice Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS piperVoice (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    language TEXT NOT NULL,
                    locale TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    quality TEXT NOT NULL,
                    sampleRate INTEGER NOT NULL DEFAULT 22050,
                    modelSize INTEGER NOT NULL DEFAULT 0,
                    downloadUrl TEXT NOT NULL,
                    configUrl TEXT NOT NULL,
                    checksum TEXT NOT NULL DEFAULT '',
                    license TEXT NOT NULL DEFAULT 'MIT',
                    description TEXT NOT NULL DEFAULT '',
                    tags TEXT NOT NULL DEFAULT '',
                    isDownloaded INTEGER NOT NULL DEFAULT 0,
                    lastUpdated INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("piperVoice")
            
            // ==================== Reader Theme Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reader_theme (
                    _id INTEGER NOT NULL PRIMARY KEY,
                    background_color INTEGER NOT NULL,
                    on_textcolor INTEGER NOT NULL
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("reader_theme")
            
            // ==================== Custom Fonts Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS customFonts(
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    filePath TEXT NOT NULL,
                    isSystemFont INTEGER NOT NULL DEFAULT 0,
                    dateAdded INTEGER NOT NULL
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("customFonts")
            
            // ==================== Reading Analytics Tables ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reading_session (
                    id TEXT NOT NULL PRIMARY KEY,
                    book_id INTEGER NOT NULL,
                    book_title TEXT NOT NULL,
                    start_time INTEGER NOT NULL,
                    end_time INTEGER,
                    start_chapter_id INTEGER NOT NULL,
                    end_chapter_id INTEGER,
                    start_position INTEGER NOT NULL,
                    end_position INTEGER,
                    pages_read INTEGER NOT NULL DEFAULT 0,
                    words_read INTEGER NOT NULL DEFAULT 0,
                    characters_read INTEGER NOT NULL DEFAULT 0,
                    pause_duration_ms INTEGER NOT NULL DEFAULT 0,
                    device_type TEXT NOT NULL,
                    is_completed INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_session_book ON reading_session(book_id);", 0)
            Logger.logTableCreated("reading_session")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS daily_reading_stats (
                    date TEXT NOT NULL PRIMARY KEY,
                    total_reading_time_ms INTEGER NOT NULL DEFAULT 0,
                    sessions_count INTEGER NOT NULL DEFAULT 0,
                    books_read INTEGER NOT NULL DEFAULT 0,
                    chapters_read INTEGER NOT NULL DEFAULT 0,
                    pages_read INTEGER NOT NULL DEFAULT 0,
                    words_read INTEGER NOT NULL DEFAULT 0,
                    average_wpm REAL NOT NULL DEFAULT 0,
                    longest_session_ms INTEGER NOT NULL DEFAULT 0,
                    peak_reading_hour INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("daily_reading_stats")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reading_goal (
                    id TEXT NOT NULL PRIMARY KEY,
                    type TEXT NOT NULL,
                    target INTEGER NOT NULL,
                    period TEXT NOT NULL,
                    start_date INTEGER NOT NULL,
                    end_date INTEGER,
                    current_progress INTEGER NOT NULL DEFAULT 0,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    completed_date INTEGER,
                    streak_days INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("reading_goal")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reading_achievement (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    icon_url TEXT,
                    category TEXT NOT NULL,
                    tier TEXT NOT NULL,
                    requirement_type TEXT NOT NULL,
                    requirement_value INTEGER NOT NULL,
                    requirement_description TEXT NOT NULL,
                    progress INTEGER NOT NULL DEFAULT 0,
                    is_unlocked INTEGER NOT NULL DEFAULT 0,
                    unlocked_date INTEGER,
                    points INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("reading_achievement")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reading_milestone (
                    id TEXT NOT NULL PRIMARY KEY,
                    type TEXT NOT NULL,
                    value INTEGER NOT NULL,
                    reached_date INTEGER NOT NULL,
                    book_id INTEGER,
                    book_title TEXT
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("reading_milestone")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS reading_streak (
                    id INTEGER NOT NULL PRIMARY KEY,
                    current_streak INTEGER NOT NULL DEFAULT 0,
                    longest_streak INTEGER NOT NULL DEFAULT 0,
                    last_read_date TEXT,
                    streak_start_date TEXT
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("reading_streak")
            
            // ==================== Plugin Analytics Tables ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_analytics_event (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    event_type TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    session_id TEXT NOT NULL,
                    user_id TEXT,
                    properties_json TEXT NOT NULL DEFAULT '{}',
                    metrics_json TEXT NOT NULL DEFAULT '{}',
                    device_info_json TEXT,
                    uploaded INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_analytics_event")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_crash_report (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    plugin_version TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    error_type TEXT NOT NULL,
                    error_message TEXT NOT NULL,
                    stack_trace TEXT NOT NULL,
                    device_info_json TEXT NOT NULL,
                    breadcrumbs_json TEXT NOT NULL DEFAULT '[]',
                    custom_data_json TEXT NOT NULL DEFAULT '{}',
                    uploaded INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_crash_report")
            
            // ==================== Plugin Cache Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_cache (
                    plugin_id TEXT NOT NULL,
                    version TEXT NOT NULL,
                    plugin_name TEXT NOT NULL,
                    version_code INTEGER NOT NULL,
                    cached_at INTEGER NOT NULL,
                    expires_at INTEGER,
                    file_path TEXT NOT NULL,
                    file_size INTEGER NOT NULL,
                    checksum TEXT NOT NULL,
                    is_update INTEGER NOT NULL DEFAULT 0,
                    current_installed_version TEXT,
                    download_url TEXT NOT NULL,
                    status TEXT NOT NULL,
                    PRIMARY KEY (plugin_id, version)
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_cache")
            
            // ==================== Plugin Collection Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_collection (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    author_id TEXT NOT NULL,
                    author_name TEXT NOT NULL,
                    plugin_ids TEXT NOT NULL DEFAULT '',
                    cover_image_url TEXT,
                    tags TEXT NOT NULL DEFAULT '',
                    is_public INTEGER NOT NULL DEFAULT 1,
                    is_featured INTEGER NOT NULL DEFAULT 0,
                    likes_count INTEGER NOT NULL DEFAULT 0,
                    saves_count INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    is_synced INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_collection")
            
            // ==================== Plugin Permission Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_permission (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    plugin_id TEXT NOT NULL,
                    permission TEXT NOT NULL,
                    granted_at INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    UNIQUE(plugin_id, permission)
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_permission")
            
            // ==================== Plugin Pipeline Table ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_pipeline (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    steps_json TEXT NOT NULL,
                    input_type TEXT NOT NULL,
                    output_type TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    is_public INTEGER NOT NULL DEFAULT 0,
                    author_id TEXT,
                    tags TEXT NOT NULL DEFAULT ''
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_pipeline")
            
            // ==================== Plugin Sync Tables ====================
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_sync_change (
                    id TEXT NOT NULL PRIMARY KEY,
                    change_type TEXT NOT NULL,
                    entity_type TEXT NOT NULL,
                    entity_id TEXT NOT NULL,
                    old_value TEXT,
                    new_value TEXT,
                    timestamp INTEGER NOT NULL,
                    synced INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_sync_change")
            
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS plugin_sync_conflict (
                    id TEXT NOT NULL PRIMARY KEY,
                    plugin_id TEXT NOT NULL,
                    conflict_type TEXT NOT NULL,
                    local_value TEXT NOT NULL,
                    remote_value TEXT NOT NULL,
                    local_timestamp INTEGER NOT NULL,
                    remote_timestamp INTEGER NOT NULL,
                    device_id TEXT NOT NULL,
                    resolved INTEGER NOT NULL DEFAULT 0,
                    resolution TEXT
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("plugin_sync_conflict")
            
            Logger.logMigrationSuccess(21)
            
        } catch (e: Exception) {
            Logger.logMigrationError(21, e)
        }
    }

    /**
     * Migration from version 21 to version 22
     * Adds Piper Voice Model table for TTS voice catalog
     */
    private fun migrateV21toV22(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(21, 22)
            
            val createPiperVoiceSql = """
                CREATE TABLE IF NOT EXISTS piperVoice (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    language TEXT NOT NULL,
                    locale TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    quality TEXT NOT NULL,
                    sampleRate INTEGER NOT NULL DEFAULT 22050,
                    modelSize INTEGER NOT NULL DEFAULT 0,
                    downloadUrl TEXT NOT NULL,
                    configUrl TEXT NOT NULL,
                    checksum TEXT NOT NULL DEFAULT '',
                    license TEXT NOT NULL DEFAULT 'MIT',
                    description TEXT NOT NULL DEFAULT '',
                    tags TEXT NOT NULL DEFAULT '',
                    isDownloaded INTEGER NOT NULL DEFAULT 0,
                    lastUpdated INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent()
            
            driver.execute(null, createPiperVoiceSql, 0)
            Logger.logTableCreated("piperVoice")
            
            Logger.logMigrationSuccess(22)
            
        } catch (e: Exception) {
            Logger.logMigrationError(22, e)
        }
    }

    /**
     * Migration from version 22 to version 23
     * Adds global_glossary table for glossaries independent of library books
     */
    private fun migrateV22toV23(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(22, 23)
            
            val createGlobalGlossarySql = """
                CREATE TABLE IF NOT EXISTS global_glossary(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    book_key TEXT NOT NULL,
                    book_title TEXT NOT NULL,
                    source_term TEXT NOT NULL,
                    target_term TEXT NOT NULL,
                    term_type TEXT NOT NULL,
                    notes TEXT,
                    source_language TEXT NOT NULL DEFAULT 'auto',
                    target_language TEXT NOT NULL DEFAULT 'en',
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    synced_at INTEGER,
                    remote_id TEXT,
                    UNIQUE(book_key, source_term)
                );
            """.trimIndent()
            
            driver.execute(null, createGlobalGlossarySql, 0)
            Logger.logTableCreated("global_glossary")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS global_glossary_book_key_index ON global_glossary(book_key);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS global_glossary_source_term_index ON global_glossary(source_term);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS global_glossary_remote_id_index ON global_glossary(remote_id);", 0)
            Logger.logIndexCreated("global_glossary indexes")
            
            Logger.logMigrationSuccess(23)
            
        } catch (e: Exception) {
            Logger.logMigrationError(23, e)
        }
    }

    /**
     * Migration from version 23 to version 24
     * Adds plugin_repository table for managing plugin sources
     */
    private fun migrateV23toV24(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(23, 24)
            
            val createPluginRepositorySql = """
                CREATE TABLE IF NOT EXISTS plugin_repository (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    url TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL DEFAULT 1,
                    is_official INTEGER NOT NULL DEFAULT 0,
                    plugin_count INTEGER NOT NULL DEFAULT 0,
                    last_updated INTEGER NOT NULL DEFAULT 0,
                    last_error TEXT,
                    created_at INTEGER NOT NULL
                );
            """.trimIndent()
            
            driver.execute(null, createPluginRepositorySql, 0)
            Logger.logTableCreated("plugin_repository")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_repository_enabled ON plugin_repository(is_enabled);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_repository_official ON plugin_repository(is_official);", 0)
            Logger.logIndexCreated("plugin_repository indexes")
            
            Logger.logMigrationSuccess(24)
            
        } catch (e: Exception) {
            Logger.logMigrationError(24, e)
        }
    }

    /**
     * Migration from version 24 to version 25
     * Adds plugin_permission table for storing granted permissions
     */
    private fun migrateV24toV25(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(24, 25)
            
            val createPluginPermissionSql = """
                CREATE TABLE IF NOT EXISTS plugin_permission (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    plugin_id TEXT NOT NULL,
                    permission TEXT NOT NULL,
                    granted_at INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    UNIQUE(plugin_id, permission)
                );
            """.trimIndent()
            
            driver.execute(null, createPluginPermissionSql, 0)
            Logger.logTableCreated("plugin_permission")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_permission_plugin ON plugin_permission(plugin_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_plugin_permission_active ON plugin_permission(is_active);", 0)
            Logger.logIndexCreated("plugin_permission indexes")
            
            Logger.logMigrationSuccess(25)
            
        } catch (e: Exception) {
            Logger.logMigrationError(25, e)
        }
    }

    /**
     * Migration from version 25 to version 26
     * Adds Reading Buddy fields to reading_statistics table
     */
    private fun migrateV25toV26(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(25, 26)
            
            driver.execute(null, "ALTER TABLE reading_statistics ADD COLUMN longest_streak INTEGER NOT NULL DEFAULT 0;", 0)
            Logger.logColumnAdded("reading_statistics", "longest_streak")
            
            driver.execute(null, "ALTER TABLE reading_statistics ADD COLUMN buddy_level INTEGER NOT NULL DEFAULT 1;", 0)
            Logger.logColumnAdded("reading_statistics", "buddy_level")
            
            driver.execute(null, "ALTER TABLE reading_statistics ADD COLUMN buddy_experience INTEGER NOT NULL DEFAULT 0;", 0)
            Logger.logColumnAdded("reading_statistics", "buddy_experience")
            
            driver.execute(null, "ALTER TABLE reading_statistics ADD COLUMN unlocked_achievements TEXT NOT NULL DEFAULT '';", 0)
            Logger.logColumnAdded("reading_statistics", "unlocked_achievements")
            
            driver.execute(null, "ALTER TABLE reading_statistics ADD COLUMN last_interaction_time INTEGER NOT NULL DEFAULT 0;", 0)
            Logger.logColumnAdded("reading_statistics", "last_interaction_time")
            
            Logger.logMigrationSuccess(26)
            
        } catch (e: Exception) {
            Logger.logMigrationError(26, e)
        }
    }

    /**
     * Migration from version 26 to version 27
     * Adds custom_cover column to book table for user-set cover images
     * Also recreates views to include custom_cover column
     */
    private fun migrateV26toV27(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(26, 27)
            
            driver.execute(null, "ALTER TABLE book ADD COLUMN custom_cover TEXT NOT NULL DEFAULT '';", 0)
            Logger.logColumnAdded("book", "custom_cover")
            
            // Drop and recreate views to include custom_cover
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            Logger.logDebug("Dropped views for recreation with custom_cover")
            
            // Recreate historyView with custom_cover
            val historyViewSql = """
                CREATE VIEW IF NOT EXISTS historyView AS
                SELECT
                    history._id AS id,
                    book._id AS bookId,
                    chapter._id AS chapterId,
                    chapter.name AS chapterName,
                    book.title,
                    book.thumbnail_url AS thumbnailUrl,
                    book.custom_cover AS customCover,
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
                JOIN chapter ON book._id = chapter.book_id
                JOIN history ON chapter._id = history.chapter_id
                JOIN (
                    SELECT chapter.book_id, chapter._id AS chapter_id, MAX(history.last_read) AS last_read
                    FROM chapter JOIN history ON chapter._id = history.chapter_id
                    GROUP BY chapter.book_id
                ) AS max_last_read ON chapter.book_id = max_last_read.book_id;
            """.trimIndent()
            driver.execute(null, historyViewSql, 0)
            Logger.logDebug("Recreated historyView with custom_cover")
            
            // Recreate updatesView with custom_cover
            val updatesViewSql = """
                CREATE VIEW IF NOT EXISTS updatesView AS
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
                    book.custom_cover AS customCover,
                    book.cover_last_modified AS coverLastModified,
                    chapter.date_upload AS dateUpload,
                    chapter.date_fetch AS datefetch,
                    chapter.content IS NOT '' AS downlaoded,
                    history.progress AS readingProgress,
                    history.last_read AS lastReadAt
                FROM book JOIN chapter ON book._id = chapter.book_id
                LEFT JOIN history ON chapter._id = history.chapter_id
                WHERE favorite = 1 AND date_fetch > date_added
                ORDER BY date_fetch DESC;
            """.trimIndent()
            driver.execute(null, updatesViewSql, 0)
            Logger.logDebug("Recreated updatesView with custom_cover")
            
            Logger.logMigrationSuccess(27)
            
        } catch (e: Exception) {
            Logger.logMigrationError(27, e)
        }
    }
    
    /**
     * Migration from version 27 to version 28
     * Fixes translated_chapter table UNIQUE constraint to be (chapter_id, target_language) only
     * Previously was (chapter_id, target_language, translator_engine_id) which caused issues
     * when loading translations regardless of which engine created them.
     */
    private fun migrateV27toV28(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(27, 28)
            
            // Check if translated_chapter table exists
            var tableExists = false
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='translated_chapter'",
                mapper = { cursor ->
                    val result = cursor.next()
                    tableExists = result.value
                    result
                },
                parameters = 0
            )
            
            if (!tableExists) {
                Logger.logDebug("translated_chapter table doesn't exist, creating fresh")
                
                // Create the table with correct schema
                val createTableSql = """
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
                        UNIQUE(chapter_id, target_language)
                    );
                """.trimIndent()
                driver.execute(null, createTableSql, 0)
                
                // Create indexes
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_chapter_id_index ON translated_chapter(chapter_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_book_id_index ON translated_chapter(book_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_target_language_index ON translated_chapter(target_language);", 0)
                
                Logger.logTableCreated("translated_chapter")
            } else {
                Logger.logDebug("Migrating translated_chapter table to new schema")
                
                // SQLite doesn't support ALTER TABLE to change constraints
                // We need to recreate the table
                
                // 1. Create new table with correct schema
                val createNewTableSql = """
                    CREATE TABLE translated_chapter_new(
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
                        UNIQUE(chapter_id, target_language)
                    );
                """.trimIndent()
                driver.execute(null, createNewTableSql, 0)
                
                // 2. Copy data, keeping only the most recent translation per chapter+language
                val copyDataSql = """
                    INSERT INTO translated_chapter_new 
                    SELECT t1._id, t1.chapter_id, t1.book_id, t1.source_language, t1.target_language,
                           t1.translator_engine_id, t1.translated_content, t1.created_at, t1.updated_at
                    FROM translated_chapter t1
                    INNER JOIN (
                        SELECT chapter_id, target_language, MAX(updated_at) as max_updated
                        FROM translated_chapter
                        GROUP BY chapter_id, target_language
                    ) t2 ON t1.chapter_id = t2.chapter_id 
                        AND t1.target_language = t2.target_language 
                        AND t1.updated_at = t2.max_updated;
                """.trimIndent()
                driver.execute(null, copyDataSql, 0)
                
                // 3. Drop old table
                driver.execute(null, "DROP TABLE translated_chapter;", 0)
                
                // 4. Rename new table
                driver.execute(null, "ALTER TABLE translated_chapter_new RENAME TO translated_chapter;", 0)
                
                // 5. Recreate indexes
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_chapter_id_index ON translated_chapter(chapter_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_book_id_index ON translated_chapter(book_id);", 0)
                driver.execute(null, "CREATE INDEX IF NOT EXISTS translated_chapter_target_language_index ON translated_chapter(target_language);", 0)
                
                Logger.logDebug("Migrated translated_chapter table with new UNIQUE constraint")
            }
            
            Logger.logMigrationSuccess(28)
            
        } catch (e: Exception) {
            Logger.logMigrationError(28, e)
        }
    }
    
    /**
     * Migration from version 28 to version 29
     * Adds cached chapter counts and last_read_at columns to book table for fast library queries.
     * These columns are maintained by triggers on the chapter and history tables.
     */
    private fun migrateV28toV29(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(28, 29)
            
            // Check which columns already exist
            val columnsCheck = "PRAGMA table_info(book)"
            var hasCachedUnreadCount = false
            var hasCachedReadCount = false
            var hasCachedTotalChapters = false
            var hasLastReadAt = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        when (columnName) {
                            "cached_unread_count" -> hasCachedUnreadCount = true
                            "cached_read_count" -> hasCachedReadCount = true
                            "cached_total_chapters" -> hasCachedTotalChapters = true
                            "last_read_at" -> hasLastReadAt = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            // Add missing columns
            if (!hasCachedUnreadCount) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN cached_unread_count INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "cached_unread_count")
            }
            
            if (!hasCachedReadCount) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN cached_read_count INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "cached_read_count")
            }
            
            if (!hasCachedTotalChapters) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN cached_total_chapters INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "cached_total_chapters")
            }
            
            if (!hasLastReadAt) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN last_read_at INTEGER NOT NULL DEFAULT 0;", 0)
                Logger.logColumnAdded("book", "last_read_at")
            }
            
            // Create indexes for sorting by cached counts
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_favorite_unread ON book(favorite, cached_unread_count DESC) WHERE favorite = 1;", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_favorite_total_chapters ON book(favorite, cached_total_chapters DESC) WHERE favorite = 1;", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_book_favorite_last_read ON book(favorite, last_read_at DESC) WHERE favorite = 1;", 0)
            Logger.logIndexCreated("book cached count indexes")
            
            // Populate cached counts from existing chapter data
            Logger.logDebug("Populating cached chapter counts...")
            val populateCountsSql = """
                UPDATE book SET
                    cached_total_chapters = COALESCE((SELECT COUNT(*) FROM chapter WHERE book_id = book._id), 0),
                    cached_unread_count = COALESCE((SELECT COUNT(*) FROM chapter WHERE book_id = book._id AND read = 0), 0),
                    cached_read_count = COALESCE((SELECT COUNT(*) FROM chapter WHERE book_id = book._id AND read = 1), 0)
                WHERE favorite = 1;
            """.trimIndent()
            driver.execute(null, populateCountsSql, 0)
            Logger.logDebug("Populated cached chapter counts")
            
            // Populate last_read_at from history table
            Logger.logDebug("Populating last_read_at from history...")
            val populateLastReadSql = """
                UPDATE book SET
                    last_read_at = COALESCE(
                        (SELECT MAX(h.last_read) 
                         FROM history h 
                         JOIN chapter c ON h.chapter_id = c._id 
                         WHERE c.book_id = book._id), 
                        0
                    )
                WHERE favorite = 1;
            """.trimIndent()
            driver.execute(null, populateLastReadSql, 0)
            Logger.logDebug("Populated last_read_at")
            
            // Create triggers to keep cached counts in sync
            Logger.logDebug("Creating triggers for cached counts...")
            
            // Trigger: After INSERT on chapter
            driver.execute(null, "DROP TRIGGER IF EXISTS trigger_chapter_insert_update_book_counts;", 0)
            val triggerInsertSql = """
                CREATE TRIGGER IF NOT EXISTS trigger_chapter_insert_update_book_counts
                AFTER INSERT ON chapter
                BEGIN
                    UPDATE book SET
                        cached_total_chapters = (SELECT COUNT(*) FROM chapter WHERE book_id = NEW.book_id),
                        cached_unread_count = (SELECT COUNT(*) FROM chapter WHERE book_id = NEW.book_id AND read = 0),
                        cached_read_count = (SELECT COUNT(*) FROM chapter WHERE book_id = NEW.book_id AND read = 1)
                    WHERE _id = NEW.book_id;
                END;
            """.trimIndent()
            driver.execute(null, triggerInsertSql, 0)
            
            // Trigger: After UPDATE on chapter read status
            driver.execute(null, "DROP TRIGGER IF EXISTS trigger_chapter_update_read_status;", 0)
            val triggerUpdateSql = """
                CREATE TRIGGER IF NOT EXISTS trigger_chapter_update_read_status
                AFTER UPDATE OF read ON chapter
                BEGIN
                    UPDATE book SET
                        cached_unread_count = (SELECT COUNT(*) FROM chapter WHERE book_id = NEW.book_id AND read = 0),
                        cached_read_count = (SELECT COUNT(*) FROM chapter WHERE book_id = NEW.book_id AND read = 1),
                        last_read_at = CASE WHEN NEW.read = 1 THEN strftime('%s', 'now') * 1000 ELSE last_read_at END
                    WHERE _id = NEW.book_id;
                END;
            """.trimIndent()
            driver.execute(null, triggerUpdateSql, 0)
            
            // Trigger: After DELETE on chapter
            driver.execute(null, "DROP TRIGGER IF EXISTS trigger_chapter_delete_update_book_counts;", 0)
            val triggerDeleteSql = """
                CREATE TRIGGER IF NOT EXISTS trigger_chapter_delete_update_book_counts
                AFTER DELETE ON chapter
                BEGIN
                    UPDATE book SET
                        cached_total_chapters = (SELECT COUNT(*) FROM chapter WHERE book_id = OLD.book_id),
                        cached_unread_count = (SELECT COUNT(*) FROM chapter WHERE book_id = OLD.book_id AND read = 0),
                        cached_read_count = (SELECT COUNT(*) FROM chapter WHERE book_id = OLD.book_id AND read = 1)
                    WHERE _id = OLD.book_id;
                END;
            """.trimIndent()
            driver.execute(null, triggerDeleteSql, 0)
            
            // Trigger: After INSERT on history (when user reads a chapter)
            driver.execute(null, "DROP TRIGGER IF EXISTS trigger_history_insert_update_book_last_read;", 0)
            val triggerHistoryInsertSql = """
                CREATE TRIGGER IF NOT EXISTS trigger_history_insert_update_book_last_read
                AFTER INSERT ON history
                BEGIN
                    UPDATE book SET
                        last_read_at = NEW.last_read
                    WHERE _id = (SELECT book_id FROM chapter WHERE _id = NEW.chapter_id)
                    AND NEW.last_read > last_read_at;
                END;
            """.trimIndent()
            driver.execute(null, triggerHistoryInsertSql, 0)
            
            // Trigger: After UPDATE on history (when user continues reading)
            driver.execute(null, "DROP TRIGGER IF EXISTS trigger_history_update_update_book_last_read;", 0)
            val triggerHistoryUpdateSql = """
                CREATE TRIGGER IF NOT EXISTS trigger_history_update_update_book_last_read
                AFTER UPDATE OF last_read ON history
                BEGIN
                    UPDATE book SET
                        last_read_at = NEW.last_read
                    WHERE _id = (SELECT book_id FROM chapter WHERE _id = NEW.chapter_id)
                    AND NEW.last_read > last_read_at;
                END;
            """.trimIndent()
            driver.execute(null, triggerHistoryUpdateSql, 0)
            
            Logger.logDebug("Created triggers for cached counts")
            
            Logger.logMigrationSuccess(29)
            
        } catch (e: Exception) {
            Logger.logMigrationError(29, e)
        }
    }

    /**
     * Migration from version 29 to version 30
     * Adds track table for external tracking services (AniList, MAL, Kitsu, etc.)
     */
    private fun migrateV29toV30(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(29, 30)
            
            // Create track table
            val createTrackTableSql = """
                CREATE TABLE IF NOT EXISTS track (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    manga_id INTEGER NOT NULL,
                    site_id INTEGER NOT NULL,
                    entry_id INTEGER NOT NULL DEFAULT 0,
                    media_id INTEGER NOT NULL DEFAULT 0,
                    media_url TEXT NOT NULL DEFAULT '',
                    title TEXT NOT NULL DEFAULT '',
                    last_read REAL NOT NULL DEFAULT 0,
                    total_chapters INTEGER NOT NULL DEFAULT 0,
                    score REAL NOT NULL DEFAULT 0,
                    status INTEGER NOT NULL DEFAULT 1,
                    start_read_time INTEGER NOT NULL DEFAULT 0,
                    end_read_time INTEGER NOT NULL DEFAULT 0,
                    UNIQUE(manga_id, site_id)
                );
            """.trimIndent()
            
            driver.execute(null, createTrackTableSql, 0)
            Logger.logTableCreated("track")
            
            // Create indexes
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_track_manga_id ON track(manga_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_track_site_id ON track(site_id);", 0)
            Logger.logIndexCreated("track indexes")
            
            Logger.logMigrationSuccess(30)
            
        } catch (e: Exception) {
            Logger.logMigrationError(30, e)
        }
    }

    /**
     * Migration from version 30 to version 31
     * Adds explore_book table for temporary storage of browsed books
     * This table has a fixed size limit and auto-cleans old entries
     * Books are promoted to the main 'book' table when favorited or viewed in detail
     */
    private fun migrateV30toV31(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(30, 31)
            
            // Create explore_book table
            val createExploreBookTableSql = """
                CREATE TABLE IF NOT EXISTS explore_book(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    source_id INTEGER NOT NULL,
                    url TEXT NOT NULL,
                    title TEXT NOT NULL,
                    author TEXT,
                    description TEXT,
                    genre TEXT,
                    status INTEGER NOT NULL DEFAULT 0,
                    cover TEXT,
                    date_added INTEGER NOT NULL,
                    UNIQUE(url, source_id)
                );
            """.trimIndent()
            
            driver.execute(null, createExploreBookTableSql, 0)
            Logger.logTableCreated("explore_book")
            
            // Create indexes
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_explore_book_source ON explore_book(source_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_explore_book_date ON explore_book(date_added ASC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_explore_book_url ON explore_book(url, source_id);", 0)
            Logger.logIndexCreated("explore_book indexes")
            
            Logger.logMigrationSuccess(31)
            
        } catch (e: Exception) {
            Logger.logMigrationError(31, e)
        }
    }

    /**
     * Migration from version 31 to version 32
     * No-op migration to handle database version mismatch from previous builds
     */
    private fun migrateV31toV32(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(31, 32)
            // No schema changes - this migration exists to handle version mismatch
            Logger.logMigrationSuccess(32)
        } catch (e: Exception) {
            Logger.logMigrationError(32, e)
        }
    }

    /**
     * Migration from version 32 to version 33
     * Adds chapter_page column to book table for paginated chapter loading
     */
    private fun migrateV32toV33(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(32, 33)
            
            // Check if chapter_page column already exists
            val columnsCheck = "PRAGMA table_info(book)"
            var hasChapterPageColumn = false
            
            driver.executeQuery(
                identifier = null,
                sql = columnsCheck,
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName == "chapter_page") {
                            hasChapterPageColumn = true
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            // Add chapter_page column if it doesn't exist
            if (!hasChapterPageColumn) {
                driver.execute(null, "ALTER TABLE book ADD COLUMN chapter_page INTEGER NOT NULL DEFAULT 1;", 0)
                Logger.logColumnAdded("book", "chapter_page")
            }
            
            Logger.logMigrationSuccess(33)
            
        } catch (e: Exception) {
            Logger.logMigrationError(33, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }

    /**
     * Migration from version 33 to version 34
     * Adds category_auto_rules table for automatic categorization of books
     * based on genre or source
     */
    private fun migrateV33toV34(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(33, 34)
            
            // Create category_auto_rules table
            val createCategoryAutoRulesTableSql = """
                CREATE TABLE IF NOT EXISTS category_auto_rules(
                    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    category_id INTEGER NOT NULL,
                    rule_type TEXT NOT NULL,
                    value TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(category_id) REFERENCES categories (_id)
                    ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createCategoryAutoRulesTableSql, 0)
            Logger.logTableCreated("category_auto_rules")
            
            // Create indexes
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_category_auto_rules_category_id ON category_auto_rules(category_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_category_auto_rules_type ON category_auto_rules(rule_type);", 0)
            Logger.logIndexCreated("category_auto_rules indexes")
            
            Logger.logMigrationSuccess(34)
            
        } catch (e: Exception) {
            Logger.logMigrationError(34, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 34 to 35
     * Adds local_quote and quote_context tables for Quote Copy Mode feature
     */
    private fun migrateV34toV35(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(34, 35)
            
            // Create local_quote table
            val createLocalQuoteTableSql = """
                CREATE TABLE IF NOT EXISTS local_quote (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    text TEXT NOT NULL,
                    book_id INTEGER NOT NULL,
                    book_title TEXT NOT NULL,
                    chapter_title TEXT NOT NULL,
                    chapter_number INTEGER,
                    author TEXT,
                    created_at INTEGER NOT NULL,
                    has_context_backup INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent()
            
            driver.execute(null, createLocalQuoteTableSql, 0)
            Logger.logTableCreated("local_quote")
            
            // Create indexes for local_quote
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_local_quote_book_id ON local_quote(book_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_local_quote_created_at ON local_quote(created_at DESC);", 0)
            Logger.logIndexCreated("local_quote indexes")
            
            // Create quote_context table
            val createQuoteContextTableSql = """
                CREATE TABLE IF NOT EXISTS quote_context (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    quote_id INTEGER NOT NULL,
                    chapter_id INTEGER NOT NULL,
                    chapter_title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    FOREIGN KEY (quote_id) REFERENCES local_quote(id) ON DELETE CASCADE
                );
            """.trimIndent()
            
            driver.execute(null, createQuoteContextTableSql, 0)
            Logger.logTableCreated("quote_context")
            
            // Create index for quote_context
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_quote_context_quote_id ON quote_context(quote_id);", 0)
            Logger.logIndexCreated("quote_context indexes")
            
            Logger.logMigrationSuccess(35)
            
        } catch (e: Exception) {
            Logger.logMigrationError(35, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }
    
    /**
     * Migration from version 35 to version 36
     * Adds unique constraint on (book_id, url) to chapter table to prevent duplicate chapters
     * when fetching from remote sources.
     */
    private fun migrateV35toV36(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(35, 36)
            
            // First, remove any duplicate chapters that may exist (keep the one with lowest _id)
            // This is necessary before adding the unique constraint
            driver.execute(null, """
                DELETE FROM chapter
                WHERE _id NOT IN (
                    SELECT MIN(_id)
                    FROM chapter
                    GROUP BY book_id, url
                );
            """.trimIndent(), 0)
            Logger.logDebug("Removed duplicate chapters before adding unique constraint")
            
            // Create unique index on (book_id, url) to prevent duplicate chapters
            driver.execute(null, """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_chapter_book_url_unique
                ON chapter(book_id, url);
            """.trimIndent(), 0)
            Logger.logIndexCreated("idx_chapter_book_url_unique")
            
            Logger.logMigrationSuccess(36)
            
        } catch (e: Exception) {
            Logger.logMigrationError(36, e)
            // Don't throw - allow the app to continue even if migration fails
        }
    }

    /**
     * Migration from version 36 to version 37
     * Adds sync-related tables for Local WiFi Book Sync feature:
     * - sync_metadata: Device sync metadata
     * - trusted_devices: Paired/trusted devices
     * - sync_log: Sync operation history
     */
    private fun migrateV36toV37(driver: SqlDriver) {
        try {
            Logger.logMigrationStart(36, 37)
            
            // Create sync_metadata table
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS sync_metadata(
                    device_id TEXT NOT NULL PRIMARY KEY,
                    device_name TEXT NOT NULL,
                    device_type TEXT NOT NULL,
                    last_sync_time INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("sync_metadata")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_metadata_device_id ON sync_metadata(device_id);", 0)
            Logger.logIndexCreated("idx_sync_metadata_device_id")
            
            // Create trusted_devices table
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS trusted_devices(
                    device_id TEXT NOT NULL PRIMARY KEY,
                    device_name TEXT NOT NULL,
                    paired_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("trusted_devices")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_device_id ON trusted_devices(device_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_expires_at ON trusted_devices(expires_at);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_trusted_devices_is_active ON trusted_devices(is_active) WHERE is_active = 1;", 0)
            Logger.logIndexCreated("trusted_devices indexes")
            
            // Create sync_log table
            driver.execute(null, """
                CREATE TABLE IF NOT EXISTS sync_log(
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    sync_id TEXT NOT NULL,
                    device_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    items_synced INTEGER NOT NULL,
                    duration INTEGER NOT NULL,
                    error_message TEXT,
                    timestamp INTEGER NOT NULL
                );
            """.trimIndent(), 0)
            Logger.logTableCreated("sync_log")
            
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_device_id ON sync_log(device_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_timestamp ON sync_log(timestamp DESC);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_sync_id ON sync_log(sync_id);", 0)
            driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_sync_log_status ON sync_log(status);", 0)
            Logger.logIndexCreated("sync_log indexes")
            
            Logger.logMigrationSuccess(37)
            
        } catch (e: Exception) {
            Logger.logMigrationError(37, e)
            // Don't throw - allow the app to continue even if migration fails
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