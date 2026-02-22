package ireader.data.sync.encryption

import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.domain.services.sync.CertificateService

/**
 * Manager for certificate pinning functionality.
 * 
 * Implements certificate pinning to prevent MITM attacks by storing and validating
 * certificate fingerprints for trusted devices. Each device has a pinned certificate
 * fingerprint that must match during TLS handshake.
 * 
 * Task 9.2.3: Certificate Pinning Implementation
 * 
 * @property certificateService Service for certificate operations
 * @property localStorage Local data source for persistent storage
 */
class CertificatePinningManager(
    private val certificateService: CertificateService,
    private val localStorage: SyncLocalDataSource
) {
    
    /**
     * Pin a certificate fingerprint for a device.
     * 
     * Stores the certificate fingerprint in the trusted_devices table.
     * If a fingerprint already exists for the device, it will be updated.
     * 
     * @param deviceId Unique identifier of the device
     * @param fingerprint SHA-256 fingerprint of the certificate
     * @return Result indicating success or failure
     */
    suspend fun pinCertificate(deviceId: String, fingerprint: String): Result<Unit> {
        return try {
            // Validate inputs
            if (deviceId.isBlank()) {
                return Result.failure(IllegalArgumentException("Device ID cannot be empty"))
            }
            if (fingerprint.isBlank()) {
                return Result.failure(IllegalArgumentException("Fingerprint cannot be empty"))
            }
            
            // Store fingerprint in database
            localStorage.updateCertificateFingerprint(deviceId, fingerprint)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate a certificate against the pinned fingerprint for a device.
     * 
     * Calculates the fingerprint of the provided certificate and compares it
     * with the stored fingerprint for the device.
     * 
     * @param deviceId Unique identifier of the device
     * @param certificate Certificate bytes to validate
     * @return Result containing true if valid, false if invalid, or failure if error
     */
    suspend fun validatePinnedCertificate(deviceId: String, certificate: ByteArray): Result<Boolean> {
        return try {
            // Validate inputs
            if (certificate.isEmpty()) {
                return Result.failure(IllegalArgumentException("Certificate cannot be empty"))
            }
            
            // Get pinned fingerprint from database
            val pinnedFingerprint = localStorage.getCertificateFingerprint(deviceId)
                ?: return Result.failure(IllegalStateException("No pinned certificate for device: $deviceId"))
            
            // Calculate actual fingerprint
            val actualFingerprint = certificateService.calculateFingerprint(certificate)
            
            // Verify fingerprint matches
            val isValid = certificateService.verifyCertificateFingerprint(certificate, pinnedFingerprint)
            
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update the pinned certificate fingerprint for a device.
     * 
     * This is used during certificate rotation when a device generates a new certificate.
     * The device must already have a pinned certificate.
     * 
     * @param deviceId Unique identifier of the device
     * @param newFingerprint New SHA-256 fingerprint
     * @return Result indicating success or failure
     */
    suspend fun updatePinnedCertificate(deviceId: String, newFingerprint: String): Result<Unit> {
        return try {
            // Verify device exists and has a fingerprint
            val existingFingerprint = localStorage.getCertificateFingerprint(deviceId)
                ?: return Result.failure(IllegalStateException("Device not found: $deviceId"))
            
            // Update fingerprint in database
            localStorage.updateCertificateFingerprint(deviceId, newFingerprint)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove the pinned certificate for a device.
     * 
     * This is typically called when a device is removed from the trusted devices list.
     * 
     * @param deviceId Unique identifier of the device
     * @return Result indicating success or failure
     */
    suspend fun removePinnedCertificate(deviceId: String): Result<Unit> {
        return try {
            // Remove fingerprint from database
            localStorage.updateCertificateFingerprint(deviceId, null)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the pinned fingerprint for a device.
     * 
     * @param deviceId Unique identifier of the device
     * @return Result containing the fingerprint or null if not found
     */
    suspend fun getPinnedFingerprint(deviceId: String): Result<String?> {
        return try {
            val fingerprint = localStorage.getCertificateFingerprint(deviceId)
            Result.success(fingerprint)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
