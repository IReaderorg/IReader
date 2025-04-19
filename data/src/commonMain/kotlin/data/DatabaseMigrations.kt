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
            // Apply migrations one by one in order
            for (version in oldVersion until CURRENT_VERSION) {
                applyMigration(driver, version)
            }
            
            // Initialize views after all migrations
            initializeViews(driver)
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
     * Initialize or recreate database views
     * This should be called after running all migrations to ensure views are properly set up
     */
    private fun initializeViews(driver: SqlDriver) {
        try {
            // Drop existing views if they exist (for recreation)
            driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
            driver.execute(null, "DROP VIEW IF EXISTS updatesView;", 0)
            
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
            
            // Execute historyView creation separately
            driver.execute(null, historyViewSql, 0)
            
            // Create updatesView with progress information
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
            
            // Execute updatesView creation separately
            driver.execute(null, updatesViewSql, 0)
        } catch (e: Exception) {
            // Log the error but don't crash - this will help with debugging
            println("Error creating database views: ${e.message}")
            e.printStackTrace()
        }
    }
} 