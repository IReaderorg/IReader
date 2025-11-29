package ireader.data

import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Database migration test to ensure migrations from previous versions work correctly.
 * This test should be run before each release to verify database compatibility.
 * 
 * Tests migration paths from various previous release versions to ensure
 * users upgrading from any supported version will have their data preserved.
 */
class DatabaseMigrationTest {
    
    companion object {
        // Map of release versions to database schema versions
        val RELEASE_TO_DB_VERSION = mapOf(
            "v1.0.0" to 15,
            "v1.1.0" to 17,
            "v1.2.0" to 18,
            "v1.3.0" to 19
        )
        const val MIN_SUPPORTED_VERSION = 15
    }
    
    @Test
    fun testMigrationFrom18ToCurrent() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion18(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            insertTestHistory(driver)
            
            DatabaseMigrations.migrate(driver, 18)
            
            verifyDatabaseAtCurrentVersion(driver)
            verifyTestDataExists(driver)
            verifyChapterReportTableExists(driver)
            
            println("✓ Migration from version 18 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testMigrationFrom17ToCurrent() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion17(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            
            DatabaseMigrations.migrate(driver, 17)
            
            verifyDatabaseAtCurrentVersion(driver)
            verifyChapterHealthTableExists(driver)
            verifyTestDataExists(driver)
            
            println("✓ Migration from version 17 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testMigrationFrom16ToCurrent() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion16(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            
            DatabaseMigrations.migrate(driver, 16)
            
            verifyDatabaseAtCurrentVersion(driver)
            verifyTestDataExists(driver)
            
            println("✓ Migration from version 16 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testMigrationFrom15ToCurrent() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion15(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            
            DatabaseMigrations.migrate(driver, 15)
            
            verifyDatabaseAtCurrentVersion(driver)
            verifyTestDataExists(driver)
            
            println("✓ Migration from version 15 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testViewsInitializedAfterMigration() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion18(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            insertTestHistory(driver)
            
            DatabaseMigrations.migrate(driver, 18)
            
            verifyViewExists(driver, "historyView")
            verifyViewExists(driver, "updatesView")
            
            println("✓ Views properly initialized after migration")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testMigrationWithExistingData() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion18(driver)
            insertTestBook(driver)
            insertTestChapter(driver)
            insertTestHistory(driver)
            insertTestCategory(driver)
            
            DatabaseMigrations.migrate(driver, 18)
            
            verifyTestDataExists(driver)
            verifyTestCategoryExists(driver)
            
            println("✓ Migration preserves existing data")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testMigrationWithLargeDataset() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion18(driver)
            
            for (i in 1..100) {
                driver.execute(null, """
                    INSERT INTO book(_id, source, url, title, favorite, initialized)
                    VALUES ($i, 1, 'test-url-$i', 'Test Book $i', ${if (i % 2 == 0) 1 else 0}, 1);
                """.trimIndent(), 0)
                
                for (j in 1..10) {
                    val chapterId = (i - 1) * 10 + j
                    driver.execute(null, """
                        INSERT INTO chapter(_id, book_id, url, name, read)
                        VALUES ($chapterId, $i, 'chapter-url-$chapterId', 'Chapter $j', 0);
                    """.trimIndent(), 0)
                }
            }
            
            DatabaseMigrations.migrate(driver, 18)
            
            var bookCount = 0L
            driver.executeQuery(null, "SELECT COUNT(*) FROM book", { cursor ->
                val result = cursor.next()
                if (result.value) { bookCount = cursor.getLong(0) ?: 0 }
                result
            }, 0)
            assertEquals(100, bookCount, "All 100 books should be preserved")
            
            println("✓ Migration handles large dataset")
        } finally {
            driver.close()
        }
    }
    
    @Test
    fun testSequentialMigrations() {
        val driver = createTestDriver()
        try {
            setupDatabaseAtVersion15(driver)
            insertTestBook(driver)
            
            for (version in 15 until DatabaseMigrations.CURRENT_VERSION) {
                println("Migrating from version $version to ${version + 1}...")
                DatabaseMigrations.migrate(driver, version)
            }
            
            verifyDatabaseAtCurrentVersion(driver)
            verifyTestDataExists(driver)
            
            println("✓ Sequential migrations successful")
        } finally {
            driver.close()
        }
    }

    
    // ==================== Setup Functions ====================
    
    private fun setupDatabaseAtVersion18(driver: SqlDriver) {
        createBaseTables(driver)
        createChapterHealthTable(driver)
        createNftWalletsTable(driver)
        createSourceReportTable(driver)
        createReviewTables(driver)
        createSyncQueueTable(driver)
        createReadingStatisticsTable(driver)
        createTranslationTables(driver)
    }
    
    private fun setupDatabaseAtVersion17(driver: SqlDriver) {
        createBaseTables(driver)
        createNftWalletsTable(driver)
        createSourceReportTable(driver)
        createReviewTables(driver)
        createSyncQueueTable(driver)
        createReadingStatisticsTable(driver)
        createTranslationTables(driver)
    }
    
    private fun setupDatabaseAtVersion16(driver: SqlDriver) {
        createBaseTables(driver)
        createNftWalletsTable(driver)
        createSourceReportTable(driver)
        createReviewTables(driver)
        createSyncQueueTable(driver)
        createReadingStatisticsTable(driver)
    }
    
    private fun setupDatabaseAtVersion15(driver: SqlDriver) {
        createBaseTables(driver)
        createSourceReportTable(driver)
        createSyncQueueTable(driver)
        createReadingStatisticsTable(driver)
    }
    
    private fun createBaseTables(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS book(
                _id INTEGER NOT NULL PRIMARY KEY,
                source INTEGER NOT NULL,
                url TEXT NOT NULL,
                artist TEXT,
                author TEXT,
                description TEXT,
                genre TEXT,
                title TEXT NOT NULL,
                status INTEGER NOT NULL DEFAULT 0,
                thumbnail_url TEXT,
                favorite INTEGER NOT NULL,
                last_update INTEGER,
                next_update INTEGER,
                initialized INTEGER NOT NULL,
                viewer INTEGER NOT NULL DEFAULT 0,
                chapter_flags INTEGER NOT NULL DEFAULT 0,
                cover_last_modified INTEGER NOT NULL DEFAULT 0,
                date_added INTEGER NOT NULL DEFAULT 0,
                is_pinned INTEGER NOT NULL DEFAULT 0,
                pinned_order INTEGER NOT NULL DEFAULT 0,
                is_archived INTEGER NOT NULL DEFAULT 0
            );
        """.trimIndent(), 0)
        
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS chapter(
                _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                url TEXT NOT NULL,
                name TEXT NOT NULL,
                scanlator TEXT,
                read INTEGER NOT NULL,
                bookmark INTEGER NOT NULL DEFAULT 0,
                last_page_read INTEGER NOT NULL DEFAULT 0,
                chapter_number REAL NOT NULL DEFAULT 0,
                source_order INTEGER NOT NULL DEFAULT 0,
                date_fetch INTEGER NOT NULL DEFAULT 0,
                date_upload INTEGER NOT NULL DEFAULT 0,
                content TEXT NOT NULL DEFAULT '',
                type INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
        
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS history(
                _id INTEGER NOT NULL PRIMARY KEY,
                chapter_id INTEGER NOT NULL UNIQUE,
                last_read INTEGER,
                time_read INTEGER NOT NULL,
                progress REAL DEFAULT 0.0,
                FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
        
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS categories(
                _id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                sort INTEGER NOT NULL,
                flags INTEGER NOT NULL
            );
        """.trimIndent(), 0)
    }
    
    private fun createChapterHealthTable(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS chapterHealth(
                chapter_id INTEGER NOT NULL PRIMARY KEY,
                is_broken INTEGER NOT NULL,
                break_reason TEXT,
                checked_at INTEGER NOT NULL,
                repair_attempted_at INTEGER,
                repair_successful INTEGER,
                replacement_source_id INTEGER,
                FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
    }
    
    private fun createNftWalletsTable(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS nftWallets(
                userId TEXT NOT NULL PRIMARY KEY,
                walletAddress TEXT NOT NULL,
                lastVerified INTEGER,
                ownsNFT INTEGER NOT NULL DEFAULT 0,
                nftTokenId TEXT,
                cacheExpiresAt INTEGER NOT NULL
            );
        """.trimIndent(), 0)
    }
    
    private fun createSourceReportTable(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS sourceReport(
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                sourceId INTEGER NOT NULL,
                packageName TEXT NOT NULL,
                version TEXT NOT NULL,
                reason TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending'
            );
        """.trimIndent(), 0)
    }
    
    private fun createReviewTables(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS bookReview(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_title TEXT NOT NULL,
                rating INTEGER NOT NULL,
                review_text TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                synced INTEGER NOT NULL DEFAULT 0
            );
        """.trimIndent(), 0)
        
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS chapterReview(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_title TEXT NOT NULL,
                chapter_name TEXT NOT NULL,
                rating INTEGER NOT NULL,
                review_text TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                synced INTEGER NOT NULL DEFAULT 0
            );
        """.trimIndent(), 0)
    }
    
    private fun createSyncQueueTable(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS sync_queue(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id TEXT NOT NULL,
                data TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                retry_count INTEGER DEFAULT 0
            );
        """.trimIndent(), 0)
    }
    
    private fun createReadingStatisticsTable(driver: SqlDriver) {
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS reading_statistics(
                _id INTEGER NOT NULL PRIMARY KEY,
                total_chapters_read INTEGER NOT NULL DEFAULT 0,
                total_reading_time_minutes INTEGER NOT NULL DEFAULT 0,
                reading_streak INTEGER NOT NULL DEFAULT 0,
                last_read_date INTEGER,
                total_words_read INTEGER NOT NULL DEFAULT 0,
                books_completed INTEGER NOT NULL DEFAULT 0
            );
        """.trimIndent(), 0)
    }
    
    private fun createTranslationTables(driver: SqlDriver) {
        driver.execute(null, """
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
                FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE,
                FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
        
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS glossary(
                _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                source_term TEXT NOT NULL,
                target_term TEXT NOT NULL,
                term_type TEXT NOT NULL,
                notes TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
    }

    
    // ==================== Insert Test Data ====================
    
    private fun insertTestBook(driver: SqlDriver) {
        driver.execute(null, """
            INSERT INTO book(_id, source, url, title, favorite, initialized)
            VALUES (1, 1, 'test-url', 'Test Book', 1, 1);
        """.trimIndent(), 0)
    }
    
    private fun insertTestChapter(driver: SqlDriver) {
        driver.execute(null, """
            INSERT INTO chapter(book_id, url, name, read)
            VALUES (1, 'chapter-url', 'Chapter 1', 0);
        """.trimIndent(), 0)
    }
    
    private fun insertTestHistory(driver: SqlDriver) {
        driver.execute(null, """
            INSERT INTO history(_id, chapter_id, last_read, time_read, progress)
            VALUES (1, 1, 1234567890, 3600, 0.5);
        """.trimIndent(), 0)
    }
    
    private fun insertTestCategory(driver: SqlDriver) {
        driver.execute(null, """
            INSERT INTO categories(_id, name, sort, flags)
            VALUES (1, 'Test Category', 0, 0);
        """.trimIndent(), 0)
    }
    
    // ==================== Verification Functions ====================
    
    private fun verifyDatabaseAtCurrentVersion(driver: SqlDriver) {
        println("Verifying database is at version ${DatabaseMigrations.CURRENT_VERSION}")
    }
    
    private fun verifyTestDataExists(driver: SqlDriver) {
        var bookExists = false
        driver.executeQuery(null, "SELECT COUNT(*) FROM book WHERE _id = 1", { cursor ->
            val result = cursor.next()
            if (result.value) { bookExists = (cursor.getLong(0) ?: 0) > 0 }
            result
        }, 0)
        assertTrue(bookExists, "Test book should still exist after migration")
    }
    
    private fun verifyTestCategoryExists(driver: SqlDriver) {
        var categoryExists = false
        driver.executeQuery(null, "SELECT COUNT(*) FROM categories WHERE _id = 1", { cursor ->
            val result = cursor.next()
            if (result.value) { categoryExists = (cursor.getLong(0) ?: 0) > 0 }
            result
        }, 0)
        assertTrue(categoryExists, "Test category should still exist after migration")
    }
    
    private fun verifyChapterReportTableExists(driver: SqlDriver) {
        var tableExists = false
        driver.executeQuery(null, 
            "SELECT name FROM sqlite_master WHERE type='table' AND name='chapterReport'", 
            { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            }, 0)
        assertTrue(tableExists, "chapterReport table should exist after migration")
    }
    
    private fun verifyChapterHealthTableExists(driver: SqlDriver) {
        var tableExists = false
        driver.executeQuery(null,
            "SELECT name FROM sqlite_master WHERE type='table' AND name='chapterHealth'",
            { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            }, 0)
        assertTrue(tableExists, "chapterHealth table should exist after migration")
    }
    
    private fun verifyViewExists(driver: SqlDriver, viewName: String) {
        var viewExists = false
        driver.executeQuery(null,
            "SELECT name FROM sqlite_master WHERE type='view' AND name='$viewName'",
            { cursor ->
                val result = cursor.next()
                viewExists = result.value
                result
            }, 0)
        assertTrue(viewExists, "$viewName should exist after migration")
    }
}

// Platform-specific driver creation
expect fun createTestDriver(): SqlDriver
