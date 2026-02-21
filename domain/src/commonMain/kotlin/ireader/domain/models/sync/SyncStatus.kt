package ireader.domain.models.sync

/**
 * Represents the current status of a sync operation.
 */
sealed class SyncStatus {
    /**
     * No sync operation is currently active.
     */
    data object Idle : SyncStatus()

    /**
     * Discovering devices on the local network.
     */
    data object Discovering : SyncStatus()

    /**
     * Connecting to a specific device.
     *
     * @property deviceName Name of the device being connected to
     */
    data class Connecting(val deviceName: String) : SyncStatus()

    /**
     * Actively syncing data with a device.
     *
     * @property deviceName Name of the device being synced with
     * @property progress Sync progress as a percentage (0.0 to 1.0)
     * @property currentItem Name of the item currently being synced
     * @property currentIndex Current item index (1-based, 0 if not available)
     * @property totalItems Total number of items to sync (0 if not available)
     *
     * @throws IllegalArgumentException if progress is not between 0.0 and 1.0
     */
    data class Syncing(
        val deviceName: String,
        val progress: Float,
        val currentItem: String,
        val currentIndex: Int = 0,
        val totalItems: Int = 0
    ) : SyncStatus() {
        init {
            require(progress in 0.0f..1.0f) { "Progress must be between 0.0 and 1.0, got: $progress" }
            require(currentIndex >= 0) { "Current index cannot be negative, got: $currentIndex" }
            require(totalItems >= 0) { "Total items cannot be negative, got: $totalItems" }
        }
    }

    /**
     * Sync operation completed successfully.
     *
     * @property deviceName Name of the device that was synced with
     * @property syncedItems Number of items that were synced
     * @property duration Duration of the sync operation in milliseconds
     *
     * @throws IllegalArgumentException if syncedItems or duration is negative
     */
    data class Completed(
        val deviceName: String,
        val syncedItems: Int,
        val duration: Long
    ) : SyncStatus() {
        init {
            require(syncedItems >= 0) { "Synced items cannot be negative, got: $syncedItems" }
            require(duration >= 0) { "Duration cannot be negative, got: $duration" }
        }
    }

    /**
     * Sync operation failed.
     *
     * @property deviceName Name of the device (null if failure occurred before device selection)
     * @property error The error that caused the failure
     */
    data class Failed(
        val deviceName: String?,
        val error: SyncError
    ) : SyncStatus()
}
