package ireader.domain.models.sync

/**
 * Represents errors that can occur during sync operations.
 */
sealed class SyncError {
    /**
     * Network is not available or WiFi is disconnected.
     */
    data object NetworkUnavailable : SyncError()

    /**
     * Failed to establish connection with the remote device.
     *
     * @property message Detailed error message
     */
    data class ConnectionFailed(val message: String) : SyncError()

    /**
     * Device authentication/pairing failed.
     *
     * @property message Detailed error message
     */
    data class AuthenticationFailed(val message: String) : SyncError()

    /**
     * Sync protocol versions are incompatible.
     *
     * @property localVersion Local device's protocol version
     * @property remoteVersion Remote device's protocol version
     */
    data class IncompatibleVersion(
        val localVersion: Int,
        val remoteVersion: Int
    ) : SyncError()

    /**
     * Data transfer failed.
     *
     * @property message Detailed error message
     */
    data class TransferFailed(val message: String) : SyncError()

    /**
     * Failed to resolve data conflicts.
     *
     * @property message Detailed error message
     */
    data class ConflictResolutionFailed(val message: String) : SyncError()

    /**
     * Insufficient storage space on receiving device.
     *
     * @property required Required storage space in bytes
     * @property available Available storage space in bytes
     */
    data class InsufficientStorage(
        val required: Long,
        val available: Long
    ) : SyncError()

    /**
     * The specified device was not found or is no longer available.
     *
     * @property deviceId ID of the device that was not found
     */
    data class DeviceNotFound(val deviceId: String) : SyncError()

    /**
     * Sync operation was cancelled by the user.
     */
    data object Cancelled : SyncError()

    /**
     * An unknown or unexpected error occurred.
     *
     * @property message Detailed error message
     */
    data class Unknown(val message: String) : SyncError()
}
