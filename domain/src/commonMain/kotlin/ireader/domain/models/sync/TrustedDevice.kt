package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents a trusted device that has been paired for syncing.
 * 
 * Trust expires after a configurable period (default 30 days) and requires
 * re-authentication to continue syncing.
 * 
 * @property deviceId Unique identifier of the device
 * @property deviceName Human-readable name of the device
 * @property pairedAt Timestamp when the device was first paired (milliseconds since epoch)
 * @property expiresAt Timestamp when the trust expires (milliseconds since epoch)
 * @property isActive Whether the device is currently active (not revoked)
 */
@Serializable
data class TrustedDevice(
    val deviceId: String,
    val deviceName: String,
    val pairedAt: Long,
    val expiresAt: Long,
    val isActive: Boolean = true
)
