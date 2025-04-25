package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import migrations.Catalog
import data.DatabaseMigrations
import migrations.Download
import migrations.Reader_theme
import migrations.Theme
import ir.kazemcodes.infinityreader.Database
import ireader.data.book.bookGenresConverter
import ireader.data.book.floatDoubleColumnAdapter
import ireader.data.book.intLongColumnAdapter
import ireader.data.book.longConverter
import ireader.data.chapter.chapterContentConvertor
import migrations.Book
import migrations.Chapter
import java.io.Reader

fun createDatabase(driver: SqlDriver): Database {
    // Create the database instance with appropriate adapters
    val database = Database(
        driver = driver,
        bookAdapter = Book.Adapter(
            cover_last_modifiedAdapter = longConverter,
            date_addedAdapter = longConverter
        ),
        chapterAdapter = Chapter.Adapter(
            date_fetchAdapter = longConverter,
            date_uploadAdapter = longConverter,
            typeAdapter = longConverter,
            chapter_numberAdapter = floatDoubleColumnAdapter
        ),
        reader_themeAdapter = Reader_theme.Adapter(
            background_colorAdapter = intLongColumnAdapter,
            on_textcolorAdapter = intLongColumnAdapter
        ),
        catalogAdapter = Catalog.Adapter(versionCodeAdapter = intLongColumnAdapter),
        themeAdapter = Theme.Adapter(
            errorContainerAdapter = intLongColumnAdapter,
            inversePrimaryAdapter = intLongColumnAdapter,
            inverseSurfaceAdapter =intLongColumnAdapter ,
            outlineVariantAdapter =intLongColumnAdapter ,
            surfaceVariantAdapter =intLongColumnAdapter ,
            onBackgroundAdapter = intLongColumnAdapter,
            onPrimaryAdapter = intLongColumnAdapter,
            onSurfaceAdapter = intLongColumnAdapter,
            secondaryAdapter = intLongColumnAdapter,
            backgroundAdapter = intLongColumnAdapter,
            onTertiaryAdapter =intLongColumnAdapter ,
            onSecondaryAdapter =intLongColumnAdapter ,
            surfaceTintAdapter = intLongColumnAdapter, surfaceAdapter = intLongColumnAdapter,
            onErrorAdapter = intLongColumnAdapter,
            outlineAdapter = intLongColumnAdapter,
            primaryAdapter = intLongColumnAdapter, errorAdapter = intLongColumnAdapter, scrimAdapter = intLongColumnAdapter, onBarsAdapter = intLongColumnAdapter, barsAdapter = intLongColumnAdapter, tertiaryAdapter = intLongColumnAdapter, onErrorContainerAdapter = intLongColumnAdapter, primaryContainerAdapter = intLongColumnAdapter,
            inverseOnSurfaceAdapter = intLongColumnAdapter,
            onSurfaceVariantAdapter = intLongColumnAdapter, tertiaryContainerAdapter = intLongColumnAdapter, onTertiaryContainerAdapter = intLongColumnAdapter, onPrimaryContainerAdapter = intLongColumnAdapter, secondaryContainerAdapter = intLongColumnAdapter,
            onSecondaryContainerAdapter = intLongColumnAdapter,
        ),
        downloadAdapter = Download.Adapter(
            priorityAdapter = intLongColumnAdapter
        ),
    )
    
    // Verify all required tables exist before attempting to create views
    try {
        // Check if the history table exists
        val hasHistoryTable = checkTableExists(driver, "history")
        if (!hasHistoryTable) {
            println("WARNING: history table doesn't exist. Creating it...")
            createHistoryTable(driver)
        }
        
        // Now that we've verified tables exist, initialize views
        DatabaseMigrations.initializeViewsDirectly(driver)
    } catch (e: Exception) {
        println("Error during database initialization: ${e.message}")
        e.printStackTrace()
        
        // Last resort - try a more specific approach to ensure the history table exists
        try {
            createHistoryTable(driver)
            println("Created history table as last resort")
            
            // Try to create the view again after ensuring table exists
            try {
                createHistoryView(driver)
            } catch (viewEx: Exception) {
                println("Still couldn't create historyView: ${viewEx.message}")
            }
        } catch (tableEx: Exception) {
            println("Failed to create history table as last resort: ${tableEx.message}")
        }
    }
    
    return database
}

/**
 * Checks if a table exists in the database
 */
private fun checkTableExists(driver: SqlDriver, tableName: String): Boolean {
    var exists = false
    try {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'",
            mapper = { cursor ->
                val res = cursor.next()
                exists = res.value
                res
            },
            parameters = 0
        )
    } catch (e: Exception) {
        println("Error checking if table $tableName exists: ${e.message}")
    }
    println("Table $tableName exists: $exists")
    return exists
}

/**
 * Creates the history table directly to solve the "no such table" error
 */
private fun createHistoryTable(driver: SqlDriver) {
    try {
        val sql = """
            CREATE TABLE IF NOT EXISTS history (
                _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                chapter_id INTEGER NOT NULL UNIQUE,
                last_read INTEGER,
                time_read INTEGER NOT NULL,
                progress REAL DEFAULT 0.0,
                FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE
            )
        """.trimIndent()
        
        driver.execute(null, sql, 0)
        println("Created history table")
        
        // Create necessary indices
        driver.execute(null, "CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);", 0)
        driver.execute(null, "CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);", 0)
    } catch (e: Exception) {
        println("Error creating history table: ${e.message}")
        throw e
    }
}

/**
 * Creates the historyView manually
 */
private fun createHistoryView(driver: SqlDriver) {
    try {
        // First drop the view if it exists to avoid conflicts
        driver.execute(null, "DROP VIEW IF EXISTS historyView;", 0)
        
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
        
        driver.execute(null, historyViewSql, 0)
        println("Created historyView manually")
    } catch (e: Exception) {
        println("Error creating historyView manually: ${e.message}")
        throw e
    }
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}