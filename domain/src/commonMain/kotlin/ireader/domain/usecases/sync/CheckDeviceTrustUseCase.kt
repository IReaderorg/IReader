package ireader.domain.usecases.sync

import ireader.domain.repositories.TrustedDeviceRepository

/**
 * Use case for checking if a device is still trusted.
 * 
 * A device is considered trusted if:
 * 1. It exists in the trusted devices list
 * 2. It is marked as active (isActive = true)
 * 3. Its trust has not expired (expiresAt > current time)
 * 
 * Trust expires after 30 days from the pairing date. After expiration,
 * the device must be re-authenticated to continue syncing.
 * 
 * @param trustedDeviceRepository Repository for accessing trusted device information
 */
class CheckDeviceTrustUseCase(
    private val trustedDeviceRepository: TrustedDeviceRepository
) {
    
    /**
     * Checks if a device is currently trusted.
     * 
     * @param deviceId Unique identifier of the device to check
     * @return true if the device is trusted and not expired, false otherwise
     */
    suspend operator fun invoke(deviceId: String): Boolean {
        // Get the device from storage
        val device = trustedDeviceRepository.getTrustedDevice(deviceId)
            ?: return false // Device not found
        
        // Check if device is active
        if (!device.isActive) {
            return false
        }
        
        // Check if trust has expired
        val now = System.currentTimeMillis()
        if (device.expiresAt <= now) {
            return false
        }
        
        // Device is trusted and not expired
        return true
    }
}
