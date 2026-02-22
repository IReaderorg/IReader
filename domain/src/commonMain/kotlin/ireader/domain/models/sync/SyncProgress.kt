package ireader.domain.models.sync

/**
 * Represents progress of an ongoing sync operation.
 */
data class SyncProgress(
    val deviceId: String,
    val status: SyncStatus,
    val totalItems: Int,
    val completedItems: Int,
    val progressPercentage: Int
)
