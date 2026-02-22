package ireader.domain.repositories

import ireader.domain.models.sync.TrustedDevice
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing trusted devices.
 * 
 * Trusted devices are devices that have been paired and verified for syncing.
 * Trust expires after a configurable period (default 30 days) and requires
 * re-authentication to continue syncing.
 */
interface TrustedDeviceRepository {
    
    /**
     * Get a trusted device by its ID.
     * 
     * @param deviceId Unique identifier of the device
     * @return The trusted device if found, null otherwise
     */
    suspend fun getTrustedDevice(deviceId: String): TrustedDevice?
    
    /**
     * Add or update a trusted device.
     * 
     * @param device The trusted device to store
     */
    suspend fun upsertTrustedDevice(device: TrustedDevice)
    
    /**
     * Observe all active trusted devices.
     * 
     * @return Flow of active trusted devices
     */
    fun getActiveTrustedDevices(): Flow<List<TrustedDevice>>
    
    /**
     * Deactivate a trusted device (revoke trust).
     * 
     * @param deviceId Unique identifier of the device to deactivate
     */
    suspend fun deactivateTrustedDevice(deviceId: String)
    
    /**
     * Update the expiration time of a trusted device.
     * 
     * @param deviceId Unique identifier of the device
     * @param expiresAt New expiration timestamp in milliseconds
     */
    suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long)
    
    /**
     * Delete a trusted device from storage.
     * 
     * @param deviceId Unique identifier of the device to delete
     */
    suspend fun deleteTrustedDevice(deviceId: String)
}
