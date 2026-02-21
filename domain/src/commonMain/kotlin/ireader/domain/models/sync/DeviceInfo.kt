package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents information about a device that can participate in sync operations.
 *
 * @property deviceId Unique identifier for the device (UUID format recommended)
 * @property deviceName Human-readable name of the device
 * @property deviceType Type of device (Android or Desktop)
 * @property appVersion Version of the IReader app running on the device
 * @property ipAddress IP address of the device on the local network
 * @property port Port number the device is listening on for sync connections
 * @property lastSeen Timestamp (milliseconds since epoch) when the device was last seen
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val appVersion: String,
    val ipAddress: String,
    val port: Int,
    val lastSeen: Long
) {
    init {
        require(deviceId.isNotBlank()) { "Device ID cannot be empty or blank" }
        require(deviceName.isNotBlank()) { "Device name cannot be empty or blank" }
        require(port in 1..65535) { "Port must be between 1 and 65535, got: $port" }
        require(lastSeen >= 0) { "Last seen timestamp cannot be negative, got: $lastSeen" }
    }
}

/**
 * Enum representing the type of device.
 */
@Serializable
enum class DeviceType {
    /** Android mobile or tablet device */
    ANDROID,
    
    /** Desktop computer (Windows, macOS, or Linux) */
    DESKTOP
}
