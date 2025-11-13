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
    const val CURRENT_VERSION = 8
    
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
            // Add more migration cases as the database evolves
            // 6 -> migrateV6toV7(driver)
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