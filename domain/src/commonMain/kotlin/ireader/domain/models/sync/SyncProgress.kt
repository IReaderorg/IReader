package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents progress of an ongoing sync operation.
 *
 * @property deviceId ID of the device being synced with
 * @property statusType Current sync status type (simplified for serialization)
 * @property totalItems Total number of items to sync
 * @property completedItems Number of items completed
 * @property progressPercentage Progress as a percentage (0-100)
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncProgress(
    val deviceId: String,
    val statusType: String, // "idle", "discovering", "connecting", "syncing", "completed", "failed"
    val totalItems: Int,
    val completedItems: Int,
    val progressPercentage: Int
) {
    init {
        require(deviceId.isNotBlank()) { "Device ID cannot be empty or blank" }
        require(totalItems >= 0) { "Total items cannot be negative, got: $totalItems" }
        require(completedItems >= 0) { "Completed items cannot be negative, got: $completedItems" }
        require(completedItems <= totalItems) { "Completed items ($completedItems) cannot exceed total items ($totalItems)" }
        require(progressPercentage in 0..100) { "Progress percentage must be between 0 and 100, got: $progressPercentage" }
    }
}
