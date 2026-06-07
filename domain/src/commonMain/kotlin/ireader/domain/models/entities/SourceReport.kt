package ireader.domain.models.entities

import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents a user report for a broken or problematic source
 */
@Serializable
data class SourceReport(
    val id: Long = 0,
    val sourceId: Long,
    val packageName: String,
    val version: String,
    val reason: String = "",
    val timestamp: Long = currentTimeToLong(),
    val status: String = "pending" // pending, submitted, resolved
)
