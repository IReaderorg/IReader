package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents a saved/trusted device for fast local Wi-Fi reconnection ("Your Devices").
 *
 * @property deviceId Unique persistent identifier of the device
 * @property deviceName Hardware/broadcast name of the device
 * @property customAlias Optional user-specified nickname (e.g. "Living Room Tablet")
 * @property deviceType Type of client (PHONE, TABLET, DESKTOP, TV)
 * @property lastKnownIp Last resolved local IP address
 * @property lastKnownPort Port number for Wi-Fi sync (default: 8963)
 * @property lastConnected Milliseconds since epoch of last successful connection
 */
@Serializable
data class SavedDevice(
    val deviceId: String,
    val deviceName: String,
    val customAlias: String? = null,
    val deviceType: DeviceType = DeviceType.PHONE,
    val lastKnownIp: String,
    val lastKnownPort: Int = 8963,
    val lastConnected: Long = 0L
) {
    /**
     * Display name showing the custom alias if set, otherwise the device name.
     */
    val displayName: String
        get() = customAlias?.takeIf { it.isNotBlank() } ?: deviceName
}
