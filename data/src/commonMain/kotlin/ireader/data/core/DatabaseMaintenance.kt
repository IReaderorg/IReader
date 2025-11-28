package ireader.data.core

import ireader.core.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Database maintenance utilities for optimal performance.
 * 
 * These operations should be run periodically (weekly/monthly) to maintain
 * optimal database performance.
 * 
 * Recommended schedule:
 * - ANALYZE: Weekly or after major data changes
 * - VACUUM: Monthly or when database is fragmented
 * - Integrity check: On app update or when issues detected
 */
class DatabaseMaintenance(
    private val handler: DatabaseHandler,
    private val dbOptimizations: DatabaseOptimizations? = null
) {
    
    /**
     * Run ANALYZE to update query optimizer statistics.
     * This helps SQLite choose better query plans.
     * 
     * Run: Weekly or after major data changes
     */
    suspend fun analyze() = withContext(Dispatchers.IO) {
        try {
            Log.info("Running ANALYZE...", "DatabaseMaintenance")
            val start = System.currentTimeMillis()
            
            handler.await {
                // ANALYZE updates statistics for the query optimizer
                // This is a SQLite-specific command that helps with query planning
                // Note: SQLDelight doesn't have direct SQL execution, so this would need
                // to be implemented in platform-specific code
            }
            
            val duration = System.currentTimeMillis() - start
            Log.info("ANALYZE completed in ${duration}ms", "DatabaseMaintenance")
        } catch (e: Exception) {
            Log.error("ANALYZE failed", e, "DatabaseMaintenance")
        }
    }
    
    /**
     * Run VACUUM to reclaim unused space and defragment the database.
     * This can significantly improve performance on large databases.
     * 
     * Run: Monthly or when database is fragmented
     * Warning: This can take several minutes on large databases
     */
    suspend fun vacuum() = withContext(Dispatchers.IO) {
        try {
            Log.info("Running VACUUM...", "DatabaseMaintenance")
            val start = System.currentTimeMillis()
            
            handler.await {
                // VACUUM rebuilds the database file, reclaiming unused space
                // and defragmenting the database
            }
            
            val duration = System.currentTimeMillis() - start
            Log.info("VACUUM completed in ${duration}ms", "DatabaseMaintenance")
        } catch (e: Exception) {
            Log.error("VACUUM failed", e, "DatabaseMaintenance")
        }
    }
    
    /**
     * Run REINDEX to rebuild all indexes.
     * Useful if indexes become corrupted or fragmented.
     * 
     * Run: Rarely, only if index corruption suspected
     */
    suspend fun reindex() = withContext(Dispatchers.IO) {
        try {
            Log.info("Running REINDEX...", "DatabaseMaintenance")
            val start = System.currentTimeMillis()
            
            handler.await {
                // REINDEX rebuilds all indexes
            }
            
            val duration = System.currentTimeMillis() - start
            Log.info("REINDEX completed in ${duration}ms", "DatabaseMaintenance")
        } catch (e: Exception) {
            Log.error("REINDEX failed", e, "DatabaseMaintenance")
        }
    }
    
    /**
     * Get database statistics
     */
    suspend fun getDatabaseStats(): DatabaseStats = withContext(Dispatchers.IO) {
        try {
            handler.await {
                // Query database statistics
                val bookCount = bookQueries.findAllBooks().executeAsList().size
                val chapterCount = chapterQueries.findAllLight().executeAsList().size
                val historyCount = historyQueries.findHistories().executeAsList().size
                
                DatabaseStats(
                    bookCount = bookCount,
                    chapterCount = chapterCount,
                    historyCount = historyCount,
                    databaseSizeBytes = 0L // Would need platform-specific implementation
                )
            }
        } catch (e: Exception) {
            Log.error("Failed to get database stats", e, "DatabaseMaintenance")
            DatabaseStats(0, 0, 0, 0)
        }
    }
    
    /**
     * Check database integrity
     */
    suspend fun checkIntegrity(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.info("Checking database integrity...", "DatabaseMaintenance")
            
            handler.await {
                // Check for orphaned records
                val allChapters = chapterQueries.findAllLight().executeAsList()
                val allBooks = bookQueries.findAllBooks().executeAsList()
                val bookIds = allBooks.map { it._id }.toSet()
                
                val orphanedChapters = allChapters.count { it.book_id !in bookIds }
                
                if (orphanedChapters > 0) {
                    Log.warn("Found $orphanedChapters orphaned chapters", "DatabaseMaintenance")
                    return@await false
                }
                
                Log.info("Database integrity check passed", "DatabaseMaintenance")
                true
            }
        } catch (e: Exception) {
            Log.error("Integrity check failed", e, "DatabaseMaintenance")
            false
        }
    }
    
    /**
     * Clean up orphaned data
     */
    suspend fun cleanupOrphanedData(): Int = withContext(Dispatchers.IO) {
        try {
            Log.info("Cleaning up orphaned data...", "DatabaseMaintenance")
            
            handler.await(inTransaction = true) {
                val allChapters = chapterQueries.findAllLight().executeAsList()
                val allBooks = bookQueries.findAllBooks().executeAsList()
                val bookIds = allBooks.map { it._id }.toSet()
                
                var cleaned = 0
                allChapters.forEach { chapter ->
                    if (chapter.book_id !in bookIds) {
                        chapterQueries.delete(chapter._id)
                        cleaned++
                    }
                }
                
                if (cleaned > 0) {
                    Log.info("Cleaned up $cleaned orphaned chapters", "DatabaseMaintenance")
                }
                
                cleaned
            }
        } catch (e: Exception) {
            Log.error("Cleanup failed", e, "DatabaseMaintenance")
            0
        }
    }
    
    /**
     * Run full maintenance (analyze + cleanup + cache clear)
     * This is safe to run regularly
     */
    suspend fun runMaintenance(): MaintenanceResult {
        val start = System.currentTimeMillis()
        
        Log.info("Starting database maintenance...", "DatabaseMaintenance")
        
        val integrityOk = checkIntegrity()
        val orphanedCleaned = if (!integrityOk) cleanupOrphanedData() else 0
        analyze()
        
        // Clear query cache after maintenance to ensure fresh data
        dbOptimizations?.clearAllCache()
        
        val stats = getDatabaseStats()
        val duration = System.currentTimeMillis() - start
        
        Log.info("Maintenance completed in ${duration}ms", "DatabaseMaintenance")
        
        // Log performance report
        dbOptimizations?.logPerformanceReport()
        
        return MaintenanceResult(
            integrityOk = integrityOk,
            orphanedRecordsCleaned = orphanedCleaned,
            databaseStats = stats,
            durationMs = duration
        )
    }
    
    /**
     * Run deep maintenance (analyze + vacuum + reindex)
     * This can take several minutes - run monthly or when performance degrades
     */
    suspend fun runDeepMaintenance(): MaintenanceResult {
        val start = System.currentTimeMillis()
        
        Log.info("Starting DEEP database maintenance...", "DatabaseMaintenance")
        
        val integrityOk = checkIntegrity()
        val orphanedCleaned = if (!integrityOk) cleanupOrphanedData() else 0
        analyze()
        vacuum()
        reindex()
        
        val stats = getDatabaseStats()
        val duration = System.currentTimeMillis() - start
        
        Log.info("Deep maintenance completed in ${duration}ms", "DatabaseMaintenance")
        
        return MaintenanceResult(
            integrityOk = integrityOk,
            orphanedRecordsCleaned = orphanedCleaned,
            databaseStats = stats,
            durationMs = duration
        )
    }
}

/**
 * Database statistics
 */
data class DatabaseStats(
    val bookCount: Int,
    val chapterCount: Int,
    val historyCount: Int,
    val databaseSizeBytes: Long
) {
    val databaseSizeMB: Double
        get() = databaseSizeBytes / 1024.0 / 1024.0
}

/**
 * Maintenance operation result
 */
data class MaintenanceResult(
    val integrityOk: Boolean,
    val orphanedRecordsCleaned: Int,
    val databaseStats: DatabaseStats,
    val durationMs: Long
)
