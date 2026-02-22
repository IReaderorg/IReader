package ireader.domain.usecases.sync

import ireader.domain.repositories.TrustedDeviceRepository

/**
 * Use case for re-authenticating a device when its trust has expired.
 * 
 * When a device's trust expires (after 30 days), it must be re-authenticated
 * to continue syncing. This use case:
 * 1. Verifies the device exists in the trusted devices list
 * 2. Extends the expiration by 30 days from the current time
 * 3. Reactivates the device if it was deactivated
 * 
 * Re-authentication typically requires the user to confirm the action on both
 * devices (e.g., by entering a new PIN or confirming a prompt).
 * 
 * @param trustedDeviceRepository Repository for accessing and updating trusted devices
 */
class ReauthenticateDeviceUseCase(
    private val trustedDeviceRepository: TrustedDeviceRepository
) {
    
    companion object {
        /**
         * Trust duration in milliseconds (30 days).
         */
        private const val TRUST_DURATION_MS = 30L * 24 * 60 * 60 * 1000
    }
    
    /**
     * Re-authenticates a device by extending its trust period.
     * 
     * @param deviceId Unique identifier of the device to re-authenticate
     * @return true if re-authentication succeeded, false if device not found
     */
    suspend operator fun invoke(deviceId: String): Boolean {
        // Get the device from storage
        val device = trustedDeviceRepository.getTrustedDevice(deviceId)
            ?: return false // Device not found
        
        // Calculate new expiration (30 days from now)
        val now = System.currentTimeMillis()
        val newExpiration = now + TRUST_DURATION_MS
        
        // Update the device with new expiration and ensure it's active
        val updatedDevice = device.copy(
            expiresAt = newExpiration,
            isActive = true
        )
        
        trustedDeviceRepository.upsertTrustedDevice(updatedDevice)
        
        return true
    }
}
