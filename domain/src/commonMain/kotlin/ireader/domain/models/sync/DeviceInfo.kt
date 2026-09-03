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
    /** Smartphone */
    PHONE,

    /** Tablet device */
    TABLET,

    /** Desktop computer (Windows, macOS, or Linux) */
    DESKTOP,

    /** Smart TV or Android TV device */
    TV,

    /** Android mobile or tablet device (legacy alias) */
    ANDROID,

    /** Unknown device type */
    UNKNOWN;

    val displayName: String
        get() = when (this) {
            PHONE, ANDROID -> "Phone"
            TABLET -> "Tablet"
            DESKTOP -> "PC / Desktop"
            TV -> "TV"
            UNKNOWN -> "Device"
        }

    companion object {
        fun fromString(name: String?): DeviceType {
            if (name.isNullOrBlank()) return UNKNOWN
            return when (name.trim().uppercase()) {
                "PHONE" -> PHONE
                "TABLET" -> TABLET
                "DESKTOP", "PC" -> DESKTOP
                "TV" -> TV
                "ANDROID" -> ANDROID
                else -> {
                    try {
                        valueOf(name.trim().uppercase())
                    } catch (_: Exception) {
                        UNKNOWN
                    }
                }
            }
        }
    }
}
