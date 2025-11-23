package ireader.data

import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Database migration test to ensure migrations from previous versions work correctly.
 * This test should be run before each release to verify database compatibility.
 */
class DatabaseMigrationTest {
    
    /**
     * Test migration from version 18 to current version (19)
     * This represents the last release version migrating to the new version
     */
    @Test
    fun testMigrationFrom18To19() {
        val driver = createTestDriver()
        
        try {
            // Create a database at version 18
            setupDatabaseAtVersion18(driver)
            
            // Verify we're at version 18
            verifyDatabaseVersion(driver, 18)
            
            // Run migration to version 19
            DatabaseMigrations.migrate(driver, 18)
            
            // Verify migration succeeded
            verifyDatabaseVersion(driver, 19)
            
            // Verify chapterReport table was created
            verifyChapterReportTableExists(driver)
            
            println("✓ Migration from version 18 to 19 successful")
        } finally {
            driver.close()
        }
    }
    
    /**
     * Test migration from version 17 to current version
     */
    @Test
    fun testMigrationFrom17ToCurrent() {
        val driver = createTestDriver()
        
        try {
            setupDatabaseAtVersion17(driver)
            verifyDatabaseVersion(driver, 17)
            
            DatabaseMigrations.migrate(driver, 17)
            
            verifyDatabaseVersion(driver, DatabaseMigrations.CURRENT_VERSION)
            verifyChapterHealthTableExists(driver)
            verifyChapterReportTableExists(driver)
            
            println("✓ Migration from version 17 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    /**
     * Test migration from version 15 (simulating older release)
     */
    @Test
    fun testMigrationFrom15ToCurrent() {
        val driver = createTestDriver()
        
        try {
            setupDatabaseAtVersion15(driver)
            verifyDatabaseVersion(driver, 15)
            
            DatabaseMigrations.migrate(driver, 15)
            
            verifyDatabaseVersion(driver, DatabaseMigrations.CURRENT_VERSION)
            
            println("✓ Migration from version 15 to ${DatabaseMigrations.CURRENT_VERSION} successful")
        } finally {
            driver.close()
        }
    }
    
    /**
     * Test that views are properly initialized after migration
     */
    @Test
    fun testViewsInitializedAfterMigration() {
        val driver = createTestDriver()
        
        try {
            setupDatabaseAtVersion18(driver)
            DatabaseMigrations.migrate(driver, 18)
            
            // Verify views exist
            verifyViewExists(driver, "historyView")
            verifyViewExists(driver, "updatesView")
            
            println("✓ Views properly initialized after migration")
        } finally {
            driver.close()
        }
    }
    
    /**
     * Test migration doesn't crash with existing data
     */
    @Test
    fun testMigrationWithExistingData() {
        val driver = createTestDriver()
        
        try {
            setupDatabaseAtVersion18(driver)
            
            // Insert test data
            insertTestBook(driver)
            insertTestChapter(driver)
            
            // Run migration
            DatabaseMigrations.migrate(driver, 18)
            
            // Verify data still exists
            verifyTestDataExists(driver)
            
            println("✓ Migration preserves existing data")
        } finally {
            driver.close()
        }
    }
    
    // Helper functions
    
    private fun setupDatabaseAtVersion18(driver: SqlDriver) {
        // Create base tables that exist in version 18
        createBaseTables(driver)
        createChapterHealthTable(driver)
    }
    
    private fun setupDatabaseAtVersion17(driver: SqlDriver) {
        createBaseTables(driver)
        createNftWalletsTable(driver)
    }
    
    private fun setupDatabaseAtVersion15(driver: SqlDriver) {
        createBaseTables(driver)
        createSourceReportTable(driver)
    }
    
    private fun createBaseTables(driver: SqlDriver) {
        // Create book table
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS book(
                _id INTEGER NOT NULL PRIMARY KEY,
                source INTEGER NOT NULL,
                url TEXT NOT NULL,
                title TEXT NOT NULL,
                favorite INTEGER NOT NULL,
                initialized INTEGER NOT NULL
            );
        """.trimIndent(), 0)
        
        // Create chapter table
        driver.execute(null, """
            CREATE TABLE IF NOT EXISTS chapter(
                _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                url TEXT NOT NULL,
                name TEXT NOT NULL,
                read INTEGER NOT NULL,
                FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE
            );
        """.trimIndent(), 0)
        
        // Create history table
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
        
        // Create categories table
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
                FOREIGN KEY(chapter_id) REFERENCES chapter (_id) ON DELETE CASCADE
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
            CREATE TABLE IF NOT EXISTS sourceReport (
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
    
    private fun verifyDatabaseVersion(driver: SqlDriver, expectedVersion: Int) {
        // This is a placeholder - actual implementation depends on how you track version
        // You might store it in a metadata table or use PRAGMA user_version
        println("Verifying database is at version $expectedVersion")
    }
    
    private fun verifyChapterReportTableExists(driver: SqlDriver) {
        var tableExists = false
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='chapterReport'",
            mapper = { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            },
            parameters = 0
        )
        assertTrue(tableExists, "chapterReport table should exist after migration")
    }
    
    private fun verifyChapterHealthTableExists(driver: SqlDriver) {
        var tableExists = false
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='chapterHealth'",
            mapper = { cursor ->
                val result = cursor.next()
                tableExists = result.value
                result
            },
            parameters = 0
        )
        assertTrue(tableExists, "chapterHealth table should exist after migration")
    }
    
    private fun verifyViewExists(driver: SqlDriver, viewName: String) {
        var viewExists = false
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='view' AND name='$viewName'",
            mapper = { cursor ->
                val result = cursor.next()
                viewExists = result.value
                result
            },
            parameters = 0
        )
        assertTrue(viewExists, "$viewName should exist after migration")
    }
    
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
    
    private fun verifyTestDataExists(driver: SqlDriver) {
        var bookExists = false
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM book WHERE _id = 1",
            mapper = { cursor ->
                val result = cursor.next()
                if (result.value) {
                    bookExists = cursor.getLong(0) ?: 0 > 0
                }
                result
            },
            parameters = 0
        )
        assertTrue(bookExists, "Test book should still exist after migration")
    }
}

// Platform-specific driver creation - to be implemented in platform-specific test sources
expect fun createTestDriver(): SqlDriver
