package ireader.data.core

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ireader.core.log.Log

/**
 * Android-specific database optimizations.
 * These settings significantly improve SQLite performance on Android.
 */
object AndroidDatabaseOptimizations {
    
    /**
     * Apply optimal SQLite PRAGMA settings for Android.
     * Call this when creating the database driver.
     */
    fun applyOptimalPragmas(db: SupportSQLiteDatabase) {
        try {
            Log.info("Applying optimal SQLite pragmas...", "AndroidDatabaseOptimizations")
            
            // Write-Ahead Logging - Allows concurrent reads and writes
            // This is one of the most important optimizations
            db.execSQL("PRAGMA journal_mode = WAL")
            
            // Synchronous mode - NORMAL is safe and much faster than FULL
            // FULL: Sync after every write (slow but safest)
            // NORMAL: Sync at critical moments (fast and safe enough)
            // OFF: No sync (fastest but risky)
            db.execSQL("PRAGMA synchronous = NORMAL")
            
            // Temp store in memory - Faster temp operations
            db.execSQL("PRAGMA temp_store = MEMORY")
            
            // Cache size - 64MB cache (negative value = KB)
            // Larger cache = fewer disk reads
            db.execSQL("PRAGMA cache_size = -64000")
            
            // Memory-mapped I/O - 256MB
            // Allows SQLite to use memory-mapped files for better performance
            db.execSQL("PRAGMA mmap_size = 268435456")
            
            // Page size - 4KB (optimal for most Android devices)
            // Must be set before any tables are created
            // db.execSQL("PRAGMA page_size = 4096")
            
            // Auto vacuum - Incremental
            // Automatically reclaims space without blocking
            db.execSQL("PRAGMA auto_vacuum = INCREMENTAL")
            
            // Foreign keys - Enable for data integrity
            db.execSQL("PRAGMA foreign_keys = ON")
            
            // Optimize for read-heavy workloads
            db.execSQL("PRAGMA optimize")
            
            Log.info("SQLite pragmas applied successfully", "AndroidDatabaseOptimizations")
            
            // Log current settings
            logPragmaSettings(db)
            
        } catch (e: Exception) {
            Log.error("Failed to apply SQLite pragmas", e, "AndroidDatabaseOptimizations")
        }
    }
    
    /**
     * Run ANALYZE to update query optimizer statistics
     */
    fun runAnalyze(db: SupportSQLiteDatabase) {
        try {
            Log.info("Running ANALYZE...", "AndroidDatabaseOptimizations")
            val start = System.currentTimeMillis()
            
            db.execSQL("ANALYZE")
            
            val duration = System.currentTimeMillis() - start
            Log.info("ANALYZE completed in ${duration}ms", "AndroidDatabaseOptimizations")
        } catch (e: Exception) {
            Log.error("ANALYZE failed", e, "AndroidDatabaseOptimizations")
        }
    }
    
    /**
     * Run VACUUM to reclaim space and defragment
     */
    fun runVacuum(db: SupportSQLiteDatabase) {
        try {
            Log.info("Running VACUUM...", "AndroidDatabaseOptimizations")
            val start = System.currentTimeMillis()
            
            db.execSQL("VACUUM")
            
            val duration = System.currentTimeMillis() - start
            Log.info("VACUUM completed in ${duration}ms", "AndroidDatabaseOptimizations")
        } catch (e: Exception) {
            Log.error("VACUUM failed", e, "AndroidDatabaseOptimizations")
        }
    }
    
    /**
     * Run incremental vacuum to reclaim some space without blocking
     */
    fun runIncrementalVacuum(db: SupportSQLiteDatabase, pages: Int = 100) {
        try {
            Log.info("Running incremental VACUUM ($pages pages)...", "AndroidDatabaseOptimizations")
            val start = System.currentTimeMillis()
            
            db.execSQL("PRAGMA incremental_vacuum($pages)")
            
            val duration = System.currentTimeMillis() - start
            Log.info("Incremental VACUUM completed in ${duration}ms", "AndroidDatabaseOptimizations")
        } catch (e: Exception) {
            Log.error("Incremental VACUUM failed", e, "AndroidDatabaseOptimizations")
        }
    }
    
    /**
     * Get database file size
     */
    fun getDatabaseSize(context: Context, dbName: String): Long {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) dbFile.length() else 0L
        } catch (e: Exception) {
            Log.error("Failed to get database size", e, "AndroidDatabaseOptimizations")
            0L
        }
    }
    
    /**
     * Log current PRAGMA settings for debugging
     */
    private fun logPragmaSettings(db: SupportSQLiteDatabase) {
        try {
            val settings = mapOf(
                "journal_mode" to db.query("PRAGMA journal_mode"),
                "synchronous" to db.query("PRAGMA synchronous"),
                "cache_size" to db.query("PRAGMA cache_size"),
                "page_size" to db.query("PRAGMA page_size"),
                "mmap_size" to db.query("PRAGMA mmap_size"),
                "auto_vacuum" to db.query("PRAGMA auto_vacuum"),
                "foreign_keys" to db.query("PRAGMA foreign_keys")
            )
            
            Log.debug("Current SQLite settings:", "AndroidDatabaseOptimizations")
            settings.forEach { (key, value) ->
                Log.debug("  $key = $value", "AndroidDatabaseOptimizations")
            }
        } catch (e: Exception) {
            Log.error("Failed to log pragma settings", e, "AndroidDatabaseOptimizations")
        }
    }
    
    /**
     * Helper to query pragma value
     */
    private fun SupportSQLiteDatabase.query(sql: String): String {
        return query(sql).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                "unknown"
            }
        }
    }
    
    /**
     * Create optimized AndroidSqliteDriver with all optimizations applied
     */
    fun createOptimizedDriver(
        context: Context,
        schema: app.cash.sqldelight.db.SqlSchema<app.cash.sqldelight.db.QueryResult.Value<Unit>>,
        name: String
    ): AndroidSqliteDriver {
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = name,
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    applyOptimalPragmas(db)
                }
                
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    // Enable foreign keys before any operations
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            }
        )
    }
}
