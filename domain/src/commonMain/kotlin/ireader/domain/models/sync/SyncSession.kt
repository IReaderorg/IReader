package ireader.domain.models.sync

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Represents an active or completed sync session.
 *
 * @property id Unique identifier for this sync session
 * @property deviceId ID of the device being synced with
 * @property status Current session status
 * @property totalItems Total number of items to sync
 * @property completedItems Number of items completed
 * @property failedItems Number of items that failed to sync
 * @property conflicts List of conflicts detected during sync
 * @property itemsToSend Number of items to send to remote device
 * @property itemsToReceive Number of items to receive from remote device
 * @property retryCount Number of retry attempts made
 * @property wasResumed Whether this session was resumed from a previous attempt
 * @property resumedFromItem Item index from which the session was resumed
 * @property startTime Timestamp when the session started
 * @property completionTime Timestamp when the session completed
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncSession @OptIn(ExperimentalTime::class) constructor(
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
    val startTime: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val completionTime: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(id.isNotBlank()) { "Session ID cannot be empty or blank" }
        require(deviceId.isNotBlank()) { "Device ID cannot be empty or blank" }
        require(totalItems >= 0) { "Total items cannot be negative, got: $totalItems" }
        require(completedItems >= 0) { "Completed items cannot be negative, got: $completedItems" }
        require(failedItems >= 0) { "Failed items cannot be negative, got: $failedItems" }
        require(itemsToSend >= 0) { "Items to send cannot be negative, got: $itemsToSend" }
        require(itemsToReceive >= 0) { "Items to receive cannot be negative, got: $itemsToReceive" }
        require(retryCount >= 0) { "Retry count cannot be negative, got: $retryCount" }
        require(resumedFromItem >= 0) { "Resumed from item cannot be negative, got: $resumedFromItem" }
        require(startTime >= 0) { "Start time cannot be negative, got: $startTime" }
        require(completionTime >= 0) { "Completion time cannot be negative, got: $completionTime" }
    }
}

/**
 * Status of a sync session.
 */
@Serializable
enum class SessionStatus {
    /** No active session */
    IDLE,
    
    /** Discovering devices */
    DISCOVERING,
    
    /** Pairing with device */
    PAIRING,
    
    /** Sync in progress */
    IN_PROGRESS,
    
    /** Sync completed successfully */
    COMPLETED,
    
    /** Sync completed with some errors */
    COMPLETED_WITH_ERRORS,
    
    /** Sync failed */
    FAILED,
    
    /** Sync cancelled by user */
    CANCELLED
}

/**
 * Represents a conflict between local and remote versions of a book.
 *
 * @property itemId ID of the conflicting item
 * @property localVersion Local version of the book
 * @property remoteVersion Remote version of the book
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncConflict(
    val itemId: Long,
    val localVersion: SyncableBook,
    val remoteVersion: SyncableBook
) {
    init {
        require(itemId >= 0) { "Item ID cannot be negative, got: $itemId" }
    }
}
