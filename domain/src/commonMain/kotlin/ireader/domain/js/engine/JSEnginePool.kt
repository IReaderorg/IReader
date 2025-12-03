package ireader.domain.js.engine

import ireader.domain.js.models.PluginPerformanceMetrics
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Compiled code cache entry.
 */
private data class CompiledCode(
    val code: String,
    val compiledAt: Long = currentTimeToLong(),
    var lastAccessTime: Long = currentTimeToLong(),
    val sizeBytes: Long = code.length.toLong()
)

/**
 * Pool of JavaScript engines for reusing engine instances across plugin invocations.
 * Implements LRU eviction, idle timeout, compiled code caching, and performance monitoring.
 */
class JSEnginePool(
    private val maxPoolSize: Int = 50, // Increased from 10 to support more plugins
    private val idleTimeoutMillis: Long = 30 * 60 * 1000L, // 30 minutes (not used for auto-cleanup)
    private val maxConcurrentExecutions: Int = 5,
    private val maxCacheSizeBytes: Long = 50 * 1024 * 1024L // 50MB
) {
    
    private data class EngineEntry(
        val engine: JSEngine,
        var lastUsedTime: Long = currentTimeToLong()
    )
    
    private val engines = mutableMapOf<String, EngineEntry>()
    private val mutex = Mutex()
    
    // Compiled code cache with LRU eviction
    private val codeCache = mutableMapOf<String, CompiledCode>()
    private val cacheMutex = Mutex()
    
    // Concurrent execution limiter
    private val executionSemaphore = Semaphore(maxConcurrentExecutions)
    
    // Performance metrics
    private val metricsMap = mutableMapOf<String, PluginPerformanceMetrics>()
    private val metricsMutex = Mutex()
    
    /**
     * Gets or creates a JavaScript engine for the given plugin ID.
     * @param pluginId The plugin identifier
     * @return A JavaScript engine instance
     */
    suspend fun getOrCreate(pluginId: String): JSEngine = mutex.withLock {
        // Get existing engine or create new one
        val entry = engines[pluginId]
        if (entry != null) {
            // Update last used time to prevent cleanup
            val idleTime = currentTimeToLong() - entry.lastUsedTime
            println("[JSEnginePool] Reusing existing engine for plugin: $pluginId (was idle for ${idleTime}ms)")
            entry.lastUsedTime = currentTimeToLong()
            return entry.engine
        }
        
        // Clean up idle engines (but not the one we're about to use)
        cleanupIdleEngines()
        
        // Evict LRU engine if pool is full
        if (engines.size >= maxPoolSize) {
            evictLRU()
        }
        
        // Create new engine
        println("[JSEnginePool] Creating new engine for plugin: $pluginId (pool size: ${engines.size})")
        val engine = JSEngine()
        engine.initialize()
        engines[pluginId] = EngineEntry(engine)
        
        return engine
    }
    
    /**
     * Returns an engine to the pool.
     * @param pluginId The plugin identifier
     */
    suspend fun returnEngine(pluginId: String) = mutex.withLock {
        engines[pluginId]?.lastUsedTime = currentTimeToLong()
    }
    
    /**
     * Removes and disposes an engine from the pool.
     * @param pluginId The plugin identifier
     */
    suspend fun remove(pluginId: String) = mutex.withLock {
        println("[JSEnginePool] Explicitly removing engine for plugin: $pluginId")
        engines.remove(pluginId)?.engine?.dispose()
    }
    
    /**
     * Clears all engines from the pool.
     */
    suspend fun clear() = mutex.withLock {
        engines.values.forEach { it.engine.dispose() }
        engines.clear()
    }
    
    /**
     * Gets the current pool size.
     */
    suspend fun size(): Int = mutex.withLock {
        engines.size
    }
    
    /**
     * Cleans up engines that have been idle for too long.
     * NOTE: This is currently disabled because engines are referenced by JSPluginBridge
     * and disposing them would break active plugins. Engines are only disposed when
     * explicitly removed or when the pool is cleared.
     */
    private fun cleanupIdleEngines() {
        // DISABLED: Don't clean up idle engines automatically
        // Engines are disposed only when:
        // 1. Plugin is explicitly unloaded (enginePool.remove())
        // 2. Pool is cleared (enginePool.clear())
        // 3. Pool is full and LRU eviction is needed
        
        // This prevents "Engine not initialized" errors when JSPluginBridge
        // holds a reference to an engine that gets disposed.
        
        /* Original code (disabled):
        val now = currentTimeToLong()
        val toRemove = engines.filter { (_, entry) ->
            val idleTime = now - entry.lastUsedTime
            idleTime > idleTimeoutMillis
        }.keys
        
        if (toRemove.isNotEmpty()) {
            println("[JSEnginePool] Cleaning up ${toRemove.size} idle engines: $toRemove")
        }
        
        toRemove.forEach { pluginId ->
            println("[JSEnginePool] Disposing idle engine for plugin: $pluginId")
            engines.remove(pluginId)?.engine?.dispose()
        }
        */
    }
    
    /**
     * Evicts the least recently used engine from the pool.
     */
    private fun evictLRU() {
        val lruEntry = engines.minByOrNull { it.value.lastUsedTime }
        lruEntry?.let { (pluginId, entry) ->
            println("[JSEnginePool] Evicting LRU engine for plugin: $pluginId (pool is full)")
            entry.engine.dispose()
            engines.remove(pluginId)
        }
    }
    
    /**
     * Executes a plugin operation with concurrent execution limiting.
     * 
     * @param pluginId The plugin identifier
     * @param operation The operation to execute
     * @return The result of the operation
     */
    suspend fun <T> executeWithLimit(pluginId: String, operation: suspend () -> T): T {
        return executionSemaphore.withPermit {
            val startTime = currentTimeToLong()
            var success = false
            
            try {
                val result = operation()
                success = true
                return@withPermit result
            } finally {
                val executionTime = currentTimeToLong() - startTime
                recordMetrics(pluginId, executionTime, success)
            }
        }
    }
    
    /**
     * Gets compiled code from cache or compiles and caches it.
     * 
     * @param cacheKey The cache key
     * @param code The code to compile
     * @return The compiled code
     */
    suspend fun getCachedCode(cacheKey: String, code: String): String = cacheMutex.withLock {
        val cached = codeCache[cacheKey]
        if (cached != null) {
            cached.lastAccessTime = currentTimeToLong()
            return cached.code
        }
        
        // Evict old entries if cache is too large
        evictCacheIfNeeded(code.length.toLong())
        
        // Cache the code
        val compiled = CompiledCode(code)
        codeCache[cacheKey] = compiled
        
        return code
    }
    
    /**
     * Evicts cache entries if adding new code would exceed the size limit.
     */
    private fun evictCacheIfNeeded(newCodeSize: Long) {
        var currentSize = codeCache.values.sumOf { it.sizeBytes }
        
        while (currentSize + newCodeSize > maxCacheSizeBytes && codeCache.isNotEmpty()) {
            // Remove least recently accessed entry
            val lruEntry = codeCache.minByOrNull { it.value.lastAccessTime }
            lruEntry?.let { (key, entry) ->
                codeCache.remove(key)
                currentSize -= entry.sizeBytes
            }
        }
    }
    
    /**
     * Records performance metrics for a plugin execution.
     */
    private suspend fun recordMetrics(pluginId: String, executionTime: Long, success: Boolean) {
        metricsMutex.withLock {
            val current = metricsMap[pluginId] ?: PluginPerformanceMetrics(pluginId)
            metricsMap[pluginId] = current.recordCall(executionTime, success)
        }
    }
    
    /**
     * Updates memory usage for a plugin.
     * Note: Memory tracking is platform-specific and may not be accurate on all platforms.
     */
    suspend fun updateMemoryUsage(pluginId: String) = metricsMutex.withLock {
        // Use a simple estimation based on cache size since Runtime is JVM-only
        val estimatedMemory = codeCache.values.sumOf { it.sizeBytes }
        
        val current = metricsMap[pluginId] ?: PluginPerformanceMetrics(pluginId)
        metricsMap[pluginId] = current.updateMemoryUsage(estimatedMemory)
    }
    
    /**
     * Gets performance metrics for a plugin.
     */
    suspend fun getMetrics(pluginId: String): PluginPerformanceMetrics? = metricsMutex.withLock {
        metricsMap[pluginId]
    }
    
    /**
     * Gets all performance metrics.
     */
    suspend fun getAllMetrics(): Map<String, PluginPerformanceMetrics> = metricsMutex.withLock {
        metricsMap.toMap()
    }
    
    /**
     * Clears performance metrics for a plugin.
     */
    suspend fun clearMetrics(pluginId: String) = metricsMutex.withLock {
        metricsMap.remove(pluginId)
    }
    
    /**
     * Gets the current cache size in bytes.
     */
    suspend fun getCacheSizeBytes(): Long = cacheMutex.withLock {
        codeCache.values.sumOf { it.sizeBytes }
    }
    
    /**
     * Clears the compiled code cache.
     */
    suspend fun clearCache() = cacheMutex.withLock {
        codeCache.clear()
    }
}
