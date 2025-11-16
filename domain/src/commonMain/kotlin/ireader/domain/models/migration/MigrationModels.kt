package ireader.domain.models.migration

import ireader.domain.models.entities.BookItem

/**
 * Request to migrate a novel from one source to another
 */
data class MigrationRequest(
    val novelId: Long,
    val sourceId: Long,
    val targetSourceId: Long,
    val preserveProgress: Boolean = true
)

/**
 * A potential match for a novel during migration
 */
data class MigrationMatch(
    val novel: BookItem,
    val confidenceScore: Float,
    val matchReason: String
)

/**
 * Result of a migration operation
 */
data class MigrationResult(
    val novelId: Long,
    val success: Boolean,
    val newNovelId: Long?,
    val error: String?
)

/**
 * Progress information for an ongoing migration
 */
data class MigrationProgress(
    val novelId: Long,
    val status: MigrationStatus,
    val progress: Float,
    val error: String?
)

/**
 * Status of a migration operation
 */
enum class MigrationStatus {
    PENDING,
    SEARCHING,
    MATCHING,
    TRANSFERRING,
    COMPLETED,
    FAILED
}

/**
 * History record of a migration
 */
data class MigrationHistory(
    val id: String,
    val oldBookId: Long,
    val newBookId: Long,
    val oldSourceId: Long,
    val newSourceId: Long,
    val timestamp: Long,
    val chaptersTransferred: Int,
    val progressPreserved: Boolean,
    val canRollback: Boolean = true
)
