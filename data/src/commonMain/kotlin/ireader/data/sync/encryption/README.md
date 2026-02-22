# Certificate Pinning Implementation

## Overview

The `CertificatePinningManager` implements certificate pinning for secure device-to-device communication. It stores and validates certificate fingerprints to prevent Man-in-the-Middle (MITM) attacks.

## Implementation Status

### Completed (Task 9.2.3)

- ✅ `CertificatePinningManager` class with full API
- ✅ Comprehensive test suite (8 tests)
- ✅ Pin certificate for device
- ✅ Validate pinned certificate
- ✅ Update pinned certificate
- ✅ Remove pinned certificate
- ✅ Get pinned fingerprint
- ✅ Input validation and error handling

### Current Storage Implementation

**Temporary In-Memory Storage**: The current implementation uses an in-memory `Map` for storing certificate fingerprints. This is sufficient for testing but **NOT suitable for production**.

### Required Database Schema Update

To make certificate pinning persistent, the `trusted_devices` table needs to be extended:

```sql
-- Add certificate_fingerprint column to trusted_devices table
ALTER TABLE trusted_devices ADD COLUMN certificate_fingerprint TEXT;

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_trusted_devices_fingerprint 
ON trusted_devices(certificate_fingerprint);
```

### Integration Steps

1. **Update Database Schema**:
   - Add `certificate_fingerprint` column to `trusted_devices` table
   - Update SQLDelight queries in `data/src/commonMain/sqldelight/data/trusted_devices.sq`

2. **Extend SyncLocalDataSource**:
   ```kotlin
   interface SyncLocalDataSource {
       // Add these methods:
       suspend fun getCertificateFingerprint(deviceId: String): String?
       suspend fun updateCertificateFingerprint(deviceId: String, fingerprint: String?)
   }
   ```

3. **Update CertificatePinningManager**:
   - Replace in-memory storage with database calls
   - Remove `fingerprintStorage` map
   - Update helper methods to use `localStorage`

4. **Update TrustedDeviceEntity**:
   ```kotlin
   data class TrustedDeviceEntity(
       val deviceId: String,
       val deviceName: String,
       val pairedAt: Long,
       val expiresAt: Long,
       val isActive: Boolean,
       val certificateFingerprint: String? = null  // Add this field
   )
   ```

## Usage Example

```kotlin
val pinningManager = CertificatePinningManager(certificateService, localStorage)

// Pin a certificate when device is paired
val fingerprint = certificateService.calculateFingerprint(certificate)
pinningManager.pinCertificate(deviceId, fingerprint)

// Validate certificate during TLS handshake
val isValid = pinningManager.validatePinnedCertificate(deviceId, certificate)
if (isValid.getOrNull() == true) {
    // Proceed with connection
} else {
    // Reject connection - possible MITM attack
}

// Update certificate during rotation
pinningManager.updatePinnedCertificate(deviceId, newFingerprint)

// Remove when device is untrusted
pinningManager.removePinnedCertificate(deviceId)
```

## Integration with KtorTransferDataSource

The `CertificatePinningManager` should be integrated into `KtorTransferDataSource` during TLS handshake:

```kotlin
class KtorTransferDataSource(
    private val certificatePinningManager: CertificatePinningManager
) : TransferDataSource {
    
    suspend fun connectToDevice(address: String, port: Int, deviceId: String): Result<Connection> {
        // ... establish connection ...
        
        // Validate certificate during handshake
        val certificate = session.call.request.certificates()?.firstOrNull()
        if (certificate != null) {
            val isValid = certificatePinningManager.validatePinnedCertificate(
                deviceId, 
                certificate.encoded
            )
            
            if (isValid.getOrNull() != true) {
                return Result.failure(SecurityException("Certificate pinning validation failed"))
            }
        }
        
        // ... continue with connection ...
    }
}
```

## Test Coverage

The test suite covers:

1. **Pin Certificate**:
   - Store fingerprint for device
   - Update existing fingerprint
   - Reject empty fingerprint
   - Reject empty deviceId

2. **Validate Certificate**:
   - Pass for matching fingerprint
   - Fail for mismatched fingerprint
   - Fail for non-existent device
   - Handle empty certificate

3. **Update Certificate**:
   - Update fingerprint
   - Fail for non-existent device

4. **Remove Certificate**:
   - Remove fingerprint
   - Succeed for non-existent device

5. **Get Fingerprint**:
   - Return stored fingerprint
   - Return null for non-existent device

## Security Considerations

1. **Certificate Pinning**: Prevents MITM attacks by validating certificate fingerprints
2. **SHA-256 Fingerprints**: Uses strong cryptographic hash for certificate identification
3. **Validation on Every Connection**: Certificate must be validated on each TLS handshake
4. **Certificate Rotation**: Supports updating pinned certificates when devices rotate keys
5. **Persistent Storage**: Fingerprints must be stored persistently (requires database update)

## Next Steps

1. Update database schema with `certificate_fingerprint` column
2. Extend `SyncLocalDataSource` with fingerprint methods
3. Replace in-memory storage with database storage
4. Integrate with `KtorTransferDataSource` TLS handshake
5. Add integration tests with real TLS connections
6. Document certificate rotation workflow
