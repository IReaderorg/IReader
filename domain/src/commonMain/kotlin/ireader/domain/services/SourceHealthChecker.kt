package ireader.domain.services

import ireader.domain.models.entities.SourceHealth
import ireader.domain.models.entities.SourceStatus

/**
 * Interface for checking the health status of sources
 */
interface SourceHealthChecker {
    /**
     * Check the health status of a specific source
     * @param sourceId The unique identifier of the source
     * @return SourceHealth containing the status and metadata
     */
    suspend fun checkStatus(sourceId: Long): SourceHealth
    
    /**
     * Check the health status of multiple sources
     * @param sourceIds List of source identifiers to check
     * @return Map of source IDs to their health status
     */
    suspend fun checkMultipleSources(sourceIds: List<Long>): Map<Long, SourceHealth>
    
    /**
     * Get cached status for a source if available
     * @param sourceId The unique identifier of the source
     * @return Cached SourceHealth or null if not cached
     */
    suspend fun getCachedStatus(sourceId: Long): SourceHealth?
    
    /**
     * Clear cached status for a specific source
     * @param sourceId The unique identifier of the source
     */
    suspend fun clearCache(sourceId: Long)
    
    /**
     * Clear all cached statuses
     */
    suspend fun clearAllCache()
}
