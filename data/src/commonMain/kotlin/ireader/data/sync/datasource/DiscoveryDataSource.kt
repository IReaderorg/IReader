package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DiscoveredDevice
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for device discovery operations on the local network.
 * 
 * This interface defines the contract for discovering devices that can participate
 * in sync operations using mDNS (Multicast DNS) service discovery.
 * 
 * Platform-specific implementations:
 * - Android: Uses NsdManager (Network Service Discovery)
 * - Desktop: Uses JmDNS library
 */
interface DiscoveryDataSource {
    
    /**
     * Start broadcasting this device's presence on the local network.
     * 
     * This makes the device discoverable by other devices running IReader.
     * The device will advertise itself using mDNS with the service type "_ireader._tcp".
     * 
     * @param deviceInfo Information about this device to broadcast
     * @return Result indicating success or failure
     */
    suspend fun startBroadcasting(deviceInfo: DeviceInfo): Result<Unit>
    
    /**
     * Stop broadcasting this device's presence on the local network.
     * 
     * @return Result indicating success or failure
     */
    suspend fun stopBroadcasting(): Result<Unit>
    
    /**
     * Start discovering other devices on the local network.
     * 
     * This begins scanning for other IReader devices advertising themselves
     * via mDNS. Discovered devices will be emitted through [observeDiscoveredDevices].
     * 
     * @return Result indicating success or failure
     */
    suspend fun startDiscovery(): Result<Unit>
    
    /**
     * Stop discovering devices on the local network.
     * 
     * @return Result indicating success or failure
     */
    suspend fun stopDiscovery(): Result<Unit>
    
    /**
     * Observe the list of discovered devices on the local network.
     * 
     * This Flow emits updates whenever:
     * - A new device is discovered
     * - A device becomes unreachable
     * - A device's information is updated
     * 
     * @return Flow emitting the current list of discovered devices
     */
    fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>>
    
    /**
     * Verify that a device is reachable on the network.
     * 
     * This performs a connectivity check (e.g., TCP connection attempt or ping)
     * to ensure the device can be reached before attempting sync operations.
     * 
     * @param deviceInfo Information about the device to verify
     * @return Result containing true if reachable, false otherwise
     */
    suspend fun verifyDevice(deviceInfo: DeviceInfo): Result<Boolean>
}
