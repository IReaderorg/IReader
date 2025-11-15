package ireader.desktop

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Connection
import java.sql.Statement

/**
 * Utility class for verifying and fixing database integrity issues
 */
object DatabaseVerifier {
    /**
     * Main function for direct execution to verify database integrity
     */
    @JvmStatic
    fun main(args: Array<String>) {
        println("IReader Database Verifier")
        println("-------------------------")
        
        // Find the database file
        val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
        if (!dbDir.exists()) {
            println("Database directory doesn't exist at: ${dbDir.absolutePath}")
            println("Creating directory...")
            dbDir.mkdirs()
            println("No database files to check. Application will create a new one on startup.")
            return
        }
        
        val dbFiles = dbDir.listFiles { file -> file.name.endsWith(".db") }
        
        if (dbFiles == null || dbFiles.isEmpty()) {
            println("No database files found in ${dbDir.absolutePath}")
            println("Application will create a new database on startup.")
            return
        }
        
        // Process each database file
        dbFiles.forEach { dbFile ->
            println("\nVerifying database: ${dbFile.name}")
            println("Path: ${dbFile.absolutePath}")
            println("Size: ${dbFile.length() / 1024} KB")
            
            // Create backup before making any changes
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val backupFile = File(dbFile.parentFile, "${dbFile.nameWithoutExtension}_backup_$timestamp.db")
            
            try {
                dbFile.copyTo(backupFile, overwrite = true)
                println("Backup created at: ${backupFile.absolutePath}")
            } catch (e: Exception) {
                println("WARNING: Failed to create backup: ${e.message}")
            }
            
            // Connect to database and check structure
            try {
                Class.forName("org.sqlite.JDBC")
                val connection = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
                connection.use { conn ->
                    verifyDatabaseStructure(conn)
                }
            } catch (e: Exception) {
                println("ERROR: Failed to connect to database: ${e.message}")
                e.printStackTrace()
            }
        }
        
        println("\nVerification complete. You can now start the application.")
    }
    
    /**
     * Verifies and fixes database structure issues
     */
    private fun verifyDatabaseStructure(connection: Connection) {
        connection.createStatement().use { stmt ->
            // Check tables
            println("\nChecking tables...")
            val requiredTables = listOf("book", "chapter", "history", "categories")
            val missingTables = requiredTables.filter { !tableExists(stmt, it) }
            
            if (missingTables.isEmpty()) {
                println("All required tables exist.")
            } else {
                println("Missing tables: ${missingTables.joinToString(", ")}")
                
                // Create missing tables
                missingTables.forEach { tableName ->
                    try {
                        when (tableName) {
                            "history" -> createHistoryTable(stmt)
                            // Add more table creation logic as needed
                            else -> println("No creation script available for table: $tableName")
                        }
                    } catch (e: Exception) {
                        println("ERROR creating table $tableName: ${e.message}")
                    }
                }
            }
            
            // Check views
            println("\nChecking views...")
            val requiredViews = listOf("historyView", "updatesView")
            val existingViews = requiredViews.filter { viewExists(stmt, it) }
            
            if (existingViews.isNotEmpty()) {
                println("Existing views: ${existingViews.joinToString(", ")}")
                println("Dropping existing views for recreation...")
                
                // Drop views to recreate them
                existingViews.forEach { viewName ->
                    try {
                        stmt.execute("DROP VIEW IF EXISTS $viewName;")
                        println("Dropped view: $viewName")
                    } catch (e: Exception) {
                        println("ERROR dropping view $viewName: ${e.message}")
                    }
                }
            }
            
            // Verify history table structure
            if (tableExists(stmt, "history")) {
                println("\nChecking history table structure...")
                verifyHistoryTableStructure(stmt)
            }
            
            // Create views if required tables exist
            if (tableExists(stmt, "book") && tableExists(stmt, "chapter") && tableExists(stmt, "history")) {
                println("\nCreating views...")
                try {
                    createHistoryView(stmt)
                } catch (e: Exception) {
                    println("ERROR creating historyView: ${e.message}")
                }
                
                try {
                    createUpdatesView(stmt)
                } catch (e: Exception) {
                    println("ERROR creating updatesView: ${e.message}")
                }
            } else {
                println("\nCannot create views because required tables are missing.")
            }
        }
    }
    
    private fun tableExists(stmt: Statement, tableName: String): Boolean {
        stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'").use { rs ->
            val exists = rs.next()
            println("Table $tableName exists: $exists")
            return exists
        }
    }
    
    private fun viewExists(stmt: Statement, viewName: String): Boolean {
        stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='view' AND name='$viewName'").use { rs ->
            return rs.next()
        }
    }
    
    private fun createHistoryTable(stmt: Statement) {
        println("Creating history table...")
        val createHistorySql = """
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
        
        stmt.execute(createHistorySql)
        stmt.execute("CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);")
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);")
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);")
        println("History table created successfully with indices.")
    }
    
    private fun verifyHistoryTableStructure(stmt: Statement) {
        // Check if progress column exists
        val rs = stmt.executeQuery("PRAGMA table_info(history)")
        val columns = mutableListOf<String>()
        
        while (rs.next()) {
            columns.add(rs.getString("name"))
        }
        rs.close()
        
        println("History table columns: ${columns.joinToString(", ")}")
        
        if ("progress" !in columns) {
            println("Adding missing progress column to history table...")
            stmt.execute("ALTER TABLE history ADD COLUMN progress REAL DEFAULT 0.0;")
            println("Added progress column to history table.")
            
            // Create missing index
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);")
        }
    }
    
    private fun createHistoryView(stmt: Statement) {
        println("Creating historyView...")
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
        
        stmt.execute(historyViewSql)
        println("historyView created successfully.")
    }
    
    private fun createUpdatesView(stmt: Statement) {
        println("Creating updatesView...")
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
        
        stmt.execute(updatesViewSql)
        println("updatesView created successfully.")
    }
} 