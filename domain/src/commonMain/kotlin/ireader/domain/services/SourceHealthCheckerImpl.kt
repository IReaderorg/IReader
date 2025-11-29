package ireader.domain.services

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.SourceHealth
import ireader.domain.models.entities.SourceStatus
import ireader.domain.models.entities.isUsable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of SourceHealthChecker that monitors source availability and performance
 */
class SourceHealthCheckerImpl(
    private val catalogStore: CatalogStore
) : SourceHealthChecker {
    
    private val cache = mutableMapOf<Long, SourceHealth>()
    private val cacheLock = Mutex()
    
    // Cache TTL: 5 minutes
    private val CACHE_TTL_MS = 5 * 60 * 1000L
    
    /**
     * Check the health status of a specific source
     */
    override suspend fun checkStatus(sourceId: Long): SourceHealth {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Get catalog source
            val catalog = catalogStore.get(sourceId)
            
            if (catalog == null) {
                Log.warn { "Source not found: $sourceId" }
                return createHealthResult(
                    sourceId,
                    SourceStatus.Error("Source not found"),
                    null
                )
            }
            
            // Check if source is available and usable
            val result = try {
                val responseTime = System.currentTimeMillis() - startTime
                
                // Check if the catalog has a valid source
                val source = catalog.source
                if (source == null) {
                    createHealthResult(sourceId, SourceStatus.Error("Source not initialized"), responseTime)
                } else if (catalog is CatalogInstalled && !catalog.isUsable) {
                    createHealthResult(sourceId, SourceStatus.Error("Source is obsolete or unusable"), responseTime)
                } else {
                    // Source is available
                    createHealthResult(sourceId, SourceStatus.Online, responseTime)
                }
            } catch (e: Exception) {
                Log.error { "Source health check failed for source $sourceId" }
                val responseTime = System.currentTimeMillis() - startTime
                
                // Determine status based on error type
                val status = when {
                    e.message?.contains("401", ignoreCase = true) == true ||
                    e.message?.contains("403", ignoreCase = true) == true ||
                    e.message?.contains("auth", ignoreCase = true) == true -> 
                        SourceStatus.LoginRequired
                    
                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("unreachable", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ->
                        SourceStatus.Offline
                    
                    else -> SourceStatus.Error(e.message ?: "Unknown error")
                }
                
                createHealthResult(sourceId, status, responseTime)
            }
            
            // Cache the result
            cacheLock.withLock {
                cache[sourceId] = result
            }
            
            result
        } catch (e: Exception) {
            Log.error { "Unexpected error during source health check" }
            createHealthResult(
                sourceId,
                SourceStatus.Error(e.message ?: "Unexpected error"),
                null
            )
        }
    }
    
    /**
     * Check the health status of multiple sources
     * Note: Sequential checks to avoid overwhelming the network
     */
    override suspend fun checkMultipleSources(sourceIds: List<Long>): Map<Long, SourceHealth> {
        return sourceIds.associateWith { sourceId ->
            try {
                // Check cache first
                val cached = getCachedStatus(sourceId)
                if (cached != null && !isCacheStale(cached)) {
                    cached
                } else {
                    checkStatus(sourceId)
                }
            } catch (e: Exception) {
                Log.error { "Failed to check source $sourceId" }
                createHealthResult(
                    sourceId,
                    SourceStatus.Error(e.message ?: "Check failed"),
                    null
                )
            }
        }
    }
    
    /**
     * Get cached status for a source if available
     */
    override suspend fun getCachedStatus(sourceId: Long): SourceHealth? {
        return cacheLock.withLock {
            cache[sourceId]?.takeIf { !isCacheStale(it) }
        }
    }
    
    /**
     * Clear cached status for a specific source
     */
    override suspend fun clearCache(sourceId: Long) {
        cacheLock.withLock {
            cache.remove(sourceId)
        }
    }
    
    /**
     * Clear all cached statuses
     */
    override suspend fun clearAllCache() {
        cacheLock.withLock {
            cache.clear()
        }
    }
    
    /**
     * Create a SourceHealth result
     */
    private fun createHealthResult(
        sourceId: Long,
        status: SourceStatus,
        responseTime: Long?
    ): SourceHealth {
        return SourceHealth(
            sourceId = sourceId,
            status = status,
            lastChecked = System.currentTimeMillis(),
            responseTime = responseTime
        )
    }
    
    /**
     * Check if cached result is stale (older than TTL)
     */
    private fun isCacheStale(health: SourceHealth): Boolean {
        val age = System.currentTimeMillis() - health.lastChecked
        return age > CACHE_TTL_MS
    }
}
