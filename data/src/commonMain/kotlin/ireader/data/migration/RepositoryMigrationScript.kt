package ireader.data.migration

import ireader.core.log.Log
import ireader.data.core.DatabaseHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Migration script for transitioning from old repository structure to new consolidated repositories.
 *
 * This script handles:
 * - Database schema updates
 * - Data migration
 * - Dependency injection updates
 * - Cleanup of deprecated code
 *
 * @property handler Database handler for executing migrations
 * @property scope Coroutine scope for async operations
 */
class RepositoryMigrationScript(
    private val handler: DatabaseHandler,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    /**
     * Executes the complete migration process.
     *
     * Steps:
     * 1. Backup current database
     * 2. Create new tables/columns
     * 3. Migrate data
     * 4. Verify data integrity
     * 5. Cleanup old structures
     *
     * @return MigrationResult indicating success or failure
     */
    suspend fun executeMigration(): MigrationResult {
        return try {
            Log.info("Starting repository migration...")
            
            // Step 1: Backup
            backupDatabase()
            
            // Step 2: Schema updates
            updateDatabaseSchema()
            
            // Step 3: Data migration
            migrateBookData()
            migrateChapterData()
            migrateCategoryData()
            migrateHistoryData()
            migrateDownloadData()
            
            // Step 4: Verification
            verifyDataIntegrity()
            
            // Step 5: Cleanup
            cleanupDeprecatedStructures()
            
            Log.info("Repository migration completed successfully")
            MigrationResult.Success
        } catch (e: Exception) {
            Log.error("Repository migration failed", e)
            rollbackMigration()
            MigrationResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Creates a backup of the current database.
     */
    private suspend fun backupDatabase() {
        Log.info("Creating database backup...")
        handler.await {
            // Create backup tables
            execSQL("""
                CREATE TABLE IF NOT EXISTS books_backup AS 
                SELECT * FROM books
            """.trimIndent())
            
            execSQL("""
                CREATE TABLE IF NOT EXISTS chapters_backup AS 
                SELECT * FROM chapters
            """.trimIndent())
            
            execSQL("""
                CREATE TABLE IF NOT EXISTS categories_backup AS 
                SELECT * FROM categories
            """.trimIndent())
        }
        Log.info("Database backup created")
    }

    /**
     * Updates database schema for new repository structure.
     */
    private suspend fun updateDatabaseSchema() {
        Log.info("Updating database schema...")
        handler.await {
            // Add new columns for enhanced functionality
            execSQL("""
                ALTER TABLE books 
                ADD COLUMN IF NOT EXISTS last_modified_at INTEGER DEFAULT 0
            """.trimIndent())
            
            execSQL("""
                ALTER TABLE books 
                ADD COLUMN IF NOT EXISTS sync_status INTEGER DEFAULT 0
            """.trimIndent())
            
            execSQL("""
                ALTER TABLE chapters 
                ADD COLUMN IF NOT EXISTS last_modified_at INTEGER DEFAULT 0
            """.trimIndent())
            
            execSQL("""
                ALTER TABLE chapters 
                ADD COLUMN IF NOT EXISTS sync_status INTEGER DEFAULT 0
            """.trimIndent())
            
            // Create indexes for performance
            execSQL("""
                CREATE INDEX IF NOT EXISTS idx_books_source_id 
                ON books(sourceId)
            """.trimIndent())
            
            execSQL("""
                CREATE INDEX IF NOT EXISTS idx_books_favorite 
                ON books(favorite)
            """.trimIndent())
            
            execSQL("""
                CREATE INDEX IF NOT EXISTS idx_chapters_book_id 
                ON chapters(bookId)
            """.trimIndent())
            
            execSQL("""
                CREATE INDEX IF NOT EXISTS idx_chapters_read 
                ON chapters(read)
            """.trimIndent())
        }
        Log.info("Database schema updated")
    }

    /**
     * Migrates book data to new structure.
     */
    private suspend fun migrateBookData() {
        Log.info("Migrating book data...")
        handler.await {
            // Update last_modified_at for existing books
            execSQL("""
                UPDATE books 
                SET last_modified_at = lastUpdate 
                WHERE last_modified_at = 0
            """.trimIndent())
            
            // Set default sync_status
            execSQL("""
                UPDATE books 
                SET sync_status = 0 
                WHERE sync_status = 0
            """.trimIndent())
        }
        Log.info("Book data migrated")
    }

    /**
     * Migrates chapter data to new structure.
     */
    private suspend fun migrateChapterData() {
        Log.info("Migrating chapter data...")
        handler.await {
            // Update last_modified_at for existing chapters
            execSQL("""
                UPDATE chapters 
                SET last_modified_at = dateFetch 
                WHERE last_modified_at = 0
            """.trimIndent())
            
            // Set default sync_status
            execSQL("""
                UPDATE chapters 
                SET sync_status = 0 
                WHERE sync_status = 0
            """.trimIndent())
        }
        Log.info("Chapter data migrated")
    }

    /**
     * Migrates category data to new structure.
     */
    private suspend fun migrateCategoryData() {
        Log.info("Migrating category data...")
        handler.await {
            // Ensure all categories have proper ordering
            execSQL("""
                UPDATE categories 
                SET sort = id 
                WHERE sort IS NULL OR sort = 0
            """.trimIndent())
        }
        Log.info("Category data migrated")
    }

    /**
     * Migrates history data to new structure.
     */
    private suspend fun migrateHistoryData() {
        Log.info("Migrating history data...")
        handler.await {
            // Clean up orphaned history entries
            execSQL("""
                DELETE FROM history 
                WHERE chapterId NOT IN (SELECT id FROM chapters)
            """.trimIndent())
        }
        Log.info("History data migrated")
    }

    /**
     * Migrates download data to new structure.
     */
    private suspend fun migrateDownloadData() {
        Log.info("Migrating download data...")
        handler.await {
            // Clean up orphaned download entries
            execSQL("""
                DELETE FROM downloads 
                WHERE chapterId NOT IN (SELECT id FROM chapters)
            """.trimIndent())
        }
        Log.info("Download data migrated")
    }

    /**
     * Verifies data integrity after migration.
     */
    private suspend fun verifyDataIntegrity() {
        Log.info("Verifying data integrity...")
        handler.await {
            // Verify book count
            val bookCount = rawQuery("SELECT COUNT(*) FROM books", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            }
            
            val backupBookCount = rawQuery("SELECT COUNT(*) FROM books_backup", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            }
            
            if (bookCount != backupBookCount) {
                throw IllegalStateException("Book count mismatch: $bookCount vs $backupBookCount")
            }
            
            // Verify chapter count
            val chapterCount = rawQuery("SELECT COUNT(*) FROM chapters", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            }
            
            val backupChapterCount = rawQuery("SELECT COUNT(*) FROM chapters_backup", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            }
            
            if (chapterCount != backupChapterCount) {
                throw IllegalStateException("Chapter count mismatch: $chapterCount vs $backupChapterCount")
            }
        }
        Log.info("Data integrity verified")
    }

    /**
     * Cleans up deprecated database structures.
     */
    private suspend fun cleanupDeprecatedStructures() {
        Log.info("Cleaning up deprecated structures...")
        handler.await {
            // Drop backup tables after successful migration
            execSQL("DROP TABLE IF EXISTS books_backup")
            execSQL("DROP TABLE IF EXISTS chapters_backup")
            execSQL("DROP TABLE IF EXISTS categories_backup")
            
            // Remove deprecated columns (if any)
            // Note: SQLite doesn't support DROP COLUMN directly,
            // so we would need to recreate tables if needed
        }
        Log.info("Deprecated structures cleaned up")
    }

    /**
     * Rolls back migration in case of failure.
     */
    private suspend fun rollbackMigration() {
        Log.warn("Rolling back migration...")
        try {
            handler.await {
                // Restore from backup
                execSQL("DROP TABLE IF EXISTS books")
                execSQL("ALTER TABLE books_backup RENAME TO books")
                
                execSQL("DROP TABLE IF EXISTS chapters")
                execSQL("ALTER TABLE chapters_backup RENAME TO chapters")
                
                execSQL("DROP TABLE IF EXISTS categories")
                execSQL("ALTER TABLE categories_backup RENAME TO categories")
            }
            Log.info("Migration rolled back successfully")
        } catch (e: Exception) {
            Log.error("Rollback failed", e)
        }
    }

    /**
     * Checks if migration is needed.
     */
    suspend fun isMigrationNeeded(): Boolean {
        return try {
            handler.await {
                // Check if new columns exist
                val cursor = rawQuery("PRAGMA table_info(books)", null)
                var hasLastModifiedAt = false
                
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndex("name"))
                    if (columnName == "last_modified_at") {
                        hasLastModifiedAt = true
                        break
                    }
                }
                cursor.close()
                
                !hasLastModifiedAt
            }
        } catch (e: Exception) {
            Log.error("Error checking migration status", e)
            false
        }
    }
}

/**
 * Result of migration operation.
 */
sealed class MigrationResult {
    /**
     * Migration completed successfully.
     */
    object Success : MigrationResult()
    
    /**
     * Migration failed with error.
     *
     * @property message Error message
     */
    data class Failure(val message: String) : MigrationResult()
}

/**
 * Migration statistics for monitoring.
 */
data class MigrationStats(
    val startTime: Long,
    val endTime: Long,
    val booksProcessed: Int,
    val chaptersProcessed: Int,
    val categoriesProcessed: Int,
    val errors: List<String>
) {
    val duration: Long get() = endTime - startTime
    val success: Boolean get() = errors.isEmpty()
}
