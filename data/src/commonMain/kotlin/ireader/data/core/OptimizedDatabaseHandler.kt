package ireader.data.core

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.core.log.Log
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.minutes

/**
 * Optimized DatabaseHandler wrapper that delegates to DatabaseOptimizations.
 * 
 * This class provides a convenient wrapper around DatabaseHandler that automatically
 * uses the caching and performance monitoring features of DatabaseOptimizations.
 * 
 * For new code, prefer using DatabaseOptimizations directly.
 */
class OptimizedDatabaseHandler(
    private val delegate: DatabaseHandler,
    private val optimizations: DatabaseOptimizations? = null
) : DatabaseHandler by delegate {
    
    companion object {
        private const val TAG = "OptimizedDatabaseHandler"
    }
    
    /**
     * Execute query with caching support.
     * Falls back to regular execution if optimizations not available.
     */
    suspend fun <T> awaitCached(
        cacheKey: String,
        ttl: Long = 5.minutes.inWholeMilliseconds,
        inTransaction: Boolean = false,
        block: suspend Database.() -> T
    ): T {
        return optimizations?.awaitCached(cacheKey, ttl, inTransaction, block)
            ?: delegate.await(inTransaction, block)
    }
    
    /**
     * Execute list query with caching.
     * Falls back to regular execution if optimizations not available.
     */
    suspend fun <T : Any> awaitListCached(
        cacheKey: String,
        ttl: Long = 5.minutes.inWholeMilliseconds,
        inTransaction: Boolean = false,
        block: suspend Database.() -> Query<T>
    ): List<T> {
        return optimizations?.awaitListCached(cacheKey, ttl, inTransaction, block)
            ?: delegate.awaitList(inTransaction, block)
    }
    
    /**
     * Invalidate cache for specific key or pattern.
     */
    suspend fun invalidateCache(keyPattern: String) {
        optimizations?.invalidateCache(keyPattern)
        Log.debug("Cache invalidated: $keyPattern", TAG)
    }
    
    /**
     * Clear all cache.
     */
    suspend fun clearCache() {
        optimizations?.clearAllCache()
        Log.debug("All cache cleared", TAG)
    }
    
    /**
     * Get performance statistics.
     */
    fun getPerformanceStats(): PerformanceReport? {
        return optimizations?.getStats()
    }
    
    /**
     * Log performance report.
     */
    fun logPerformanceReport() {
        optimizations?.logPerformanceReport()
    }
}
