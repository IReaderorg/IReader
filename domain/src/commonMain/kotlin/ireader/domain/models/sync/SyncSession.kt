package ireader.domain.models.sync

/**
 * Represents an active or completed sync session.
 */
data class SyncSession(
    val id: String,
    val deviceId: String,
    val status: SessionStatus,
    val totalItems: Int,
    val completedItems: Int,
    val failedItems: Int,
    val conflicts: List<SyncConflict>,
    val itemsToSend: Int,
    val itemsToReceive: Int,
    val retryCount: Int = 0,
    val wasResumed: Boolean = false,
    val resumedFromItem: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val completionTime: Long = System.currentTimeMillis()
)

enum class SessionStatus {
    IDLE,
    DISCOVERING,
    PAIRING,
    IN_PROGRESS,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED,
    CANCELLED
}

data class SyncConflict(
    val itemId: Long,
    val localVersion: SyncableBook,
    val remoteVersion: SyncableBook
)
