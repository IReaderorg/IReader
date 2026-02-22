package ireader.domain.models.sync

import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * Represents a device that has been discovered on the local network.
 *
 * @property deviceInfo Information about the discovered device
 * @property isReachable Whether the device is currently reachable on the network
 * @property discoveredAt Timestamp (milliseconds since epoch) when the device was discovered
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class DiscoveredDevice(
    val deviceInfo: DeviceInfo,
    val isReachable: Boolean,
    val discoveredAt: Long
) {
    init {
        require(discoveredAt >= 0) { "Discovered timestamp cannot be negative, got: $discoveredAt" }
    }
    
    /**
     * Determines if the device should be considered stale and removed from the list.
     * A device is stale if it hasn't been seen for more than 5 minutes.
     *
     * @return true if the device is stale, false otherwise
     */
    fun isStale(): Boolean {
        val fiveMinutesInMillis = 5 * 60 * 1000
        val timeSinceDiscovery = Clock.System.now().toEpochMilliseconds() - discoveredAt
        return timeSinceDiscovery > fiveMinutesInMillis
    }
}
