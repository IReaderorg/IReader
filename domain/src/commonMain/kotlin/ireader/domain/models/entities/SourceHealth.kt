package ireader.domain.models.entities

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents the health check result for a source
 * @param sourceId The unique identifier of the source
 * @param status The current status of the source
 * @param lastChecked Timestamp of the last health check
 * @param responseTime Response time in milliseconds (null if check failed)
 */
data class SourceHealth(
    val sourceId: Long,
    val status: SourceStatus,
    val lastChecked: Long = currentTimeToLong(),
    val responseTime: Long? = null
)
