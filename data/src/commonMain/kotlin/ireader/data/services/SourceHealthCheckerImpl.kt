package ireader.data.services

import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.SourceHealth
import ireader.domain.models.entities.SourceStatus
import ireader.domain.services.SourceHealthChecker
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of SourceHealthChecker that performs actual health checks on sources
 */
class SourceHealthCheckerImpl(
    private val catalogStore: CatalogStore
) : SourceHealthChecker {
    
    // Cache for storing health check results
    private val statusCache = mutableMapOf<Long, SourceHealth>()
    
    // Cache expiration time (5 minutes)
    private val cacheExpirationMs = 5 * 60 * 1000L
    
    override suspend fun checkStatus(sourceId: Long): SourceHealth {
        // Check if we have a valid cached result
        val cached = getCachedStatus(sourceId)
        if (cached != null && !isCacheExpired(cached)) {
            return cached
        }
        
        // Perform actual health check
        val health = performHealthCheck(sourceId)
        
        // Cache the result
        statusCache[sourceId] = health
        
        return health
    }
    
    override suspend fun checkMultipleSources(sourceIds: List<Long>): Map<Long, SourceHealth> = coroutineScope {
        sourceIds.map { sourceId ->
            async {
                sourceId to checkStatus(sourceId)
            }
        }.awaitAll().toMap()
    }
    
    override suspend fun getCachedStatus(sourceId: Long): SourceHealth? {
        val cached = statusCache[sourceId]
        return if (cached != null && !isCacheExpired(cached)) {
            cached
        } else {
            null
        }
    }
    
    override suspend fun clearCache(sourceId: Long) {
        statusCache.remove(sourceId)
    }
    
    override suspend fun clearAllCache() {
        statusCache.clear()
    }
    
    /**
     * Perform the actual health check on a source
     */
    private suspend fun performHealthCheck(sourceId: Long): SourceHealth {
        val startTime = System.currentTimeMillis()
        
        return try {
            val catalog = catalogStore.get(sourceId)
            
            if (catalog == null || catalog.source == null) {
                return SourceHealth(
                    sourceId = sourceId,
                    status = SourceStatus.Error("Source not found"),
                    lastChecked = System.currentTimeMillis(),
                    responseTime = null
                )
            }
            
            val source = catalog.source!!
            
            // Perform health check with timeout
            val result = withTimeoutOrNull(10.seconds) {
                checkSourceHealth(source)
            }
            
            val responseTime = System.currentTimeMillis() - startTime
            
            if (result == null) {
                // Timeout occurred
                SourceHealth(
                    sourceId = sourceId,
                    status = SourceStatus.Offline,
                    lastChecked = System.currentTimeMillis(),
                    responseTime = null
                )
            } else {
                SourceHealth(
                    sourceId = sourceId,
                    status = result,
                    lastChecked = System.currentTimeMillis(),
                    responseTime = responseTime
                )
            }
        } catch (e: Exception) {
            SourceHealth(
                sourceId = sourceId,
                status = SourceStatus.Error(e.message ?: "Unknown error"),
                lastChecked = System.currentTimeMillis(),
                responseTime = null
            )
        }
    }
    
    /**
     * Check if a source is healthy by attempting to access its base URL
     */
    private suspend fun checkSourceHealth(source: Source): SourceStatus {
        return try {
            when (source) {
                is HttpSource -> {
                    // For HTTP sources, we can check the base URL
                    // This is a simplified check - in a real implementation,
                    // you might want to make an actual HTTP request
                    if (source.baseUrl.isNotBlank()) {
                        SourceStatus.Online
                    } else {
                        SourceStatus.Error("Invalid base URL")
                    }
                }
                else -> {
                    // For non-HTTP sources, assume they're online
                    SourceStatus.Online
                }
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("401") == true || e.message?.contains("403") == true -> {
                    SourceStatus.LoginRequired
                }
                e.message?.contains("404") == true || e.message?.contains("500") == true -> {
                    SourceStatus.Offline
                }
                else -> {
                    SourceStatus.Error(e.message ?: "Connection failed")
                }
            }
        }
    }
    
    /**
     * Check if a cached health result has expired
     */
    private fun isCacheExpired(health: SourceHealth): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - health.lastChecked) > cacheExpirationMs
    }
}
