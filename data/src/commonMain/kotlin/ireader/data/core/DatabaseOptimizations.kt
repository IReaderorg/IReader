package ireader.data.core

import app.cash.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Database performance optimizations providing:
 * - In-memory query caching with TTL
 * - Batch operations with proper transaction handling
 * - Flow optimizations (debounce, distinctUntilChanged)
 * - Performance monitoring and logging
 * - Preloading for critical data
 */
class DatabaseOptimizations(
    private val handler: DatabaseHandler
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val queryCache = QueryCache()
    private val performanceStats = PerformanceStats()
    
    companion object {
        private const val TAG = "DatabaseOptimizations"
        
        // Cache TTL defaults
        const val SHORT_CACHE_TTL = 10_000L      // 10 seconds
        const val MEDIUM_CACHE_TTL = 60_000L     // 1 minute
        const val LONG_CACHE_TTL = 300_000L      // 5 minutes
        
        // Debounce defaults
        const val DEFAULT_DEBOUNCE_MS = 100L
        const val FAST_DEBOUNCE_MS = 50L
    }
    
    // ==================== Cached Query Execution ====================
    
    /**
     * Execute a query with caching support.
     * Results are cached for the specified TTL to avoid repeated DB hits.
     */
    suspend fun <T> awaitCached(
        cacheKey: String,
        ttl: Long = MEDIUM_CACHE_TTL,
        inTransaction: Boolean = false,
        block: suspend Database.() -> T
    ): T {
        // Check cache first
        queryCache.get<T>(cacheKey)?.let { cached ->
            performanceStats.recordCacheHit(cacheKey)
            Log.debug("Cache HIT: $cacheKey", TAG)
            return cached
        }
        
        // Cache miss - execute query
        performanceStats.recordCacheMiss(cacheKey)
        val startTime = currentTimeToLong()
        
        val result = handler.await(inTransaction, block)
        
        val duration = currentTimeToLong() - startTime
        performanceStats.recordQuery(cacheKey, duration)
        
        if (duration > 100) {
            Log.warn("Slow query ($duration ms): $cacheKey", TAG)
        }
        
        // Store in cache
        queryCache.put(cacheKey, result, ttl)
        
        return result
    }
    
    /**
     * Execute a list query with caching support.
     */
    suspend fun <T : Any> awaitListCached(
        cacheKey: String,
        ttl: Long = MEDIUM_CACHE_TTL,
        inTransaction: Boolean = false,
        block: suspend Database.() -> Query<T>
    ): List<T> {
        queryCache.get<List<T>>(cacheKey)?.let { cached ->
            performanceStats.recordCacheHit(cacheKey)
            return cached
        }
        
        performanceStats.recordCacheMiss(cacheKey)
        val startTime = currentTimeToLong()
        
        val result = handler.awaitList(inTransaction, block)
        
        val duration = currentTimeToLong() - startTime
        performanceStats.recordQuery(cacheKey, duration)
        
        queryCache.put(cacheKey, result, ttl)
        
        return result
    }
    
    // ==================== Batch Operations ====================
    
    /**
     * Execute batch insert/update operations efficiently.
     * All operations run in a single transaction for better performance.
     */
    suspend fun <T, R> executeBatch(
        items: List<T>,
        operationName: String = "batch",
        operation: suspend Database.(T) -> R
    ): BatchResult<R> {
        if (items.isEmpty()) {
            return BatchResult(emptyList(), 0, emptyList())
        }
        
        val startTime = currentTimeToLong()
        val results = mutableListOf<R>()
        val errors = mutableListOf<Pair<Int, Throwable>>()
        var successCount = 0
        
        handler.await(inTransaction = true) {
            items.forEachIndexed { index, item ->
                try {
                    results.add(operation(item))
                    successCount++
                } catch (e: Exception) {
                    errors.add(index to e)
                    Log.error("Batch operation failed at index $index: ${e.message}", TAG)
                }
            }
        }
        
        val duration = currentTimeToLong() - startTime
        Log.info("Batch '$operationName': $successCount/${items.size} succeeded in ${duration}ms", TAG)
        performanceStats.recordQuery("batch_$operationName", duration)
        
        return BatchResult(results, successCount, errors)
    }
    
    /**
     * Execute batch operations with chunking for very large datasets.
     * Prevents memory issues and allows progress tracking.
     */
    suspend fun <T, R> executeBatchChunked(
        items: List<T>,
        chunkSize: Int = 100,
        operationName: String = "chunked_batch",
        onProgress: ((processed: Int, total: Int) -> Unit)? = null,
        operation: suspend Database.(T) -> R
    ): BatchResult<R> {
        if (items.isEmpty()) {
            return BatchResult(emptyList(), 0, emptyList())
        }
        
        val startTime = currentTimeToLong()
        val allResults = mutableListOf<R>()
        val allErrors = mutableListOf<Pair<Int, Throwable>>()
        var totalSuccess = 0
        var processedCount = 0
        
        items.chunked(chunkSize).forEachIndexed { chunkIndex, chunk ->
            val chunkResult = executeBatch(chunk, "${operationName}_chunk_$chunkIndex", operation)
            allResults.addAll(chunkResult.results)
            allErrors.addAll(chunkResult.errors.map { (idx, err) -> 
                (chunkIndex * chunkSize + idx) to err 
            })
            totalSuccess += chunkResult.successCount
            processedCount += chunk.size
            onProgress?.invoke(processedCount, items.size)
        }
        
        val duration = currentTimeToLong() - startTime
        Log.info("Chunked batch '$operationName': $totalSuccess/${items.size} in ${duration}ms", TAG)
        
        return BatchResult(allResults, totalSuccess, allErrors)
    }
    
    // ==================== Flow Optimizations ====================
    
    /**
     * Subscribe to a query with optimizations:
     * - Debouncing to prevent rapid emissions
     * - distinctUntilChanged to skip duplicate values
     * - Error handling with logging
     */
    @OptIn(FlowPreview::class)
    fun <T : Any> subscribeOptimized(
        queryName: String,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        block: Database.() -> Query<T>
    ): Flow<List<T>> {
        return handler.subscribeToList(block)
            .debounce(debounceMs.milliseconds)
            .distinctUntilChanged()
            .onEach { list ->
                Log.debug("Query '$queryName' emitted ${list.size} items", TAG)
            }
            .catch { e ->
                Log.error("Query '$queryName' failed: ${e.message}", TAG)
                throw e
            }
    }
    
    /**
     * Subscribe to a single value query with optimizations.
     */
    @OptIn(FlowPreview::class)
    fun <T : Any> subscribeOneOptimized(
        queryName: String,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        block: Database.() -> Query<T>
    ): Flow<T?> {
        return handler.subscribeToOneOrNull(block)
            .debounce(debounceMs.milliseconds)
            .distinctUntilChanged()
            .catch { e ->
                Log.error("Query '$queryName' failed: ${e.message}", TAG)
                throw e
            }
    }
    
    // ==================== Cache Management ====================
    
    /**
     * Invalidate cache entries matching a pattern.
     * Use after write operations to ensure fresh data.
     */
    suspend fun invalidateCache(pattern: String) {
        queryCache.invalidate(pattern)
        Log.debug("Cache invalidated: $pattern", TAG)
    }
    
    /**
     * Invalidate all cache entries for a specific entity type.
     */
    suspend fun invalidateCacheForEntity(entityType: String) {
        queryCache.invalidate(entityType)
        Log.debug("Cache invalidated for entity: $entityType", TAG)
    }
    
    /**
     * Clear all cached data.
     */
    suspend fun clearAllCache() {
        queryCache.clear()
        Log.info("All cache cleared", TAG)
    }
    
    // ==================== Performance Monitoring ====================
    
    /**
     * Get current performance statistics.
     */
    fun getStats(): PerformanceReport {
        return performanceStats.getReport()
    }
    
    /**
     * Log performance report.
     */
    fun logPerformanceReport() {
        val report = getStats()
        Log.info("""
            |=== Database Performance Report ===
            |Total queries: ${report.totalQueries}
            |Cache hits: ${report.cacheHits} (${report.cacheHitRate}%)
            |Cache misses: ${report.cacheMisses}
            |Average query time: ${report.averageQueryTimeMs}ms
            |Slow queries (>100ms): ${report.slowQueryCount}
            |===================================
        """.trimMargin(), TAG)
    }
    
    /**
     * Reset performance statistics.
     */
    fun resetStats() {
        performanceStats.reset()
    }
    
    // ==================== Preloading ====================
    
    /**
     * Preload critical data into cache for faster initial access.
     * Call this during app startup via DatabasePreloader.
     */
    suspend fun preloadCriticalData() = withContext(ioDispatcher) {
        Log.info("Starting critical data preload...", TAG)
        val startTime = currentTimeToLong()
        
        try {
            // Preload is handled by DatabasePreloader which has access to mappers
            val duration = currentTimeToLong() - startTime
            Log.info("Critical data preload completed in ${duration}ms", TAG)
        } catch (e: Exception) {
            Log.error("Critical data preload failed: ${e.message}", TAG)
        }
    }
    
    /**
     * Preload data for a specific book (chapters, history).
     * Call when user opens a book detail screen.
     * Note: Actual preloading is done via DatabasePreloader.
     */
    suspend fun preloadBookData(bookId: Long) = withContext(ioDispatcher) {
        Log.debug("Book data preload requested for book $bookId", TAG)
        // Preloading is handled by DatabasePreloader which has access to mappers
    }
}

// ==================== Supporting Classes ====================

/**
 * Result of a batch operation.
 */
data class BatchResult<R>(
    val results: List<R>,
    val successCount: Int,
    val errors: List<Pair<Int, Throwable>>
) {
    val failureCount: Int get() = errors.size
    val totalCount: Int get() = successCount + failureCount
    val successRate: Float get() = if (totalCount > 0) successCount.toFloat() / totalCount else 0f
    val isFullySuccessful: Boolean get() = errors.isEmpty()
}

/**
 * In-memory cache with TTL support.
 */
private class QueryCache {
    private data class CacheEntry<T>(
        val value: T,
        val expiresAt: Long
    )
    
    private val cache = mutableMapOf<String, CacheEntry<*>>()
    private val mutex = Mutex()
    
    suspend fun <T> get(key: String): T? = mutex.withLock {
        @Suppress("UNCHECKED_CAST")
        val entry = cache[key] as? CacheEntry<T> ?: return null
        
        if (currentTimeToLong() > entry.expiresAt) {
            cache.remove(key)
            return null
        }
        
        entry.value
    }
    
    suspend fun <T> put(key: String, value: T, ttl: Long) = mutex.withLock {
        cache[key] = CacheEntry(
            value = value,
            expiresAt = currentTimeToLong() + ttl
        )
        
        // Cleanup expired entries periodically (every 100 puts)
        if (cache.size % 100 == 0) {
            cleanupExpired()
        }
    }
    
    suspend fun invalidate(pattern: String) = mutex.withLock {
        val keysToRemove = cache.keys.filter { it.contains(pattern, ignoreCase = true) }
        keysToRemove.forEach { cache.remove(it) }
    }
    
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    private fun cleanupExpired() {
        val now = currentTimeToLong()
        val expiredKeys = cache.entries
            .filter { it.value.expiresAt < now }
            .map { it.key }
        expiredKeys.forEach { cache.remove(it) }
    }
}

/**
 * Performance statistics tracker.
 */
private class PerformanceStats {
    private data class QueryStats(
        var count: Long = 0,
        var totalTime: Long = 0,
        var maxTime: Long = 0
    )
    
    private val queryStats = mutableMapOf<String, QueryStats>()
    private var cacheHits = 0L
    private var cacheMisses = 0L
    private val mutex = Mutex()
    
    suspend fun recordQuery(key: String, duration: Long) = mutex.withLock {
        val stats = queryStats.getOrPut(key) { QueryStats() }
        stats.count++
        stats.totalTime += duration
        stats.maxTime = maxOf(stats.maxTime, duration)
    }
    
    suspend fun recordCacheHit(key: String) = mutex.withLock {
        cacheHits++
    }
    
    suspend fun recordCacheMiss(key: String) = mutex.withLock {
        cacheMisses++
    }
    
    fun getReport(): PerformanceReport {
        val totalQueries = queryStats.values.sumOf { it.count }
        val totalTime = queryStats.values.sumOf { it.totalTime }
        val avgTime = if (totalQueries > 0) totalTime / totalQueries else 0
        val slowQueries = queryStats.count { it.value.maxTime > 100 }
        val hitRate = if (cacheHits + cacheMisses > 0) {
            (cacheHits * 100) / (cacheHits + cacheMisses)
        } else 0
        
        return PerformanceReport(
            totalQueries = totalQueries,
            cacheHits = cacheHits,
            cacheMisses = cacheMisses,
            cacheHitRate = hitRate,
            averageQueryTimeMs = avgTime,
            slowQueryCount = slowQueries
        )
    }
    
    fun reset() {
        queryStats.clear()
        cacheHits = 0
        cacheMisses = 0
    }
}

/**
 * Performance report data.
 */
data class PerformanceReport(
    val totalQueries: Long,
    val cacheHits: Long,
    val cacheMisses: Long,
    val cacheHitRate: Long,
    val averageQueryTimeMs: Long,
    val slowQueryCount: Int
)
